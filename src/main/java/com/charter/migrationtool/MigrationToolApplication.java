package com.charter.migrationtool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.charter.migrationtool.configuration.YAMLConfig;
import com.charter.migrationtool.services.SubscriberService;

@SpringBootApplication
public class MigrationToolApplication implements CommandLineRunner {
	
	private static Logger log = LoggerFactory.getLogger(MigrationToolApplication.class);

	@Autowired
	private YAMLConfig myConfig;
	
	@Autowired
	private SubscriberService subscriberService;

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(MigrationToolApplication.class);
		app.run();

	}

	@Override
	public void run(String... args) throws Exception {
		log.info("ericssonSubscriberUrl: " + myConfig.getEricssonSubscriberUrl());
		log.info("sericssonSubscriberAdTagUrl: " + myConfig.getEricssonSubscriberAdTagUrl());
		log.info("ericssonSubscriberSeriesUrl: " + myConfig.getEricssonSubscriberSeriesUrl());
		log.info("ericssonSubscriberRecordingUrl: " + myConfig.getEricssonSubscriberRecordingUrl());
		log.info("ericssonDeleteRecordingUrl: " + myConfig.getEricssonDeleteRecordingUrl());
		log.info("ericssonDeleteSeriesUrl: " + myConfig.getEricssonDeleteSeriesUrl());
		log.info("cdvrCsBaseUrl: " + myConfig.getCdvrCsBaseUrl());
		log.info("cdvrSmsBaseUrl: " + myConfig.getCdvrSmsBaseUrl());
		log.info("Number of threads: " + myConfig.getNumThreads());
		
		if (subscriberService.createServicePackage() == 200) {
			subscriberService.migrateAllSubcribers();
			
		} 

		
	}

}