package org.openecomp.sdc.uici.tests.datatypes;

public enum CreateAndUpdateStepsEnum {
	GENERAL("Generalstep"), 
	ICON("Iconstep"), 
	DEPLOYMENT_ARTIFACT("Deployment Artifactstep"), 
	INFORMATION_ARTIFACT("Information Artifactstep"), 
	PROPERTIES("Propertiesstep"), 
	ATTRIBUTES("Attributesstep"), 
	COMPOSITION("Compositionstep"), 
	DEPLOYMENT("Deploymentstep"), 
	REQUIREMENTS_AND_CAPABILITIES("Req. & Capabilitiesstep"),
	INPUTS("Inputsstep");

	private String value;

	public String getValue() {
		return value;
	}

	private CreateAndUpdateStepsEnum(String value) {
		this.value = value;
	}

}
