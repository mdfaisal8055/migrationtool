package com.charter.migrationtool.services;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.charter.migrationtool.models.CdvrReScheduleEventRecording;
import com.charter.migrationtool.models.EricssonRecording;
import com.charter.migrationtool.models.Options;
import com.charter.migrationtool.models.ProgramInfo;
import com.charter.migrationtool.models.RecordingMigrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EventRecordingServiceImpl implements EventRecordingService {

	private static Logger log = LoggerFactory.getLogger(EventRecordingServiceImpl.class);

	@Value("${ericssonSubscriberRecordingUrl}")
	private String ericssonSubscriberRecordingUrl;

	@Value("${cdvrCsBaseUrl}")
	private String cdvrCsBaseUrl;

	/* Migrating Event Recordings */
	@Override
	public void migrateEventRecordings(String channelStr, String subId, Map<String, String> summaryReportMap) {
		log.info("Started migrating all event recording for subcriber Id: " + subId);
		List<String> eventRecordingReplyXMLStrList = getEventRecordingFromEricsson(subId);
		JSONObject jsonObject = new JSONObject(channelStr);
		JSONArray entitiesArr = jsonObject.getJSONArray("entities");
		int totalEventRescheduleCount = 0;
		int totalCompletedMigrationCount = 0;
		SAXBuilder saxBuilder = new SAXBuilder();
		try {

			for (String eventRecordingReplyXMLStr : eventRecordingReplyXMLStrList) {

				Document doc = saxBuilder.build(new StringReader(eventRecordingReplyXMLStr));
				Element evetDataElement = doc.getRootElement();
				List<Element> eventDataListElement = evetDataElement.getChildren();

				if (!eventDataListElement.isEmpty()) {
					int totalEventCount = Integer.parseInt(doc.getRootElement().getAttributeValue("TotalResults"));
					if (summaryReportMap.containsKey("TotalEvent")) {
						int count = Integer.parseInt(summaryReportMap.get("TotalEvent"));
						count += totalEventCount;
						summaryReportMap.put("TotalEvent", String.valueOf(count));
					} else {

						summaryReportMap.put("TotalEvent", String.valueOf(totalEventCount));
					}
					for (Element eventRecordingElement : eventDataListElement) {
						/*
						 * Filter events which has State equal to 1=scheduled, 2=ongoing, 3=processing
						 */
						String state = eventRecordingElement.getChild("ABRDetails").getAttributeValue("State");

						if ("1".equals(state) || "2".equals(state) || "3".equals(state)) {
							for (int i = 0; i < entitiesArr.length(); i++) {
								if (entitiesArr.getString(i)
										.equals(eventRecordingElement.getAttributeValue("Channel"))) {
									totalEventRescheduleCount++;
									if (summaryReportMap.containsKey("TotalEventReschuled")) {
										int count = Integer.parseInt(summaryReportMap.get("TotalEventReschuled"));
										count += 1;
										summaryReportMap.put("TotalEventReschuled", String.valueOf(count));
									} else {

										summaryReportMap.put("TotalEventReschuled",
												String.valueOf(totalEventRescheduleCount));
									}

									String channelId = eventRecordingElement.getAttributeValue("Channel");
									String programId = eventRecordingElement.getChild("ProgramInfo")
											.getAttributeValue("ProgramID");

									/* Formating Start time */
									String date = eventRecordingElement.getAttributeValue("ShowStartTime");

									DateTimeFormatter startTimeFormatter = DateTimeFormatter
											.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
									LocalDateTime datetime = LocalDateTime.parse(date, startTimeFormatter);

									String airingId = buildAiringId(channelId, datetime.toInstant(ZoneOffset.UTC),
											programId);

									CdvrReScheduleEventRecording cdvrReScheduleEventRecording = new CdvrReScheduleEventRecording();
									cdvrReScheduleEventRecording.setAiringId(airingId);

									Options options = new Options();
									/* Check Space Conflict Policy */
									if ("true".equals(eventRecordingElement.getAttributeValue("IsProtected"))) {
										options.setSpaceConflictPolicy("FOREVER");
									} else {
										options.setSpaceConflictPolicy("UNTIL_SPACE_NEEDED");
									}
									cdvrReScheduleEventRecording.setOptions(options);
									ResponseEntity<String> response = sendEventScheduleRecording(
											cdvrReScheduleEventRecording, subId, summaryReportMap);
									if (null != response) {
										if (response.getStatusCodeValue() == 201) {
											if (summaryReportMap.containsKey("SuccessScheduledRecording")) {
												String successAiringId = summaryReportMap
														.get("SuccessScheduledRecording");
												successAiringId += "," + cdvrReScheduleEventRecording.getAiringId();
												summaryReportMap.put("SuccessScheduledRecording", successAiringId);
											} else {
												summaryReportMap.put("SuccessScheduledRecording",
														cdvrReScheduleEventRecording.getAiringId());
											}
											log.debug("Successfully Rescheduled the event recording in  cdvr: "
													+ cdvrReScheduleEventRecording.getAiringId());

										} else {
											if (summaryReportMap.containsKey("FailedScheduledRecording")) {
												String failedAiringId = summaryReportMap
														.get("FailedScheduledRecording");
												failedAiringId += "," + cdvrReScheduleEventRecording.getAiringId();
												summaryReportMap.put("FailedScheduledRecording", failedAiringId);
											} else {
												summaryReportMap.put("FailedScheduledRecording",
														cdvrReScheduleEventRecording.getAiringId());
											}

										}
									}
									break;
								}

							}
						}
						/* Filter events which has State equal to 4=completed */
						else if ("4".equals(state)) {
							totalCompletedMigrationCount++;

							if (summaryReportMap.containsKey("TotalCompletedMigration")) {
								int count = Integer.parseInt(summaryReportMap.get("TotalCompletedMigration"));
								count += 1;
								summaryReportMap.put("TotalCompletedMigration", String.valueOf(count));
							} else {

								summaryReportMap.put("TotalCompletedMigration",
										String.valueOf(totalCompletedMigrationCount));
							}
							List<EricssonRecording> ericssonRecordingList = new ArrayList<EricssonRecording>();

							RecordingMigrationRequest recordinMigrationRequest = new RecordingMigrationRequest();
							EricssonRecording ericssonRecording = new EricssonRecording();
							ericssonRecordingList.add(ericssonRecording);
							ProgramInfo programInfo = new ProgramInfo();

							for (int i = 0; i < entitiesArr.length(); i++) {
								if (entitiesArr.getString(i)
										.equals(eventRecordingElement.getAttributeValue("Channel"))) {
									ericssonRecording.setChannel(eventRecordingElement.getAttributeValue("Channel"));
									break;
								} else {
									ericssonRecording.setChannel("999999");
								}
							}
							ericssonRecording.setShowingID(eventRecordingElement.getAttributeValue("ShowingID"));
							ericssonRecording
									.setChannelCallLetter(eventRecordingElement.getAttributeValue("ChannelCallLetter"));
							ericssonRecording
									.setShowStartTime(eventRecordingElement.getAttributeValue("ShowStartTime"));
							ericssonRecording.setShowEndTime(eventRecordingElement.getAttributeValue("ShowEndTime"));
							ericssonRecording.setStartTime(eventRecordingElement.getAttributeValue("StartTime"));
							ericssonRecording.setEndTime(eventRecordingElement.getAttributeValue("EndTime"));
							ericssonRecording.setIsProtected(
									Boolean.valueOf(eventRecordingElement.getAttributeValue("IsProtected")));
							ericssonRecording.setBookmark(eventRecordingElement.getAttributeValue("Bookmark"));
							programInfo.setProgramID(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("ProgramID"));
							programInfo
									.setName(eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Name"));
							programInfo.setSubTitle(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("SubTitle"));
							programInfo.setSeasonID(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("SeasonID"));
							programInfo.setSeasonName(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("SeasonName"));
							programInfo.setSeriesID(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("SeriesID"));
							programInfo.setSeriesName(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("SeriesName"));
							programInfo.setRating(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Rating"));
							programInfo.setDescription(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Description"));
							if (null != eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Year")) {
								programInfo.setYear(Integer.valueOf(
										eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Year")));
							} else {
								programInfo.setYear(0);
							}
							if (null != eventRecordingElement.getChild("ProgramInfo")
									.getAttributeValue("EpisodeNumber")) {
								programInfo.setEpisodeNumber(Integer.valueOf(eventRecordingElement
										.getChild("ProgramInfo").getAttributeValue("EpisodeNumber")));
							} else {
								programInfo.setEpisodeNumber(0);
							}

							programInfo.setEpisodeTitle(
									eventRecordingElement.getChild("ProgramInfo").getAttributeValue("EpisodeTitle"));
							programInfo
									.setGenre(eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Genre"));
							programInfo
									.setType(eventRecordingElement.getChild("ProgramInfo").getAttributeValue("Type"));
							ericssonRecording.setProgramInfo(programInfo);

							recordinMigrationRequest.setEricssonRecording(ericssonRecordingList);

							ResponseEntity<String> response = sendCompletedRecordingMigration(recordinMigrationRequest,
									subId, summaryReportMap);
							if (null != response) {
								if (response.getStatusCodeValue() == 200) {
									if (summaryReportMap.containsKey("SuccessCompletedRecording")) {
										String successCompletedId = summaryReportMap.get("SuccessCompletedRecording");
										successCompletedId += "," + ericssonRecording.getShowingID();
										summaryReportMap.put("SuccessCompletedRecording", successCompletedId);

									} else {
										summaryReportMap.put("SuccessCompletedRecording",
												ericssonRecording.getShowingID());
									}
									log.debug("Successfully Migrated the completed recording: "
											+ ericssonRecording.getShowingID());

								} else {
									if (summaryReportMap.containsKey("FailedCompletedRecording")) {
										String failedCompletedId = summaryReportMap.get("FailedCompletedRecording");
										failedCompletedId += "," + ericssonRecording.getShowingID();
										summaryReportMap.put("FailedCompletedRecording", failedCompletedId);
									} else {
										summaryReportMap.put("FailedCompletedRecording",
												ericssonRecording.getShowingID());
									}

								}
							}

						}
					}

				}

			}
		} catch (JDOMException e) {
			log.error("JDOMException in migrateEventRecordings ", e);
		} catch (IOException e) {
			log.error("IOException in migrateEventRecordings ", e);
		}
	}

	/* Building airingId with Channel_ShowStartTime_ProgramInfo->ProgramID */
	private String buildAiringId(String channel, Instant startTime, String programId) {
		Date date = Date.from(startTime);
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String sdateString = df.format(date);
		StringBuilder sb = new StringBuilder();
		sb.append(channel).append("_").append(sdateString).append("_").append(programId);
		return sb.toString();
	}

	/* Send Reschedule request for event recording */
	private ResponseEntity<String> sendEventScheduleRecording(CdvrReScheduleEventRecording cdvrReScheduleEventRecording,
			String subId, Map<String, String> summaryReportMap) {

		log.debug("Rescheduling the event recording in  cdvr");
		ResponseEntity<String> response = null;
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		try {
			String jsonCdvrReScheduleEventRecordingRequest = mapper.writeValueAsString(cdvrReScheduleEventRecording);
			jsonCdvrReScheduleEventRecordingRequest = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(cdvrReScheduleEventRecording);
			String scheduleEventEndpoint = cdvrCsBaseUrl + "/subscribers/" + subId + "/recordings";
			log.debug("scheduleEventEndpoint: " + scheduleEventEndpoint);
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Content-Type", "application/json");
			HttpEntity<String> request = new HttpEntity<>(jsonCdvrReScheduleEventRecordingRequest, httpHeaders);
			response = restTemplate.exchange(scheduleEventEndpoint, HttpMethod.POST, request, String.class);

		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in sendEventScheduleRecording ", e);
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException in sendEventScheduleRecording ", e);
			if (summaryReportMap.containsKey("FailedScheduledRecording")) {
				String failedAiringId = summaryReportMap.get("FailedScheduledRecording");
				failedAiringId += "," + cdvrReScheduleEventRecording.getAiringId();
				summaryReportMap.put("FailedScheduledRecording", failedAiringId);
			} else {
				summaryReportMap.put("FailedScheduledRecording", cdvrReScheduleEventRecording.getAiringId());
			}

		}
		return response;
	}

	private List<String> getEventRecordingFromEricsson(String subId) {
		log.debug("Retriveing the list of recording from Ericsson for Subscriber Id: " + subId);
		String eventRecordingReplyXMLStr = null;
		List<String> eventRecordingReplyXMLStrList = new ArrayList<String>();
		try {
			RestTemplate restTemplate = new RestTemplate();
			HttpHeaders headers = new HttpHeaders();
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));

			HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
			String eventEndpoint = ericssonSubscriberRecordingUrl + "/" + subId;
			log.debug("EventEndpoint" + eventEndpoint);
			ResponseEntity<String> response = restTemplate.exchange(eventEndpoint, HttpMethod.GET, entity,
					String.class);
			eventRecordingReplyXMLStr = response.getBody();
			eventRecordingReplyXMLStrList.add(eventRecordingReplyXMLStr);
			SAXBuilder saxBuilder = new SAXBuilder();
			Document doc = saxBuilder.build(new StringReader(eventRecordingReplyXMLStr));
			int total = Integer.parseInt(doc.getRootElement().getAttributeValue("TotalResults"));
			int mod = total % 1000;
			int offset = 0;
			int count = 0;
			if (total > 1000) {
				if (mod == 0) {
					count = total / 1000;
				} else {
					count = total / 1000;
					count += 1;
				}
				for (int i = 1; i < count; i++) {
					if (i == 1) {
						offset = 1000;

					} else {
						offset += 1000;
					}
					eventEndpoint = ericssonSubscriberRecordingUrl + "/" + subId + "?Offset=" + offset + "&Limit=1000";
					response = restTemplate.exchange(eventEndpoint, HttpMethod.GET, entity, String.class);

					eventRecordingReplyXMLStrList.add(response.getBody());

				}

			}

		}

		catch (RestClientException e) {
			log.error("RestClientException in getEventRecordingFromEricsson ", e);
		} catch (IOException e) {
			log.error("IOException getSubcribersFromEricsson :", e);
		} catch (JDOMException e) {
			log.error("IOException getSubcribersFromEricsson :", e);
		}

		return eventRecordingReplyXMLStrList;

	}

	/* Send Completed record Migration request for event recording */
	private ResponseEntity<String> sendCompletedRecordingMigration(RecordingMigrationRequest recordingMigrationRequest,
			String subId, Map<String, String> summaryReportMap) {
		log.debug("Sending Completed Recording from Ericsson to Cdvr for Subscriber Id: " + subId);
		ResponseEntity<String> response = null;
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		try {
			String jsonRecordingMigrationRequest = mapper.writeValueAsString(recordingMigrationRequest);
			jsonRecordingMigrationRequest = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(recordingMigrationRequest);
			// String scheduleSeriesEndpoint = cdvrCsBaseUrl + "/subscribers/" + subId +
			// "/recordingmigration";
			String completedRecordingMigrationEndpoint = "http://10.0.2.15:8092/cs/v3/subscribers/4E654G009851/recordingmigration";
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Content-Type", "application/json");
			HttpEntity<String> request = new HttpEntity<>(jsonRecordingMigrationRequest, httpHeaders);
			response = restTemplate.exchange(completedRecordingMigrationEndpoint, HttpMethod.POST, request,
					String.class);

		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in sendCompletedRecordingMigration :", e);
		} catch (HttpClientErrorException e) {
			log.error("HttpClientErrorException in sendCompletedRecordingMigration ", e);
			if (summaryReportMap.containsKey("FailedCompletedRecording")) {
				String failedCompletedId = summaryReportMap.get("FailedCompletedRecording");
				failedCompletedId += "," + recordingMigrationRequest.getEricssonRecording().get(0).getShowingID();
				summaryReportMap.put("FailedCompletedRecording", failedCompletedId);
			} else {
				summaryReportMap.put("FailedCompletedRecording",
						recordingMigrationRequest.getEricssonRecording().get(0).getShowingID());
			}

		}

		return response;
	}
}
