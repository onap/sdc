package org.openecomp.sdc.ci.tests.datatypes;

public class AmdocsLicenseMembers {

	private String vendorId;
	private String vendorLicenseName;
	private String vendorLicenseAgreementId;
	private String featureGroupId;

	public AmdocsLicenseMembers(String vendorId, String vendorLicenseName, String vendorLicenseAgreementId, String featureGroupId) {
		super();
		this.vendorId = vendorId;
		this.vendorLicenseName = vendorLicenseName;
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
		this.featureGroupId = featureGroupId;
	}

	public String getVendorId() {
		return vendorId;
	}

	public void setVendorId(String vendorId) {
		this.vendorId = vendorId;
	}

	public String getVendorLicenseName() {
		return vendorLicenseName;
	}

	public void setVendorLicenseName(String vendorLicenseName) {
		this.vendorLicenseName = vendorLicenseName;
	}

	public String getVendorLicenseAgreementId() {
		return vendorLicenseAgreementId;
	}

	public void setVendorLicenseAgreementId(String vendorLicenseAgreementId) {
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
	}

	public String getFeatureGroupId() {
		return featureGroupId;
	}

	public void setFeatureGroupId(String featureGroupId) {
		this.featureGroupId = featureGroupId;
	}

	@Override
	public String toString() {
		return "AmdocsLicenseMembers [vendorId=" + vendorId + ", vendorLicenseName=" + vendorLicenseName + ", vendorLicenseAgreementId=" + vendorLicenseAgreementId + ", featureGroupId=" + featureGroupId + "]";
	}
	
	
}
