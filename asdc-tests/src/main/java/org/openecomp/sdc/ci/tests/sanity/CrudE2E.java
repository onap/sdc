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

package org.openecomp.sdc.ci.tests.sanity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.AssocType;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import fj.data.Either;

public class CrudE2E extends ComponentBaseTest {
	private static Logger log = LoggerFactory.getLogger(CrudE2E.class.getName());

	public Component resourceDetailsVFCcomp_01;
	public Component resourceDetailsVFCsoft_01;
	public Component resourceDetailsCP_01;
	public Component resourceDetailsVL_01;
	public Component resourceDetailsVF_01;
	public Component resourceDetailsVF_02;

	public ComponentInstance resourceDetailsVFC1compIns1;
	public ComponentInstance resourceDetailsVFC1softIns1;
	public ComponentInstance resourceDetailsCP1ins_01;
	public ComponentInstance resourceDetailsVL1ins_01;
	public ComponentInstance resourceDetailsVF1ins_01;
	public Component defaultService1;
	private List<String> variablesAsList = new ArrayList<String>();

	@Rule
	public static TestName name = new TestName();

	public CrudE2E() {
		super(name, CrudE2E.class.getName());
	}

	@Test
	public void complexScenario() throws Exception {

		User designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		//////// create defaultService1 ///////////////////////
		Either<Service, RestResponse> createDefaultService1e = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true);
		defaultService1 = createDefaultService1e.left().value();

		//////// create VFC1 (resourceDetailsVFCcomp_01) DerivedFrom COMPUTE
		//////// type add all possible informational artifacts and change state
		//////// to CERTIFY////////
		Either<Resource, RestResponse> resourceDetailsVFCcompE = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.COMPUTE,ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVFCcomp_01 = resourceDetailsVFCcompE.left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.CHEF, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.PUPPET, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG_XML, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.DG_XML, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MURANO_PKG, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsVFCcomp_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.changeComponentState(resourceDetailsVFCcomp_01, UserRoleEnum.DESIGNER,LifeCycleStatesEnum.CERTIFY, true);

		//////// create VFC2 (resourceDetailsVFCsoft_01) DerivedFrom SOFTWARE
		//////// type and change state to CERTIFY////////
		Either<Resource, RestResponse> resourceDetailsVFCsoftE = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VFC, NormativeTypesEnum.SOFTWARE_COMPONENT,ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVFCsoft_01 = resourceDetailsVFCsoftE.left().value();
		AtomicOperationUtils.changeComponentState(resourceDetailsVFCsoft_01, UserRoleEnum.DESIGNER,LifeCycleStatesEnum.CERTIFY, true);

