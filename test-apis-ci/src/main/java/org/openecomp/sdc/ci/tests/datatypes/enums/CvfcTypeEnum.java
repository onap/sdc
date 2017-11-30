package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum CvfcTypeEnum {

		SNMP_POLL ("SNMP_POLL"),
		SNMP_TRAP ("SNMP_TRAP"),
		VES_EVENTS ("VES_EVENTS");
		
		private String value;
	
		public String getValue() {
			return value;
		}
	
		private CvfcTypeEnum(String value) {
			this.value = value;
	}
		
}
