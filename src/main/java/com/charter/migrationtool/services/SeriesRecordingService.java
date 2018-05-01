package com.charter.migrationtool.services;

import java.util.Map;

public interface SeriesRecordingService {

	public void migrateSeriesRecordings(String channelStr, String id, Map<String, String> summaryReportMap);
}