		//////// create CP1 (resourceDetailsVFCsoft_01) DerivedFrom PORT type
		//////// and change state to CHECKIN////////
		Either<Resource, RestResponse> resourceDetailsCP_01e = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.CP, NormativeTypesEnum.PORT,ResourceCategoryEnum.GENERIC_DATABASE, UserRoleEnum.DESIGNER, true);
		resourceDetailsCP_01 = resourceDetailsCP_01e.left().value();
		AtomicOperationUtils.changeComponentState(resourceDetailsCP_01, UserRoleEnum.DESIGNER,LifeCycleStatesEnum.CHECKIN, true);

		//////// create VL1 (resourceDetailsVFCsoft_01) DerivedFrom NETWORK type
		//////// and change state to CERTIFY////////
		Either<Resource, RestResponse> resourceDetailsVL_01e = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VL, NormativeTypesEnum.NETWORK,ResourceCategoryEnum.GENERIC_NETWORK_ELEMENTS, UserRoleEnum.DESIGNER, true);
		resourceDetailsVL_01 = resourceDetailsVL_01e.left().value();
		AtomicOperationUtils.changeComponentState(resourceDetailsVL_01, UserRoleEnum.DESIGNER,LifeCycleStatesEnum.CERTIFY, true);

		//////// create VF1 (resourceDetailsVFCcomp_01) DerivedFrom COMPUTE type
		//////// add all possible deployment and informational artifacts
		//////// //////////
		Either<Resource, RestResponse> resourceDetailsVF_01e = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT,ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF_01 = resourceDetailsVF_01e.left().value();
		ArtifactDefinition heatArtifact = AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT, resourceDetailsVF_01, UserRoleEnum.DESIGNER, true, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_VOL, resourceDetailsVF_01,UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.HEAT_NET, resourceDetailsVF_01,UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsVF_01, UserRoleEnum.DESIGNER,true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.CHEF, resourceDetailsVF_01, UserRoleEnum.DESIGNER,false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.PUPPET, resourceDetailsVF_01, UserRoleEnum.DESIGNER,false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG, resourceDetailsVF_01, UserRoleEnum.DESIGNER,false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.YANG_XML, resourceDetailsVF_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.DG_XML, resourceDetailsVF_01, UserRoleEnum.DESIGNER,false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MURANO_PKG, resourceDetailsVF_01,UserRoleEnum.DESIGNER, false, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.OTHER, resourceDetailsVF_01, UserRoleEnum.DESIGNER,false, true);

		//////// Add VFC1 VFC2 CP and VL to VF container /////////////
		resourceDetailsVFC1compIns1 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCcomp_01, resourceDetailsVF_01,UserRoleEnum.DESIGNER, true).left().value();
		resourceDetailsVFC1softIns1 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVFCsoft_01, resourceDetailsVF_01,UserRoleEnum.DESIGNER, true).left().value();
		resourceDetailsCP1ins_01 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsCP_01,resourceDetailsVF_01, UserRoleEnum.DESIGNER, true).left().value();
		resourceDetailsVL1ins_01 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVL_01,resourceDetailsVF_01, UserRoleEnum.DESIGNER, true).left().value();

		//////// associate cp-vl vl-vfcComp and vfcComp-vfcSoft////////
		resourceDetailsVF_01 = AtomicOperationUtils.getResourceObject(resourceDetailsVF_01, UserRoleEnum.DESIGNER);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF_01, resourceDetailsCP1ins_01,resourceDetailsVL1ins_01, AssocType.LINKABLE.getAssocType(), UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF_01, resourceDetailsCP1ins_01,resourceDetailsVFC1compIns1, AssocType.BINDABLE.getAssocType(), UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF_01, resourceDetailsVFC1compIns1,resourceDetailsVFC1softIns1, AssocType.NODE.getAssocType(), UserRoleEnum.DESIGNER, true);

		//////// download all VF1 artifacts////////
		Collection<ArtifactDefinition> artifacts = resourceDetailsVF_01.getDeploymentArtifacts().values();
		List<String> collect = artifacts.stream().filter(p -> p.checkEsIdExist() == true).map(p -> p.getUniqueId()).collect(Collectors.toList());
		artifacts.stream().filter(p -> p.checkEsIdExist() == true).map(p -> p.getUniqueId()).forEach(item -> log.debug(item));

		//////// get all VF1 artifacts////////

		Collection<List<ComponentInstanceProperty>> componentInstancesProperties = resourceDetailsVF_01.getComponentInstancesProperties().values();
		List<String> collect2 = componentInstancesProperties.stream().filter(p -> p.isEmpty() == false).flatMap(l -> l.stream()).collect(Collectors.toList()).stream().map(p -> p.getUniqueId()).collect(Collectors.toList());
		
		//////// certify VF1 - failed due to uncertified CP instance ////////
		RestResponse changeVfStateFailed = LifecycleRestUtils.changeComponentState(resourceDetailsVF_01, designer,LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		Resource resResourceDetailsVF_01 = (Resource) resourceDetailsVF_01;
		variablesAsList = Arrays.asList(resResourceDetailsVF_01.getResourceType().toString(),resourceDetailsCP_01.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name(), variablesAsList,changeVfStateFailed.getResponse());

		//////// certify resources CP1 ////////
		resourceDetailsCP_01 = AtomicOperationUtils.changeComponentState(resourceDetailsCP_01, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		//////// replace VF1 instances with new certified instances (CP1
		//////// replaced) ////////
		Either<Pair<Component, ComponentInstance>, RestResponse> changeComponentInstanceVersion = AtomicOperationUtils.changeComponentInstanceVersion(resourceDetailsVF_01, resourceDetailsCP1ins_01, resourceDetailsCP_01,UserRoleEnum.DESIGNER, true);
		resourceDetailsVF_01 = changeComponentInstanceVersion.left().value().getLeft();
		resourceDetailsCP1ins_01 = changeComponentInstanceVersion.left().value().getRight();

		//////// associate cp-vl and cp-vfc1,////////
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF_01, resourceDetailsCP1ins_01,resourceDetailsVL1ins_01, AssocType.LINKABLE.getAssocType(), UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.associate2ResourceInstances(resourceDetailsVF_01, resourceDetailsCP1ins_01,resourceDetailsVFC1compIns1, AssocType.BINDABLE.getAssocType(), UserRoleEnum.DESIGNER, true);

		/////// change VF1 state to CHECK-IN and add it as instance to service1
		/////// container
		resourceDetailsVF_01 = AtomicOperationUtils.changeComponentState(resourceDetailsVF_01, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resourceDetailsVF1ins_01 = AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceDetailsVF_01,defaultService1, UserRoleEnum.DESIGNER, true).left().value();

		//////// distribute service1 - failed due to incorrect LifeCyclestatus
		//////// ////////
		RestResponse distributeService = AtomicOperationUtils.distributeService(defaultService1, false);
		Assert.assertEquals(distributeService, null, "verification failed");

		//////// certify service1 - failed due to uncertified instances ////////
		designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
		RestResponse changeServicetStateFailed = LifecycleRestUtils.changeComponentState(defaultService1, designer,LifeCycleStatesEnum.CERTIFICATIONREQUEST);
		variablesAsList = Arrays.asList(defaultService1.getComponentType().toString().toLowerCase(),resourceDetailsVF_01.getName());
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.VALIDATED_RESOURCE_NOT_FOUND.name(), variablesAsList,changeServicetStateFailed.getResponse());

		////// change VF1 state to CERTIFIED
		resourceDetailsVF_01 = AtomicOperationUtils.changeComponentState(resourceDetailsVF_01, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		//////// replace VF1 instances with new certified instances ////////
		changeComponentInstanceVersion = AtomicOperationUtils.changeComponentInstanceVersion(defaultService1,resourceDetailsVF1ins_01, resourceDetailsVF_01, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF_01 = changeComponentInstanceVersion.left().value().getLeft();
		resourceDetailsVFC1compIns1 = changeComponentInstanceVersion.left().value().getRight();

		/////// certify service1 ////////
		defaultService1 = AtomicOperationUtils.changeComponentState(defaultService1, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		//////// distribute service1 - successfully ////////
		AtomicOperationUtils.distributeService(defaultService1, true);

		/////// create VF2 ////////
		Either<Resource, RestResponse> resourceDetailsVF_02e = AtomicOperationUtils.createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum.VF, NormativeTypesEnum.ROOT,ResourceCategoryEnum.GENERIC_INFRASTRUCTURE, UserRoleEnum.DESIGNER, true);
		resourceDetailsVF_02 = resourceDetailsVF_02e.left().value();

	}

}
