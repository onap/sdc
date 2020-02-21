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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_CLASS_NAME;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_END_TIMESTAMP;
import static org.openecomp.sdc.common.log.api.ILogConfiguration.MDC_OPT_FIELD1;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.onap.logging.ref.slf4j.ONAPLogConstants;
import org.slf4j.MDC;

public class LogFieldsMdcHandlerTest {

	private LogFieldsMdcHandler ecompMdcWrapper;

	@Before
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
		ecompMdcWrapper.clear();
		assertNull(MDC.get(MDC_CLASS_NAME));
		assertNull(MDC.get(ONAPLogConstants.MDCs.PARTNER_NAME));
		assertNull(MDC.get(MDC_OPT_FIELD1));
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

}

