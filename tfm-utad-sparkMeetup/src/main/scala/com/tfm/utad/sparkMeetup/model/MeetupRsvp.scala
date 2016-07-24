package com.tfm.utad.sparkMeetup.model

case class RsvpEvent(
                      event_id: String,
                      event_name: Option[String],
                      event_url: Option[String],
                      time: Option[Long]
                    )

case class RsvpGroupTopics(
                            topic_name: Option[String],
                            urlkey: Option[String]
                          )

case class RsvpGroup(
                      group_id: Long,
                      group_name: String,
                      group_city: Option[String],
                      group_country: Option[String],
                      group_state: Option[String],
                      group_urlname: Option[String],
                      group_lat: Option[String],
                      group_lon: Option[String],
                      group_topics: List[RsvpGroupTopics]
                    )

case class RsvpMember(
                       member_id: Long,
                       member_name: Option[String],
                       other_services: Option[String],
                       photo: Option[String]
                     )

case class RsvpVenue(
                      venue_id: Option[Long],
                      venue_name: Option[String],
                      lat: Option[String],
                      lon: Option[String]
                    )

case class MeetupRsvp(
                       rsvp_id: Long,
                       response: String,
                       guests: Int,
                       mtime: Long,
                       visibility: String,
                       event: RsvpEvent,
                       group: RsvpGroup,
                       member: RsvpMember,
                       venue: RsvpVenue
                     )

case class RsvpsByCountry(
                           key: String,
                           country: String,
                           location: String,
                           response: String,
                           guests: Long,
                           attendees: Long,
                           date: Long
                         )

case class GlobalTrendingTopics(
                                 topic_name: String,
                                 counter: Long,
                                 date: Long
                               )

case class TrendingTopicsByCountry(
                                    key: String,
                                    country: String,
                                    location: String,
                                    topic_name: String,
                                    counter: Long,
                                    date: Long
                                  )

case class MeetupGroupsCounts(
                               key: String,
                               group_id: Long,
                               group_name: String,
                               group_city: String,
                               group_country: String,
                               location: String,
                               group_topics: List[RsvpGroupTopics],
                               members: Long,
                               events: Long,
                               responses: Long,
                               responses_yes: Long,
                               responses_no: Long,
                               guests: Long,
                               attendees: Long,
                               date: Long
                             )

case class MeetupEventsCounts(
                               key: String,
                               event_id: String,
                               event_name: String,
                               event_time: Long,
                               group_name: String,
                               group_city: String,
                               group_country: String,
                               location: String,
                               group_topics: List[RsvpGroupTopics],
                               responses: Long,
                               responses_yes: Long,
                               responses_no: Long,
                               guests: Long,
                               attendees: Long,
                               date: Long
                             )