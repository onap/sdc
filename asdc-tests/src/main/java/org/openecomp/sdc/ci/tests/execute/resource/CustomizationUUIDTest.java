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

package org.openecomp.sdc.ci.tests.execute.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.Test;

public class CustomizationUUIDTest extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public CustomizationUUIDTest() {
		super(name, CustomizationUUIDTest.class.getName());
	}

	@Test(enabled = true)
	public void resourceCustomUUIDTestUpdateMeta() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource in checkin status
		Resource resource1 = createVfFromCSAR(sdncModifierDetails, "csar_1");

		RestResponse checkinState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// create instance 1
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(resource1);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		ComponentInstance ci1 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci1.getCustomizationUUID());
		String ci1CustUUID = ci1.getCustomizationUUID();

		// get service with 1 instance
		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());

		// change name of instance 1 and check custom UUID
		String newCi1Name = "newCi1Name";
		ci1.setName(newCi1Name);
		RestResponse updateComponentInstance = ComponentInstanceRestUtils.updateComponentInstance(ci1, sdncModifierDetails, service.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(updateComponentInstance);
		ComponentInstance ci1AfterChange = ResponseParser.parseToObjectUsingMapper(updateComponentInstance.getResponse(), ComponentInstance.class);

		// must be different
		assertFalse(ci1.getCustomizationUUID().equals(ci1AfterChange.getCustomizationUUID()));

		// change position of instance 1 and check UUID
		ci1.setPosX("151");
		ci1.setPosY("20");
		updateComponentInstance = ComponentInstanceRestUtils.updateComponentInstance(ci1, sdncModifierDetails, service.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(updateComponentInstance);
		ci1AfterChange = ResponseParser.parseToObjectUsingMapper(updateComponentInstance.getResponse(), ComponentInstance.class);
		// must be same
		assertTrue(ci1.getCustomizationUUID().equals(ci1AfterChange.getCustomizationUUID()));
	}

	@Test(enabled = true)
	public void resourceCustomUUIDPropertyTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource
		Resource resource1 = createVfFromCSAR(sdncModifierDetails, "csar_1");

		RestResponse checkinState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// 2 create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// create instance
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(resource1);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		ComponentInstance ci1 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci1.getCustomizationUUID());
		String ci1CustUUID = ci1.getCustomizationUUID();

		// get service with 1 instance
		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());

		// instance property values
		Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = service.getComponentInstancesProperties();
		assertNotNull(componentInstancesProperties);
		List<ComponentInstanceProperty> listProps = componentInstancesProperties.get(ci1.getUniqueId());
		assertNotNull(listProps);

		ComponentInstanceProperty updatedInstanceProperty = null;
		for (ComponentInstanceProperty cip : listProps) {
			if (cip.getType().equals("string")) {
				updatedInstanceProperty = cip;
				break;
			}
		}
		assertNotNull(updatedInstanceProperty);
		updatedInstanceProperty.setValue("newValue");

		RestResponse updatePropRes = ComponentInstanceRestUtils.updatePropertyValueOnResourceInstance(service, ci1, sdncModifierDetails, updatedInstanceProperty);
		BaseRestUtils.checkSuccess(updatePropRes);

		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		ComponentInstance ciAfterUpdateProp = componentInstances.get(0);

		assertFalse(ci1.getCustomizationUUID().equals(ciAfterUpdateProp.getCustomizationUUID()));
	}

	@Test(enabled = true)
	public void resourceCustomUUIDChangeVersionTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource in checkin status
		Resource resource1 = createVfFromCSAR(sdncModifierDetails, "csar_1");

		RestResponse checkinState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// create instance 1
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(resource1);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		ComponentInstance ci1 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci1.getCustomizationUUID());
		String ci1CustUUID = ci1.getCustomizationUUID();

		// create 0.2 version of resource( check out and check in)
		RestResponse checkoutState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKOUT);
		BaseRestUtils.checkSuccess(checkoutState);
		resource1 = ResponseParser.parseToObjectUsingMapper(checkoutState.getResponse(), Resource.class);
		assertNotNull(resource1);

		checkinState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// change version of instance
		RestResponse changeComponentInstanceVersion = ComponentInstanceRestUtils.changeComponentInstanceVersion(service, ci1, resource1, sdncModifierDetails);
		BaseRestUtils.checkSuccess(changeComponentInstanceVersion);
		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());
		assertFalse(ci1CustUUID.equals(componentInstances.get(0).getCustomizationUUID()));

	}

	@Test(enabled = true)
	public void resourceCustomUUIDChangeArtifactsTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource in checkin status
		Resource resource1 = createVfFromCSAR(sdncModifierDetails, "csar_1");

		RestResponse checkinState = LifecycleRestUtils.changeComponentState(resource1, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// create instance 1
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(resource1);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		ComponentInstance ci1 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci1.getCustomizationUUID());
		String lastUUID = ci1.getCustomizationUUID();

		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(1, componentInstances.size());
		ComponentInstance ci = componentInstances.get(0);
		Map<String, ArtifactDefinition> deploymentArtifacts = ci.getDeploymentArtifacts();
		assertNotNull(deploymentArtifacts);
		// find artifact for update
		ArtifactDefinition artifactForUpdate = null;
		for (ArtifactDefinition ad : deploymentArtifacts.values()) {
			if (ad.getArtifactType().equals("HEAT_ENV")) {
				artifactForUpdate = ad;
				break;
			}
		}

		assertNotNull(artifactForUpdate);
		// update heat env on instance
		RestResponse updateArtifact = ArtifactRestUtils.updateDeploymentArtifactToRI(artifactForUpdate, sdncModifierDetails, ci.getUniqueId(), service.getUniqueId());
		BaseRestUtils.checkSuccess(updateArtifact);
		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertFalse(lastUUID.equals(componentInstances.get(0).getCustomizationUUID()));
		lastUUID = componentInstances.get(0).getCustomizationUUID();

		// add artifact to instance
		ArtifactReqDetails artifactDetails = ElementFactory.getDefaultArtifact();
		RestResponse addArtifactToResourceInstance = ArtifactRestUtils.addArtifactToResourceInstance(artifactDetails, sdncModifierDetails, ci.getUniqueId(), service.getUniqueId());
		BaseRestUtils.checkSuccess(addArtifactToResourceInstance);
		ArtifactDefinition artifactDef = ResponseParser.parseToObjectUsingMapper(addArtifactToResourceInstance.getResponse(), ArtifactDefinition.class);
		assertNotNull(artifactDef);

		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);

		componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertFalse(lastUUID.equals(componentInstances.get(0).getCustomizationUUID()));
		lastUUID = componentInstances.get(0).getCustomizationUUID();

		//update artifact
		//not supported now!!!!!
