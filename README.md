# migrationtool
Ericsson nDVR system will phase out by end of the year. The existing subscribers and their recordings in Ericsson system should be migrated into the new CDVR cloud scheduler system. All the future recordings done by the subscribers imported from Ericsson system after migration will be handled and managed by CDVR system. A deadline will be set, for example, the end of year 2018. After the deadline, recordings done on Ericsson system will not be available in the CDVR system.  Ericsson system used recording time in "MINUTES" to count against quota. CDVR uses the number of recordings as the unit of quota. The quota number in Ericsson system is ignored. The used quota for migrated recordings will be counted directly on the number of migrated recordings.    The following lists the requirements in items easy to read.  1.Subscribers in Ericsson system are migrated into SMS (Subscriber Management Service) in CDVR. 2.Recordings in Ericsson system are migrated into CS (Cloud Scheduler) in CDVR. Only recording records are imported into CS. The recording data are still kept by Ericsson system. 3.Scheduled event and series recordings which happen in future will be re-scheduled in CDVR's way. Scheduled recordings in Ericsson system will be deleted after migration. 4.CS needs to make a difference between migrated recordings and native recordings. 5.BG needs to delete all migrated recordings after a set deadline. 6.Playback of a completed recording on Ericsson system will be handled by KUMO, which means playback requests to FM only target recordings done in CDVR system.   A new tool should be created to migrate subscribers and recordings from Ericsson system to CDVR. The tool just runs to complete. It's not needed to deploy the tool to AWS cluster. CS, BG and SMS need to be modified to support the migrated and native recordings at the same time.