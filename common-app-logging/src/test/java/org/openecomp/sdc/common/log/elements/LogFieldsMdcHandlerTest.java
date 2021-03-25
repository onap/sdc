/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.common.log.elements;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.onap.logging.ref.slf4j.ONAPLogConstants.MDCs.PARTNER_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_CLASS_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_END_TIMESTAMP;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_OPT_FIELD1;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_OUTGOING_INVOCATION_ID;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_ACTION;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_UUID;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_COMPONENT_VERSION;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_CSAR_UUID;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_CSAR_VERSION;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SUPPORTABLITY_STATUS_CODE;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_SERVICE_INSTANCE_ID;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_TARGET_VIRTUAL_ENTITY;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

public class LogFieldsMdcHandlerTest {

	private LogFieldsMdcHandler ecompMdcWrapper;

	@BeforeEach
	public void init(){
		ecompMdcWrapper = new LogFieldsMdcHandler();
		ecompMdcWrapper.clear();
		MDC.clear();
	}

	@Test
	public void isMDCParamEmpty_shouldReturnTrue_onNonNullValueInMDC(){
		MDC.put("Key","value1");
		assertFalse(ecompMdcWrapper.isMDCParamEmpty("Key"));
	}
	@Test
	public void isMDCParamEmpty_shouldReturnFalse_onEmptyStringInMDC(){
		MDC.put("Key","");
		assertTrue(ecompMdcWrapper.isMDCParamEmpty("Key"));
	}

	@Test
	public void isMDCParamEmpty_shouldReturnFalse_onNullValueInMDC(){
		MDC.put("Key",null);
		assertTrue(ecompMdcWrapper.isMDCParamEmpty("Key"));
	}

	@Test
	public void startTimer_shouldFilecompMdcWrappereginTimestampField(){
		ecompMdcWrapper.startMetricTimer();
		assertFalse(ecompMdcWrapper.isMDCParamEmpty(ONAPLogConstants.MDCs.INVOKE_TIMESTAMP));
	}

	@Test
	public void stopTimer_shouldFillEndTimestampField_ifStartTimerWasCalledPreviously(){
		ecompMdcWrapper.startAuditTimer();
		ecompMdcWrapper.stopAuditTimer();
		assertFalse(ecompMdcWrapper.isMDCParamEmpty(MDC_END_TIMESTAMP));
	}

	@Test
	public void stopTimer_shouldTimestampsBeIsoFormat() {
		ecompMdcWrapper.startAuditTimer();
		ecompMdcWrapper.stopAuditTimer();
		// Expect no exceptions thrown
		ZonedDateTime.parse(MDC.get(ONAPLogConstants.MDCs.ENTRY_TIMESTAMP), DateTimeFormatter.ISO_ZONED_DATE_TIME);
		ZonedDateTime.parse(MDC.get(MDC_END_TIMESTAMP), DateTimeFormatter.ISO_ZONED_DATE_TIME);
	}

	@Test
	public void clear_shouldRemoveAllMandatoryAndOptionalFields_And_OnlyThem(){
		ecompMdcWrapper.setClassName("class1");
		ecompMdcWrapper.setPartnerName("partner1");
		ecompMdcWrapper.setOptCustomField1("of1");
		ecompMdcWrapper.setOutgoingInvocationId("invocationId");
		ecompMdcWrapper.clear();
		assertNull(MDC.get(MDC_CLASS_NAME));
		assertNull(MDC.get(PARTNER_NAME));
		assertNull(MDC.get(MDC_OPT_FIELD1));
		assertNull(MDC.get(MDC_OUTGOING_INVOCATION_ID));
	}

	@Test
	public void clear_shouldNotThrowAnException_WhenNoFieldWasAssignedAsMandatoryOrOptional(){
		ecompMdcWrapper.setClassName("class1");
		ecompMdcWrapper.setPartnerName("partner1");
		ecompMdcWrapper.setOptCustomField1("of1");
		Exception exp = null;
		try {
			ecompMdcWrapper.clear();
		}
		catch (Exception e)
		{
			exp =e;
		}
		assertNull(exp);
	}

