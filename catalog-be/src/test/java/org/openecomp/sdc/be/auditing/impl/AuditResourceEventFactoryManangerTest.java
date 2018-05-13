package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceAuditData;

public class AuditResourceEventFactoryManangerTest {

	private AuditResourceEventFactoryMananger createTestSubject() {
		return new AuditResourceEventFactoryMananger();
	}

	@Test
	public void testCreateResourceEventFactory() throws Exception {
		AuditingActionEnum action = null;
		CommonAuditData commonFields = null;
		ResourceAuditData prevParams = null;
		ResourceAuditData currParams = null;
		String resourceType = "";
		String resourceName = "";
		String invariantUuid = "";
		User modifier = null;
		String artifactData = "";
		String comment = "";
		String did = "";
		String toscaNodeType = "";
		AuditBaseEventFactory result;
		
		for (AuditingActionEnum iterable_element : AuditingActionEnum.values()) {
			try {
				result = AuditResourceEventFactoryMananger.createResourceEventFactory(iterable_element, commonFields, prevParams,
						currParams, resourceType, resourceName, invariantUuid, modifier, artifactData, comment, did,
						toscaNodeType);
			} catch (Exception UnsupportedOperationException) {
				continue;
			}
		}
		// default test
	}
}