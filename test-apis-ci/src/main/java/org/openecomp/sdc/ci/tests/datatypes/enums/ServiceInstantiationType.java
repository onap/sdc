package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum ServiceInstantiationType {
	A_LA_CARTE("A-la-carte"), MACRO("Macro");
	
	private String value;

	public String getValue() {
		return value;
	}

	private ServiceInstantiationType(String value) {
		this.value = value;
	}
}
