package com.charter.migrationtool.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a logical Subscriber resource.
 *
 * @author Mohammed Ahmed
 */
public class CdvrSubscriber {

	private String id;

	private String name;
	
	private String status;

	private ServicePackage servicePackage;

	private List<String> ncsServiceIds= new ArrayList<String>();

	private List<String> geoTags= new ArrayList<String>();

	public List<String> getGeoTags() {
		return geoTags;
	}

	public void setGeoTags(List<String> geoTags) {
		this.geoTags = geoTags;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the servicePackage
	 */
	public ServicePackage getServicePackage() {
		return servicePackage;
	}

	/**
	 * @param servicePackage
	 *            the servicePackage to set
	 */
	public void setServicePackage(ServicePackage servicePackage) {
		this.servicePackage = servicePackage;
	}

	public List<String> getNcsServiceIds() {
		return ncsServiceIds;
	}

	public void setNcsServiceIds(List<String> ncsServiceIds) {
		this.ncsServiceIds = ncsServiceIds;
	}
	

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Subscriber{" + "name = " + getName() + " ,id = " + getId() + ", servicePackage = " + getServicePackage() + ", ncsServiceIds = " + getNcsServiceIds()
				+ ", geoTags = " + getGeoTags() + "}";

	}

}
