package org.openecomp.sdc.be.auditing.impl;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;

import mockit.Deencapsulation;

public class AuditingLogFormatUtilTest {

	private AuditingLogFormatUtil createTestSubject() {
		return new AuditingLogFormatUtil();
	}

	@Test
	public void testGetModifier() throws Exception {
		String modifierName = "mock";
		String modifierUid = "mock";
		String result;

		// test 0
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getModifier",
				new Object[] { modifierName, modifierUid });
		Assert.assertEquals("mock(mock)", result);
		
		// test 1
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getModifier",
				new Object[] { modifierName, String.class });
		Assert.assertEquals("", result);

		// test 2
		modifierUid = "";
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getModifier",
				new Object[] { modifierName, modifierUid });
		Assert.assertEquals("", result);

		// test 3
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getModifier",
				new Object[] { String.class, modifierUid });
		Assert.assertEquals("", result);

		// test 4
		modifierName = "";
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getModifier",
				new Object[] { modifierName, modifierUid });
		Assert.assertEquals("", result);
	}

	@Test
	public void testGetUser() throws Exception {
		String userData = "";
		String result;

		// default test
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "getUser", new Object[] { userData });
	}

	@Test
	public void testLogAuditEvent() throws Exception {
		Map<AuditingFieldsKeysEnum, Object> auditingFields = null;
		String result;

		// default test
		HashMap<AuditingFieldsKeysEnum, Object> hashMap = new HashMap<AuditingFieldsKeysEnum, Object>();
		result = Deencapsulation.invoke(AuditingLogFormatUtil.class, "logAuditEvent",
				new Object[] { hashMap.getClass() });
	}

	@Test
	public void testLogAuditEvent_1() throws Exception {
		String formattedString = "";
		Map<AuditingFieldsKeysEnum, Object> auditingFields = new HashMap<>();

		// default test
		Deencapsulation.invoke(AuditingLogFormatUtil.class, "logAuditEvent", new Object[] { formattedString });
		
		auditingFields.put(AuditingFieldsKeysEnum.AUDIT_MODIFIER_NAME, "mock");
		Deencapsulation.invoke(AuditingLogFormatUtil.class, "logAuditEvent", auditingFields);
		
		for ( AuditingFieldsKeysEnum enumValue : AuditingFieldsKeysEnum.values()) {
			auditingFields.put(enumValue, "mock");
		}
		
		Deencapsulation.invoke(AuditingLogFormatUtil.class, "logAuditEvent", auditingFields);
		
		auditingFields = new HashMap<>();
		for (AuditingActionEnum iterable_element : AuditingActionEnum.values()) {
			auditingFields.put(AuditingFieldsKeysEnum.AUDIT_ACTION, iterable_element.getName());
			Deencapsulation.invoke(AuditingLogFormatUtil.class, "logAuditEvent", auditingFields);
		}
	}
}