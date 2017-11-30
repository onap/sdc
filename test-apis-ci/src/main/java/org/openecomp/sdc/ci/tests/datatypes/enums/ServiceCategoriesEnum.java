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

import java.util.Random;

public enum ServiceCategoriesEnum {

	VOIP("VoIP Call Control"), MOBILITY("Mobility"), NETWORK_L4("Network L4+"), NETWORK_L3("Network L1-3");
	String value;

	private ServiceCategoriesEnum(String value) {
		this.value = value;
	}

	public String getValue() {

		return value;
	}

    public static ServiceCategoriesEnum getRandomElement() {
        Random random = new Random();
        return ServiceCategoriesEnum.values()[random.nextInt(ServiceCategoriesEnum.values().length)];
    }
}
