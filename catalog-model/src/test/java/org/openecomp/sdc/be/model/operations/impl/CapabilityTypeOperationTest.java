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

package org.openecomp.sdc.be.model.operations.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openecomp.sdc.be.dao.titan.TitanGenericDao;
import org.openecomp.sdc.be.dao.titan.TitanOperationStatus;
import org.openecomp.sdc.be.model.CapabilityTypeDefinition;
import org.openecomp.sdc.be.model.ModelTestBase;
import org.openecomp.sdc.be.model.PropertyConstraint;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.tosca.ToscaType;
import org.openecomp.sdc.be.model.tosca.constraints.GreaterThanConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.InRangeConstraint;
import org.openecomp.sdc.be.model.tosca.constraints.LessOrEqualConstraint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fj.data.Either;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:application-context-test.xml")
// @TestExecutionListeners(listeners = {
// DependencyInjectionTestExecutionListener.class,
// DirtiesContextTestExecutionListener.class,
// TransactionalTestExecutionListener.class })
public class CapabilityTypeOperationTest extends ModelTestBase {

	@Resource(name = "titan-generic-dao")
	private TitanGenericDao titanDao;

	@Resource(name = "capability-type-operation")
	private CapabilityTypeOperation capabilityTypeOperation;

	@BeforeClass
	public static void setupBeforeClass() {
		// ExternalConfiguration.setAppName("catalog-model");
		// String appConfigDir = "src/test/resources/config/catalog-model";
		// ConfigurationSource configurationSource = new
		// FSConfigurationSource(ExternalConfiguration.getChangeListener(),
		// appConfigDir);

		// configurationManager = new ConfigurationManager(
		// new ConfigurationSource() {
		//
		// @Override
		// public <T> T getAndWatchConfiguration(Class<T> className,
		// ConfigurationListener configurationListener) {
		// // TODO Auto-generated method stub
		// return null;
		// }
		//
		// @Override
		// public <T> void addWatchConfiguration(Class<T> className,
		// ConfigurationListener configurationListener) {
		// // TODO Auto-generated method stub
		//
		// }
		// });
		//
		// Configuration configuration = new Configuration();
		// configuration.setTitanInMemoryGraph(true);
		//
		// configurationManager.setConfiguration(configuration);
		ModelTestBase.init();

	}

	@Test
	public void testDummy() {

		assertTrue(capabilityTypeOperation != null);

	}

	@Test
	public void testAddCapabilityType() {

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setDescription("desc1");
		capabilityTypeDefinition.setType("tosca.capabilities.Container1");

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
		assertEquals("check capability type added", true, addCapabilityType1.isLeft());

		CapabilityTypeDefinition capabilityTypeAdded = addCapabilityType1.left().value();
		compareBetweenCreatedToSent(capabilityTypeDefinition, capabilityTypeAdded);

		Either<CapabilityTypeDefinition, TitanOperationStatus> capabilityTypeByUid = capabilityTypeOperation.getCapabilityTypeByUid(capabilityTypeAdded.getUniqueId());
		compareBetweenCreatedToSent(capabilityTypeByUid.left().value(), capabilityTypeDefinition);

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType2 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
		assertEquals("check capability type failed", true, addCapabilityType2.isRight());
		assertEquals("check returned error", StorageOperationStatus.SCHEMA_VIOLATION, addCapabilityType2.right().value());

	}

