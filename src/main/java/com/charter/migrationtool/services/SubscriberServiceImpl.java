package com.charter.migrationtool.services;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.charter.migrationtool.models.CdvrSubscriber;
import com.charter.migrationtool.models.ServicePackage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SubscriberServiceImpl implements SubscriberService {
	@Autowired
	private SeriesRecordingService seriesRecordingService;

	@Autowired
	private EventRecordingService eventRecordingService;

	private static Logger log = LoggerFactory.getLogger(SubscriberServiceImpl.class);

	@Value("${ericssonSubscriberUrl}")
	private String ericssonSubscriberUrl;

	@Value("${ericssonSubscriberAdTagUrl}")
	private String ericssonSubscriberAdTagUrl;

	@Value("${cdvrSmsBaseUrl}")
	private String cdvrSmsBaseUrl;

	@Value("${cdvrCsBaseUrl}")
	private String cdvrCsBaseUrl;

	@Value("${numThreads}")
	private String numThreads;

	private volatile Map<String, String> summaryReportMap = new HashMap<>();

	/* Migrate All subscribers from Ericsson to Arris Scheduler */
	@Override
	public void migrateAllSubcribers() {
		log.info("Started migrating all subscribers");
		RestTemplate restTemplate = new RestTemplate();
		List<String> subscribersReplyXMLStrList = getSubcribersFromEricsson();
		SAXBuilder saxBuilder = new SAXBuilder();
		int activeSubscriberCount = 0;
		int inActiveSubscriberCount = 0;

		ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(numThreads));
		try {
			String channelStr = getChannelsFromCS();
			for (String subscribersReplyXMLStr : subscribersReplyXMLStrList) {

				Document doc = saxBuilder.build(new StringReader(subscribersReplyXMLStr));
				summaryReportMap.put("TotalSubscribers", doc.getRootElement().getAttributeValue("TotalResults"));
				Element subscribersElement = doc.getRootElement().getChild("Subscribers");
				List<Element> subscriberListElement = subscribersElement.getChildren();
				if (!subscriberListElement.isEmpty()) {
					for (Element subscriberElement : subscriberListElement) {
						if ("1".equals(subscriberElement.getAttributeValue("State"))) {
							activeSubscriberCount++;

							log.debug("Migrating subscriber with Id:" + subscriberElement.getAttributeValue("HomeID"));
							CdvrSubscriber cdvrSubscriber = new CdvrSubscriber();
							cdvrSubscriber.setId(subscriberElement.getAttributeValue("HomeID"));
							cdvrSubscriber.setName(subscriberElement.getAttributeValue("HomeID"));
							cdvrSubscriber.setStatus("ACTIVE");
							ServicePackage servicePackage = new ServicePackage();
							servicePackage.setId(ServicePackage.DEFAULT_ID);
							cdvrSubscriber.setServicePackage(servicePackage);

							String getHomeResponse = null;
							try {
								getHomeResponse = restTemplate.getForObject(ericssonSubscriberAdTagUrl + "/"
										+ subscriberElement.getAttributeValue("HomeID"), String.class);
								doc = saxBuilder.build(new StringReader(getHomeResponse));
								Element adtagsElements = doc.getRootElement().getChild("ad_tags");
								List<Element> adtagListElement = adtagsElements.getChildren();

								if (!adtagListElement.isEmpty()) {
									List<String> adtagList = new ArrayList<>();
									for (Element adtagElement : adtagListElement) {
										adtagList.add(adtagElement.getText());

									}
									cdvrSubscriber.setGeoTags(adtagList);
								}
							} catch (NullPointerException e) {
								log.error("NullPointerException in getting the Geotags :", e);
							}

							catch (RestClientException e) {
								log.error("RestClientException in getting the Geotags :", e);
							}

							Runnable worker = new Runnable() {

								@Override
								public void run() {
									log.info("Thread name: " + Thread.currentThread().getName());

									if (200 == creatCdvrSubscriber(cdvrSubscriber, summaryReportMap)) {

										seriesRecordingService.migrateSeriesRecordings(channelStr,
												cdvrSubscriber.getId(), summaryReportMap);

										eventRecordingService.migrateEventRecordings(channelStr, cdvrSubscriber.getId(),
												summaryReportMap);

									}

								}

							};
							executor.execute(worker);

						} else {
							inActiveSubscriberCount++;

						}
					}

				}
			}

			executor.shutdown();
			// Wait until all threads are terminated
			while (!executor.isTerminated()) {
			}
			log.info("Finished migrating all subscribers");

			summaryReportMap.put("ActiveSubscribers", String.valueOf(activeSubscriberCount));
			summaryReportMap.put("InactiveSubscribers", String.valueOf(inActiveSubscriberCount));
		} catch (JDOMException e) {
			log.error("JDOMException in migrateAllSubcribers :", e);
		} catch (IOException e) {
			log.error("IOException in migrateAllSubcribers :", e);
		}

		printSummaryReport(summaryReportMap);
		log.info("End method migrateAllSubcribers()");

	}

	@Override
	public Integer createServicePackage() {
		log.info("Creating Service Package for Migrating Subscribers with name: default_ericsson_svcpackage");
		ServicePackage servicePackage = new ServicePackage();
		ObjectMapper mapper = new ObjectMapper();

		Integer status = 0;
		try {
			String jsonCreateServicePackageRequest = mapper.writeValueAsString(servicePackage);
			jsonCreateServicePackageRequest = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(servicePackage);

			try {

				RestTemplate restTemplate = new RestTemplate();
				String createServicePackageEndpoint = cdvrSmsBaseUrl + "/servicePackages";
				log.info("Requesting Endpoint to Create Service Package: " + createServicePackageEndpoint);
				HttpHeaders httpHeaders = new HttpHeaders();
				httpHeaders.set("Content-Type", "application/json");
				HttpEntity<String> request = new HttpEntity<>(jsonCreateServicePackageRequest, httpHeaders);
				ResponseEntity<String> response = restTemplate.exchange(createServicePackageEndpoint, HttpMethod.PUT,
						request, String.class);
				status = response.getStatusCodeValue();
			} catch (RestClientException e) {
				log.error("RestClientException in  createServicePackage :", e);
			}

		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in createServicePackage:", e);
		}
		log.info("Successfully ServicePackage: default_ericsson_svcpackage");
		return status;
	}

	private List<String> getSubcribersFromEricsson() {
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_XML));
		HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
		List<String> subscribersReplyXMLStrList = new ArrayList<String>();
		ResponseEntity<String> response = restTemplate.exchange(ericssonSubscriberUrl, HttpMethod.GET, entity,
				String.class);

		String subscribersReplyXMLStr = response.getBody();
		subscribersReplyXMLStrList.add(subscribersReplyXMLStr);
		try {
			SAXBuilder saxBuilder = new SAXBuilder();
			Document doc = saxBuilder.build(new StringReader(subscribersReplyXMLStr));
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
					String endpoint = ericssonSubscriberUrl + "?Offset=" + offset + "&Limit=1000";
					response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, String.class);

					subscribersReplyXMLStrList.add(response.getBody());

				}

			}

		}

		catch (JDOMException e) {
			log.error("JDOMException getSubcribersFromEricsson :", e);
		} catch (IOException e) {
			log.error("IOException getSubcribersFromEricsson :", e);
		} catch (RestClientException e) {
			log.error("RestClientException getSubcribersFromEricsson :", e);
		}

		return subscribersReplyXMLStrList;

	}

	/* Get Channels list from the arris Cloud Scheduler */

	private String getChannelsFromCS() {
		RestTemplate restTemplate = new RestTemplate();
		String channelStr = restTemplate.getForObject(cdvrCsBaseUrl + "/channels?summary=true", String.class);

		return channelStr;
	}

	private int creatCdvrSubscriber(CdvrSubscriber cdvrSubscriber, Map<String, String> summaryReportMap) {
		log.debug("Creating subscriber with ID:" + cdvrSubscriber.getId());
		int status = 0;
		RestTemplate restTemplate = new RestTemplate();
		ObjectMapper mapper = new ObjectMapper();

		try {
			String jsonCdvrSubscriberRequest = mapper.writeValueAsString(cdvrSubscriber);
			jsonCdvrSubscriberRequest = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(cdvrSubscriber);
			String creatCdvrSubscriberEndpoint = cdvrSmsBaseUrl + "/subscribers/";
			HttpHeaders httpHeaders = new HttpHeaders();
			httpHeaders.set("Content-Type", "application/json");
			HttpEntity<String> request = new HttpEntity<>(jsonCdvrSubscriberRequest, httpHeaders);
			ResponseEntity<String> response = restTemplate.exchange(creatCdvrSubscriberEndpoint, HttpMethod.PUT,
					request, String.class);

			if (null != response) {
				if (response.getStatusCodeValue() == 200) {
					String responseBody = response.getBody();
					JSONObject jsonObject = new JSONObject(responseBody);
					JSONArray jsonDataArrayObject = jsonObject.getJSONArray("responses");
					status = jsonDataArrayObject.getJSONObject(0).getInt("status");
					log.debug("Successfully Created subscriber with ID:" + cdvrSubscriber.getId());
					if (status == 200) {
						if (summaryReportMap.containsKey("SuccessSubscribers")) {
							String successSubscribers = summaryReportMap.get("SuccessSubscribers");
							successSubscribers += "," + cdvrSubscriber.getId();
							summaryReportMap.put("SuccessSubscribers", successSubscribers);
						} else {
							summaryReportMap.put("SuccessSubscribers", cdvrSubscriber.getId());
						}

					} else {
						if (summaryReportMap.containsKey("FailedSubscribers")) {
							String failedSubscribers = summaryReportMap.get("FailedSubscribers");
							failedSubscribers += "," + cdvrSubscriber.getId();
							summaryReportMap.put("FailedSubscribers", failedSubscribers);
						} else {
							summaryReportMap.put("FailedSubscribers", cdvrSubscriber.getId());
						}

					}
				} else {
					if (summaryReportMap.containsKey("FailedSubscribers")) {
						String failedSubscribers = summaryReportMap.get("FailedSubscribers");
						failedSubscribers += "," + cdvrSubscriber.getId();
						summaryReportMap.put("FailedSubscribers", failedSubscribers);
					} else {
						summaryReportMap.put("FailedSubscribers", cdvrSubscriber.getId());
					}

				}
			}
		} catch (JsonProcessingException e) {
			log.error("JsonProcessingException in  creatCdvrSubscriber :", e);
		}

		return status;
	}
	/* Create deafult Service Package in arris SMS */

	private void printSummaryReport(Map<String, String> summaryReportMap) {
		if (!summaryReportMap.isEmpty()) {
			String successSubcribers = summaryReportMap.get("SuccessSubscribers");
			String failedSubcribers = summaryReportMap.get("FailedSubscribers");

			String successSeriesRecording = summaryReportMap.get("SuccessSeriesRecording");
			String failedSeriesRecording = summaryReportMap.get("FailedSeriesRecording");

			String successScheduledRecording = summaryReportMap.get("SuccessScheduledRecording");
			String failedScheduledRecording = summaryReportMap.get("FailedScheduledRecording");

			String successCompletedRecording = summaryReportMap.get("SuccessCompletedRecording");
			String failedCompletedRecording = summaryReportMap.get("FailedCompletedRecording");

			int successSeriesCount = 0;
			int failedSeriesCount = 0;

			int successSubscriberCount = 0;
			int failedSubscriberCount = 0;

			int successScheduledCount = 0;
			int failedScheduledCount = 0;

			int successCompletedCount = 0;
			int failedCompletedCount = 0;

			if (null != successSubcribers) {
				successSubscriberCount = (successSubcribers.split(",")).length;

			} else {
				successSubcribers = "None";
			}
			if (null != failedSubcribers) {
				failedSubscriberCount = (failedSubcribers.split(",")).length;

			} else {
				failedSubcribers = "None";
			}

			if (null != successSeriesRecording) {
				successSeriesCount = (successSeriesRecording.split(",")).length;

			} else {
				successSeriesRecording = "None";
			}
			if (null != failedSeriesRecording) {
				failedSeriesCount = (failedSeriesRecording.split(",")).length;

			} else {
				failedSeriesRecording = "None";
			}

			if (null != successScheduledRecording) {
				successScheduledCount = (successScheduledRecording.split(",")).length;

			} else {
				successScheduledRecording = "None";
			}
			if (null != failedScheduledRecording) {
				failedScheduledCount = (failedScheduledRecording.split(",")).length;

			} else {
				failedScheduledRecording = "None";
			}

			if (null != successCompletedRecording) {
				successCompletedCount = (successCompletedRecording.split(",")).length;

			} else {
				successCompletedRecording = "None";
			}
			if (null != failedCompletedRecording) {
				failedCompletedCount = (failedCompletedRecording.split(",")).length;

			} else {
				failedCompletedRecording = "None";
			}
			log.info("=========================================================================================");
			log.info("==================================Migration Summary Report===============================");
			log.info("=========================================================================================");
			log.info("Subscriber Migration=====================================================================");
			log.info("=========================================================================================");
			log.info("Total Number of Subscribers in Ericsson:" + summaryReportMap.get("TotalSubscribers"));
			log.info("Total Number of Inactive Subscribers :" + summaryReportMap.get("InactiveSubscribers"));
			log.info("Total Number of Active Subscribers eligible for Migration:"
					+ summaryReportMap.get("ActiveSubscribers"));

			log.info("The Number of Subscribers Migrated Successfully:" + successSubscriberCount);
			log.info("The List of Subscribers Migrated Successfully:" + successSubcribers);
			log.info("The Number of Subscribers Migrated with Failures:" + failedSubscriberCount);
			log.info("The List of Subscribers Migrated with Failures:" + failedSubcribers);

			log.info("=========================================================================================");
			log.info("Series Recordings Migration==============================================================");
			log.info("=========================================================================================");

			log.info("Total Number of Series in Ericsson:" + summaryReportMap.get("TotalSeries"));
			log.info("Total Number of Series Eligible for Migration:" + summaryReportMap.get("TotalSeriesEligible"));
			log.info("The Number of Series Migrated Successfully:" + successSeriesCount);
			log.info("The List of Series Migrated Successfully:" + successSeriesRecording);
			log.info("The Number of Series Migrated with Failures:" + failedSeriesCount);
			log.info("The List of Series Migrated with Failures:" + failedSeriesRecording);

			log.info("=========================================================================================");
			log.info("Event Recordings Migration===============================================================");
			log.info("=========================================================================================");

			log.info("Total Number of eligible Scheduled Recordings in Ericsson for Migration:"
					+ summaryReportMap.get("TotalEventReschuled"));
			log.info("The Number of Scheduled Recordings Migrated Successfully:" + successScheduledCount);
			log.info("The List of Scheduled Recordings Migrated Successfully:" + successScheduledRecording);
			log.info("The Number of Scheduled Recordings Migrated with Failures:" + failedScheduledCount);
			log.info("The List of Scheduled Recordings Migrated with Failures:" + failedScheduledRecording);

			log.info("=========================================================================================");
			log.info("Completed Recordings Migration===========================================================");
			log.info("=========================================================================================");

			log.info("Total Number of Completed Recordings in Ericsson for Migration:"
					+ summaryReportMap.get("TotalCompletedMigration"));
			log.info("The Number of Completed Recordings Migrated Successfully:" + successCompletedCount);
			log.info("The List of Completed Recordings Migrated Successfully:" + successCompletedRecording);
			log.info("The Number of Completed Recordings Migrated with Failures:" + failedCompletedCount);
			log.info("The List of Completed Recordings Migrated with Failures:" + failedCompletedRecording);
		}

	}

}