	@Test
	public void testSetterGetterRemove(){
		ecompMdcWrapper.setErrorCode(200);
		ecompMdcWrapper.setErrorCategory("errorCategory");
		ecompMdcWrapper.setTargetEntity("targetEntity");
		ecompMdcWrapper.setTargetServiceName("targetServiceName");
		ecompMdcWrapper.setPartnerName("partnerName");
		ecompMdcWrapper.setServiceInstanceId("serviceInstanceId");
		ecompMdcWrapper.setServerIPAddress("serverIpAddress");
		ecompMdcWrapper.setAuditMessage("auditMsg");
		ecompMdcWrapper.setTargetVirtualEntity("targetVirtualEntity");
		ecompMdcWrapper.setSupportablityStatusCode("supportablityStatusCode");
		ecompMdcWrapper.setSupportablityAction("supportablityAction");
		ecompMdcWrapper.setRemoteHost("remoteHost");
		ecompMdcWrapper.setSupportablityCsarUUID("csarUUID");
		ecompMdcWrapper.setSupportablityCsarVersion("csarVersion");
		ecompMdcWrapper.setSupportablityComponentName("componentName");
		ecompMdcWrapper.setSupportablityComponentUUID("componentUUID");
		ecompMdcWrapper.setSupportablityComponentVersion("componentVersion");
		ecompMdcWrapper.setKeyInvocationId("keyInvocationId");

		assertEquals("200", ecompMdcWrapper.getErrorCode());
		assertEquals("errorCategory", ecompMdcWrapper.getErrorCategory());
		assertNotNull(ecompMdcWrapper.getFqdn());
		assertNotNull(ecompMdcWrapper.getHostAddress());
		assertEquals("targetEntity", ecompMdcWrapper.getTargetEntity());
		assertEquals("targetServiceName", ecompMdcWrapper.getTargetServiceName());
		assertEquals("partnerName", ecompMdcWrapper.getPartnerName());
		assertEquals("auditMsg", ecompMdcWrapper.getAuditMessage());
		assertEquals("supportablityStatusCode", ecompMdcWrapper.getSupportablityStatusCode());
		assertEquals("supportablityAction", ecompMdcWrapper.getSupportablityAction());
		assertEquals("remoteHost", ecompMdcWrapper.getRemoteHost());
		assertEquals("serverIpAddress", ecompMdcWrapper.getServerIpAddress());
		assertEquals("csarUUID", ecompMdcWrapper.getSupportablityCsarUUID());
		assertEquals("csarVersion", ecompMdcWrapper.getSupportablityCsarVersion());
		assertEquals("componentName", ecompMdcWrapper.getSupportablityComponentName());
		assertEquals("componentUUID", ecompMdcWrapper.getSupportablityComponentUUID());
		assertEquals("componentVersion", ecompMdcWrapper.getSupportablityComponentVersion());
		assertEquals("keyInvocationId", ecompMdcWrapper.getKeyInvocationId());

		ecompMdcWrapper.removePartnerName();
		ecompMdcWrapper.removeSupportablityAction();
		ecompMdcWrapper.removeSupportablityComponentName();
		ecompMdcWrapper.removeSupportablityComponentUUID();
		ecompMdcWrapper.removeSupportablityComponentVersion();
		ecompMdcWrapper.removeSupportablityCsarUUID();
		ecompMdcWrapper.removeSupportablityCsarVersion();
		ecompMdcWrapper.removeSupportablityStatusCode();
		ecompMdcWrapper.removeServiceInstanceId();
		ecompMdcWrapper.removeTargetVirtualEntity();

		assertNull(MDC.get(PARTNER_NAME));
		assertNull(MDC.get(MDC_SUPPORTABLITY_ACTION));
		assertNull(MDC.get(MDC_SUPPORTABLITY_COMPONENT_NAME));
		assertNull(MDC.get(MDC_SUPPORTABLITY_COMPONENT_UUID));
		assertNull(MDC.get(MDC_SUPPORTABLITY_COMPONENT_VERSION));
		assertNull(MDC.get(MDC_SUPPORTABLITY_CSAR_UUID));
		assertNull(MDC.get(MDC_SUPPORTABLITY_CSAR_VERSION));
		assertNull(MDC.get(MDC_SUPPORTABLITY_STATUS_CODE));
		assertNull(MDC.get(MDC_SERVICE_INSTANCE_ID));
		assertNull(MDC.get(MDC_TARGET_VIRTUAL_ENTITY));

	}
}

