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

import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanEqualsFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanHashCodeFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidBeanToStringFor;
import static com.google.code.beanmatchers.BeanMatchers.hasValidGettersAndSettersExcluding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.CapabilityDataDefinition.OwnerType;
import org.openecomp.sdc.be.model.tosca.constraints.EqualConstraint;

public class CapabilityDefinitionTest {

	private static final String OWNER_NAME = "OWNER";
	private static final String OWNER_ID = "OWNER_ID";
	private static final String NAME = "NAME";
	private static final OwnerType OWNER_TYPE = OwnerType.COMPONENT_INSTANCE;
	private static final String PROP = "PROP";
	private static final String EQ = "eq";
	private static final String PROPERTIES = "properties";
	private static final String VALUE = "VALUE";

	@Test
	public void hasValidGettersAndSettersTest() {
		assertThat(CapabilityDefinition.class,
			hasValidGettersAndSettersExcluding("empty", "ownerIdIfEmpty", "version"));
	}

	@Test
	public void shouldHaveValidToString() {
		assertThat(CapabilityDefinition.class, hasValidBeanToStringFor(PROPERTIES));
	}

	@Test
	public void shouldHaveEquals() {
		assertThat(CapabilityDefinition.class, hasValidBeanEqualsFor(PROPERTIES));
	}

	@Test
	public void shouldHaveHashCode() {
		assertThat(CapabilityDefinition.class, hasValidBeanHashCodeFor(PROPERTIES));
	}

	@Test
	public void testParamConstructor() {
		EqualConstraint equalConstraint = new EqualConstraint(EQ);
		CapabilityDefinition capabilityDefinition = createCapabilityDefinition(equalConstraint);
		assertEquals(capabilityDefinition.getOwnerName(), OWNER_NAME);
		assertEquals(capabilityDefinition.getProperties().get(0).getConstraints().get(0), equalConstraint);
		assertEquals(capabilityDefinition.getName(), NAME);
		assertEquals(capabilityDefinition.getOwnerType(), OWNER_TYPE);
	}

	@Test
	public void testCopyConstructor() {
		EqualConstraint equalConstraint = new EqualConstraint(EQ);
		CapabilityDefinition capabilityDefinition = createCapabilityDefinition(equalConstraint);
		CapabilityDefinition copiedCapabilityDefinition = new CapabilityDefinition(capabilityDefinition);
		assertEquals(copiedCapabilityDefinition.getOwnerName(), OWNER_NAME);
		assertEquals(copiedCapabilityDefinition.getProperties().get(0).getConstraints().get(0), equalConstraint);
		assertEquals(copiedCapabilityDefinition.getName(), NAME);
		assertEquals(copiedCapabilityDefinition.getOwnerType(), OWNER_TYPE);
	}

	@Test
	public void shouldUpdateCapabilityProperties() {
		EqualConstraint equalConstraint = new EqualConstraint(EQ);
		CapabilityDefinition referenceCapabilityDefinition = createCapabilityDefinition(equalConstraint);
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		ArrayList<ComponentInstanceProperty> properties = new ArrayList<>();
		ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty();
		componentInstanceProperty.setName(NAME);
		properties.add(componentInstanceProperty);
		capabilityDefinition.setProperties(properties);
		capabilityDefinition.updateCapabilityProperties(referenceCapabilityDefinition);
		assertEquals(capabilityDefinition.getProperties().get(0).getValue(), VALUE);
	}

	@Test
	public void shouldUpdateEmptyCapabilityOwnerFields() {
		CapabilityDefinition capabilityDefinition = new CapabilityDefinition();
		capabilityDefinition.updateEmptyCapabilityOwnerFields(OWNER_ID, OWNER_NAME, OWNER_TYPE);
		assertEquals(capabilityDefinition.getOwnerName(), OWNER_NAME);
		assertEquals(capabilityDefinition.getOwnerType(), OWNER_TYPE);
		assertEquals(capabilityDefinition.getOwnerId(), OWNER_ID);
	}

	private CapabilityDefinition createCapabilityDefinition(EqualConstraint equalConstraint){
		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		HashMap<String, PropertyDefinition> properties = new HashMap<>();
		PropertyDefinition propertyDefinition = new PropertyDefinition();
		ArrayList<PropertyConstraint> constraints = new ArrayList<>();
		constraints.add(equalConstraint);
		propertyDefinition.setConstraints(constraints);
		propertyDefinition.setName(NAME);
		propertyDefinition.setValue(VALUE);
		properties.put(PROP, propertyDefinition);
		capabilityTypeDefinition.setProperties(properties);
		return new CapabilityDefinition(capabilityTypeDefinition, OWNER_NAME, NAME,
			OWNER_TYPE);
	}

}
