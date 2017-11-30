package org.openecomp.sdc.ci.tests.datatypes;

public class LicensingVersion {

    private String id;
    private String label;

    public LicensingVersion(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public LicensingVersion() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "LicensingVersion{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                '}';
    }
}