	@Test
	public void testAddDerviedCapabilityType() {

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setDescription("desc1");
		capabilityTypeDefinition.setType("tosca.capabilities.Container2");
		capabilityTypeDefinition.setDerivedFrom("derivedFrom");

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);
		// assertEquals("check capability type parent not exist",
		// StorageOperationStatus.INVALID_ID,
		// addCapabilityType1.right().value());
		// TODO: esofer change to INVALID_ID
		assertEquals("check capability type parent not exist", StorageOperationStatus.INVALID_ID, addCapabilityType1.right().value());
	}

	public CapabilityTypeDefinition createCapability(String capabilityTypeName) {

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setDescription("desc1");
		capabilityTypeDefinition.setType(capabilityTypeName);

		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

		String propName1 = "disk_size";
		String propName2 = "num_cpus";

		PropertyDefinition property1 = buildProperty1();

		properties.put(propName1, property1);

		PropertyDefinition property2 = buildProperty2();

		properties.put(propName2, property2);

		capabilityTypeDefinition.setProperties(properties);

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);

		CapabilityTypeDefinition capabilityTypeDefinitionCreated = addCapabilityType1.left().value();
		Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType(capabilityTypeDefinitionCreated.getUniqueId(), true);
		assertEquals("check capability type fetched", true, capabilityType.isLeft());
		CapabilityTypeDefinition fetchedCTD = capabilityType.left().value();

		Map<String, PropertyDefinition> fetchedProps = fetchedCTD.getProperties();

		compareProperties(fetchedProps, properties);

		return fetchedCTD;

	}

	@Test
	public void testAddCapabilityTypeWithProperties() {

		CapabilityTypeDefinition capabilityTypeDefinition = new CapabilityTypeDefinition();
		capabilityTypeDefinition.setDescription("desc1");
		capabilityTypeDefinition.setType("tosca.capabilities.Container3");

		Map<String, PropertyDefinition> properties = new HashMap<String, PropertyDefinition>();

		String propName1 = "disk_size";
		String propName2 = "num_cpus";

		PropertyDefinition property1 = buildProperty1();

		properties.put(propName1, property1);

		PropertyDefinition property2 = buildProperty2();

		properties.put(propName2, property2);

		capabilityTypeDefinition.setProperties(properties);

		Either<CapabilityTypeDefinition, StorageOperationStatus> addCapabilityType1 = capabilityTypeOperation.addCapabilityType(capabilityTypeDefinition, true);

		CapabilityTypeDefinition capabilityTypeDefinitionCreated = addCapabilityType1.left().value();
		Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType(capabilityTypeDefinitionCreated.getUniqueId());
		assertEquals("check capability type fetched", true, capabilityType.isLeft());
		CapabilityTypeDefinition fetchedCTD = capabilityType.left().value();

		Map<String, PropertyDefinition> fetchedProps = fetchedCTD.getProperties();

		compareProperties(fetchedProps, properties);
	}

	private void compareProperties(Map<String, PropertyDefinition> first, Map<String, PropertyDefinition> second) {

		assertTrue("check properties are full or empty", ((first == null && second == null) || (first != null && second != null)));
		if (first != null) {
			assertEquals("check properties size", first.size(), second.size());

			for (Entry<String, PropertyDefinition> entry : first.entrySet()) {

				String propName = entry.getKey();
				PropertyDefinition secondPD = second.get(propName);
				assertNotNull("Cannot find property " + propName + " in " + second, secondPD);

				PropertyDefinition firstPD = entry.getValue();

				comparePropertyDefinition(firstPD, secondPD);
			}

		}

	}

	@Test
	public void testGetCapabilityTypeNotFound() {

		Either<CapabilityTypeDefinition, StorageOperationStatus> capabilityType = capabilityTypeOperation.getCapabilityType("not_exists");
		assertEquals("check not found is returned", StorageOperationStatus.NOT_FOUND, capabilityType.right().value());

	}

	private void comparePropertyDefinition(PropertyDefinition first, PropertyDefinition second) {

		assertTrue("check objects are full or empty", ((first == null && second == null) || (first != null && second != null)));
		if (first != null) {
			assertTrue("check property default value", compareValue(first.getDefaultValue(), second.getDefaultValue()));
			assertTrue("check property description", compareValue(first.getDescription(), second.getDescription()));
			assertTrue("check property type", compareValue(first.getType(), second.getType()));
			compareList(first.getConstraints(), second.getConstraints());
		}

	}

	private void compareList(List<PropertyConstraint> first, List<PropertyConstraint> second) {

		assertTrue("check lists are full or empty", ((first == null && second == null) || (first != null && second != null)));
		if (first != null) {
			assertEquals("check list size", first.size(), second.size());
		}
	}

	private PropertyDefinition buildProperty2() {
		PropertyDefinition property2 = new PropertyDefinition();
		property2.setDefaultValue("2");
		property2.setDescription("Number of (actual or virtual) CPUs associated with the Compute node.");
		property2.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints3 = new ArrayList<PropertyConstraint>();
		List<String> range = new ArrayList<String>();
		range.add("1");
		range.add("4");

		InRangeConstraint propertyConstraint3 = new InRangeConstraint(range);
		constraints3.add(propertyConstraint3);
		// property2.setConstraints(constraints3);
		property2.setConstraints(constraints3);
		return property2;
	}

	private PropertyDefinition buildProperty1() {
		PropertyDefinition property1 = new PropertyDefinition();
		property1.setDefaultValue("10");
		property1.setDescription("Size of the local disk, in Gigabytes (GB), available to applications running on the Compute node.");
		property1.setType(ToscaType.INTEGER.name().toLowerCase());
		List<PropertyConstraint> constraints = new ArrayList<PropertyConstraint>();
		GreaterThanConstraint propertyConstraint1 = new GreaterThanConstraint("0");
		constraints.add(propertyConstraint1);

		LessOrEqualConstraint propertyConstraint2 = new LessOrEqualConstraint("10");
		constraints.add(propertyConstraint2);

		property1.setConstraints(constraints);
		return property1;
	}

	private void compareBetweenCreatedToSent(CapabilityTypeDefinition x, CapabilityTypeDefinition y) {

		assertTrue(compareValue(x.getDerivedFrom(), y.getDerivedFrom()));
		assertTrue(compareValue(x.getType(), y.getType()));
		assertTrue(compareValue(x.getDescription(), y.getDescription()));

	}

	public boolean compareValue(String first, String second) {

		if (first == null && second == null) {
			return true;
		}
		if (first != null) {
			return first.equals(second);
		} else {
			return false;
		}
	}

	public void setOperations(TitanGenericDao titanDao, CapabilityTypeOperation capabilityTypeOperation) {
		this.titanDao = titanDao;
		this.capabilityTypeOperation = capabilityTypeOperation;

	}

}
