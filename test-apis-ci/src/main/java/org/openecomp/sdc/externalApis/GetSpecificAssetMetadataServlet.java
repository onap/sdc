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

package org.openecomp.sdc.externalApis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.openecomp.sdc.common.datastructure.AuditingFieldsKeysEnum;
import org.testng.annotations.Test;

public class GetSpecificAssetMetadataServlet extends ComponentBaseTest {

	@Rule
	public static TestName name = new TestName();

	public GetSpecificAssetMetadataServlet() {
		super(name, GetSpecificAssetMetadataServlet.class.getName());
	}

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithNonCertifiedResourceInstancesSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Resource resource3 = AtomicOperationUtils.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true).getLeft();

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}

	@Test // (enabled = false)
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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}

	@Test // (enabled = false)
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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
	}

	// Import CSAR

	// Service
	@Test // (enabled = false)
	public void getServiceAssetMetadataSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES, service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);

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

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		Map <AuditingFieldsKeysEnum, String> body = new HashMap<>();
        body.put(AuditingFieldsKeysEnum.AUDIT_RESOURCE_URL, expectedAssetListAudit.getRESOURCE_URL());
        AuditValidationUtils.validateExternalAudit(expectedAssetListAudit, AuditingActionEnum.GET_ASSET_METADATA.getName(), body);
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

}
