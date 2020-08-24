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

package org.onap.sdc.backend.externalApis;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceDetailedAssetStructure;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.*;
import org.onap.sdc.backend.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.testng.annotations.Test;

import java.util.ArrayList;

public class GetSpecificAssetMetadataServlet extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

//	@BeforeMethod
//	public void setup() throws Exception {
//		AtomicOperationUtils.createDefaultConsumer(true);
//	}

	// get specific asset metadata

	// Resource
	@Test // (enabled = false)
	public void getResourceAssetMetadataSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

	/*	// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*/
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		/*// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*/
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithNonCertifiedResourceInstancesSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER, true);

		// certify resource2 and add to VF(VF with resource2 0.1, 1.0 and 1.1
		// versions)
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

	/*	// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*/
	}

	/*@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesVfInSubmitForTestingSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER, true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

	*//*	// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*//*
	}*/

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesVfInStartCertificationSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,	true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		/*// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*/
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesCertifiedVFSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,	true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

	/*	// Validate audit message
		validateAudit(resourceVF, AssetTypeEnum.RESOURCES);*/
	}

	/*@Test // (enabled = false)
	public void getResourceAssetMetadataWithNonCertifiedResourceInstancesAndArtifactsSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,	true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,	true);

		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES, resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

	}*/


	// Import CSAR

	// Service
	@Test // (enabled = false)
	public void getServiceAssetMetadataSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		/*// Validate audit message
		validateAudit(service, AssetTypeEnum.SERVICES);*/
	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithResourceInstancesSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(),Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		/*// Validate audit message
		validateAudit(service, AssetTypeEnum.SERVICES);*/

	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithNonCertifiedResourceInstancesWithArtifactsSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER, true, true);
		resourceVF2 = (Resource) AtomicOperationUtils.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with 0.1, 0.1 and 1.0 RIs versions
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(), Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		/*// Validate audit message
		validateAudit(service, AssetTypeEnum.SERVICES);*/

	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithCertifiedResourceInstancesAndArtifactsOnRIsAndServiceSuccess()
			throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER, true, true);
		resourceVF2 = (Resource) AtomicOperationUtils.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with resourceVF 1.0(1 art), 2.0(2 art) versions and
		// resourceVF2 1.0(1 art), service 1 artifact version
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, service, UserRoleEnum.DESIGNER, true, true);
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(), Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		/*// Validate audit message
		validateAudit(service, AssetTypeEnum.SERVICES);*/

	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithResourceInstancesServiceInSubmitForTestingSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, resourceVF, UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with resourceVF 1.0(1 art), 2.0(2 art) versions and
		// resourceVF2 1.0(1 art), service 1 artifact version
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, service, UserRoleEnum.DESIGNER, true, true);
		service = (Service) AtomicOperationUtils.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		/*// Validate audit message
		validateAudit(service, AssetTypeEnum.SERVICES);*/
	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataServiceNotFound() throws Exception {

		String serviceUuid = "notExistingServiceUuid";
		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, serviceUuid);

		// Validate audit message
		ArrayList<String> variables = new ArrayList<>();
		variables.add(serviceUuid);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variables, assetResponse.getResponse());
	}

	/*private void validateAudit(Component component, AssetTypeEnum assetType) throws Exception {
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(assetType, component);
		Map<AuditingFieldsKey, String> body = new HashMap<>();
		body.put(AuditingFieldsKey.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}*/

}
