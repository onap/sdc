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

package org.openecomp.sdc.ci.tests.utils.general;

import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.DistributionStatusEnum;
import org.openecomp.sdc.be.model.Product;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ArtifactReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.DistributionMonitorObject;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.PropertyReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceDistributionStatus;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ArtifactTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.PropertyTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.DistributionUtils;
import org.openecomp.sdc.ci.tests.utils.Utils;
import org.openecomp.sdc.ci.tests.utils.rest.ArtifactRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ConsumerRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.LifecycleRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.PropertyRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.common.api.ArtifactGroupTypeEnum;
import org.testng.SkipException;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;

import fj.data.Either;

public final class AtomicOperationUtils {

	static final String basicAuthentication = "Basic Y2k6MTIzNDU2";
	
	private AtomicOperationUtils() {
		throw new UnsupportedOperationException();
	}

	// *********** RESOURCE ****************
	/**
	 * Import a vfc From tosca file
	 * 
	 * @param filePath
	 * @param fileName
	 * @return
	 * @throws IOException
	 * @throws JSONException
	 */
	public static Either<Resource, RestResponse> importResource(String filePath, String fileName) {
		try {
			User designer = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
			ImportReqDetails importReqDetails = ElementFactory.getDefaultImportResource("ciTmpVFC");
			importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(importReqDetails, filePath, fileName);
			RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, designer, null);
			return buildResourceFromResponse(importResourceResponse);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}

	public static Either<Resource, RestResponse> createResourceByType(ResourceTypeEnum resourceType, UserRoleEnum userRole, Boolean validateState) {
		try {
			User defaultUser = ElementFactory.getDefaultUser(userRole);
			ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByType(resourceType, defaultUser);
			RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

			if (validateState) {
				assertTrue(resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
			}

			if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
				Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
				return Either.left(resourceResponseObject);
			}
			return Either.right(resourceResp);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}

	public static Either<Resource, RestResponse> createResourceByResourceDetails(ResourceReqDetails resourceDetails, UserRoleEnum userRole, Boolean validateState) {
		try {
			User defaultUser = ElementFactory.getDefaultUser(userRole);
			RestResponse resourceResp = ResourceRestUtils.createResource(resourceDetails, defaultUser);

			if (validateState) {
				assertTrue(resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
			}

			if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
				Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
				return Either.left(resourceResponseObject);
			}
			return Either.right(resourceResp);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}
	
	public static Either<Resource, RestResponse> createResourcesByTypeNormTypeAndCatregory(ResourceTypeEnum resourceType, NormativeTypesEnum normativeTypes, ResourceCategoryEnum resourceCategory, UserRoleEnum userRole, Boolean validateState)
			throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(resourceType, normativeTypes, resourceCategory, defaultUser);
		RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

		if (validateState) {
			assertTrue("Actual Response Code is: " + resourceResp.getErrorCode(), resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
		}

		if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			// Resource resourceResponseObject = ResponseParser
			// .convertResourceResponseToJavaObject(resourceResp.getResponse());
			Resource resourceResponseObject = ResponseParser.parseToObjectUsingMapper(resourceResp.getResponse(), Resource.class);
			return Either.left(resourceResponseObject);
		}
		return Either.right(resourceResp);
	}

	public static Either<Resource, RestResponse> createResourcesByCustomNormativeTypeAndCatregory(ResourceTypeEnum resourceType, Resource resourceNormativeType, ResourceCategoryEnum resourceCategory, UserRoleEnum userRole, Boolean validateState)
			throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ResourceReqDetails defaultResource = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(resourceType, resourceNormativeType, resourceCategory, defaultUser);
		RestResponse resourceResp = ResourceRestUtils.createResource(defaultResource, defaultUser);

		if (validateState) {
			assertTrue("actual result: " + resourceResp.getResponseMessage(), resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
		}

		if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			// Resource resourceResponseObject = ResponseParser
			// .convertResourceResponseToJavaObject(resourceResp.getResponse());
			Resource resourceResponseObject = ResponseParser.parseToObjectUsingMapper(resourceResp.getResponse(), Resource.class);
			return Either.left(resourceResponseObject);
		}
		return Either.right(resourceResp);
	}

