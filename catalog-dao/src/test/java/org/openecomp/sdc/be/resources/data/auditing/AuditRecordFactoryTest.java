package org.openecomp.sdc.be.resources.data.auditing;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

public class AuditRecordFactoryTest {

	private AuditRecordFactory createTestSubject() {
		return new AuditRecordFactory();
	}

	@Test
	public void testCreateAuditRecord() throws Exception {
		Map<AuditingFieldsKeysEnum, Object> auditingFields = new HashMap<>();
		AuditingGenericEvent result;

		// default test
		result = AuditRecordFactory.createAuditRecord(null);
		result = AuditRecordFactory.createAuditRecord(auditingFields);
		
		for (AuditingActionEnum iterable_element : AuditingActionEnum.values()) {
			auditingFields = new HashMap<>();
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, iterable_element.getName());
			result = AuditRecordFactory.createAuditRecord(auditingFields);
		}
	}
}