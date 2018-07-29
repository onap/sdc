package org.openecomp.sdc.ci.tests.execute.AmdocsComplexService;


public class OnboardItemObjectReqDetails {

    private String creationMethod;
    private String description;

    public OnboardItemObjectReqDetails() {
    }

    public OnboardItemObjectReqDetails(String creationMethod, String description) {
        this.creationMethod = creationMethod;
        this.description = description;
    }

    public String getCreationMethod() {
        return creationMethod;
    }

    public void setCreationMethod(String creationMethod) {
        this.creationMethod = creationMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "OnboardItemObjectReqDetails{" +
                "creationMethod='" + creationMethod + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
