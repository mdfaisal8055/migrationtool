package com.charter.migrationtool.models;

public class CdvrScheduleSeriesRecording {
	private String channelId;

	private String seriesId;

	private Options options;

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getSeriesId() {
		return seriesId;
	}

	public void setSeriesId(String seriesId) {
		this.seriesId = seriesId;
	}

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return "CdvrScheduleSeriesRecording [channelId = " + channelId + ", seriesId = " + seriesId + ", options = "
				+ options + "]";
	}

}
