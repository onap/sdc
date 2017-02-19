package org.openecomp.sdc.uici.tests.datatypes;

public enum MenuOptionsEnum {

	EDIT("Edit"), CHECK_IN("Check in"), CHECK_OUT("Check out"), VIEW("View"), SUBMIT_FOR_TEST("Submit For Test"), ACCEPT("Accept"), REJECT("Reject"), START_TEST("Start test"), DISTREBUTE("Distribute");

	private String value;

	public String getValue() {
		return value;
	}

	private MenuOptionsEnum(String value) {
		this.value = value;
	}

}
