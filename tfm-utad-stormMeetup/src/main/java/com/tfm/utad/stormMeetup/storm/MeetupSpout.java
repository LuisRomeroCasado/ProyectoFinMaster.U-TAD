package com.tfm.utad.stormMeetup.storm;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.concurrent.LinkedBlockingQueue;

import com.tfm.utad.stormMeetup.websocket.WebsocketClientEndpoint;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class MeetupSpout extends BaseRichSpout {

    public static Logger LOG = Logger.getLogger(MeetupSpout.class);

    private SpoutOutputCollector collector;
    private WebsocketClientEndpoint clientEndPoint;
    private LinkedBlockingQueue<String> queue = null;
    private int thisTaskIndex;

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(Field.MESSAGE));

    }

    @Override
    public void open(Map conf, TopologyContext context,
                     SpoutOutputCollector collector) {

        thisTaskIndex = context.getThisTaskIndex();
        String websocket_uri = Conf.WS_RSVPS;

        this.collector = collector;
        queue = new LinkedBlockingQueue<String>(1000);

        try {
            // open websocket
            clientEndPoint = new WebsocketClientEndpoint(new URI(websocket_uri));

            // add listener
            clientEndPoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    queue.offer(message);
                }
            });

        } catch (URISyntaxException ex) {
            LOG.error("URISyntaxException exception: " + ex.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    @Override
    public void nextTuple() {

        String msg = queue.poll();

        if (msg == null) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
            }
        } else {
            JSONObject obj = (JSONObject) JSONValue.parse(msg);
            Long rowId = Long.parseLong(obj.get(Field.ID).toString());
            if (rowId % Conf.PARALLELISM_SPOUT == thisTaskIndex) {
                System.out.println("Se obtiene mensaje " + thisTaskIndex + "-" + rowId + ": " + msg);
                collector.emit(new Values(msg));
            } else {
                System.out.println("Ignoramos mensaje " + thisTaskIndex + "-" + rowId + ": " + msg);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }
            }
        }

    }
}
