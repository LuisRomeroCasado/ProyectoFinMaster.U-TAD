package com.tfm.utad.sparkMeetup.batch

import java.text.SimpleDateFormat
import java.util.Calendar

import com.tfm.utad.sparkMeetup.model.{MeetupEventsCounts, MeetupGroupsCounts, RsvpGroupTopics}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{Logging, SparkConf, SparkContext}
import org.elasticsearch.spark._


object GroupBatchProcess extends Logging {

  //ELASTICSEARCH CONFIGURATION
  final val ES_NODES = "master,slave1,slave2";
  final val ES_NODES_LOCALHOST = "localhost";
  final val ES_PORT = "9200";
  final val ES_QUERY =
    s"""{"query": {"filtered": {"query": {"match_all": {}},
        |"filter": {"range": {"mtime": {"gte": "now/d"}}}}}}""".stripMargin

  final val TODAY = Calendar.getInstance().getTime();

  def main(args: Array[String]) {
    //: Unit = {

    /* Spark initialization */
    val conf = new SparkConf()
      .setAppName(getClass.getSimpleName)
      .setMaster("local[*]")
      .set("es.index.auto.create", "true")
      .set("es.query", ES_QUERY)
      .set("es.nodes", ES_NODES)
      .set("es.port", ES_PORT)

    val sc = new SparkContext(conf)

    val sqlContext = new SQLContext(sc)

    //Temp table RSVPS
    sqlContext.sql(
      """CREATE TEMPORARY TABLE RSVPS
        |USING org.elasticsearch.spark.sql
        |OPTIONS (resource 'meetup_rsvps/meetup_rsvps/',
        |es.read.field.as.array.include 'group.group_topics')""".stripMargin)

    //Temp table distinct Groups
    sqlContext.sql(
      """SELECT group.group_id as group_id, group.group_name as group_name, group.group_city as city,
        |group.group_country as country, CONCAT(group.group_lat, ",", group.group_lon) as location,
        |group.group_topics as topics FROM RSVPS""".stripMargin)
      .distinct()
      .registerTempTable("GROUPS")

    //Temp table distinct events
    sqlContext.sql(
      """SELECT group.group_id as group_id, group.group_name as group_name, group.group_city as city,
        |group.group_country as country, CONCAT(group.group_lat, ",", group.group_lon) as location,
        |group.group_topics as topics, event.event_id as event_id, event.event_name as event_name,
        |if (event.time is null, '1970-01-01 01:00:00', date_format(event.time,'yyyy-MM-dd HH:mm:ss')) as event_time
        |FROM RSVPS""".stripMargin)
      .distinct()
      .registerTempTable("EVENTS")

    //Temp table repsponses, members and events counts for each Group
    sqlContext.sql(
      """SELECT group.group_id AS group_id,
        |count (member.member_id) AS members,
        |count (event.event_id) AS events,
        |count (response) AS responses,
        |sum (if (response = 'yes' , 1, 0)) AS responses_yes,
        |sum (if (response = 'no' , 1, 0)) AS responses_no,
        |sum (if (response = 'yes' , guests + 1, 0)) AS attendees,
        |sum (if (response = 'yes' , guests, 0)) AS guests
        |FROM RSVPS GROUP BY group.group_id""".stripMargin)
      .registerTempTable("GROUP_COUNTS")

    //Temp table repsponses counts for each event
    sqlContext.sql(
      """SELECT event.event_id AS event_id,
        |count (response) AS responses,
        |sum (if (response = 'yes' , 1, 0)) AS responses_yes,
        |sum (if (response = 'no' , 1, 0)) AS responses_no,
        |sum (if (response = 'yes' , guests + 1, 0)) AS attendees,
        |sum (if (response = 'yes' , guests, 0)) AS guests
        |FROM RSVPS GROUP BY event.event_id""".stripMargin)
      .registerTempTable("EVENT_COUNTS")


    //Join groups and group counts
    val joinedGroupsDF =
      sqlContext.sql(
        """SELECT g.group_id, g.group_name, g.city, g.country, g.location, g.topics, gc.members,
          |gc.events, gc.responses, gc.responses_yes, gc.responses_no, gc.guests, gc.attendees
          |FROM GROUPS g JOIN GROUP_COUNTS gc
          |ON (g.group_id = gc.group_id)""".stripMargin)
        .map { row => parseGroupRow(row) }


    joinedGroupsDF
      .cache()
      .saveToEs("meetup_groups/meetup_groups",
        Map("es.mapping.timestamp" -> "date",
          "es.mapping.id" -> "key",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        ))


    //Join events and event couonts
    val joinedEventsDF =
      sqlContext.sql(
        """SELECT e.event_id, e.event_name, e.event_time, e.group_name, e.city, e.country, e.location,
          |e.topics, ec.responses, ec.responses_yes, ec.responses_no, ec.guests, ec.attendees
          |FROM EVENTS e JOIN EVENT_COUNTS ec
          |ON (e.event_id = ec.event_id)""".stripMargin)
        .map { row => parseEventRow(row) }

    joinedEventsDF
      .cache()
      .saveToEs("meetup_events/meetup_events",
        Map("es.mapping.timestamp" -> "date",
          "es.mapping.id" -> "key",
          "es.nodes" -> ES_NODES,
          "es.port" -> ES_PORT
        ))

  }

  private def parseGroupRow(row: Row): MeetupGroupsCounts = {

    val date = TODAY.getTime

    MeetupGroupsCounts(
      s"${row.getLong(0)}-${date}",
      row.getLong(0),
      row.getString(1),
      row.getString(2),
      row.getString(3),
      row.getString(4),
      parseTopics(row.getAs[Seq[Row]](5)),
      row.getLong(6),
      row.getLong(7),
      row.getLong(8),
      row.getLong(9),
      row.getLong(10),
      row.getLong(11),
      row.getLong(12),
      date)
  }

  private def parseEventRow(row: Row): MeetupEventsCounts = {

    val date = TODAY.getTime
    val event_date = parseDate(row.getString(2))

    MeetupEventsCounts(
      s"${row.getString(0)}-${date}",
      row.getString(0),
      row.getString(1),
      event_date,
      row.getString(3),
      row.getString(4),
      row.getString(5),
      row.getString(6),
      parseTopics(row.getAs[Seq[Row]](7)),
      row.getLong(8),
      row.getLong(9),
      row.getLong(10),
      row.getLong(11),
      row.getLong(12),
      date)
  }


  private def parseDate(dateString: String): Long = {

    val format = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss")
    format.parse(dateString).getTime;
  }

  private def parseTopics(topics: Seq[Row]): List[RsvpGroupTopics] = {
    topics
      .map { case Row(topic_name: String, urlkey: String) => RsvpGroupTopics(Some(topic_name), Some(urlkey)) }.toList

  }

}