//		artifactDef.setDescription("new description");
//		RestResponse updateArtifactRes = ArtifactRestUtils.updateArtifactToResourceInstance(artifactDef, sdncModifierDetails, ci.getUniqueId(), service.getUniqueId());
//		BaseRestUtils.checkSuccess(updateArtifactRes);
//		artifactDef = ResponseParser.parseToObjectUsingMapper(addArtifactToResourceInstance.getResponse(), ArtifactDefinition.class);
//		assertNotNull(artifactDef);
//		
//		getService = ServiceRestUtils.getService(service.getUniqueId());
//		BaseRestUtils.checkSuccess(getService);
//		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
//
//		componentInstances = service.getComponentInstances();
//		assertNotNull(componentInstances);
//		assertFalse(lastUUID.equals(componentInstances.get(0).getCustomizationUUID()));
//		lastUUID = componentInstances.get(0).getCustomizationUUID();
//		
//		//delete artifact
//		RestResponse deleteArtifactRes = ArtifactRestUtils.deleteArtifactFromResourceInstance (artifactDef, sdncModifierDetails, ci.getUniqueId(), service.getUniqueId());
//		BaseRestUtils.checkSuccess(deleteArtifactRes);
//		getService = ServiceRestUtils.getService(service.getUniqueId());
//		BaseRestUtils.checkSuccess(getService);
//		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
//
//		componentInstances = service.getComponentInstances();
//		assertNotNull(componentInstances);
//		assertFalse(lastUUID.equals(componentInstances.get(0).getCustomizationUUID()));
	}

	@Test(enabled = true)
	public void resourceCustomUUIDRelationTest() throws Exception {
		User sdncModifierDetails = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);

		// create resource in checkin status
		Resource resource = createVfFromCSAR(sdncModifierDetails, "csar_1");

		RestResponse checkinState = LifecycleRestUtils.changeComponentState(resource, sdncModifierDetails, LifeCycleStatesEnum.CHECKIN);
		BaseRestUtils.checkSuccess(checkinState);

		// create service
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciNewtestservice1", ServiceCategoriesEnum.MOBILITY, sdncModifierDetails.getUserId());
		RestResponse createServiceResponse = ServiceRestUtils.createService(serviceDetails, sdncModifierDetails);
		ResourceRestUtils.checkCreateResponse(createServiceResponse);
		Service service = ResponseParser.parseToObjectUsingMapper(createServiceResponse.getResponse(), Service.class);

		// create instance 1
		ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(resource);
		RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);
		
		ComponentInstance ci1 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci1.getCustomizationUUID());
		String ci1LastUUID = ci1.getCustomizationUUID();

		// create instance 2
		createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, sdncModifierDetails, service);
		ResourceRestUtils.checkCreateResponse(createComponentInstance);

		ComponentInstance ci2 = ResponseParser.parseToObjectUsingMapper(createComponentInstance.getResponse(), ComponentInstance.class);
		assertNotNull(ci2.getCustomizationUUID());
		String ci2LastUUID = ci2.getCustomizationUUID();
		
		// get service with 2 instances
		RestResponse getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		List<ComponentInstance> componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		assertEquals(2, componentInstances.size());

		ComponentInstance ciFrom = componentInstances.get(0);
		ComponentInstance ciTo = componentInstances.get(1);
		
		Map<String, List<RequirementDefinition>> requirements = ciFrom.getRequirements();
		assertNotNull(requirements);
		List<RequirementDefinition> listReq = requirements.get("tosca.capabilities.network.Bindable");
		assertNotNull(listReq);
		RequirementDefinition req = listReq.get(0);
		
		
		Map<String, List<CapabilityDefinition>> capabilities = ciTo.getCapabilities();
		assertNotNull(capabilities);
		List<CapabilityDefinition> listCap = capabilities.get("tosca.capabilities.network.Bindable");
		assertNotNull(listCap);
		CapabilityDefinition cap = listCap.get(0);
			
		List<CapabilityDefinition> capList = new ArrayList<>();
		capList.add(cap);
		List<RequirementDefinition> reqList = new ArrayList<>();
		reqList.add(req);
		
		RequirementCapabilityRelDef relation = ElementFactory.getReqCapRelation(ciFrom.getUniqueId(),ciTo.getUniqueId(), req.getOwnerId(), cap.getOwnerId(), cap.getType(), req.getName(), capList, reqList );
		
		RestResponse associateInstances = ComponentInstanceRestUtils.associateInstances(relation, sdncModifierDetails,service.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		
		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		
		for ( ComponentInstance ci : componentInstances){
			if ( ci.getUniqueId().equals(ci1.getUniqueId()) ){
				assertFalse( ci1LastUUID.equals(ci.getCustomizationUUID()) );
				ci1LastUUID = ci.getCustomizationUUID();
			}else{
				assertFalse( ci2LastUUID.equals(ci.getCustomizationUUID()) );
				ci2LastUUID = ci.getCustomizationUUID();
			}
		}
		associateInstances = ComponentInstanceRestUtils.dissociateInstances(relation, sdncModifierDetails,service.getUniqueId(), ComponentTypeEnum.SERVICE);
		ResourceRestUtils.checkSuccess(associateInstances);
		
		getService = ServiceRestUtils.getService(service.getUniqueId());
		BaseRestUtils.checkSuccess(getService);
		service = ResponseParser.parseToObjectUsingMapper(getService.getResponse(), Service.class);
		componentInstances = service.getComponentInstances();
		assertNotNull(componentInstances);
		
		for ( ComponentInstance ci : componentInstances){
			if ( ci.getUniqueId().equals(ci1.getUniqueId()) ){
				assertFalse( ci1LastUUID.equals(ci.getCustomizationUUID()) );
			}else{
				assertFalse( ci2LastUUID.equals(ci.getCustomizationUUID()) );
			}
		}
	}
}
