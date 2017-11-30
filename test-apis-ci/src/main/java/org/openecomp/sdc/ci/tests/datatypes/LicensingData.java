package org.openecomp.sdc.ci.tests.datatypes;
import java.util.List;

public class LicensingData {
    private String licenseAgreement;
    private List<String> featureGroups = null;

    public LicensingData() {
    }

    public LicensingData(String licenseAgreement, List<String> featureGroups) {
        this.licenseAgreement = licenseAgreement;
        this.featureGroups = featureGroups;
    }

    public String getLicenseAgreement() {
        return licenseAgreement;
    }

    public void setLicenseAgreement(String licenseAgreement) {
        this.licenseAgreement = licenseAgreement;
    }

    public List<String> getFeatureGroups() {
        return featureGroups;
    }

    public void setFeatureGroups(List<String> featureGroups) {
        this.featureGroups = featureGroups;
    }

    @Override
    public String toString() {
        return "LicensingData{" +
                "licenseAgreement='" + licenseAgreement + '\'' +
                ", featureGroups=" + featureGroups +
                '}';
    }
}
