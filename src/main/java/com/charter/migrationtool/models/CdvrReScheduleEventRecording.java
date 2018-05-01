package com.charter.migrationtool.models;

public class CdvrReScheduleEventRecording {
	private String airingId;
	
	private Options options;

    public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public String getAiringId ()
    {
        return airingId;
    }

    public void setAiringId (String airingId)
    {
        this.airingId = airingId;
    }

    @Override
    public String toString()
    {
        return "CdvrReScheduleEventRecording [airingId = "+airingId+"]";
    }

}
