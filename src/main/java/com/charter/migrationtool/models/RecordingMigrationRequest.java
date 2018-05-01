package com.charter.migrationtool.models;

import java.util.ArrayList;
import java.util.List;

public class RecordingMigrationRequest {

	private List<EricssonRecording> ericssonRecording = new ArrayList<EricssonRecording>();

	

	public List<EricssonRecording> getEricssonRecording() {
		return ericssonRecording;
	}



	public void setEricssonRecording(List<EricssonRecording> ericssonRecording) {
		this.ericssonRecording = ericssonRecording;
	}



	@Override
	public String toString() {
		return "RecordingMigrationRequest [EricssonRecording = " + ericssonRecording + "]";
	}

}
