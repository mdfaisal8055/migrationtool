package com.charter.migrationtool.models;

public class Options {

	private String spaceConflictPolicy;

	private Integer endOffset;

	private String accept;

	private Integer startOffset;

	public String getSpaceConflictPolicy() {
		return spaceConflictPolicy;
	}

	public void setSpaceConflictPolicy(String spaceConflictPolicy) {
		this.spaceConflictPolicy = spaceConflictPolicy;
	}

	public String getAccept() {
		return accept;
	}

	public void setAccept(String accept) {
		this.accept = accept;
	}

	public Integer getEndOffset() {
		return endOffset;
	}

	public void setEndOffset(Integer endOffset) {
		this.endOffset = endOffset;
	}

	public Integer getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(Integer startOffset) {
		this.startOffset = startOffset;
	}

	@Override
	public String toString() {
		return "Options [spaceConflictPolicy = " + spaceConflictPolicy + ", endOffset = " + endOffset + ", accept = "
				+ accept + ", startOffset = " + startOffset + "]";
	}

}
