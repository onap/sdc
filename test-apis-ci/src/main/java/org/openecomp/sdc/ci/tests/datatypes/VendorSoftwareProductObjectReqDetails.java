package org.openecomp.sdc.ci.tests.datatypes;

public class VendorSoftwareProductObjectReqDetails {

    private String name;
    private String description;
    private String category;
    private String subCategory;
    private String vendorId;
    private String vendorName;
    private LicensingVersion licensingVersion;
    private LicensingData licensingData;
    private String onboardingMethod;
    private String networkPackageName;
    private String onboardingOrigin;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    private String icon;

    public VendorSoftwareProductObjectReqDetails() {
    }

    public VendorSoftwareProductObjectReqDetails(String name, String description, String category, String subCategory, String vendorId, String vendorName, LicensingVersion licensingVersion, LicensingData licensingData, String onboardingMethod, String networkPackageName, String onboardingOrigin) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.subCategory = subCategory;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.licensingVersion = licensingVersion;
        this.licensingData = licensingData;
        this.onboardingMethod = onboardingMethod;
        this.networkPackageName = networkPackageName;
        this.onboardingOrigin = onboardingOrigin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public LicensingVersion getLicensingVersion() {
        return licensingVersion;
    }

    public void setLicensingVersion(LicensingVersion licensingVersion) {
        this.licensingVersion = licensingVersion;
    }

    public LicensingData getLicensingData() {
        return licensingData;
    }

    public void setLicensingData(LicensingData licensingData) {
        this.licensingData = licensingData;
    }

    public String getOnboardingMethod() {
        return onboardingMethod;
    }

    public void setOnboardingMethod(String onboardingMethod) {
        this.onboardingMethod = onboardingMethod;
    }

    public String getNetworkPackageName() {
        return networkPackageName;
    }

    public void setNetworkPackageName(String networkPackageName) {
        this.networkPackageName = networkPackageName;
    }

    public String getOnboardingOrigin() {
        return onboardingOrigin;
    }

    public void setOnboardingOrigin(String onboardingOrigin) {
        this.onboardingOrigin = onboardingOrigin;
    }

    @Override
    public String toString() {
        return "VendorSoftwareProductObjectReqDetails{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", category='" + category + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", vendorId='" + vendorId + '\'' +
                ", vendorName='" + vendorName + '\'' +
                ", licensingVersion=" + licensingVersion +
                ", licensingData=" + licensingData +
                ", onboardingMethod='" + onboardingMethod + '\'' +
                ", networkPackageName='" + networkPackageName + '\'' +
                ", onboardingOrigin='" + onboardingOrigin + '\'' +
                '}';
    }
}
