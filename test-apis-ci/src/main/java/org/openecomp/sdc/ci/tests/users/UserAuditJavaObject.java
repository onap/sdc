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

package org.openecomp.sdc.ci.tests.users;

public class UserAuditJavaObject {

	String TIMESTAMP;
	String ACTION;
	// String MODIFIER_NAME;
	String MODIFIER;
	String USER;
	// String USER_NAME;
	// String USER_EMAIL;
	// String USER_ROLE;
	String STATUS;
	String DESC;

	public UserAuditJavaObject(String timestamp, String action, String modifier, String user, String status,
			String desc) {
		super();
		this.TIMESTAMP = timestamp;
		this.ACTION = action;
		// this.MODIFIER_NAME = modifierName;
		this.MODIFIER = modifier;
		this.USER = user;
		// this.USER_NAME = userName;
		// this.USER_EMAIL = userEmail;
		// this.USER_ROLE = userRole;
		this.STATUS = status;
		this.DESC = desc;
	}

	public UserAuditJavaObject() {
		super();
	}

	public String getTIMESTAMP() {
		return TIMESTAMP;
	}

	public void setTIMESTAMP(String tIMESTAMP) {
		TIMESTAMP = tIMESTAMP;
	}

	public String getACTION() {
		return ACTION;
	}

	public void setACTION(String aCTION) {
		ACTION = aCTION;
	}

	// public String getMODIFIER_NAME() {
	// return MODIFIER_NAME;
	// }
	// public void setMODIFIER_NAME(String mODIFIER_NAME) {
	// MODIFIER_NAME = mODIFIER_NAME;
	// }
	public String getMODIFIER() {
		return MODIFIER;
	}

	public void setMODIFIER(String mODIFIER_UID) {
		MODIFIER = mODIFIER_UID;
	}

	public String getUSER() {
		return USER;
	}

	public void setUSER(String uSER) {
		USER = uSER;
	}

	// public String getUSER_NAME() {
	// return USER_NAME;
	// }
	// public void setUSER_NAME(String uSER_NAME) {
	// USER_NAME = uSER_NAME;
	// }
	// public String getUSER_EMAIL() {
	// return USER_EMAIL;
	// }
	// public void setUSER_EMAIL(String uSER_EMAIL) {
	// USER_EMAIL = uSER_EMAIL;
	// }
	// public String getUSER_ROLE() {
	// return USER_ROLE;
	// }
	// public void setUSER_ROLE(String uSER_ROLE) {
	// USER_ROLE = uSER_ROLE;
	// }
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

	@Override
	public String toString() {
		return "UserAuditJavaObject [timestamp=" + TIMESTAMP + ", action=" + ACTION + ", modifier=" + MODIFIER
				+ ", user=" + USER + ", status=" + STATUS + ", desc=" + DESC + "]";
	}

}
