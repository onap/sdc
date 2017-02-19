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

public class ExpectedProductAudit {

	String ACTION;
	String MODIFIER;
	String STATUS;
	String DESC;
	String RESOURCE_NAME;
	String RESOURCE_TYPE;
	String PREV_VERSION;
	String CURR_VERSION;
	String PREV_STATE;
	String CURR_STATE;
	String TIMESTAMP;
	String SERVICE_INSTANCE_ID;
	String COMMENT;

	public String getCOMMENT() {
		return COMMENT;
	}

	public void setCOMMENT(String cOMMENT) {
		COMMENT = cOMMENT;
	}

	public String getSERVICE_INSTANCE_ID() {
		return SERVICE_INSTANCE_ID;
	}

	public void setSERVICE_INSTANCE_ID(String sERVICE_INSTANCE_ID) {
		SERVICE_INSTANCE_ID = sERVICE_INSTANCE_ID;
	}

	public String getACTION() {
		return ACTION;
	}

	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}

	public String getMODIFIER() {
		return MODIFIER;
	}

	public void setMODIFIER(String mODIFIER) {
		MODIFIER = mODIFIER;
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

	public String getPREV_VERSION() {
		return PREV_VERSION;
	}

	public void setPREV_VERSION(String pREV_VERSION) {
		PREV_VERSION = pREV_VERSION;
	}

	public String getCURR_VERSION() {
		return CURR_VERSION;
	}

	public void setCURR_VERSION(String cURR_VERSION) {
		CURR_VERSION = cURR_VERSION;
	}

	public String getPREV_STATE() {
		return PREV_STATE;
	}

	public void setPREV_STATE(String pREV_STATE) {
		PREV_STATE = pREV_STATE;
	}

	public String getCURR_STATE() {
		return CURR_STATE;
	}

	public void setCURR_STATE(String cURR_STATE) {
		CURR_STATE = cURR_STATE;
	}

	public String getTIMESTAMP() {
		return TIMESTAMP;
	}

	public void setTIMESTAMP(String tIMESTAMP) {
		TIMESTAMP = tIMESTAMP;
	}
}
