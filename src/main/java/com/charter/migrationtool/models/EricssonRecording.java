package com.charter.migrationtool.models;

public class EricssonRecording {

    private String showingID;
    
    private String channel;
    
    private String channelCallLetter;

    private String showStartTime;

    private String showEndTime;

    private String startTime;   
    
    private String endTime; 

    private boolean isProtected;

    private String bookmark;
    
    private ProgramInfo programInfo;


    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getShowingID() {
        return showingID;
    }

    public void setShowingID(String showingID) {
        this.showingID = showingID;
    }

    public String getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(String showStartTime) {
        this.showStartTime = showStartTime;
    }
    
    public String getChannelCallLetter() {
        return channelCallLetter;
    }

    public void setChannelCallLetter(String channelCallLetter) {
        this.channelCallLetter = channelCallLetter;
    }

    public String getShowEndTime() {
        return showEndTime;
    }

    public void setShowEndTime(String showEndTime) {
        this.showEndTime = showEndTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }    

    public boolean isIsProtected() {
        return isProtected;
    }

    public void setIsProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }
    
    public ProgramInfo getProgramInfo() {
        return programInfo;
    }

    public void setProgramInfo(ProgramInfo programInfo) {
        this.programInfo = programInfo;
    }

    @Override
    public String toString() {
        return "EricssonRecording [ProgramInfo = " + programInfo + ", Channel = " + channel + ", ShowingID = " + showingID
                + ", ShowStartTime = " + showStartTime + ", ChannelCallLetter = " + channelCallLetter
                + ", ShowEndTime = " + showEndTime + ", EndTime = " + endTime + ", StartTime = " + startTime  
                + ", IsProtected = " + isProtected + ", Bookmark = " + bookmark + "]";
    }

}


