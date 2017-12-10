package org.openecomp.sdc.ci.tests.datatypes;

public class ConnectionWizardPopUpObject {

    private String capabilityTypeFirstItem;
    private String capabilityTypeSecondItem;
    private String capabilityNameFirstItem;
    private String capabilityNameSecondItem;

    public ConnectionWizardPopUpObject(String capabilityTypeFirstItem, String capabilityNameFirstItem, String capabilityTypeSecondItem, String capabilityNameSecondItem) {
        this.capabilityTypeFirstItem = capabilityTypeFirstItem;
        this.capabilityTypeSecondItem = capabilityTypeSecondItem;
        this.capabilityNameFirstItem = capabilityNameFirstItem;
        this.capabilityNameSecondItem = capabilityNameSecondItem;
    }

    public String getCapabilityTypeFirstItem() {
        return capabilityTypeFirstItem;
    }

    public void setCapabilityTypeFirstItem(String capabilityTypeFirstItem) {
        this.capabilityTypeFirstItem = capabilityTypeFirstItem;
    }

    public String getCapabilityTypeSecondItem() {
        return capabilityTypeSecondItem;
    }

    public void setCapabilityTypeSecondItem(String capabilityTypeSecondItem) {
        this.capabilityTypeSecondItem = capabilityTypeSecondItem;
    }

    public String getCapabilityNameFirstItem() {
        return capabilityNameFirstItem;
    }

    public void setCapabilityNameFirstItem(String capabilityNameFirstItem) {
        this.capabilityNameFirstItem = capabilityNameFirstItem;
    }

    public String getCapabilityNameSecondItem() {
        return capabilityNameSecondItem;
    }

    public void setCapabilityNameSecondItem(String capabilityNameSecondItem) {
        this.capabilityNameSecondItem = capabilityNameSecondItem;
    }

    @Override
    public String toString() {
        return "ConnectionWizardPopUpObject{" +
                "capabilityTypeFirstItem='" + capabilityTypeFirstItem + '\'' +
                ", capabilityTypeSecondItem='" + capabilityTypeSecondItem + '\'' +
                ", capabilityNameFirstItem='" + capabilityNameFirstItem + '\'' +
                ", capabilityNameSecondItem='" + capabilityNameSecondItem + '\'' +
                '}';
    }
}
