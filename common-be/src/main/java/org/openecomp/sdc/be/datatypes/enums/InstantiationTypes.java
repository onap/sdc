package org.openecomp.sdc.be.datatypes.enums;

import java.util.stream.Stream;

public enum InstantiationTypes {
	
	A_LA_CARTE("A-la-carte"),
	MACRO("Macro");
	
	private String value;
	
	private InstantiationTypes(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	/**
	 * Checks if enum with the given type exists.
	 *
	 * @param type
	 * @return bool
	 */
	
	public static boolean containsName(String type) {
		return Stream.of(InstantiationTypes.values())
				.anyMatch(instType -> type.equals(instType.getValue()));
	}	
}
