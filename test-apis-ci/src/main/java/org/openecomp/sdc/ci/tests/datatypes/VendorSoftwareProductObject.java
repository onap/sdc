package org.openecomp.sdc.ci.tests.datatypes;

public class VendorSoftwareProductObject extends VendorSoftwareProductObjectReqDetails {

	private String vspId;
	private String componentId;
	private String attContact;
	private String version;

	public VendorSoftwareProductObject(){super();}

	public VendorSoftwareProductObject(String vspId, String componentId, String attContact, String version) {
		this.vspId = vspId;
		this.componentId = componentId;
		this.attContact = attContact;
		this.version = version;
	}

	public VendorSoftwareProductObject(String name, String description, String category, String subCategory, String vendorId, String vendorName, LicensingVersion licensingVersion, LicensingData licensingData, String onboardingMethod, String networkPackageName, String onboardingOrigin, String vspId, String componentId, String attContact, String version) {
		super(name, description, category, subCategory, vendorId, vendorName, licensingVersion, licensingData, onboardingMethod, networkPackageName, onboardingOrigin);
		this.vspId = vspId;
		this.componentId = componentId;
		this.attContact = attContact;
		this.version = version;
	}

	public String getVspId() {
		return vspId;
	}

	public void setVspId(String vspId) {
		this.vspId = vspId;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getAttContact() {
		return attContact;
	}

	public void setAttContact(String attContact) {
		this.attContact = attContact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		return "VendorSoftwareProductObject{" +
				"vspId='" + vspId + '\'' +
				", componentId='" + componentId + '\'' +
				", attContact='" + attContact + '\'' +
				", version='" + version + '\'' +
				'}';
	}
}
