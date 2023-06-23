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

package org.onap.sdc.backend.ci.tests.execute.property;

import fj.data.Either;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.datatypes.enums.*;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.PropertyReqDetails;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.PropertyRestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertTrue;

// open bug for this class: DE199108 - closed, DE199741
public class ComponentInstancePropertyTest extends ComponentBaseTest {

	protected Resource basicVFC;
	protected Resource vfc1FromBasicVFC;
	protected Resource vfc2FromVfc1;
	protected Resource vfResource;

	private List<ComponentInstanceProperty> expectedPropertyList;
	private List<ComponentInstanceProperty> actualPropertyList;
	// protected String updatedStringValue = "{Not Default String Value}";
	protected String updatedStringValue = "Not Default String Value";
	protected String updatedIntegerValue = "666";
	protected String updatedBooleanValue = "false";
	protected String newStringPropName = "stringProp2";
	protected String newIntegerPropName = "integerProp2";
	protected String newBooleanPropName = "booleanProp2";
	// bug DE199741 protected String newStringPropValue = "<second string
	// value>";
	protected String newStringPropValue = "second string value";
	protected String newIntegerPropValue = "888";
	protected String newBooleanPropValue = "false";

	@BeforeMethod
	public void init() {
		expectedPropertyList = new ArrayList<>();
		actualPropertyList = new ArrayList<>();
	}

	@Rule
	public static TestName name = new TestName();

	// --------------Regular
	// resource-------------------------------------------------------------------------------

