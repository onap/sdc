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

package org.openecomp.sdc.ci.tests.datatypes.enums;

public enum LifeCycleStatesEnum {

	CHECKOUT("checkout", "NOT_CERTIFIED_CHECKOUT"), 
	CHECKIN("checkin", "NOT_CERTIFIED_CHECKIN"), 
	CERTIFICATIONREQUEST("certificationRequest", "READY_FOR_CERTIFICATION"), 
	UNDOCHECKOUT("undoCheckout", ""), 
	CANCELCERTIFICATION("cancelCertification", ""), 
	STARTCERTIFICATION("startCertification", "CERTIFICATION_IN_PROGRESS"), 
	FAILCERTIFICATION("failCertification", ""), 
	CERTIFY("certify", "CERTIFIED");

	private String state;
	private String componentState;

	private LifeCycleStatesEnum(String state, String componentState) {
		this.state = state;
		this.componentState = componentState;

	}

	public String getState() {
		return state;
	}

	public String getComponentState() {
		return componentState;
	}

	public static LifeCycleStatesEnum findByCompState(String compState) {

		for (LifeCycleStatesEnum lifeCycleStatesEnum : LifeCycleStatesEnum.values()) {
			if (lifeCycleStatesEnum.getComponentState().equals(compState)) {
				return lifeCycleStatesEnum;
			}
		}

		return null;

	}

	public static LifeCycleStatesEnum findByState(String state) {

		for (LifeCycleStatesEnum lifeCycleStatesEnum : LifeCycleStatesEnum.values()) {
			if (lifeCycleStatesEnum.name().equals(state)) {
				return lifeCycleStatesEnum;
			}
		}

		return null;

	}

}
