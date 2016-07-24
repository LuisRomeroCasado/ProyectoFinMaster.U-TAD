package com.tfm.utad.sparkMeetup.streaming

import com.tfm.utad.sparkMeetup.model._

import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.Logging
import org.apache.spark.storage.StorageLevel

import org.elasticsearch.spark._

import kafka.serializer.StringDecoder

import org.json4s._
import org.json4s.native.JsonParser


object RsvpStreaimngProcess extends Logging {


  //KAFKA CONFIGURATION
  final val KAFKA_BROKERS = "master:2181";
  final val LOCAL_KAFKA_BROKERS = "localhost:2181";
  final val KAFKA_GROUPID = "meetup_rsvp_stream"
  final val KAFKA_TOPIC = "TOPIC_RSVPS";


  //ELASTICSEARCH CONFIGURATION
  final val ES_NODES = "master,slave1,slave2";
  final val ES_NODES_LOCALHOST = "localhost";
  final val ES_PORT = "9200";

  final val CHECKPOINT_DIRECTORY = "/tmp/spark_checkpoint/";


  def main(args: Array[String]) {
    //: Unit = {

    val conf = new SparkConf().setAppName(getClass.getSimpleName)
      .setMaster("local[2]")


    val sc = new SparkContext(conf)
    val ssc = new StreamingContext(sc, Seconds(5))

    val topics = Map("TOPIC_RSVPS" -> 1)

    val kafkaParams = Map[String, String](
      "zookeeper.connect" -> KAFKA_BROKERS,
      "group.id" -> KAFKA_GROUPID,
      "zookeeper.connection.timeout.ms" -> "10000")


    val rsvps_stream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topics, StorageLevel.MEMORY_ONLY)
      .map(msg => {
        parseJson(msg._2)
      })
      .cache()

    //Bulk store of stream in ElasticSearch
    rsvps_stream.foreachRDD(rdd => {
      rdd.saveToEs("meetup_rsvps/meetup_rsvps",
        Map("es.mapping.timestamp" -> "mtime",
          "es.mapping.id" -> "rsvp_id",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        )
      )
    })

    //Number of responses by country
    val rsvpByCountry = rsvps_stream
      .map(
        rsvp => ((rsvp.group.group_country.getOrElse(""),
          s"${rsvp.group.group_lat.getOrElse("")},${rsvp.group.group_lon.getOrElse("")}",
          rsvp.response, rsvp.mtime), (rsvp.guests, rsvp.guests + 1))
      )
      .reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2))
      .map { case ((country, location, response, date), (guests, attendees)) =>
        RsvpsByCountry(
          country + "-" + date,
          country,
          location,
          response,
          guests,
          attendees,
          date
        )
      }

    rsvpByCountry.foreachRDD(rdd => {
      rdd.cache
      rdd.saveToEs("meetup_rsvp_by_country/meetup_rsvp_by_country",
        Map("es.mapping.id" -> "key",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        )
      )
      rdd.unpersist()
    })


    // Global Trending Topics
    val globalTrendingTopics = rsvps_stream
      .flatMap(rsvp => rsvp.group.group_topics)
      .map(topic => (topic.topic_name.getOrElse(""), 1))
      .reduceByKeyAndWindow((x: Int, y: Int) => x + y, Minutes(5), Seconds(10))
      .filter(t => t._2 > 5) // min threshold = 5
      .transform((rdd, time) => rdd.map {
      case (topic_name, count) => GlobalTrendingTopics(topic_name, count, time.milliseconds)
    })


    globalTrendingTopics.foreachRDD(rdd => {
      rdd.cache
      rdd.saveToEs("meetup_global_trending_topics/meetup_global_trending_topics",
        Map("es.write.operation" -> "upsert",
          "es.mapping.id" -> "topic_name",
          "es.mapping.timestamp" -> "date",
          "es.update.script.params" -> "inc:counter",
          "es.update.script" -> "ctx._source.counter+=inc",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        ))
      rdd.unpersist()
    })

    def mapdata(group: RsvpGroup) = {
      group.group_topics.map(x => (x, group.group_country.getOrElse(""),
        s"${group.group_lat.getOrElse("")},${group.group_lon.getOrElse("")}"))
    }

    // Country Trending Topics
    val countryTrendingTopics = rsvps_stream
      .flatMap(rsvp => mapdata(rsvp.group))
      .map(x => ((x._1.topic_name.getOrElse(""), x._2, x._3), 1))
      .reduceByKeyAndWindow((curr: Int, acc: Int) => curr + acc, Minutes(5), Seconds(10))
      .filter(t => t._2 > 5) // min threshold = 5
      .transform((rdd, time) => rdd.map {
      case ((topic_name, country, location), count) =>
        TrendingTopicsByCountry(country + "-" + topic_name, country, location, topic_name, count, time.milliseconds)
    })


    countryTrendingTopics.foreachRDD(rdd => {
      rdd.cache
      rdd.saveToEs("meetup_country_trending_topics/meetup_country_trending_topics",
        Map("es.write.operation" -> "upsert",
          "es.mapping.id" -> "key",
          "es.mapping.timestamp" -> "date",
          "es.update.script.params" -> "inc:counter",
          "es.update.script" -> "ctx._source.counter+=inc",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        ))
      rdd.unpersist()
    })


    ssc.start() //in separate thread
    ssc.awaitTermination()
  }


  private def parseJson(jsonStr: String): MeetupRsvp = {
    implicit lazy val formats = DefaultFormats

    val json = JsonParser.parse(jsonStr)
    json.extract[MeetupRsvp]

  }

}