	@Test
	public void nestedResourceProperty3Levels() throws Exception {

		// first res
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		// second resource
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		// third resource
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		// verify property
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, actualPropertyList);
		assertTrue("check list size failed, expected 3", actualPropertyList.size() == 3);

	}

	// --------------VF
	// resource-----------------------------------------------------------

	@Test
	public void nestedVfResourceProperty3Levels() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		// verify property
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);
	}

	@Test
	public void nestedVfResourceProperty3LevelsAndCpWithProp() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// four resource
		Resource cp = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP,
				NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails cpStringProperty = new ElementFactory().getDefaultStringProperty();
		cpStringProperty.setName("Different Name");
		cpStringProperty.setPropertyDefaultValue("Different value from default");
		new AtomicOperationUtils().addCustomPropertyToResource(cpStringProperty, cp, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		cp = new AtomicOperationUtils().getResourceObject(cp, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(cp, expectedPropertyList);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, vfResource, UserRoleEnum.DESIGNER, true).left().value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, cp, expectedPropertyList,
				vfResource);
		// verify property
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);
	}

	@Test
	public void nestedCertifiedVfResourceProperty3Levels() throws Exception {
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CERTIFY, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		// verify property
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);
	}

	@Test
	public void nestedVfResourceProperty3Levels2SameResInstances() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		// verify property
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);

		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);
	}

	// ------------------update resource
	// property-----------------------------------

	@Test
	public void nestedVfResourceProperty3LevelsUpdateFirstLevelProperty() throws Exception {
		// first res
		basicVFC = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addDefaultPropertyToResource(PropertyTypeEnum.STRING, basicVFC, UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);

		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);

		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);

		// verify property
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedStringValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(vfResource, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	@Test
	public void nestedVfResourceProperty3LevelsUpdateSecondLevelProperty() throws Exception {
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);

		// second resource
		vfc1FromBasicVFC = new AtomicOperationUtils().createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC,
				basicVFC, ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addCustomPropertyToResource(new ElementFactory().getDefaultIntegerProperty(), vfc1FromBasicVFC,
						UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(vfc1FromBasicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY,
				true);

		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);

		// verify property
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedIntegerValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(vfResource, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue(
				"expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS
						+ " ,but was " + updatePropertyValueOnResourceInstance.getErrorCode(),
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated properly",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	@Test
	public void nestedVfResourceProperty3LevelsUpdateThirdLevelProperty() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);

		// third resource
		vfc2FromVfc1 = new AtomicOperationUtils().createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC,
				vfc1FromBasicVFC, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addCustomPropertyToResource(new ElementFactory().getDefaultBooleanProperty(), vfc2FromVfc1,
						UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(vfc2FromVfc1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN,
				true);

		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);

		// verify property
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedBooleanValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(vfResource, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	// ---------------------Service------------------------------------------------------------------------

	/**
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p3) (p2) (p1)
	 */
	@Test
	public void serviceWithNestedResourceProperty3Levels() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	/**
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1)
	 */
	@Test
	public void serviceWithNestedResourceProperty3LevelsAndVfProperty() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		PropertyReqDetails propDetails = new ElementFactory().getDefaultBooleanProperty();
		propDetails.setName(newBooleanPropName);
		propDetails.setPropertyDefaultValue(newBooleanPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(propDetails, vfResource, UserRoleEnum.DESIGNER, true);
		propDetails = new ElementFactory().getDefaultStringProperty();
		propDetails.setName(newStringPropName);
		propDetails.setPropertyDefaultValue(newStringPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(propDetails, vfResource, UserRoleEnum.DESIGNER, true);
		propDetails = new ElementFactory().getDefaultIntegerProperty();
		propDetails.setName(newIntegerPropName);
		propDetails.setPropertyDefaultValue(newIntegerPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(propDetails, vfResource, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfResource, expectedPropertyList);

		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	/**
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1) CP(VF inst) (p5)
	 */
	@Test
	public void serviceWithNestedResourceProperty3LevelsAndCp() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// four resource
		Resource cp = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP,
				NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails cpStringProperty = new ElementFactory().getDefaultStringProperty();
		cpStringProperty.setName("Different Name");
		cpStringProperty.setPropertyDefaultValue("Different value from default");
		new AtomicOperationUtils().addCustomPropertyToResource(cpStringProperty, cp, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		cp = new AtomicOperationUtils().getResourceObject(cp, UserRoleEnum.DESIGNER);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(cp, expectedPropertyList);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, vfResource, UserRoleEnum.DESIGNER, true).left().value();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, cp, expectedPropertyList,
				vfResource);

		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	/**
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1) CP(inst) (p5)
	 */
	@Test
	public void serviceWithNestedResourceProperty3LevelsAndCpResInst() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		// expectedPropertyList =
		// PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1,
		// expectedPropertyList);

		// four resource
		Resource cp = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP,
				NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails cpStringProperty = new ElementFactory().getDefaultStringProperty();
		cpStringProperty.setName("Different Name");
		cpStringProperty.setPropertyDefaultValue("Different value from default");
		new AtomicOperationUtils().addCustomPropertyToResource(cpStringProperty, cp, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(cp, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		cp = new AtomicOperationUtils().getResourceObject(cp, UserRoleEnum.DESIGNER);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(cp, expectedPropertyList);

		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, service, UserRoleEnum.DESIGNER, true).left().value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	/**
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) | (p3) (p2) (p1)
	 * | (VFC(inst)-->VFC-->VFC-->VFC) (p3) (p2) (p1)
	 * 
	 * VF2(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) | (p3') (p2') (p1') |
	 * (VFC(inst)-->VFC-->VFC-->VFC) (p3) (p2) (p1)
	 */
	@Test
	public void serviceNestedVfResourceProperty3Levels2SameResInstances() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1, expectedPropertyList);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		// verify property
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);

		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnResource(componentInstDetails, vfc2FromVfc1, expectedPropertyList,
				vfResource);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	// service test template
	/**
	 * Service-->VF(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1)
	 */
	@Test
	public void serviceNestedVfResourceProperty3LevelsAndSelfVfProperty() throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);
		PropertyReqDetails newProp = new ElementFactory().getDefaultStringProperty();
		newProp.setName(newStringPropName);
		newProp.setPropertyDefaultValue(newStringPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(newProp, vfResource, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

	}

	/**
	 * update property(p4)
	 * 
	 * 
	 * VFC(p1) ^ | VFC(p2) ^ | Service-->VF(inst)-->VF-->(VFC(inst)-->VFC(p3)
	 * (p4)
	 */
	@Test
	public void serviceNestedVfResourceProperty3LevelsAndSelfVfProperty_UpdateVfproperty() throws Exception {
		// Create VFC(check-in state) derived from another resource
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();

		// add property to VF
		PropertyReqDetails newProp = new ElementFactory().getDefaultStringProperty();
		newProp.setName(newStringPropName);
		newProp.setPropertyDefaultValue(newStringPropValue);
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addCustomPropertyToResource(newProp, vfResource, UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update VF instance property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedStringValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	/**
	 * update property(p1)
	 * Service-->VF(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1)
	 */
	@Test
	public void serviceNestedVfResourceProperty3LevelsAndSelfVfPropertyUpdateVfInheritance1LevelProperty()
			throws Exception {

		basicVFC = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addDefaultPropertyToResource(PropertyTypeEnum.STRING, basicVFC, UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(basicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		// verify property
		PropertyReqDetails newProp = new ElementFactory().getDefaultStringProperty();
		newProp.setName(newStringPropName);
		newProp.setPropertyDefaultValue(newStringPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(newProp, vfResource, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update VF property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedStringValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	/**
	 * update property(p2)
	 * Service-->VF(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1)
	 */
	@Test
	public void serviceNestedVfResourceProperty3LevelsAndSelfVfPropertyUpdateVfInheritance2LevelProperty()
			throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = new AtomicOperationUtils().createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC,
				basicVFC, ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addCustomPropertyToResource(new ElementFactory().getDefaultIntegerProperty(), vfc1FromBasicVFC,
						UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(vfc1FromBasicVFC, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY,
				true);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();

		// verify property
		PropertyReqDetails newProp = new ElementFactory().getDefaultStringProperty();
		newProp.setName(newStringPropName);
		newProp.setPropertyDefaultValue(newStringPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(newProp, vfResource, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update VF property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedIntegerValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	/**
	 * update property(p3)
	 * Service-->VF(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1)
	 */
	@Test
	public void serviceNestedVfResourceProperty3LevelsAndSelfVfPropertyUpdateVfInheritance3LevelProperty()
			throws Exception {

		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC,
				vfc1FromBasicVFC, ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true).left().value();
		Either<ComponentInstanceProperty, RestResponse> propDetailsToUpdate = new AtomicOperationUtils()
				.addCustomPropertyToResource(new ElementFactory().getDefaultBooleanProperty(), vfc2FromVfc1,
						UserRoleEnum.DESIGNER, true);
		String propNameToUpdate = propDetailsToUpdate.left().value().getName();
		String propTypeToUpdate = propDetailsToUpdate.left().value().getType();
		new AtomicOperationUtils().changeComponentState(vfc2FromVfc1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN,
				true);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);

		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		// verify property
		PropertyReqDetails newProp = new ElementFactory().getDefaultStringProperty();
		newProp.setName(newStringPropName);
		newProp.setPropertyDefaultValue(newStringPropValue);
		new AtomicOperationUtils().addCustomPropertyToResource(newProp, vfResource, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		// update VF property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils
				.getPropFromListByPropNameAndType(actualPropertyList, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedBooleanValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, null);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));

	}

	/**
	 * update property p5'
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1) CP(inst on VF) (p5) CP(inst) (p5')
	 */
	@Test
	public void serviceWithNestedResourceProperty3LevelsAndCpOnVfUpdateCpInstanceOfService() throws Exception {
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CHECKIN, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		// expectedPropertyList =
		// PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1,
		// expectedPropertyList);

		// four resource
		Resource cp = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP,
				NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails cpStringProperty = new ElementFactory().getDefaultStringProperty();
		cpStringProperty.setName("Different Name");
		cpStringProperty.setPropertyDefaultValue("Different value from default");
		new AtomicOperationUtils().addCustomPropertyToResource(cpStringProperty, cp, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(cp, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		cp = new AtomicOperationUtils().getResourceObject(cp, UserRoleEnum.DESIGNER);
		// create VF + add RI
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, vfResource, UserRoleEnum.DESIGNER, true).left().value();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(cp, expectedPropertyList);

		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, service, UserRoleEnum.DESIGNER, true).left().value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		// service = new AtomicOperationUtils().getServiceObject(service,
		// UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		String propNameToUpdate = "cidr";
		String propTypeToUpdate = "string";

		// update CP property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils.getCompPropInstListByInstIdAndPropName(
				service, componentInstDetails, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedStringValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		List<String> path = expectedUpdatePropDetails.getPath();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, path);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));
	}

	/**
	 * update property p5
	 * Service-->VF1(inst)-->VF-->(VFC(inst)-->VFC-->VFC-->VFC) (p4) (p3) (p2)
	 * (p1) CP(inst on VF) (p5) CP(inst) (p5')
	 */
	@Test
	public void serviceWithNestedResourceProperty3LevelsAndCpOnVfUpdateCpInstanceOfVf() throws Exception {
		basicVFC = createResourceWithProperty(new ElementFactory().getDefaultStringProperty(), LifeCycleStatesEnum.CERTIFY);
		vfc1FromBasicVFC = createResourceWithPropertyDerivedFromOtherResource(
				new ElementFactory().getDefaultIntegerProperty(), LifeCycleStatesEnum.CERTIFY, basicVFC);
		vfc2FromVfc1 = createResourceWithPropertyDerivedFromOtherResource(new ElementFactory().getDefaultBooleanProperty(),
				LifeCycleStatesEnum.CERTIFY, vfc1FromBasicVFC);
		vfc2FromVfc1 = new AtomicOperationUtils().getResourceObject(vfc2FromVfc1, UserRoleEnum.DESIGNER);
		// expectedPropertyList =
		// PropertyRestUtils.addResourcePropertiesToList(vfc2FromVfc1,
		// expectedPropertyList);

		// four resource
		Resource cp = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP,
				NormativeTypesEnum.NETWORK, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		PropertyReqDetails cpStringProperty = new ElementFactory().getDefaultStringProperty();
		cpStringProperty.setName("Different Name");
		cpStringProperty.setPropertyDefaultValue("Different value from default");
		new AtomicOperationUtils().addCustomPropertyToResource(cpStringProperty, cp, UserRoleEnum.DESIGNER, true);
		new AtomicOperationUtils().changeComponentState(cp, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true);
		// create VF + add RI
		cp = new AtomicOperationUtils().getResourceObject(cp, UserRoleEnum.DESIGNER);
		vfResource = new AtomicOperationUtils().createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left()
				.value();
		ComponentInstance componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfc2FromVfc1, vfResource, UserRoleEnum.DESIGNER, true).left()
				.value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, vfResource, UserRoleEnum.DESIGNER, true).left().value();
		new AtomicOperationUtils().changeComponentState(vfResource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true);
		vfResource = new AtomicOperationUtils().getResourceObject(vfResource, UserRoleEnum.DESIGNER);

		// Service
		expectedPropertyList = new ArrayList<ComponentInstanceProperty>();
		actualPropertyList = new ArrayList<ComponentInstanceProperty>();
		expectedPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(vfResource, expectedPropertyList,
				null);
		expectedPropertyList = PropertyRestUtils.addResourcePropertiesToList(cp, expectedPropertyList);

		Service service = new AtomicOperationUtils().createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(vfResource, service, UserRoleEnum.DESIGNER, true).left()
				.value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		componentInstDetails = new AtomicOperationUtils()
				.addComponentInstanceToComponentContainer(cp, service, UserRoleEnum.DESIGNER, true).left().value();
		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		PropertyRestUtils.updatePropertyListWithPathOnComponentInstance(componentInstDetails, service,
				expectedPropertyList);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);
		PropertyRestUtils.comparePropertyLists(expectedPropertyList, actualPropertyList, false);

		String propNameToUpdate = "cidr";
		String propTypeToUpdate = "string";

		// update CP property
		ComponentInstanceProperty expectedUpdatePropDetails = PropertyRestUtils.getCompPropInstListByInstIdAndPropName(
				service, componentInstDetails, propNameToUpdate, propTypeToUpdate);
		expectedUpdatePropDetails.setValue(updatedStringValue);
		String propUniqeId = expectedUpdatePropDetails.getUniqueId();
		List<String> path = expectedUpdatePropDetails.getPath();
		RestResponse updatePropertyValueOnResourceInstance = ComponentInstanceRestUtils
				.updatePropertyValueOnResourceInstance(service, componentInstDetails,
						new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER), expectedUpdatePropDetails);
		assertTrue("expected updatePropertyValueOnResourceInstance response code: " + BaseRestUtils.STATUS_CODE_SUCCESS,
				updatePropertyValueOnResourceInstance.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);

		service = new AtomicOperationUtils().getServiceObject(service, UserRoleEnum.DESIGNER);

		actualPropertyList = new ArrayList<>();
		actualPropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, actualPropertyList, null);

		ComponentInstanceProperty actualUpdatedPropDetails = PropertyRestUtils
				.getPropFromListByPropIdAndPath(actualPropertyList, propUniqeId, path);
		assertTrue("property was not updated propely",
				PropertyRestUtils.comparePropertyObjects(expectedUpdatePropDetails, actualUpdatedPropDetails, true));
	}

	// -------------------Methods--------------------------
	public static PropertyDataDefinition convertToPropertyDataDefinitionObject(PropertyReqDetails prop) {
		PropertyDataDefinition propDataDef = new PropertyDataDefinition();
		propDataDef.setDefaultValue(prop.getPropertyDefaultValue());
		propDataDef.setType(prop.getPropertyType());
		propDataDef.setPassword(prop.getPropertyPassword());
		propDataDef.setDescription(prop.getPropertyDescription());
		return propDataDef;
	}

	protected Resource createResourceWithPropertyDerivedFromOtherResource(PropertyReqDetails propertyReqDetails,
			LifeCycleStatesEnum state, Resource derivedFromResource) throws Exception {
		Resource resource = new AtomicOperationUtils().createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum.VFC,
				derivedFromResource, ResourceCategoryEnum.APPLICATION_L4_BORDER, UserRoleEnum.DESIGNER, true).left()
				.value();
		if (propertyReqDetails != null) {
			new AtomicOperationUtils().addCustomPropertyToResource(propertyReqDetails, resource, UserRoleEnum.DESIGNER, true);
		}
		new AtomicOperationUtils().changeComponentState(resource, UserRoleEnum.DESIGNER, state, true);
		return new AtomicOperationUtils().getResourceObject(resource, UserRoleEnum.DESIGNER);
		// return resource;
	}

	protected Resource createResourceWithProperty(PropertyReqDetails propertyReqDetails, LifeCycleStatesEnum state)
			throws Exception {
		Resource resource = new AtomicOperationUtils().createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.GENERIC_ABSTRACT, UserRoleEnum.DESIGNER, true).left()
				.value();
		if (propertyReqDetails != null) {
			new AtomicOperationUtils().addCustomPropertyToResource(propertyReqDetails, resource, UserRoleEnum.DESIGNER, true);
		}
		new AtomicOperationUtils().changeComponentState(resource, UserRoleEnum.DESIGNER, state, true);
		return new AtomicOperationUtils().getResourceObject(resource, UserRoleEnum.DESIGNER);
	}

}
