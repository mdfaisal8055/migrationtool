package com.charter.migrationtool.models;

public class ProgramInfo {

    private String seriesID;

    private String description;

    private String episodeTitle;

    private String type;

    private String programID;

    private String seasonName;

    private String seasonID;

    private String name;

    private Integer year;

    private String rating;

    private String seriesName;

    private String genre;

    private String subTitle;

    private Integer episodeNumber;

    public String getSeriesID() {
		return seriesID;
	}

	public void setSeriesID(String seriesID) {
		this.seriesID = seriesID;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEpisodeTitle() {
		return episodeTitle;
	}

	public void setEpisodeTitle(String episodeTitle) {
		this.episodeTitle = episodeTitle;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProgramID() {
		return programID;
	}

	public void setProgramID(String programID) {
		this.programID = programID;
	}

	public String getSeasonName() {
		return seasonName;
	}

	public void setSeasonName(String seasonName) {
		this.seasonName = seasonName;
	}

	public String getSeasonID() {
		return seasonID;
	}

	public void setSeasonID(String seasonID) {
		this.seasonID = seasonID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getSeriesName() {
		return seriesName;
	}

	public void setSeriesName(String seriesName) {
		this.seriesName = seriesName;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getSubTitle() {
		return subTitle;
	}

	public void setSubTitle(String subTitle) {
		this.subTitle = subTitle;
	}

	public Integer getEpisodeNumber() {
		return episodeNumber;
	}

	public void setEpisodeNumber(Integer episodeNumber) {
		this.episodeNumber = episodeNumber;
	}

	@Override
    public String toString() {
        return "ProgramInfo [SeriesID = " + seriesID + ", Description = " + description + ", EpisodeTitle = "
                + episodeTitle + ", Type = " + type + ", ProgramID = " + programID + ", SeasonName = " + seasonName
                + ", SeasonID = " + seasonID + ", Name = " + name + ", Year = " + year + ", Rating = " + rating
                + ", SeriesName = " + seriesName + ", Genre = " + genre + ", SubTitle = " + subTitle
                + ", EpisodeNumber = " + episodeNumber + "]";

    }

}