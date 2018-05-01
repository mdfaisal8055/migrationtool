package com.charter.migrationtool.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.client.RestTemplate;

import com.charter.migrationtool.models.CdvrScheduleSeriesRecording;
import com.charter.migrationtool.models.Options;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SeriesRecordingServiceImpl implements SeriesRecordingService {

	private static Logger log = LoggerFactory.getLogger(SeriesRecordingServiceImpl.class);

	@Value("${ericssonSubscriberSeriesUrl}")
	private String ericssonSubscriberSeriesUrl;

	@Value("${cdvrCsBaseUrl}")
	private String cdvrCsBaseUrl;

	/* Migrating Series Recording */
	@Override
	public void migrateSeriesRecordings(String channelStr, String subId, Map<String, String> summaryReportMap) {
		log.info("Started migrating all series for subcriber Id: " + subId);
		String seriesRecordingReplyXMLStr = getSeriesRecordingFromEricsson(subId);
		JSONObject jsonObject = new JSONObject(channelStr);
		JSONArray entitiesArr = jsonObject.getJSONArray("entities");
		int totalSeriesEligibleCount = 0;

		SAXBuilder saxBuilder = new SAXBuilder();
		try {
			Document doc = saxBuilder.build(new StringReader(seriesRecordingReplyXMLStr));
			Element seriesDataElement = doc.getRootElement().getChild("SeriesDataList");

			List<Element> seriesDataListElement = seriesDataElement.getChildren();

			if (!seriesDataListElement.isEmpty()) {
				int totalSeriesCount = Integer.parseInt(doc.getRootElement().getAttributeValue("TotalResults"));
				if (summaryReportMap.containsKey("TotalSeries")) {
					int count = Integer.parseInt(summaryReportMap.get("TotalSeries"));
					count += totalSeriesCount;
					summaryReportMap.put("TotalSeries", String.valueOf(count));
				} else {

					summaryReportMap.put("TotalSeries", String.valueOf(totalSeriesCount));
				}

				for (Element seriesElement : seriesDataListElement) {
					for (int i = 0; i < entitiesArr.length(); i++) {
						if (entitiesArr.getString(i).equals(seriesElement.getAttributeValue("Channel"))) {
							totalSeriesEligibleCount++;
							if (summaryReportMap.containsKey("TotalSeriesEligible")) {
								int count = Integer.parseInt(summaryReportMap.get("TotalSeriesEligible"));
								count += 1;
								summaryReportMap.put("TotalSeriesEligible", String.valueOf(count));
							} else {

								summaryReportMap.put("TotalSeriesEligible", String.valueOf(totalSeriesEligibleCount));
							}

							CdvrScheduleSeriesRecording cdvrScheduleSeriesRecording = new CdvrScheduleSeriesRecording();
							cdvrScheduleSeriesRecording.setChannelId(seriesElement.getAttributeValue("Channel"));
							cdvrScheduleSeriesRecording.setSeriesId(seriesElement.getAttributeValue("SeriesID"));
							Options options = new Options();
							/* Check Space Conflict Policy */
							if ("true".equals(seriesElement.getAttributeValue("IsProtected"))) {
								options.setSpaceConflictPolicy("FOREVER");
							} else {
								options.setSpaceConflictPolicy("UNTIL_SPACE_NEEDED");
							}
							/* Check Recording Criteria */
							if ("1".equals(seriesElement.getAttributeValue("RecordCriteria"))) {
								options.setAccept("FIRST_RUN_ONLY");
							} else {
								options.setAccept("REPEATS");
							}
							if (null != seriesElement.getAttributeValue("StartTimeOffset")) {
								int startTimeOffset = Integer
										.parseInt(seriesElement.getAttributeValue("StartTimeOffset"));
								options.setStartOffset(startTimeOffset);
							}

							if (null != seriesElement.getAttributeValue("EndTimeOffset")) {
								int endTimeOffset = Integer.parseInt(seriesElement.getAttributeValue("EndTimeOffset"));
								options.setEndOffset(endTimeOffset);
							}
							cdvrScheduleSeriesRecording.setOptions(options);
							sendSeriesScheduleRecording(cdvrScheduleSeriesRecording, subId, summaryReportMap);
						}
					}
				}
			}
		} catch (JDOMException e) {
			log.error("JDOMException in migrateSeriesRecordings ", e);
		} catch (IOException e) {
			log.error("IOException in migrateSeriesRecordings ", e);
		}
	}

	private String getSeriesRecordingFromEricsson(String subId) {
		log.debug("Retrieving Series Recordings list From Ericsson");
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));

		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		String seriesEndpoint = ericssonSubscriberSeriesUrl + "/" + subId;
		ResponseEntity<String> response = restTemplate.exchange(seriesEndpoint, HttpMethod.GET, entity, String.class);
		String seriesRecordingReplyXMLStr = response.getBody();
		log.debug("Retrieved Series Recordings list From Ericsson");

		return seriesRecordingReplyXMLStr;

	}

	/* Send Reschedule request for series recording */
	private void sendSeriesScheduleRecording(CdvrScheduleSeriesRecording cdvrScheduleSeriesRecording, String subId,
			Map<String, String> summaryReportMap) {
		log.debug("Reschedule series recording for subcriber Id:" + subId + " and Series ID"
				+ cdvrScheduleSeriesRecording.getSeriesId());
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		try {
			String jsonCdvrScheduleSeriesRecordingRequest = mapper.writeValueAsString(cdvrScheduleSeriesRecording);
			jsonCdvrScheduleSeriesRecordingRequest = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(cdvrScheduleSeriesRecording);
			try {
				String scheduleSeriesEndpoint = cdvrCsBaseUrl + "/subscribers/" + subId + "/recordings";
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Content-Type", "application/json");
				HttpEntity<String> request = new HttpEntity<>(jsonCdvrScheduleSeriesRecordingRequest, httpHeaders);
				ResponseEntity<String> response = restTemplate.exchange(scheduleSeriesEndpoint, HttpMethod.POST,
						request, String.class);

				if (null != response) {
					if (response.getStatusCodeValue() == 201) {
						if (summaryReportMap.containsKey("SuccessSeriesRecording")) {
							String successSeriesId = summaryReportMap.get("SuccessSeriesRecording");
							successSeriesId += "," + cdvrScheduleSeriesRecording.getSeriesId();
							summaryReportMap.put("SuccessSeriesRecording", successSeriesId);
						} else {
							summaryReportMap.put("SuccessSeriesRecording", cdvrScheduleSeriesRecording.getSeriesId());
						}

					} else {
						if (summaryReportMap.containsKey("FailedSeriesRecording")) {
							String failedSeriesId = summaryReportMap.get("FailedSeriesRecording");
							failedSeriesId += "," + cdvrScheduleSeriesRecording.getSeriesId();
							summaryReportMap.put("FailedSeriesRecording", failedSeriesId);
						} else {
							summaryReportMap.put("FailedSeriesRecording", cdvrScheduleSeriesRecording.getSeriesId());
						}

					}
					log.debug("Successfully rescheduled series recording for subcriber Id:" + subId + " and Series ID"
							+ cdvrScheduleSeriesRecording.getSeriesId());

				}
			} catch (HttpClientErrorException e) {
				log.error("HttpClientErrorException in sendSeriesScheduleRecording ", e);
				if (summaryReportMap.containsKey("FailedSeriesRecording")) {
					String failedSeriesId = summaryReportMap.get("FailedSeriesRecording");
					failedSeriesId += "," + cdvrScheduleSeriesRecording.getSeriesId();
					summaryReportMap.put("FailedSeriesRecording", failedSeriesId);
				} else {
					summaryReportMap.put("FailedSeriesRecording", cdvrScheduleSeriesRecording.getSeriesId());
				}

			}

		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in sendSeriesScheduleRecording ", e);
		}

	}

}
