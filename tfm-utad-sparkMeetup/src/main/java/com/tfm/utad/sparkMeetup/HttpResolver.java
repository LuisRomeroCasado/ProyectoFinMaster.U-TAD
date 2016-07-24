package com.tfm.utad.sparkMeetup;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.Iterator;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class HttpResolver implements Serializable {
    private static final long serialVersionUID = 1L;

    public JSONObject getGroup(String group_id)
    {

        URI request;
        String group = "";
        String events = "";

        try {

            request = new URIBuilder()
                    .setScheme(Conf.SCHEMA)
                    .setHost(Conf.HOST)
                    .setPath(Conf.METHOD_GROUPS)
                    .setParameter(Conf.SEARCH_TYPE, group_id)
                    .setParameter(Conf.ONLY, Conf.GROUPS_ONLY)
                    .setParameter(Conf.KEY,Conf.KEY_VALUE)
                    .setParameter(Conf.PAGE, Conf.PAGE_VALUE)
                    .setParameter(Conf.SIGN, Conf.TRUE)
                    .build();

            group = resolve(request);
            events = getEvents(group_id);

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        JSONObject response = DecodeJSON (group,events);

        return response;
    }

    private String getEvents(String group_id)
    {

        URI request;
        String events = "";

        try {

            request = new URIBuilder()
                    .setScheme(Conf.SCHEMA)
                    .setHost(Conf.HOST)
                    .setPath(Conf.METHOD_EVENTS)
                    .setParameter(Conf.SEARCH_TYPE, group_id)
                    .setParameter(Conf.ONLY, Conf.EVENTS_ONLY)
                    .setParameter(Conf.KEY,Conf.KEY_VALUE)
                    .setParameter(Conf.PAGE, Conf.PAGE_VALUE)
                    .setParameter(Conf.SIGN, Conf.TRUE)
                    .build();

            events = resolve(request);


        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return events;
    }

    private String resolve(URI request) {

        String responseString = "";

        try {

            HttpGet get = new HttpGet(request);

            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get);
            responseString = EntityUtils.toString(response.getEntity());


        } catch (IOException e) {
            e.printStackTrace();
        }


        return responseString;
    }

    private static JSONObject DecodeJSON(String group, String events){

        JSONObject json_result = null;
        JSONParser parser = new JSONParser();

        try{

            json_result = (JSONObject) parser.parse(group);
            JSONArray group_results = (JSONArray) json_result.get(Conf.RESULTS);

            Iterator i = group_results.iterator();
            JSONObject json_group = (JSONObject) i.next();

            json_result = (JSONObject) parser.parse(events);
            JSONArray events_results = (JSONArray) json_result.get(Conf.RESULTS);

            json_group.put("events",events_results);

            return json_group;
        } catch(Exception e){
            System.err.println("Exception DecodeJSON: " + group );
            System.err.println("Exception DecodeJSON: " + events);
            e.printStackTrace();
        }

        return json_result;
    }

}
