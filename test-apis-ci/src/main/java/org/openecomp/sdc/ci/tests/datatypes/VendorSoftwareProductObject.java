package org.openecomp.sdc.ci.tests.datatypes;

public class VendorSoftwareProductObject {

	private String vendorName;
	private String vspId;
	private String category;
	private String subCategory;
	private String componentId;
	private String description;
	private String attContact;
	private String vspName;
	
	public VendorSoftwareProductObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public VendorSoftwareProductObject(String vendorName, String vspId, String category, String subCategory, String componentId, String description, String attContact) {
		super();
		this.vendorName = vendorName;
		this.vspId = vspId;
		this.category = category;
		this.subCategory = subCategory;
		this.componentId = componentId;
		this.description = description;
		this.attContact = attContact;
	}

	
	public String getVspName() {
		return vspName;
	}

	public void setVspName(String vspName) {
		this.vspName = vspName;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public String getVspId() {
		return vspId;
	}

	public void setVspId(String vspId) {
		this.vspId = vspId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAttContact() {
		return attContact;
	}

	public void setAttContact(String attContact) {
		this.attContact = attContact;
	}

	@Override
	public String toString() {
		return "VendorSoftwareProductObject [vendorName=" + vendorName + ", vspId=" + vspId + ", category=" + category + ", subCategory=" + subCategory + ", componentId=" + componentId + ", description=" + description + ", attContact="
				+ attContact + "]";
	}
	
	
}
