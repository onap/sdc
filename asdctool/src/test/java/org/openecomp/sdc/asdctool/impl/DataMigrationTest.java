package org.openecomp.sdc.asdctool.impl;

import java.text.SimpleDateFormat;
import java.util.EnumMap;
import java.util.TimeZone;

import org.elasticsearch.common.settings.SettingsException;
import org.junit.Test;
import org.openecomp.sdc.asdctool.impl.DataMigration.TypeToTableMapping;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

public class DataMigrationTest {

	private DataMigration createTestSubject() {
		return new DataMigration();
	}

	/*@Test(expected=IllegalArgumentException.class)
	public void testMigrateDataEsToCassandra() throws Exception {
		DataMigration testSubject;
		String appConfigDir = "src/main/resources/config/";
		boolean exportFromEs = false;
		boolean importToCassandra = false;

		// default test
		testSubject = createTestSubject();
		testSubject.migrateDataEsToCassandra(appConfigDir, exportFromEs, importToCassandra);
	}*/

	@Test(expected=NullPointerException.class)
	public void testCreateAuditRecord() throws Exception {
		DataMigration testSubject;
		
		EnumMap<AuditingFieldsKeysEnum, Object> auditingFields = new EnumMap<AuditingFieldsKeysEnum, Object>(AuditingFieldsKeysEnum.class);
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, AuditingActionEnum.GET_CATEGORY_HIERARCHY.getName());

		String DATE_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS z";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_TIMESTAMP, "2018-05-02 06:06:18.294 UTC");
		
		// default test
		testSubject = createTestSubject();
		testSubject.createAuditRecord(auditingFields);
	}
	
	@Test
	public void testTypeToTableMapping() throws Exception {
		TypeToTableMapping[] values = TypeToTableMapping.values();
		
		for (TypeToTableMapping typeToTableMapping : values) {
			TypeToTableMapping.getTableByType(typeToTableMapping.getTypeName());
			typeToTableMapping.getTable();
			
		}
		
		TypeToTableMapping.getTableByType("stam");
	}
}