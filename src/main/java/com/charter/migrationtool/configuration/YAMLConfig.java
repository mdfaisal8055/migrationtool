package com.charter.migrationtool.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class YAMLConfig {
	
	private String ericssonSubscriberUrl;

	private String ericssonSubscriberAdTagUrl;

	private String ericssonSubscriberSeriesUrl;

	private String ericssonSubscriberRecordingUrl;

	private String ericssonDeleteRecordingUrl;

	private String ericssonDeleteSeriesUrl;

	private String cdvrCsBaseUrl;

	private String cdvrSmsBaseUrl;
	
	private int numThreads;

	public String getEricssonSubscriberUrl() {
		return ericssonSubscriberUrl;
	}

	public void setEricssonSubscriberUrl(String ericssonSubscriberUrl) {
		this.ericssonSubscriberUrl = ericssonSubscriberUrl;
	}

	public String getEricssonSubscriberAdTagUrl() {
		return ericssonSubscriberAdTagUrl;
	}

	public void setEricssonSubscriberAdTagUrl(String ericssonSubscriberAdTagUrl) {
		this.ericssonSubscriberAdTagUrl = ericssonSubscriberAdTagUrl;
	}

	public String getEricssonSubscriberSeriesUrl() {
		return ericssonSubscriberSeriesUrl;
	}

	public void setEricssonSubscriberSeriesUrl(String ericssonSubscriberSeriesUrl) {
		this.ericssonSubscriberSeriesUrl = ericssonSubscriberSeriesUrl;
	}

	public String getEricssonSubscriberRecordingUrl() {
		return ericssonSubscriberRecordingUrl;
	}

	public void setEricssonSubscriberRecordingUrl(String ericssonSubscriberRecordingUrl) {
		this.ericssonSubscriberRecordingUrl = ericssonSubscriberRecordingUrl;
	}

	public String getEricssonDeleteRecordingUrl() {
		return ericssonDeleteRecordingUrl;
	}

	public void setEricssonDeleteRecordingUrl(String ericssonDeleteRecordingUrl) {
		this.ericssonDeleteRecordingUrl = ericssonDeleteRecordingUrl;
	}

	public String getEricssonDeleteSeriesUrl() {
		return ericssonDeleteSeriesUrl;
	}

	public void setEricssonDeleteSeriesUrl(String ericssonDeleteSeriesUrl) {
		this.ericssonDeleteSeriesUrl = ericssonDeleteSeriesUrl;
	}

	public String getCdvrCsBaseUrl() {
		return cdvrCsBaseUrl;
	}

	public void setCdvrCsBaseUrl(String cdvrCsBaseUrl) {
		this.cdvrCsBaseUrl = cdvrCsBaseUrl;
	}

	public String getCdvrSmsBaseUrl() {
		return cdvrSmsBaseUrl;
	}

	public void setCdvrSmsBaseUrl(String cdvrSmsBaseUrl) {
		this.cdvrSmsBaseUrl = cdvrSmsBaseUrl;
	}

	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}
	

}
