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

package org.openecomp.sdc.be.model;

public enum LifeCycleTransitionEnum {

	CHECKOUT("checkout"), 
	CHECKIN("checkin"), 
	CERTIFICATION_REQUEST("certificationRequest"), 
	UNDO_CHECKOUT("undoCheckout"), 
	CANCEL_CERTIFICATION("cancelCertification"), 
	START_CERTIFICATION("startCertification"), 
	FAIL_CERTIFICATION("failCertification"), 
	CERTIFY("certify"), 
	DISTRIBUTE("distribute");

	String displayName;

	private LifeCycleTransitionEnum(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static LifeCycleTransitionEnum getFromDisplayName(String name) {
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CHECKOUT.getDisplayName())) {
			return LifeCycleTransitionEnum.CHECKOUT;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CHECKIN.getDisplayName())) {
			return LifeCycleTransitionEnum.CHECKIN;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CERTIFICATION_REQUEST.getDisplayName())) {
			return LifeCycleTransitionEnum.CERTIFICATION_REQUEST;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.UNDO_CHECKOUT.getDisplayName())) {
			return LifeCycleTransitionEnum.UNDO_CHECKOUT;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CANCEL_CERTIFICATION.getDisplayName())) {
			return LifeCycleTransitionEnum.CANCEL_CERTIFICATION;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.START_CERTIFICATION.getDisplayName())) {
			return LifeCycleTransitionEnum.START_CERTIFICATION;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.FAIL_CERTIFICATION.getDisplayName())) {
			return LifeCycleTransitionEnum.FAIL_CERTIFICATION;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.CERTIFY.getDisplayName())) {
			return LifeCycleTransitionEnum.CERTIFY;
		}
		if (name.equalsIgnoreCase(LifeCycleTransitionEnum.DISTRIBUTE.getDisplayName())) {
			return LifeCycleTransitionEnum.DISTRIBUTE;
		} else
			throw new IllegalArgumentException(name + " value does not match any of LifeCycleTransitionEnum values");
	}

	public static String valuesAsString() {
		StringBuilder sb = new StringBuilder();
		for (LifeCycleTransitionEnum op : LifeCycleTransitionEnum.values()) {
			sb.append(op.getDisplayName()).append(" ");
		}
		return sb.toString();
	}
}
