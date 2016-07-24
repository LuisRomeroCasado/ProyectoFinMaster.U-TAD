package com.tfm.utad.stormMeetup.storm;

public class Conf {
    public static final String WS_RSVPS = "ws://stream.meetup.com/2/rsvps";
    public static final int PARALLELISM_SPOUT = 2;
    public static final int PARALLELISM_BOLT = 2;
    public static final int NUM_STORM_WORKERS = 4;
    //public static final String KAFKA_ZK_HOST = "master:2181";
    public static final String KAFKA_BROKER_LIST = "master:9092,slave1:9092,slave2:9092";
    public static final String LOCAL_KAFKA_BROKER_LIST = "localhost:9092";
    public static final String KAFKA_TOPIC = "TOPIC_RSVPS";

}
