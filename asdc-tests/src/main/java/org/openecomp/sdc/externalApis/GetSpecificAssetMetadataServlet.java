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
import java.util.List;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.dao.api.ActionStatus;
import org.openecomp.sdc.be.datatypes.enums.AssetTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.resources.data.auditing.AuditingActionEnum;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDetailedAssetStructure;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.expected.ExpectedExternalAudit;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.AssetRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.validation.AuditValidationUtils;
import org.openecomp.sdc.ci.tests.utils.validation.ErrorValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetSpecificAssetMetadataServlet extends ComponentBaseTest {
	private static Logger log = LoggerFactory.getLogger(GetAssetServlet.class.getName());

	@Rule
	public static TestName name = new TestName();

	public GetSpecificAssetMetadataServlet() {
		super(name, GetSpecificAssetMetadataServlet.class.getName());
	}

	@BeforeMethod
	public void setup() throws Exception {
		AtomicOperationUtils.createDefaultConsumer(true);
	}

	// get specific asset metadata

	// Resource
	@Test // (enabled = false)
	public void getResourceAssetMetadataSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test  (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(
				ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithNonCertifiedResourceInstancesSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Resource resource3 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils
				.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,
				true);

		// certify resource2 and add to VF(VF with resource2 0.1, 1.0 and 1.1
		// versions)
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(
				ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesVfInSubmitForTestingSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils
				.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,
				true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesVfInStartCertificationSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils
				.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,
				true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.STARTCERTIFICATION, true)
				.getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithResourceInstancesCertifiedVFSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		Resource resource3 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils
				.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,
				true);

		// certify resource2 and add to VF(VF with resource2 1.0, 2.0 and 3.0
		// versions)
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getResourceAssetMetadataWithNonCertifiedResourceInstancesAndArtifactsSuccess() throws Exception {

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		Resource resource2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource2 = (Resource) AtomicOperationUtils
				.changeComponentState(resource2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		Resource resource3 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VFC, UserRoleEnum.DESIGNER, true).left().value();
		resource3 = (Resource) AtomicOperationUtils
				.changeComponentState(resource3, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFICATIONREQUEST, true)
				.getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource2, resourceVF, UserRoleEnum.DESIGNER,
				true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resource3, resourceVF, UserRoleEnum.DESIGNER,
				true);

		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER,
				true, true);
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF, UserRoleEnum.DESIGNER, true,
				true);
		resourceVF = ResponseParser.parseToObjectUsingMapper(
				ResourceRestUtils.getResource(resourceVF.getUniqueId()).getResponse(), Resource.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.RESOURCES,
				resourceVF.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ResourceDetailedAssetStructure resourceAssetMetadata = AssetRestUtils.getResourceAssetMetadata(assetResponse);
		AssetRestUtils.resourceMetadataValidatior(resourceAssetMetadata, resourceVF, AssetTypeEnum.RESOURCES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.RESOURCES, resourceVF);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	// Import CSAR

	// Service
	@Test // (enabled = false)
	public void getServiceAssetMetadataSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithResourceInstancesSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();
		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils
				.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(),
				Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);

	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithNonCertifiedResourceInstancesWithArtifactsSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF2 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with 0.1, 0.1 and 1.0 RIs versions
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils
				.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(),
				Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);

	}

	@Test  (enabled = false)
	public void getServiceAssetMetadataWithCertifiedResourceInstancesAndArtifactsOnRIsAndServiceSuccess()
			throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF2 = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, resourceVF,
				UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with resourceVF 1.0(1 art), 2.0(2 art) versions and
		// resourceVF2 1.0(1 art), service 1 artifact version
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, service,
				UserRoleEnum.DESIGNER, true, true);
		service = ResponseParser.parseToObjectUsingMapper(ServiceRestUtils
				.getService(service.getUniqueId(), ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER)).getResponse(),
				Service.class);

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);

	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataWithResourceInstancesServiceInSubmitForTestingSuccess() throws Exception {

		Service service = AtomicOperationUtils.createDefaultService(UserRoleEnum.DESIGNER, true).left().value();

		Resource resourceVF = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.VENDOR_LICENSE, resourceVF, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		Resource resourceVF2 = AtomicOperationUtils
				.createResourceByType(ResourceTypeEnum.VF, UserRoleEnum.DESIGNER, true).left().value();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.APPC_CONFIG, resourceVF2, UserRoleEnum.DESIGNER,
				true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF2, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF2, service, UserRoleEnum.DESIGNER,
				true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, resourceVF,
				UserRoleEnum.DESIGNER, true, true);
		resourceVF = (Resource) AtomicOperationUtils
				.changeComponentState(resourceVF, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		AtomicOperationUtils.addComponentInstanceToComponentContainer(resourceVF, service, UserRoleEnum.DESIGNER, true);

		// service with resourceVF 1.0(1 art), 2.0(2 art) versions and
		// resourceVF2 1.0(1 art), service 1 artifact version
		AtomicOperationUtils.uploadArtifactByType(ArtifactTypeEnum.MODEL_INVENTORY_PROFILE, service,
				UserRoleEnum.DESIGNER, true, true);
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				service.getUUID());
		BaseRestUtils.checkSuccess(assetResponse);

		ServiceDetailedAssetStructure serviceAssetMetadata = AssetRestUtils.getServiceAssetMetadata(assetResponse);
		AssetRestUtils.serviceMetadataValidatior(serviceAssetMetadata, service, AssetTypeEnum.SERVICES);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultAssetMetadataAudit(AssetTypeEnum.SERVICES, service);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_ASSET_METADATA.getName(), null);
	}

	@Test // (enabled = false)
	public void getServiceAssetMetadataServiceNotFound() throws Exception {

		String serviceUuid = "notExistingServiceUuid";
		RestResponse assetResponse = AssetRestUtils.getAssetMetadataByAssetTypeAndUuid(true, AssetTypeEnum.SERVICES,
				serviceUuid);

		// Validate audit message
		ArrayList<String> variables = new ArrayList<>();
		variables.add(serviceUuid);
		ErrorValidationUtils.checkBodyResponseOnError(ActionStatus.SERVICE_NOT_FOUND.name(), variables,
				assetResponse.getResponse());
	}

	// Add to service VF instance imported from CSAR

	@Test
	public void getFilteredResourceAssetSuccess() throws Exception {

		List<String> expectedAssetNamesList = new ArrayList<>();

		ResourceReqDetails resourceDetails = ElementFactory
				.getDefaultResource(ResourceCategoryEnum.APPLICATION_L4_APP_SERVER);
		resourceDetails.setName("ciResource1");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		Resource resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		expectedAssetNamesList.add(resource.getName());

		resourceDetails = ElementFactory.getDefaultResource(ResourceCategoryEnum.APPLICATION_L4_BORDER);
		resourceDetails.setName("ciResource2");
		resourceDetails.setResourceType(ResourceTypeEnum.VFC.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

		resourceDetails = ElementFactory.getDefaultResource(ResourceCategoryEnum.GENERIC_INFRASTRUCTURE);
		resourceDetails.setName("ciResource3");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		resourceDetails = ElementFactory.getDefaultResource(ResourceCategoryEnum.APPLICATION_L4_FIREWALL);
		resourceDetails.setName("ciResource4");
		resourceDetails.setResourceType(ResourceTypeEnum.VF.name());
		createResource = ResourceRestUtils.createResource(resourceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createResource);
		resource = ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		resource = (Resource) AtomicOperationUtils
				.changeComponentState(resource, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		expectedAssetNamesList.add(resource.getName());

		log.debug("4 resources created");
		String query = "category=Application%20L4%2B";
		RestResponse assetResponse = AssetRestUtils.getFilteredComponentList(AssetTypeEnum.RESOURCES, query);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ResourceAssetStructure> resourceAssetList = AssetRestUtils.getResourceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getResourceNamesList(resourceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		AssetRestUtils.checkResourceTypeInObjectList(resourceAssetList, ResourceTypeEnum.VF);

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultFilteredAssetListAudit(AssetTypeEnum.RESOURCES, "?" + query);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), null);

	}

	@Test
	public void getFilteredResourceAssetCategoryNotFound() throws Exception {

		String query = "category=Application%20L3%2B";
		RestResponse assetResponse = AssetRestUtils.getFilteredComponentList(AssetTypeEnum.RESOURCES, query);
		BaseRestUtils.checkErrorResponse(assetResponse, ActionStatus.COMPONENT_CATEGORY_NOT_FOUND, "resource",
				"category", "Application L3+");

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getFilteredAssetListAuditCategoryNotFound(AssetTypeEnum.RESOURCES, "?" + query, "Application L3+");
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), null);

	}

	@Test
	public void getFilteredServiceAssetSuccess() throws Exception {

		List<String> expectedAssetNamesList = new ArrayList<>();
		ArtifactReqDetails artifactDetails = ElementFactory.getArtifactByType(ArtifactTypeEnum.OTHER,
				ArtifactTypeEnum.OTHER, true);

		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService();
		serviceDetails.setName("ciService1");
		RestResponse createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		Service service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);

		serviceDetails.setName("ciService2");
		createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		RestResponse addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(
				artifactDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();

		ServiceReqDetails certifyService = new ServiceReqDetails(service);
		LifecycleRestUtils.changeDistributionStatus(certifyService, certifyService.getVersion(),
				ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR), null,
				DistributionStatusEnum.DISTRIBUTION_APPROVED);
		AtomicOperationUtils.distributeService(service, false);
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService3");
		createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		addInformationalArtifactToService = ArtifactRestUtils.addInformationalArtifactToService(artifactDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER), service.getUniqueId());
		BaseRestUtils.checkSuccess(addInformationalArtifactToService);
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CERTIFY, true).getLeft();
		certifyService = new ServiceReqDetails(service);
		LifecycleRestUtils.changeDistributionStatus(certifyService, certifyService.getVersion(),
				ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR), null,
				DistributionStatusEnum.DISTRIBUTION_APPROVED);
		AtomicOperationUtils.distributeService(service, false);
		expectedAssetNamesList.add(service.getName());

		serviceDetails.setName("ciService4");
		createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();

		serviceDetails.setName("ciService5");
		createService = ServiceRestUtils.createService(serviceDetails,
				ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER));
		BaseRestUtils.checkCreateResponse(createService);
		service = ResponseParser.parseToObjectUsingMapper(createService.getResponse(), Service.class);
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKIN, true).getLeft();
		service = (Service) AtomicOperationUtils
				.changeComponentState(service, UserRoleEnum.DESIGNER, LifeCycleStatesEnum.CHECKOUT, true).getLeft();

		String query = "distributionStatus=Distributed";
		RestResponse assetResponse = AssetRestUtils.getFilteredComponentList(AssetTypeEnum.SERVICES, query);
		BaseRestUtils.checkSuccess(assetResponse);

		List<ServiceAssetStructure> resourceAssetList = AssetRestUtils.getServiceAssetList(assetResponse);
		List<String> getActualAssetNamesList = AssetRestUtils.getServiceNamesList(resourceAssetList);
		Utils.compareArrayLists(getActualAssetNamesList, expectedAssetNamesList, "Element");

		// Validate audit message
		ExpectedExternalAudit expectedAssetListAudit = ElementFactory
				.getDefaultFilteredAssetListAudit(AssetTypeEnum.SERVICES, "?" + query);
		AuditValidationUtils.validateExternalAudit(expectedAssetListAudit,
				AuditingActionEnum.GET_FILTERED_ASSET_LIST.getName(), null);

	}

}
