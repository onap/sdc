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

package org.openecomp.sdc.ci.tests.datatypes.expected;

public class ExpectedExternalAudit {

	String ACTION;
	String CONSUMER_ID;
	String RESOURCE_URL;
	String STATUS;
	String DESC;
	String RESOURCE_NAME;
	String RESOURCE_TYPE;
	String SERVICE_INSTANCE_ID;// resource/ service UUID
	String MODIFIER;
	String PREV_ARTIFACT_UUID;
	String CURR_ARTIFACT_UUID;
	String ARTIFACT_DATA;

	public ExpectedExternalAudit() {
		super();
	}

	public String getRESOURCE_NAME() {
		return RESOURCE_NAME;
	}

	public void setRESOURCE_NAME(String rESOURCE_NAME) {
		RESOURCE_NAME = rESOURCE_NAME;
	}

	public String getRESOURCE_TYPE() {
		return RESOURCE_TYPE;
	}

	public void setRESOURCE_TYPE(String rESOURCE_TYPE) {
		RESOURCE_TYPE = rESOURCE_TYPE;
	}

	public String getSERVICE_INSTANCE_ID() {
		return SERVICE_INSTANCE_ID;
	}

	public void setSERVICE_INSTANCE_ID(String sERVICE_INSTANCE_ID) {
		SERVICE_INSTANCE_ID = sERVICE_INSTANCE_ID;
	}

	public ExpectedExternalAudit(String aCTION, String cONSUMER_ID, String rESOURCE_URL, String sTATUS, String dESC,
			String rESOURCE_NAME, String rESOURCE_TYPE, String sERVICE_INSTANCE_ID) {
		super();
		ACTION = aCTION;
		CONSUMER_ID = cONSUMER_ID;
		RESOURCE_URL = rESOURCE_URL;
		STATUS = sTATUS;
		DESC = dESC;
		RESOURCE_NAME = rESOURCE_NAME;
		RESOURCE_TYPE = rESOURCE_TYPE;
		SERVICE_INSTANCE_ID = sERVICE_INSTANCE_ID;
	}

	public ExpectedExternalAudit(String aCTION, String cONSUMER_ID, String rESOURCE_URL, String sTATUS, String dESC) {
		super();
		ACTION = aCTION;
		CONSUMER_ID = cONSUMER_ID;
		RESOURCE_URL = rESOURCE_URL;
		STATUS = sTATUS;
		DESC = dESC;
	}

	public ExpectedExternalAudit(String aCTION, String cONSUMER_ID, String rESOURCE_URL, String sTATUS, String dESC,
			String rESOURCE_NAME, String rESOURCE_TYPE, String sERVICE_INSTANCE_ID, String mODIFIER,
			String pREV_ARTIFACT_UUID, String cURR_ARTIFACT_UUID, String aRTIFACT_DATA) {
		super();
		ACTION = aCTION;
		CONSUMER_ID = cONSUMER_ID;
		RESOURCE_URL = rESOURCE_URL;
		STATUS = sTATUS;
		DESC = dESC;
		RESOURCE_NAME = rESOURCE_NAME;
		RESOURCE_TYPE = rESOURCE_TYPE;
		SERVICE_INSTANCE_ID = sERVICE_INSTANCE_ID;
		MODIFIER = mODIFIER;
		PREV_ARTIFACT_UUID = pREV_ARTIFACT_UUID;
		CURR_ARTIFACT_UUID = cURR_ARTIFACT_UUID;
		ARTIFACT_DATA = aRTIFACT_DATA;
	}

	public String getACTION() {
		return ACTION;
	}

	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}

	public String getCONSUMER_ID() {
		return CONSUMER_ID;
	}

	public void setCONSUMER_ID(String cONSUMER_ID) {
		CONSUMER_ID = cONSUMER_ID;
	}

	public String getRESOURCE_URL() {
		return RESOURCE_URL;
	}

	public void setRESOURCE_URL(String rESOURCE_URL) {
		RESOURCE_URL = rESOURCE_URL;
	}

	public String getSTATUS() {
		return STATUS;
	}

	public void setSTATUS(String sTATUS) {
		STATUS = sTATUS;
	}

	public String getDESC() {
		return DESC;
	}

	public void setDESC(String dESC) {
		DESC = dESC;
	}

	public String getMODIFIER() {
		return MODIFIER;
	}

	public void setMODIFIER(String mODIFIER) {
		MODIFIER = mODIFIER;
	}

	public String getPREV_ARTIFACT_UUID() {
		return PREV_ARTIFACT_UUID;
	}

	public void setPREV_ARTIFACT_UUID(String pREV_ARTIFACT_UUID) {
		PREV_ARTIFACT_UUID = pREV_ARTIFACT_UUID;
	}

	public String getCURR_ARTIFACT_UUID() {
		return CURR_ARTIFACT_UUID;
	}

	public void setCURR_ARTIFACT_UUID(String cURR_ARTIFACT_UUID) {
		CURR_ARTIFACT_UUID = cURR_ARTIFACT_UUID;
	}

	public String getARTIFACT_DATA() {
		return ARTIFACT_DATA;
	}

	public void setARTIFACT_DATA(String aRTIFACT_DATA) {
		ARTIFACT_DATA = aRTIFACT_DATA;
	}

}
