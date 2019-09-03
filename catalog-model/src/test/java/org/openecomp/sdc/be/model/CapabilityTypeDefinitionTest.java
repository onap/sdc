/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.be.model;

import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition.OwnerType;
import org.openecomp.sdc.be.datatypes.elements.CapabilityTypeDataDefinition;
import org.openecomp.sdc.be.resources.data.CapabilityTypeData;

public class CapabilityTypeDefinitionTest {

	private static final String OWNER_NAME = "ownerName";
	private static final String NAME = "name";
	private static final OwnerType RESOURCE = OwnerType.RESOURCE;
	private static final String TYPE = "TYPE";
	private static final String DESCRIPTION = "DESCRIPTION";
	private static final String UNIQUE_ID = "UNIQUE_ID";

	@Test
	public void hasValidGettersAndSettersTest() {
		assertThat(CapabilityTypeDefinition.class,
			hasValidGettersAndSettersExcluding("empty", "ownerIdIfEmpty"));
	}

	@Test
	public void shouldHaveValidToString() {
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition(
			new CapabilityTypeDefinition(), OWNER_NAME, NAME, RESOURCE);
		capabilityDefinition.setProperties(Collections.emptyList());
		capabilityDefinition.setType(TYPE);
		capabilityDefinition.setDescription(DESCRIPTION);
		CapabilityTypeDefinition capabilityTypeDefinitionTest = new CapabilityTypeDefinition(capabilityDefinition);
		String toStringRepr = capabilityTypeDefinitionTest.toString();
		assertEquals(toStringRepr, "CapabilityTypeDataDefinition [uniqueId=null, description=DESCRIPTION, type=TYPE, validSourceTypes=[], version=null, creationTime=null, modificationTime=null] [ derivedFrom=null, properties={} ]");
	}

	@Test
	public void shouldCreateCapabilityTypeDefinitionFromCapabilityTypeData() {
		CapabilityTypeData capabilityTypeData = new CapabilityTypeData();
		CapabilityTypeDataDefinition capabilityTypeDataDefinition = new CapabilityTypeDataDefinition();
		capabilityTypeDataDefinition.setUniqueId(UNIQUE_ID);
		capabilityTypeDataDefinition.setType(TYPE);
		capabilityTypeData.setCapabilityTypeDataDefinition(capabilityTypeDataDefinition);
		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition(capabilityTypeData);
		assertEquals(capabilityTypeDefinition.getType(), capabilityTypeData.getCapabilityTypeDataDefinition().getType());
		assertEquals(capabilityTypeDefinition.getUniqueId(), capabilityTypeData.getCapabilityTypeDataDefinition().getUniqueId());
	}
}
