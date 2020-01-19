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
 */

package org.openecomp.sdc.be.ecomp.converters;

import fj.data.Either;
import mockit.Deencapsulation;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.externalapi.servlet.representation.*;
import org.openecomp.sdc.be.model.*;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.openecomp.sdc.exception.ResponseFormat;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AssetMetadataConverterTest {

	private AssetMetadataConverter createTestSubject() {
		return new AssetMetadataConverter();
	}

	@Test
	public void testConvertToAssetMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		List<? extends Component> componentList = null;
		String serverBaseURL = "";
		boolean detailed = false;
		Either<List<? extends AssetMetadata>, ResponseFormat> result;

		// test 1
		testSubject = createTestSubject();
		componentList = null;
		result = testSubject.convertToAssetMetadata(componentList, serverBaseURL, detailed);
	}

	@Test
	public void testConvertToSingleAssetMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		Resource component = new Resource();
		String serverBaseURL = "";
		boolean detailed = false;
		Either<? extends AssetMetadata, ResponseFormat> result;
		component.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		component.setComponentType(ComponentTypeEnum.RESOURCE);
		// default test
		testSubject = createTestSubject();
		result = testSubject.convertToSingleAssetMetadata(component, serverBaseURL, detailed);
	}

	@Test
	public void testConvertToMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		String serverBaseURL = "";
		boolean detailed = false;
		Resource curr = new Resource();
		Either<? extends AssetMetadata, ResponseFormat> result;
		curr.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		curr.setComponentType(ComponentTypeEnum.RESOURCE);
		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToMetadata", ComponentTypeEnum.RESOURCE, serverBaseURL,
				detailed, curr);
	}

	@Test
	public void testGenerateResourceMeatdata() throws Exception {
		AssetMetadataConverter testSubject;
		String serverBaseURL = "";
		Resource curr = new Resource();
		Either<? extends AssetMetadata, ResponseFormat> result;
		curr.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);
		curr.setComponentType(ComponentTypeEnum.RESOURCE);
		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "generateResourceMeatdata", serverBaseURL, true, curr);
	}

	@Test
	public void testCreateMetadaObject() throws Exception {
		AssetMetadataConverter testSubject;
		AssetMetadata result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "createMetadaObject", true, ComponentTypeEnum.RESOURCE);
	}

	@Test
	public void testGenerateServiceMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		String serverBaseURL = "";
		boolean detailed = false;
		Service curr = new Service();
		curr.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		curr.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED);

		Either<? extends AssetMetadata, ResponseFormat> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "generateServiceMetadata", serverBaseURL, detailed, curr);
	}

	@Test
	public void testConvertToAsset() throws Exception {
		AssetMetadataConverter testSubject;
		ResourceAssetMetadata asset = new ResourceAssetMetadata();
		Resource component = new Resource();
		String serverBaseURL = "";
		ResourceAssetMetadata result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToAsset", asset, component, serverBaseURL, true);
	}

	@Test
	public void testConvertToResourceMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		ResourceAssetMetadata assetToPopulate = new ResourceAssetMetadata();
		Resource resource = new Resource();
		String serverBaseURL = "";
		boolean detailed = false;
		ResourceAssetMetadata result;
		resource.setLifecycleState(LifecycleStateEnum.NOT_CERTIFIED_CHECKIN);

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToResourceMetadata", assetToPopulate, resource,
				serverBaseURL, true);
	}

	@Test
	public void testConvertToServiceAssetMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		ServiceAssetMetadata assetToPopulate = new ServiceAssetMetadata();
		Service service = new Service();
		service.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED);
		String serverBaseURL = "";
		boolean detailed = false;
		ServiceAssetMetadata result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToServiceAssetMetadata", assetToPopulate, service,
				serverBaseURL, true);
	}

	@Test
	public void testConvertToResourceDetailedMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		ResourceAssetDetailedMetadata assetToPopulate = new ResourceAssetDetailedMetadata();
		Resource resource = new Resource();
		String serverBaseURL = "";
		Either<ResourceAssetDetailedMetadata, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToResourceDetailedMetadata", assetToPopulate, resource,
				serverBaseURL);
	}

	@Test
	public void testConvertToServiceDetailedMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		ServiceAssetDetailedMetadata assetToPopulate = new ServiceAssetDetailedMetadata();
		Service service = new Service();
		Either<ServiceAssetDetailedMetadata, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToServiceDetailedMetadata", assetToPopulate, service);
	}

	@Test
	public void testPopulateResourceWithArtifacts() throws Exception {
		AssetMetadataConverter testSubject;
		ResourceAssetDetailedMetadata asset = new ResourceAssetDetailedMetadata();
		Resource resource = new Resource();
		Map<String, ArtifactDefinition> artifacts = new HashMap<>();
		ResourceAssetDetailedMetadata result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "populateResourceWithArtifacts", asset, resource, artifacts); 
	}

	@Test
	public void testPopulateServiceWithArtifacts() throws Exception {
		AssetMetadataConverter testSubject;
		ServiceAssetDetailedMetadata asset = new ServiceAssetDetailedMetadata();
		Service service = new Service();
		Map<String, ArtifactDefinition> artifacts = new HashMap<>();
		ServiceAssetDetailedMetadata result;
		service.setLifecycleState(LifecycleStateEnum.CERTIFIED);
		service.setDistributionStatus(DistributionStatusEnum.DISTRIBUTED);
		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "populateServiceWithArtifacts",
				asset, Service.class, artifacts);
	}

	@Test
	public void testPopulateAssetWithArtifacts() throws Exception {
		AssetMetadataConverter testSubject;
		Resource component = new Resource();
		Map<String, ArtifactDefinition> artifacts = new HashMap<>();
		List<ArtifactMetadata> result;

		// test 1
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "populateAssetWithArtifacts", component, artifacts);
		Assert.assertEquals(null, result);
	}

	@Test
	public void testConvertToArtifactMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		ArtifactDefinition artifact = new ArtifactDefinition();
		artifact.setArtifactGroupType(ArtifactGroupTypeEnum.DEPLOYMENT);
		String componentType = "";
		String componentUUID = "";
		String resourceInstanceName = "";
		ArtifactMetadata result;

		// test 1
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToArtifactMetadata", artifact, componentType,
				componentUUID, resourceInstanceName);
	}

	@Test
	public void testConvertToResourceInstanceMetadata() throws Exception {
		AssetMetadataConverter testSubject;
		List<ComponentInstance> componentInstances = new LinkedList<>();
		String componentType = "";
		String componentUUID = "";
		Either<List<ResourceInstanceMetadata>, StorageOperationStatus> result;

		// default test
		testSubject = createTestSubject();
		result = Deencapsulation.invoke(testSubject, "convertToResourceInstanceMetadata",
				new Object[] { componentInstances, componentType, componentUUID });
	}
}
