package org.openecomp.sdc.be.model.jsontitan.enums;

public enum JsonConstantKeysEnum {
	
	COMPOSITION("composition"),
	CAPABILITIES("capabilities"),
	REQUIREMENTS("requirements"),
	PROPERTIES("properties"),
	INPUTS("inputs"),
	GROUPS("groups"),
	INSTANCE_PROPERIES("instanceProperties");
	
	private String value;
	
	private JsonConstantKeysEnum(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
