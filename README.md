# Migration Tool  #

## Introduction ##

Ericsson nDVR system will phase out by end of the year. The existing subscribers and their recordings in Ericsson system should be migrated into the new CDVR cloud scheduler system. All the future recordings done by the subscribers imported from Ericsson system after migration will be handled and managed by CDVR system. A deadline will be set, for example, the end of year 2018. After the deadline, recordings done on Ericsson system will not be available in the CDVR system.

Ericsson system used recording time in "MINUTES" to count against quota. CDVR uses the number of recordings as the unit of quota. The quota number in Ericsson system is ignored. The used quota for migrated recordings will be counted directly on the number of migrated recordings.



The following lists the requirements in items easy to read.

1.Subscribers in Ericsson system are migrated into SMS (Subscriber Management Service) in CDVR.
2.Recordings in Ericsson system are migrated into CS (Cloud Scheduler) in CDVR. Only recording records are imported into CS. The recording data are still kept by Ericsson system.
3.Scheduled event and series recordings which happen in future will be re-scheduled in CDVR's way. Scheduled recordings in Ericsson system will be deleted after migration.
4.CS needs to make a difference between migrated recordings and native recordings.
5.BG needs to delete all migrated recordings after a set deadline.
6.Playback of a completed recording on Ericsson system will be handled by KUMO, which means playback requests to FM only target recordings done in CDVR system.


A new tool should be created to migrate subscribers and recordings from Ericsson system to CDVR. The tool just runs to complete. It's not needed to deploy the tool to AWS cluster. CS, BG and SMS need to be modified to support the migrated and native recordings at the same time.

**Migrating All Subscribers from Ericsson to Cdvr:**

**Rules for Subscriber Migration:**


The following rules should be followed when converting a subscriber in Ericsson system to CDVR.

Subscriber in "disabled" state is ignored.
Subscriber in "enabled" state will be converted to an active subscriber in CDVR.
Each migrated subscriber will be assigned with default recording quota 100 shows.
Tuner and active session quota are not used in CDVR. Set a large number 9999 to tuner and active session quota to avoid limitation by these two quota.



**Rules for Recording Migration:**


The following lists the rules for migrating recordings from Ericsson to CDVR.

Only recordings in states "scheduled", "ongoing", "processing" and "completed" are migrated. Recordings in states "failed", "deleted" and "soft delete" will be ignored.
Recordings in states "scheduled", "ongoing" and "processing" which were done on a channel that doesn't exist in CDVR will be rejected during migration.
Recordings in state "completed" which were done on a channel that doesn't exist in CDVR will be migrated with null channel and empty channel ID.
Recordings in states "scheduled", "ongoing" and "processing" with valid channel will be migrated as new recordings in CDVR.
Recordings in state "completed" will be migrated as "COMPLETED" recordings in CDVR which only allow playback and deletion.
Recordings migrated with state "completed" will be deleted from CDVR after a set deadline.

**Migration Steps:**

The migration tool should follow the following ordered steps to do proper migration.

Retrieve active subscriber data from Ericsson.
Retrieve AD tags for subscribers from Ericsson.
Combine subscriber data and AD tags, and create mapping subscriber in SMS.
Retrieve series recordings from Ericsson.
Convert and re-schedule the series recording in CS.
Retrieve scheduled event recordings from Ericsson.
Convert and re-schedule the event recordings in CS.
Retrieve completed recordings from Ericsson.
Convert and inject completed recordings to CS. Recordings belongs to an existing series recording will be attached to the series recording. Set the retention expire time at the deadline set for Ericsson recordings.
(Optional) Cancel series recordings and scheduled event recordings in Ericsson system.

## How to build? ##

**mvn package**

It builds an uber JAR file with name "migrationtool-<version>.jar" under target directory.

**docker build -t DOCKER_IMAGE_TAG .**

It builds a docker image.

## How to run it? ##

1. **_Run JAR file_**

 The tool is built as a uber jar file with name "migrationtool-<version>.jar". It reads a configuration file in YAML format with name "application.yml". This file is located in "/migrationtool/src/main/resources/". In the configuration file, you can define the following options.

 - ericssonSubscriberUrl: (String) URL for ericsson Subscriber Url REST API
 - ericssonSubscriberAdTagUrl: (String) URL for ericsson Subscriber AdTagUrl REST API
 - ericssonSubscriberSeriesUrl: (String)URL for ericsson Subscriber SeriesUrl REST API
 - ericssonSubscriberRecordingUrl: (String) URL for ericsson Subscriber RecordingUrl REST API 
 - ericssonDeleteRecordingUrl: (String) URL for ericsson Subscriber DeleteRecordingUrl REST API 
 - ericssonDeleteSeriesUrl: (String) URL for ericsson Subscriber DeleteSeriesUrl REST API
 - cdvrCsBaseUrl: (String) the endpoint base URL used to schedule a recording in CS
 - cdvrSmsBaseUrl: (String) the endpoint base URL used to create subcribers and servicepackage

 Sample "config.yaml" with default values:
 
> ericssonSubscriberUrl: 'http://69.76.117.195:5927/v2/subscribers/search'
> ericssonSubscriberAdTagUrl: 'http://69.76.122.22:5928/v1/home'
> ericssonSubscriberSeriesUrl: 'http://69.76.110.4:5927/v2/series_recordings'
> ericssonSubscriberRecordingUrl: 'http://peakview-fe-prod.timewarnercable.com:5927/v2/subscribers/recordings'
> ericssonDeleteRecordingUrl: 'http://69.76.110.4:5927/v2/recordings/delete'
> ericssonDeleteSeriesUrl: 'http://69.76.110.4:5927/v2/series_recordings'
> cdvrCsBaseUrl: 'http://localhost:25100/cs/v3'
> cdvrSmsBaseUrl: 'http://localhost:25130/sms/v3'

 Put "config.yaml" in the same directory as the JAR file. Then you can run it:

     java -jar -Dspring.profiles.active=prod migrationtool-<version>.jar

 
 The REST server will listen on port 8080. If you'd like to change the listening port, you can add option "--server.port" to do it. For example:

	java -jar -Dspring.profiles.active=prod migrationtool-<version>.jar --server.port=9091
	

The server will be changed to listen on port 9091.

2. **_Run Docker Image_**

 docker run -p <listen_port>:8080 -d DOCKER_IMAGE_TAG

for example:
 docker run -p 8080:8080 -d local/migrationtool

 Environment variables with the same option names in "application.yml" can be used to override the default values. Make sure the following configurations are configured according to your deployment environment.

 - ericssonSubscriberUrl:
 - ericssonSubscriberAdTagUrl:
 - ericssonSubscriberSeriesUrl:
 - ericssonSubscriberRecordingUrl:
 - ericssonDeleteRecordingUrl: 
 - ericssonDeleteSeriesUrl:
 - cdvrCsBaseUrl:
 - cdvrSmsBaseUrl:

 For example:

    docker run -d -p 8080:8080 -e "SPRING_PROFILES_ACTIVE=test" -e "ericssonSubscriberUrl=http://10.0.2.15:8089/v2/subscribers/search" -e "ericssonSubscriberAdTagUrl=http://10.0.2.15:8089/v1/home" -e "ericssonSubscriberSeriesUrl=http://10.0.2.15:8090/v2/series_recordings" -e "ericssonSubscriberRecordingUrl=http://10.0.2.15:8091/v2/subscribers/recordings" -e "cdvrCsBaseUrl=http://10.0.2.15:25100/cs/v3" -e "cdvrSmsBaseUrl=http://10.0.2.15:25130/sms/v3" --name migrationtool migrationtooltest

