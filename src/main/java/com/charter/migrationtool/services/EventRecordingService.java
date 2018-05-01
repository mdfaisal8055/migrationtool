package com.charter.migrationtool.services;

import java.util.Map;

public interface EventRecordingService {
	
	public void migrateEventRecordings(String channelStr, String id, Map<String, String> summaryReportMap);

}
