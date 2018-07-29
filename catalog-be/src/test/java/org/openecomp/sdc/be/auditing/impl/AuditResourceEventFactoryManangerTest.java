package org.openecomp.sdc.be.auditing.impl;

import org.junit.Test;
import org.openecomp.sdc.be.auditing.api.AuditEventFactory;
import org.openecomp.sdc.be.auditing.impl.resourceadmin.AuditResourceEventFactoryManager;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.be.resources.data.auditing.model.CommonAuditData;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceCommonInfo;
import org.openecomp.sdc.be.resources.data.auditing.model.ResourceVersionInfo;


public class AuditResourceEventFactoryManangerTest {

	private AuditResourceEventFactoryManager createTestSubject() {
		return new AuditResourceEventFactoryManager();
	}

	@Test
	public void testCreateResourceEventFactory() throws Exception {
		AuditingActionEnum action = null;
		CommonAuditData commonFields = null;
		ResourceVersionInfo prevParams = null;
		ResourceVersionInfo currParams = null;
		String resourceType = "";
		User modifier = null;
		String artifactData = "";
		String comment = "";
		String did = "";
		String toscaNodeType = "";
		AuditEventFactory result;
		
		for (AuditingActionEnum iterable_element : AuditingActionEnum.values()) {
			try {
				result = AuditResourceEventFactoryManager.createResourceEventFactory(iterable_element, commonFields,new ResourceCommonInfo(), prevParams,
						currParams, resourceType,modifier, artifactData, comment,did,
						toscaNodeType);
			} catch (Exception UnsupportedOperationException) {
				continue;
			}
		}
		// default test
	}
}