	public static Either<Resource, RestResponse> updateResource(ResourceReqDetails resourceReqDetails, User defaultUser, Boolean validateState) {
		try {

			RestResponse resourceResp = ResourceRestUtils.updateResource(resourceReqDetails, defaultUser, resourceReqDetails.getUniqueId());

			if (validateState) {
				assertTrue(resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
			}

			if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS) {
				Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
				return Either.left(resourceResponseObject);
			}
			return Either.right(resourceResp);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}
	
	// *********** SERVICE ****************

	public static Either<Service, RestResponse> createDefaultService(UserRoleEnum userRole, Boolean validateState) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService(defaultUser);
		RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

		if (validateState) {
			assertTrue(createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
		}

		if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
			return Either.left(serviceResponseObject);
		}
		return Either.right(createServiceResp);
	}

	public static Either<Service, RestResponse> createServiceByCategory(ServiceCategoriesEnum category, UserRoleEnum userRole, Boolean validateState) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ServiceReqDetails serviceDetails = ElementFactory.getDefaultService(category, defaultUser);
		RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

		if (validateState) {
			assertTrue(createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
		}

		if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
			return Either.left(serviceResponseObject);
		}
		return Either.right(createServiceResp);
	}

	public static Either<Service, RestResponse> createCustomService(ServiceReqDetails serviceDetails, UserRoleEnum userRole, Boolean validateState) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		RestResponse createServiceResp = ServiceRestUtils.createService(serviceDetails, defaultUser);

		if (validateState) {
			assertTrue(createServiceResp.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
		}

		if (createServiceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			Service serviceResponseObject = ResponseParser.convertServiceResponseToJavaObject(createServiceResp.getResponse());
			return Either.left(serviceResponseObject);
		}
		return Either.right(createServiceResp);
	}
	// *********** PRODUCT ****************

	public static Either<Product, RestResponse> createDefaultProduct(UserRoleEnum userRole, Boolean validateState) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ProductReqDetails defaultProduct = ElementFactory.getDefaultProduct();
		RestResponse createProductResp = ProductRestUtils.createProduct(defaultProduct, defaultUser);

		if (validateState) {
			assertTrue(createProductResp.getErrorCode() == ProductRestUtils.STATUS_CODE_CREATED);
		}

		if (createProductResp.getErrorCode() == ProductRestUtils.STATUS_CODE_CREATED) {
			Product productResponseJavaObject = ResponseParser.convertProductResponseToJavaObject(createProductResp.getResponse());
			return Either.left(productResponseJavaObject);
		}
		return Either.right(createProductResp);
	}

	// public static ComponentReqDetails
	// convertCompoentToComponentReqDetails(Component component){
	//
	// ComponentReqDetails componentReqDetails =
	// ElementFactory.getDefaultService();
	// componentReqDetails.setName(component.getName());
	// componentReqDetails.setDescription(component.getDescription());
	// componentReqDetails.setTags(component.getTags());
	// componentReqDetails.setContactId(component.getContactId());
	// componentReqDetails.setIcon(component.getIcon());
	// componentReqDetails.setUniqueId(component.getUniqueId());
	// componentReqDetails.setCreatorUserId(component.getCreatorUserId());
	// componentReqDetails.setCreatorFullName(component.getCreatorFullName());
	// componentReqDetails.setLastUpdaterUserId(component.getLastUpdaterUserId());
	// componentReqDetails.setLastUpdaterFullName(component.getLastUpdaterFullName());
	// componentReqDetails.setCreationDate(component.getCreationDate());
	// componentReqDetails.setLastUpdateDate(component.getLastUpdateDate());
	// componentReqDetails.setLifecycleState(component.getLifecycleState());
	// componentReqDetails.setVersion(component.getVersion());
	// componentReqDetails.setUuid(component.getUUID());
	// componentReqDetails.setCategories(component.getCategories());
	// componentReqDetails.setProjectCode(component.getProjectCode());
	//
	// return componentReqDetails;
	// }

	// *********** LIFECYCLE ***************

	public static Pair<Component, RestResponse> changeComponentState(Component component, UserRoleEnum userRole, LifeCycleStatesEnum targetState, Boolean validateState) throws Exception {

		Boolean isValidationFailed = false;
		RestResponse lifeCycleStatesResponse = null;
		User defaultUser = ElementFactory.getDefaultUser(userRole);

		LifeCycleStatesEnum curentCompState = LifeCycleStatesEnum.findByCompState(component.getLifecycleState().toString());

		if (curentCompState == targetState) {
			component = getComponentObject(component, userRole);
			return Pair.of(component, null);
		}
		// List<LifeCycleStatesEnum> lifeCycleStatesEnumOrigList = new
		// ArrayList<LifeCycleStatesEnum>(EnumSet.allOf(LifeCycleStatesEnum.class));

		ArrayList<String> lifeCycleStatesEnumList = new ArrayList<String>();
		if (curentCompState.equals(LifeCycleStatesEnum.CHECKIN) && targetState.equals(LifeCycleStatesEnum.CHECKOUT)) {
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKIN.toString());
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKOUT.toString());
		} else {
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKOUT.toString());
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CHECKIN.toString());
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CERTIFICATIONREQUEST.toString());
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.STARTCERTIFICATION.toString());
			lifeCycleStatesEnumList.add(LifeCycleStatesEnum.CERTIFY.toString());
		}
		for (int i = 0; i < lifeCycleStatesEnumList.size(); i++) {
			if (lifeCycleStatesEnumList.get(i).equals(curentCompState.name())) {
				int a;
				a = (i == lifeCycleStatesEnumList.size() - 1) ? 0 : i + 1;

				for (int n = a; n < lifeCycleStatesEnumList.size(); n++) {
					if (lifeCycleStatesEnumList.get(n).equals(LifeCycleStatesEnum.STARTCERTIFICATION.name()) || lifeCycleStatesEnumList.get(n).equals(LifeCycleStatesEnum.CERTIFY.name())) {
						defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.TESTER);
					} else
						defaultUser = ElementFactory.getDefaultUser(userRole);

					lifeCycleStatesResponse = LifecycleRestUtils.changeComponentState(component, defaultUser, LifeCycleStatesEnum.findByState(lifeCycleStatesEnumList.get(n)));
					if (lifeCycleStatesResponse.getErrorCode() != LifecycleRestUtils.STATUS_CODE_SUCCESS)
						isValidationFailed = true;
					if (lifeCycleStatesEnumList.get(n).equals(targetState.toString()) || isValidationFailed == true) {
						break;
					}
				}
			}

		}
		Component componentJavaObject = getComponentObject(component, userRole);

		if (validateState == true && isValidationFailed == true) {
			assertTrue("change state failed" + lifeCycleStatesResponse.getResponse(), false);

			return Pair.of(componentJavaObject, lifeCycleStatesResponse);
		}

		if (isValidationFailed == true) {
			return Pair.of(componentJavaObject, lifeCycleStatesResponse);
		}

		return Pair.of(componentJavaObject, lifeCycleStatesResponse);
	}

	public static RestResponse distributeService(Component component, Boolean validateState) throws Exception {

		Service service = (Service) component;

		User opsUser = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
		User governotUser = ElementFactory.getDefaultUser(UserRoleEnum.GOVERNOR);

		ServiceReqDetails serviceDetails = new ServiceReqDetails(service);
		RestResponse distributionService = null;

		RestResponse approveDistribution = LifecycleRestUtils.changeDistributionStatus(serviceDetails, null, governotUser, "approveService", DistributionStatusEnum.DISTRIBUTION_APPROVED);
		if (approveDistribution.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
			distributionService = LifecycleRestUtils.changeDistributionStatus(serviceDetails, null, opsUser, "approveService", DistributionStatusEnum.DISTRIBUTED);
		}

		if (validateState) {
			assertTrue(approveDistribution.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);
			assertTrue(distributionService.getErrorCode() == ProductRestUtils.STATUS_CODE_SUCCESS);
			return distributionService;
		}

		return distributionService;

	}

	// *********** ARTIFACTS *****************

	public static Either<ArtifactDefinition, RestResponse> uploadArtifactByType(ArtifactTypeEnum artifactType, Component component, UserRoleEnum userRole, Boolean deploymentTrue, Boolean validateState) throws Exception {

		User defaultUser = ElementFactory.getDefaultUser(userRole);
		ArtifactReqDetails artifactDetails = ElementFactory.getArtifactByType(null, artifactType, deploymentTrue);
		if (deploymentTrue == false)
			artifactDetails.setArtifactGroupType(ArtifactGroupTypeEnum.INFORMATIONAL.getType());
		RestResponse uploadArtifactResp = ArtifactRestUtils.uploadArtifact(artifactDetails, component, defaultUser);

		if (validateState) {
			assertTrue("artifact upload failed: " + artifactDetails.getArtifactName(), uploadArtifactResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		}

		if (uploadArtifactResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
			ArtifactDefinition artifactJavaObject = ResponseParser.convertArtifactDefinitionResponseToJavaObject(uploadArtifactResp.getResponse());
			return Either.left(artifactJavaObject);
		}
		return Either.right(uploadArtifactResp);
	}

	// *********** CONTAINERS *****************
	/**
	 * Adds Component instance to Component
	 * 
	 * @param compInstParent
	 * @param compContainer
	 * @return
	 */
	public static Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer(Component compInstParent, Component compContainer) {
		return addComponentInstanceToComponentContainer(compInstParent, compContainer, UserRoleEnum.DESIGNER, false);
	}

	public static Either<ComponentInstance, RestResponse> addComponentInstanceToComponentContainer(Component compInstParent, Component compContainer, UserRoleEnum userRole, Boolean validateState) {
		try {
			User defaultUser = ElementFactory.getDefaultUser(userRole);
			ComponentInstanceReqDetails componentInstanceDetails = ElementFactory.getComponentInstance(compInstParent);
			RestResponse createComponentInstance = ComponentInstanceRestUtils.createComponentInstance(componentInstanceDetails, defaultUser, compContainer);

			if (validateState) {
				if (createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_NOT_FOUND)
				{
					throw new SkipException("Open bug DE262001");
				}
				else{
				assertTrue("error - " + createComponentInstance.getErrorCode() + "instead - " + ServiceRestUtils.STATUS_CODE_CREATED, createComponentInstance.getErrorCode() == ServiceRestUtils.STATUS_CODE_CREATED);
				}
			}

			if (createComponentInstance.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
				ComponentInstance componentInstance = ResponseParser.convertComponentInstanceResponseToJavaObject(createComponentInstance.getResponse());
				return Either.left(componentInstance);
			}
			return Either.right(createComponentInstance);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}

	public static Resource getResourceObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
		// User defaultUser = ElementFactory.getDefaultUser(userRole);
		RestResponse restResponse = ResourceRestUtils.getResource(containerDetails.getUniqueId());
		Resource container = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		return container;
	}
	
	public static Resource getResourceObject(String uniqueId) throws Exception {
		RestResponse restResponse = ResourceRestUtils.getResource(uniqueId);
		Resource resource = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
		return resource;
	}
	
	public static Resource getResourceObjectByNameAndVersion(UserRoleEnum sdncModifierDetails, String resourceName, String resourceVersion) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(sdncModifierDetails);
		RestResponse resourceResponse = ResourceRestUtils.getResourceByNameAndVersion(defaultUser.getUserId(), resourceName, resourceVersion);
		Resource container = ResponseParser.convertResourceResponseToJavaObject(resourceResponse.getResponse());
		return container;
	}

	public static Service getServiceObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		RestResponse serviceResponse = ServiceRestUtils.getService(containerDetails.getUniqueId(), defaultUser);
		Service container = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
		return container;
	}
	
	public static Service getServiceObjectByNameAndVersion(UserRoleEnum sdncModifierDetails, String serviceName, String serviceVersion) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(sdncModifierDetails);
		RestResponse serviceResponse = ServiceRestUtils.getServiceByNameAndVersion(defaultUser, serviceName, serviceVersion);
		Service container = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
		return container;
	}
	
	public static Service getServiceObject(String uniqueId) throws Exception {
		RestResponse serviceResponse = ServiceRestUtils.getService(uniqueId);
		Service container = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
		return container;
	}

	public static Product getProductObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		RestResponse productRest = ProductRestUtils.getProduct(containerDetails.getUniqueId(), defaultUser.getUserId());
		Product container = ResponseParser.convertProductResponseToJavaObject(productRest.getResponse());
		return container;
	}

	public static Component getComponentObject(Component containerDetails, UserRoleEnum userRole) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);

		switch (containerDetails.getComponentType()) {
		case RESOURCE:
			RestResponse restResponse = ResourceRestUtils.getResource(containerDetails.getUniqueId());
			containerDetails = ResponseParser.convertResourceResponseToJavaObject(restResponse.getResponse());
			break;
		case SERVICE:
			RestResponse serviceResponse = ServiceRestUtils.getService(containerDetails.getUniqueId(), defaultUser);
			containerDetails = ResponseParser.convertServiceResponseToJavaObject(serviceResponse.getResponse());
			break;
		case PRODUCT:
			RestResponse productRest = ProductRestUtils.getProduct(containerDetails.getUniqueId(), defaultUser.getUserId());
			containerDetails = ResponseParser.convertProductResponseToJavaObject(productRest.getResponse());
			break;
		default:
			break;
		}
		return containerDetails;
	}

	public static Component convertReposnseToComponentObject(Component containerDetails, RestResponse restresponse) throws Exception {

		switch (containerDetails.getComponentType()) {
		case RESOURCE:
			containerDetails = ResponseParser.convertResourceResponseToJavaObject(restresponse.getResponse());
			break;
		case SERVICE:
			containerDetails = ResponseParser.convertServiceResponseToJavaObject(restresponse.getResponse());
			break;
		case PRODUCT:
			containerDetails = ResponseParser.convertProductResponseToJavaObject(restresponse.getResponse());
			break;
		default:
			break;
		}
		return containerDetails;
	}

	public static Either<Component, RestResponse> associate2ResourceInstances(Component containerDetails, ComponentInstance fromNode, ComponentInstance toNode, String assocType, UserRoleEnum userRole, Boolean validateState) throws Exception {

		User defaultUser = ElementFactory.getDefaultUser(userRole);
		RestResponse associate2ResourceInstancesResponse = ResourceRestUtils.associate2ResourceInstances(containerDetails, fromNode, toNode, assocType, defaultUser);

		if (validateState) {
			assertTrue(associate2ResourceInstancesResponse.getErrorCode() == ServiceRestUtils.STATUS_CODE_SUCCESS);
		}

		if (associate2ResourceInstancesResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS) {

			switch (containerDetails.getComponentType()) {
			case RESOURCE:
				containerDetails = ResponseParser.convertResourceResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
				break;
			case SERVICE:
				containerDetails = ResponseParser.convertServiceResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
				break;
			case PRODUCT:
				containerDetails = ResponseParser.convertProductResponseToJavaObject(associate2ResourceInstancesResponse.getResponse());
				break;
			default:
				break;
			}

			return Either.left(containerDetails);
		}
		return Either.right(associate2ResourceInstancesResponse);

	}

	public static Either<Pair<Component, ComponentInstance>, RestResponse> changeComponentInstanceVersion(Component containerDetails, ComponentInstance componentInstanceToReplace, Component newInstance, UserRoleEnum userRole, Boolean validateState)
			throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);

		RestResponse changeComponentInstanceVersionResp = ComponentInstanceRestUtils.changeComponentInstanceVersion(containerDetails, componentInstanceToReplace, newInstance, defaultUser);
		if (validateState) {
			assertTrue("change ComponentInstance version failed: " + changeComponentInstanceVersionResp.getResponseMessage(), changeComponentInstanceVersionResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		}

		if (changeComponentInstanceVersionResp.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {

			Component compoenntObject = AtomicOperationUtils.getComponentObject(containerDetails, userRole);
			ComponentInstance componentInstanceJavaObject = ResponseParser.convertComponentInstanceResponseToJavaObject(changeComponentInstanceVersionResp.getResponse());

			return Either.left(Pair.of(compoenntObject, componentInstanceJavaObject));
		}

		return Either.right(changeComponentInstanceVersionResp);
	}

	// *********** PROPERTIES *****************

	public static Either<ComponentInstanceProperty, RestResponse> addCustomPropertyToResource(PropertyReqDetails propDetails, Resource resourceDetails, UserRoleEnum userRole, Boolean validateState) throws Exception {

		User defaultUser = ElementFactory.getDefaultUser(userRole);
		Map<String, PropertyReqDetails> propertyToSend = new HashMap<String, PropertyReqDetails>();
		propertyToSend.put(propDetails.getName(), propDetails);
		Gson gson = new Gson();
		RestResponse addPropertyResponse = PropertyRestUtils.createProperty(resourceDetails.getUniqueId(), gson.toJson(propertyToSend), defaultUser);

		if (validateState) {
			assertTrue("add property to resource failed: " + addPropertyResponse.getErrorCode(), addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED);
		}

		if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
			ComponentInstanceProperty compInstProp = null;
			String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
			compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));
			return Either.left(compInstProp);
		}
		return Either.right(addPropertyResponse);
	}

	// Benny
	public static Either<ComponentInstanceProperty, RestResponse> updatePropertyOfResource(PropertyReqDetails propDetails, Resource resourceDetails, String propertyUniqueId, UserRoleEnum userRole, Boolean validateState) throws Exception {

		User defaultUser = ElementFactory.getDefaultUser(userRole);
		Map<String, PropertyReqDetails> propertyToSend = new HashMap<String, PropertyReqDetails>();
		propertyToSend.put(propDetails.getName(), propDetails);
		Gson gson = new Gson();
		RestResponse addPropertyResponse = PropertyRestUtils.updateProperty(resourceDetails.getUniqueId(), propertyUniqueId, gson.toJson(propertyToSend), defaultUser);

		if (validateState) {
			assertTrue("add property to resource failed: " + addPropertyResponse.getResponseMessage(), addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS);
		}

		if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
			ComponentInstanceProperty compInstProp = null;
			String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
			compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));
			return Either.left(compInstProp);
		}
		return Either.right(addPropertyResponse);
	}

	public static RestResponse deletePropertyOfResource(String resourceId, String propertyId, UserRoleEnum userRole) throws Exception {
		User defaultUser = ElementFactory.getDefaultUser(userRole);
		return PropertyRestUtils.deleteProperty(resourceId, propertyId, defaultUser);
	}

	public static Either<ComponentInstanceProperty, RestResponse> addDefaultPropertyToResource(PropertyTypeEnum propertyType, Resource resourceDetails, UserRoleEnum userRole, Boolean validateState) throws Exception {

		User defaultUser = ElementFactory.getDefaultUser(userRole);
		PropertyReqDetails propDetails = ElementFactory.getPropertyDetails(propertyType);
		Map<String, PropertyReqDetails> propertyToSend = new HashMap<String, PropertyReqDetails>();
		propertyToSend.put(propDetails.getName(), propDetails);
		Gson gson = new Gson();
		RestResponse addPropertyResponse = PropertyRestUtils.createProperty(resourceDetails.getUniqueId(), gson.toJson(propertyToSend), defaultUser);

		if (validateState) {
			assertTrue("add property to resource failed: " + addPropertyResponse.getResponseMessage(), addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED);
		}

		if (addPropertyResponse.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
			ComponentInstanceProperty compInstProp = null;
			String property = ResponseParser.getJsonObjectValueByKey(addPropertyResponse.getResponse(), propDetails.getName());
			compInstProp = (ResponseParser.convertPropertyResponseToJavaObject(property));

			return Either.left(compInstProp);
		}
		return Either.right(addPropertyResponse);
	}

	public static RestResponse createDefaultConsumer(Boolean validateState) {
		try {
			ConsumerDataDefinition defaultConsumerDefinition = ElementFactory.getDefaultConsumerDetails();
			RestResponse createResponse = ConsumerRestUtils.createConsumer(defaultConsumerDefinition, ElementFactory.getDefaultUser(UserRoleEnum.ADMIN));
			BaseRestUtils.checkCreateResponse(createResponse);

			if (validateState) {
				assertTrue(createResponse.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
			}
			return createResponse;
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}

	/**
	 * Builds Resource From rest response
	 * 
	 * @param resourceResp
	 * @return
	 */
	public static Either<Resource, RestResponse> buildResourceFromResponse(RestResponse resourceResp) {
		Either<Resource, RestResponse> result;
		if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
			Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
			result = Either.left(resourceResponseObject);
		} else {
			result = Either.right(resourceResp);
		}
		return result;
	}

	private static class AtomicOperationException extends RuntimeException {
		private AtomicOperationException(Exception e) {
			super(e);
		}

		private static final long serialVersionUID = 1L;
	};
	
	/**
	 * Import resource from CSAR
	 * 
	 * @param resourceType
	 * @param userRole
	 * @param fileName
	 * @param filePath
	 * @return Resource
	 * @throws Exception
	 */
	public static Resource importResourceFromCSAR(ResourceTypeEnum resourceType, UserRoleEnum userRole, String fileName, String... filePath) throws Exception {
		// Get the CSARs path
		String realFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "CI" + File.separator + "csars" ;
		if (filePath != null && filePath.length > 0) {
			realFilePath = filePath.toString();
		}
		
		// Create default import resource & user
		ImportReqDetails resourceDetails = ElementFactory.getDefaultImportResource();
		User sdncModifierDetails = ElementFactory.getDefaultUser(userRole);
		
		byte[] data = null;
		Path path = Paths.get(realFilePath + File.separator + fileName);
		data = Files.readAllBytes(path);
		String payloadName = fileName;
		String payloadData = Base64.encodeBase64String(data);
		resourceDetails.setPayloadData(payloadData);
		resourceDetails.setCsarUUID(payloadName);
		resourceDetails.setPayloadName(payloadName);
		resourceDetails.setResourceType(resourceType.name());
		
		RestResponse createResource = ResourceRestUtils.createResource(resourceDetails, sdncModifierDetails);
		BaseRestUtils.checkCreateResponse(createResource);
		return ResponseParser.parseToObjectUsingMapper(createResource.getResponse(), Resource.class);		
	};
	
	public static Either<Resource, RestResponse> importResourceByFileName(ResourceTypeEnum resourceType, UserRoleEnum userRole, String fileName, Boolean validateState, String... filePath) throws IOException {

		String realFilePath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator + "CI" + File.separator + "csars" ;
		if (filePath != null && filePath.length > 0) {
			realFilePath = filePath.toString();
		}

		try {
			User defaultUser = ElementFactory.getDefaultUser(userRole);
			ResourceReqDetails defaultResource = ElementFactory.getDefaultResource(defaultUser);			
			ImportReqDetails defaultImportResource = ElementFactory.getDefaultImportResource(defaultResource);
			ImportUtils.getImportResourceDetailsByPathAndName(defaultImportResource, realFilePath, fileName);
			RestResponse resourceResp = ResourceRestUtils.createResource(defaultImportResource, defaultUser);

			if (validateState) {
				assertTrue(resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED);
			}

			if (resourceResp.getErrorCode() == ResourceRestUtils.STATUS_CODE_CREATED) {
				Resource resourceResponseObject = ResponseParser.convertResourceResponseToJavaObject(resourceResp.getResponse());
				return Either.left(resourceResponseObject);
			}
			return Either.right(resourceResp);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}
	
	public static Either<String, RestResponse> getComponenetArtifactPayload(Component component, String artifactType) throws Exception {

		String url;
		Config config = Utils.getConfig();
		if(component.getComponentType().toString().toUpperCase().equals(ComponentTypeEnum.SERVICE.getValue().toUpperCase())){
			url = String.format(Urls.UI_DOWNLOAD_SERVICE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), component.getUniqueId(), component.getToscaArtifacts().get(artifactType).getUniqueId());
		}else{
			url = String.format(Urls.UI_DOWNLOAD_RESOURCE_ARTIFACT, config.getCatalogBeHost(), config.getCatalogBePort(), component.getUniqueId(), component.getToscaArtifacts().get(artifactType).getUniqueId());
		}
		String userId = component.getLastUpdaterUserId();
		Map<String, String> headersMap = new HashMap<String, String>();
		headersMap.put(HttpHeaderEnum.CONTENT_TYPE.getValue(), "application/json");
		headersMap.put(HttpHeaderEnum.CACHE_CONTROL.getValue(), "no-cache");
		headersMap.put(HttpHeaderEnum.AUTHORIZATION.getValue(), basicAuthentication);
		headersMap.put(HttpHeaderEnum.X_ECOMP_INSTANCE_ID.getValue(), "ci");
		if (userId != null) {
			headersMap.put(HttpHeaderEnum.USER_ID.getValue(), userId);
		}
		HttpRequest http = new HttpRequest();
		RestResponse response = http.httpSendGet(url, headersMap);
		if (response.getErrorCode() != BaseRestUtils.STATUS_CODE_SUCCESS && response.getResponse().getBytes() == null && response.getResponse().getBytes().length == 0) {
			return Either.right(response);
		}
		return Either.left(response.getResponse());

	}

	public static RestResponse getDistributionStatusByDistributionId(String distributionId ,Boolean validateState) {

		try {
			User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.OPS);
			RestResponse response = DistributionUtils.getDistributionStatus(defaultUser, distributionId);

			if (validateState) {
				assertTrue(response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
			}
			return response;
		
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
	}
	
	public static Either <RestResponse, Map<String, List<DistributionMonitorObject>>> getSortedDistributionStatusMap(Service service ,Boolean validateState) {
		
		try {
			ServiceDistributionStatus serviceDistributionObject = DistributionUtils.getLatestServiceDistributionObject(service);
			RestResponse response = getDistributionStatusByDistributionId(serviceDistributionObject.getDistributionID(), true);

			if(validateState) {
				assertTrue(response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS);
			}
			if(response.getErrorCode() == ResourceRestUtils.STATUS_CODE_SUCCESS){
				Map<String, List<DistributionMonitorObject>> parsedDistributionStatus = DistributionUtils.getSortedDistributionStatus(response);
				return Either.right(parsedDistributionStatus);
			}
			return Either.left(response);
		} catch (Exception e) {
			throw new AtomicOperationException(e);
		}
		
	}
	
	
	/**
	 * @param service
	 * @param pollingCount
	 * @param pollingInterval
	 * Recommended values for service distribution for pollingCount is 4 and for pollingInterval is 15000ms
	 * @throws Exception
	 */
	public static Boolean distributeAndValidateService(Service service, int pollingCount, int pollingInterval) throws Exception {

		Boolean statusFlag = true;
		AtomicOperationUtils.distributeService(service,  true);
		TimeUnit.MILLISECONDS.sleep(pollingInterval);
		int timeOut = pollingCount * pollingInterval;
		while (timeOut > 0) {
			Map<String,List<DistributionMonitorObject>> sortedDistributionStatusMap = AtomicOperationUtils.getSortedDistributionStatusMap(service, true).right().value();
			com.clearspring.analytics.util.Pair<Boolean,Map<String,List<String>>> verifyDistributionStatus = DistributionUtils.verifyDistributionStatus(sortedDistributionStatusMap);
			if(verifyDistributionStatus.left.equals(false)){
				if((verifyDistributionStatus.right != null && ! verifyDistributionStatus.right.isEmpty()) && timeOut == 0){
					for(Entry<String, List<String>> entry : verifyDistributionStatus.right.entrySet()){
						if(ComponentBaseTest.getExtendTest() != null){
							ComponentBaseTest.getExtendTest().log(Status.INFO, "Consumer: " + entry.getKey() + " failed on following: "+ entry.getValue());
							statusFlag = false;
						}else{
							System.out.println("Consumer: " + entry.getKey() + " failed on following: "+ entry.getValue());
						}
					}
				}
				TimeUnit.MILLISECONDS.sleep(pollingInterval);
				timeOut-=pollingInterval;
			}else {
				timeOut = 0;
			}
		}
		return statusFlag;
	}
	
	public static Boolean distributeAndValidateService(Service service) throws Exception {
		return distributeAndValidateService(service, 6, 10000);
	}

	
	
}
