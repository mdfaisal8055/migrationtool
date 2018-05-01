package com.charter.migrationtool.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ServicePackage {

	public static final String DEFAULT_ID = "default_ericsson_svcpackage";

	public static final String DEFAULT_NAME = "Default Service Package for Migrated Ericsson Subscribers";

	public static final String DEFAULT_QUOTA_TYPE = "SHOWS";

	public static final Integer DEFAULT_ASSIGNED_QUOTA = 100;

	public static final String DEFAULT_QUOTA_CONFLICT_RESOLUTION_POLICY = "OLDEST_CONTENT_FIRST_EXPIRE_POLICY";

	public static final Integer DEFAULT_ASSIGNED_TUNERS = 9999;

	public static final List<String> DEFAULT_FEATURES = Collections.unmodifiableList(Arrays.asList("NPVR"));

	private static final Integer DEFAULT_ACTIVE_SESSION_QUOTA = 9999;

	private String id = DEFAULT_ID;

	private String name = DEFAULT_NAME;

	private String quotaType = DEFAULT_QUOTA_TYPE;

	private Integer assignedQuota = DEFAULT_ASSIGNED_QUOTA;

	private String quotaConflictResolutionPolicy = DEFAULT_QUOTA_CONFLICT_RESOLUTION_POLICY;

	private Integer assignedTuners = DEFAULT_ASSIGNED_TUNERS;

	private List<String> features = DEFAULT_FEATURES;

	private Integer activeSessionsQuota = DEFAULT_ACTIVE_SESSION_QUOTA;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the quotaType
	 */
	public String getQuotaType() {
		return quotaType;
	}

	/**
	 * @param quotaType
	 *            the quotaType to set
	 */
	public void setQuotaType(String quotaType) {
		this.quotaType = quotaType;
	}

	/**
	 * @return the assignedQuota
	 */
	public Integer getAssignedQuota() {
		return assignedQuota;
	}

	/**
	 * @param assignedQuota
	 *            the assignedQuota to set
	 */
	public void setAssignedQuota(Integer assignedQuota) {
		this.assignedQuota = assignedQuota;
	}

	/**
	 * @return the quotaConflictResolutionPolicy
	 */
	public String getQuotaConflictResolutionPolicy() {
		return quotaConflictResolutionPolicy;
	}

	/**
	 * @param quotaConflictResolutionPolicy
	 *            the quotaConflictResolutionPolicy to set
	 */
	public void setQuotaConflictResolutionPolicy(String quotaConflictResolutionPolicy) {
		this.quotaConflictResolutionPolicy = quotaConflictResolutionPolicy;
	}

	/**
	 * @return the assignedTuners
	 */
	public Integer getAssignedTuners() {
		return assignedTuners;
	}

	/**
	 * @param assignedTuners
	 *            the assignedTuners to set
	 */
	public void setAssignedTuners(Integer assignedTuners) {
		this.assignedTuners = assignedTuners;
	}

	/**
	 * @return the features
	 */
	public List<String> getFeatures() {
		return features;
	}

	/**
	 * @param features
	 *            the features to set
	 */
	public void setFeatures(List<String> features) {
		this.features = features;
	}

	/**
	 * @return the activeSessionsQuota
	 */
	public Integer getActiveSessionsQuota() {
		return activeSessionsQuota;
	}

	/**
	 * @param activeSessionsQuota
	 *            the activeSessionsQuota to set
	 */
	public void setActiveSessionsQuota(Integer activeSessionsQuota) {
		this.activeSessionsQuota = activeSessionsQuota;
	}

	@Override
	public String toString() {
		return "ServicePackage{" + "name = " + getName() + " ,id = " + getId() + " , quotaType = " + getQuotaType()
				+ ", assignedQuota = " + getAssignedQuota() + ", quotaConflictResolutionPolicy = "
				+ getQuotaConflictResolutionPolicy() + ", assignedTuners = " + getAssignedTuners() + ", features = "
				+ getFeatures() + ", activeSessionsQuota = " + getActiveSessionsQuota() + "}";

	}

}
