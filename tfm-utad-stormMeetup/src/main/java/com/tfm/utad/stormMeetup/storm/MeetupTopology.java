package com.tfm.utad.stormMeetup.storm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.utils.Utils;
import com.google.common.io.Resources;
import org.apache.log4j.Logger;
import storm.kafka.bolt.KafkaBolt;
import storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.bolt.selector.DefaultTopicSelector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class MeetupTopology {
    private TopologyBuilder builder = new TopologyBuilder();
    private Config conf = new Config();
    private LocalCluster cluster;
    public static Logger LOG = Logger.getLogger(MeetupSpout.class);


    public MeetupTopology() {
        try {
            builder.setSpout("meetupSpout", new MeetupSpout(), Conf.PARALLELISM_SPOUT);

            KafkaBolt<String, String> kafkaBolt = new KafkaBolt<String, String>()
                    .withTopicSelector(new DefaultTopicSelector(Conf.KAFKA_TOPIC))
                    .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper());

            builder.setBolt("KafkaBolt", kafkaBolt, Conf.PARALLELISM_BOLT)
                    .shuffleGrouping("meetupSpout");

            initializeKafkaBolt();


        } catch (IOException e) {
            LOG.error("error MeetupTopology", e);
        }

    }

    private void initializeKafkaBolt() throws IOException {
        try {
            Properties kafka_broker_properties = new Properties();
            try (InputStream props = Resources.getResource("producer.props").openStream()) {
                kafka_broker_properties.load(props);
            }

            kafka_broker_properties.put("metadata.broker.list", Conf.KAFKA_BROKER_LIST);

            conf.put(KafkaBolt.KAFKA_BROKER_PROPERTIES, kafka_broker_properties);
            conf.put(KafkaBolt.TOPIC, Conf.KAFKA_TOPIC);
        } catch (IOException e) {
            LOG.error("error initialize KafkaBolt", e);
        }
    }

    public TopologyBuilder getBuilder() {
        return builder;
    }

    public LocalCluster getLocalCluster() {
        return cluster;
    }

    public Config getConf() {
        return conf;
    }

    public void runLocal(int runTime) {

        conf.setDebug(false);

        cluster = new LocalCluster();
        cluster.submitTopology("test", conf, builder.createTopology());
        if (runTime > 0) {
            Utils.sleep(runTime);
            shutDownLocal();
        }
    }

    public void shutDownLocal() {
        if (cluster != null) {
            cluster.killTopology("test");
            cluster.shutdown();
        }
    }

    public void runCluster(String name)
            throws AlreadyAliveException, InvalidTopologyException {

        try {
            conf.setNumWorkers(Conf.NUM_STORM_WORKERS);
            StormSubmitter.submitTopology(name, conf, builder.createTopology());
        } catch (AlreadyAliveException | InvalidTopologyException e) {
            LOG.error("error building or submitting topology: ", e);
        }

    }

    public static void main(String[] args) throws Exception {

        MeetupTopology topology = new MeetupTopology();
        if (args != null && args.length > 1) {
            topology.runCluster(args[0]);
        } else {
            if (args != null && args.length == 1)
                System.out.println("Local mode");
            topology.runLocal(-1);
        }

    }
}
