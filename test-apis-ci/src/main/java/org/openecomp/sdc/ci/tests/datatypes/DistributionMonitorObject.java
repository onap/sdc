package org.openecomp.sdc.ci.tests.datatypes;

public class DistributionMonitorObject {
	
	private String omfComponentID;
	private String timestamp;
	private String url;
	private String status;
	private String errorReason;
	
	public DistributionMonitorObject() {
		super();
	}

	public DistributionMonitorObject(String omfComponentID, String timestamp, String url, String status, String errorReason) {
		super();
		this.omfComponentID = omfComponentID;
		this.timestamp = timestamp;
		this.url = url;
		this.status = status;
		this.errorReason = errorReason;
	}

	public String getOmfComponentID() {
		return omfComponentID;
	}

	public void setOmfComponentID(String omfComponentID) {
		this.omfComponentID = omfComponentID;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getErrorReason() {
		return errorReason;
	}

	public void setErrorReason(String errorReason) {
		this.errorReason = errorReason;
	}

	@Override
	public String toString() {
		return "DistributionMonitorObject [omfComponentID=" + omfComponentID + ", timestamp=" + timestamp + ", url=" + url + ", status=" + status + ", errorReason=" + errorReason + "]";
	}
	
}
