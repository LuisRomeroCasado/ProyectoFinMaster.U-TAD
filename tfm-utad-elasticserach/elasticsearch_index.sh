##################################################
# Meetup responses
##################################################

set -x

HOST="http://localhost:9200"
SHARDS=3
REPLICAS=2

curl -XDELETE "$HOST/meetup_rsvps"

curl -XPUT "$HOST/meetup_rsvps" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_rsvps/_mappings/meetup_rsvps" -d '{

	"properties" : {
	    "event" : {
	      "properties" : {
	        "event_id" : {
	          "type" : "string"
	        },
	        "event_name" : {
	          "type" : "string"
	        },
	        "event_url" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "time" : {
	          "type": "date",
	          "doc_values": true,
	          "format": "epoch_millis"
	        }
	      }
	    },
	    "group" : {
	      "properties" : {
	        "group_city" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_country" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_id" : {
	          "type" : "long"
	        },
	        "group_lat" : {
	          "type" : "string"
	        },
	        "group_lon" : {
	          "type" : "string"
	        },
	        "group_name" : {
	          "type" : "string"
	        },
	        "group_state" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_topics" : {
	          "properties" : {
	            "topic_name" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            },
	            "urlkey" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            }
	          }
	        },
	        "group_urlname" : {
	          "type" : "string"
	        }
	      }
	    },
	    "guests" : {
	      "type" : "long"
	    },
	    "member" : {
	      "properties" : {
	        "member_id" : {
	          "type" : "long"
	        },
	        "member_name" : {
	          "type" : "string"
	        },
	        "photo" : {
	          "type" : "string"
	        },
	        "other_services" : {
	          "type" : "string"
	        }
	      }
	    },
	    "mtime" : {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	    },
	    "response" : {
	      "type" : "string",
	      "index": "not_analyzed"
	    },
	    "rsvp_id" : {
	      "type" : "long"
	    },
	    "venue" : {
	      "properties" : {
	        "lat" : {
	          "type" : "string"
	        },
	        "lon" : {
	          "type" : "string"
	        },
	        "venue_id" : {
	          "type" : "long"
	        },
	        "venue_name" : {
	          "type" : "string"
	        }
	      }
	    },
	    "visibility" : {
	      "type" : "string",
	      "index": "no"
	    }
	  }
	}'

##################################################
# Meetup responses by country
##################################################


curl -XDELETE "$HOST/meetup_rsvp_by_country"

curl -XPUT "$HOST/meetup_rsvp_by_country" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_rsvp_by_country/_mappings/meetup_rsvp_by_country" -d '{
  "_timestamp": {
    "enabled": true
  },
  "properties": {
    "key": {
      "type": "string"
    },
    "country": {
      "type": "string"
    },
    "location" : {
      "type" : "geo_point"
    },
    "response": {
      "type": "string",
      "index": "not_analyzed"
    },
    "guests": {
      "type": "long"
    },
    "attendees": {
      "type": "long"
    },	
	"date": {
	  "type": "date",
	  "doc_values": true,
	  "format": "epoch_millis"
	}
  }
}'

##################################################
# Meetup global trending topics
##################################################


curl -XDELETE "$HOST/meetup_global_trending_topics"

curl -XPUT "$HOST/meetup_global_trending_topics" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_global_trending_topics/_mappings/meetup_global_trending_topics" -d '{
	  "_timestamp": {
	    "enabled": true
	  },
	  "properties": {
	    "topic_name": {
      	      "type": "string"
      	},
	    "counter": {
	      "type": "long"
	    },
	    "date": {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	    }
	  }
	}'

##################################################
# Meetup global trending topics by country
##################################################
	

curl -XDELETE "$HOST/meetup_country_trending_topics"

curl -XPUT "$HOST/meetup_country_trending_topics" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_country_trending_topics/_mappings/meetup_country_trending_topics" -d '{
	  "_timestamp": { 
	    "enabled": true
	  },
	  "properties": {
	    "key": {
	      "type": "string"
	    },
	    "country": {
	      "type": "string"
	    },
	    "location" : {
          "type" : "geo_point"
        },
	    "topic_name": {
	      "type": "string"
	    },
	    "counter": {
	      "type": "long"
	    },
	    "date": {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	    }
	  }
	}'
	
##################################################
# Meetup groups
##################################################	


curl -XDELETE "$HOST/meetup_groups"

curl -XPUT "$HOST/meetup_groups" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_groups/_mappings/meetup_groups/" -d '{
	  "_timestamp": { 
	    "enabled": true
	  },
	  "properties": {
	    	    "key": {
	      "type": "string"
	    },
	        "group_city" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_country" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_id" : {
	          "type" : "long"
	        },
	        "location" : {
            "type" : "geo_point"
          },
	        "group_name" : {
	          "type" : "string"
	        },
	        "group_topics" : {
	          "properties" : {
	            "topic_name" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            },
	            "urlkey" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            }
	          }
	        },

	        	    "date" : {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	    },
	    "members" : {
"type" : "long"
	    },
	    "events" : {
"type" : "long"
	    },
	    
	    "responses" : {
"type" : "long"
	    }
	    ,
	    "response_yes" : {
"type" : "long"
	    }
	    ,
	    "response_no" : {
"type" : "long"
	    }
	    ,
	    "guests" : {
"type" : "long"
	    }
	    ,
	    "attendees" : {
"type" : "long"
	    }
	      }
	  }'


##################################################
# Meetup events
##################################################	

curl -XDELETE "$HOST/meetup_events"

curl -XPUT "$HOST/meetup_events" -d "{
    \"settings\" : {
        \"index\" : {
            \"number_of_shards\" : $SHARDS,
            \"number_of_replicas\" : $REPLICAS
        }
    }
}"

curl -XPUT "$HOST/meetup_events/_mappings/meetup_events/" -d '{
	  "_timestamp": { 
	    "enabled": true
	  },
	  "properties": {
	    	    "key": {
	      "type": "string"
	    },
	    
	        "event_id" : {
	          "type" : "String"
	        },
	        "event_name" : {
	          "type" : "string"
	        },
	        
	        "event_time" : {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	        },
	        "group_city" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "group_country" : {
	          "type" : "string",
	          "index": "not_analyzed"
	        },
	        "location" : {
            "type" : "geo_point"
          },
	        "group_name" : {
	          "type" : "string"
	        },
	        "group_topics" : {
	          "properties" : {
	            "topic_name" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            },
	            "urlkey" : {
	              "type" : "string",
	              "index": "not_analyzed"
	            }
	          }
	        },

	        	    "date" : {
	      "type": "date",
	      "doc_values": true,
	      "format": "epoch_millis"
	    },
	    "responses" : {
"type" : "long"
	    }
	    ,
	    "response_yes" : {
"type" : "long"
	    }
	    ,
	    "response_no" : {
"type" : "long"
	    }
	    ,
	    "guests" : {
"type" : "long"
	    }
	    ,
	    "attendees" : {
"type" : "long"
	    }
	      }
	  }'
