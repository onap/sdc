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

package org.openecomp.sdc.ci.tests.utils.rest;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.Urls;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpHeaderEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.HttpRequest;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.common.util.GeneralUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class PropertyRestUtils extends BaseRestUtils {
	private static Logger logger = LoggerFactory.getLogger(PropertyRestUtils.class.getName());

	public static RestResponse createProperty(String resourceId, String body, User user) throws Exception {
		Config config = Config.instance();
		String url = String.format(Urls.CREATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId);

		return sendPost(url, body, user.getUserId(), acceptHeaderData);
	}

	public static RestResponse updateProperty(String resourceId, String propertyId, String body, User user)
			throws Exception {
		Config config = Config.instance();

		String url = String.format(Urls.UPDATE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId, propertyId);
		return sendPut(url, body, user.getUserId(), acceptHeaderData);
	}

	public static RestResponse getProperty(String resourceId, String propertyId, User user) throws Exception {
		Config config = Config.instance();
		String url = String.format(Urls.GET_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(), resourceId,
				propertyId);
		return sendGet(url, user.getUserId());
	}

	public static RestResponse deleteProperty(String resourceId, String propertyId, User user) throws Exception {
		Config config = Config.instance();
		String url = String.format(Urls.DELETE_PROPERTY, config.getCatalogBeHost(), config.getCatalogBePort(),
				resourceId, propertyId);

		return sendDelete(url, user.getUserId());
	}

	public static ComponentInstanceProperty getPropFromListByPropNameAndType(List<ComponentInstanceProperty> propList,
			String propNameToUpdate, String propTypeToUpdate) {
		for (ComponentInstanceProperty componentInstanceProperty : propList) {
			if (componentInstanceProperty.getName().equals(propNameToUpdate)
					&& componentInstanceProperty.getType().equals(propTypeToUpdate)) {
				return componentInstanceProperty;
			}
		}
		return null;
	}

	public static ComponentInstanceProperty getPropFromListByPropNameTypeAndPath(
			List<ComponentInstanceProperty> propList, String propNameToUpdate, String propTypeToUpdate,
			List<String> path) {
		for (ComponentInstanceProperty componentInstanceProperty : propList) {
			if (componentInstanceProperty.getPath() == null) {
				return getPropFromListByPropNameAndType(propList, propNameToUpdate, propTypeToUpdate);
			}
			if (componentInstanceProperty.getName().equals(propNameToUpdate)
					&& componentInstanceProperty.getType().equals(propTypeToUpdate)
					&& path.containsAll(componentInstanceProperty.getPath())) {
				return componentInstanceProperty;
			}
		}
		return null;
	}

	public static ComponentInstanceProperty getPropFromListByPropIdAndPath(List<ComponentInstanceProperty> propList,
			String propId, List<String> path) {

		for (ComponentInstanceProperty componentInstanceProperty : propList) {
			if (path != null) {
				if (componentInstanceProperty.getUniqueId().equals(propId)
						&& componentInstanceProperty.getPath().equals(path)) {
					return componentInstanceProperty;
				}
			} else {
				if (componentInstanceProperty.getUniqueId().equals(propId)) {
					return componentInstanceProperty;
				}
			}
		}
		return null;
	}

	public static void comparePropertyLists(List<ComponentInstanceProperty> expectedList,
			List<ComponentInstanceProperty> actualList, Boolean isUpdate) {

		assertTrue(
				"list size are not equals, expected size is: " + expectedList.size() + " ,actual: " + actualList.size(),
				expectedList.size() == actualList.size());
		Boolean flag = false;
		for (ComponentInstanceProperty expectedcompInstProp : expectedList) {
			for (ComponentInstanceProperty actualcompInstProp : actualList) {
				flag = comparePropertyObjects(expectedcompInstProp, actualcompInstProp, isUpdate);
				if (flag) {
					break;
				}
			}
		}
		// System.out.println("expected: " + expectedList + ", actual: " +
		// actualList);
		logger.debug("expected: {}, actual: {}",expectedList,actualList);
		assertTrue("actual lists does not contain all uniqeIds", flag);
	}

	public static Boolean comparePropertyObjects(ComponentInstanceProperty expectedCompInstProp,
			ComponentInstanceProperty actualCompInstProp, Boolean isUpdate) {
		String uniqueId = expectedCompInstProp.getUniqueId();
		String type = expectedCompInstProp.getType();
		String defaulValue = expectedCompInstProp.getDefaultValue();
		if (actualCompInstProp.getUniqueId().equals(uniqueId)
				&& actualCompInstProp.getPath().equals(expectedCompInstProp.getPath())) {
			assertTrue("expected type is: " + type + " ,actual: " + actualCompInstProp.getType(),
					actualCompInstProp.getType().equals(type));
			if (defaulValue == null) {
				assertTrue(
						"expected defaulValue is: " + defaulValue + " ,actual: " + actualCompInstProp.getDefaultValue(),
						actualCompInstProp.getDefaultValue() == defaulValue);
			} else {
				assertTrue(
						"expected defaulValue is: " + defaulValue + " ,actual: " + actualCompInstProp.getDefaultValue(),
						actualCompInstProp.getDefaultValue().equals(defaulValue));
			}
			if (isUpdate) {
				assertTrue(
						"actual [Value] parameter " + actualCompInstProp.getName()
								+ "should equal to expected [Value]: " + actualCompInstProp.getValue() + " ,Value: "
								+ actualCompInstProp.getValue(),
						actualCompInstProp.getValue().equals(expectedCompInstProp.getValue()));
				assertNotNull("valueId is null", actualCompInstProp.getValueUniqueUid());
			} else {
				if (defaulValue == null) {
					assertTrue(
							"actual [Value] parameter " + actualCompInstProp.getName()
									+ "should equal to expected [defaultValue]: " + actualCompInstProp.getValue()
									+ " ,defaultValue: " + actualCompInstProp.getDefaultValue(),
							actualCompInstProp.getValue() == expectedCompInstProp.getDefaultValue());
				} else {
					assertTrue(
							"actual [Value] parameter " + actualCompInstProp.getName()
									+ "should equal to expected [defaultValue]: " + actualCompInstProp.getValue()
									+ " ,defaultValue: " + actualCompInstProp.getDefaultValue(),
							actualCompInstProp.getValue().equals(expectedCompInstProp.getDefaultValue()));
				}
				assertNull("valueId is not null", actualCompInstProp.getValueUniqueUid());
			}
			return true;
		}
		return false;
	}

	public static List<ComponentInstanceProperty> addResourcePropertiesToList(Resource resource,
			List<ComponentInstanceProperty> listToFill) {
		for (PropertyDefinition prop : resource.getProperties()) {
			listToFill.add(new ComponentInstanceProperty(prop, null, null));
		}
		return listToFill;
	}

	public static List<ComponentInstanceProperty> addComponentInstPropertiesToList(Component component,
			List<ComponentInstanceProperty> listToFill, String componentId) {

		if (componentId != null) {
			List<ComponentInstanceProperty> list = component.getComponentInstancesProperties().get(componentId);
			for (ComponentInstanceProperty prop : list) {
				ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty(prop, null, null);
				componentInstanceProperty.setPath(prop.getPath());
				componentInstanceProperty.setValueUniqueUid(prop.getValueUniqueUid());
				componentInstanceProperty.setValue(prop.getValue());
				listToFill.add(componentInstanceProperty);
			}
		} else {
			Map<String, List<ComponentInstanceProperty>> componentInstancesProperties = component
					.getComponentInstancesProperties();
			for (Map.Entry<String, List<ComponentInstanceProperty>> componentInstanceProperties : componentInstancesProperties
					.entrySet()) {
				for (ComponentInstanceProperty prop : componentInstanceProperties.getValue()) {
					ComponentInstanceProperty componentInstanceProperty = new ComponentInstanceProperty(prop, null,
							null);
					componentInstanceProperty.setPath(prop.getPath());
					componentInstanceProperty.setValueUniqueUid(prop.getValueUniqueUid());
					componentInstanceProperty.setValue(prop.getValue());
					listToFill.add(componentInstanceProperty);
				}
			}
		}

		if (component.getComponentType().getValue().equals("Resource")) {
			for (PropertyDefinition prop : ((Resource) component).getProperties()) {
				listToFill.add(new ComponentInstanceProperty(prop, null, null));
			}
		}
		return listToFill;
	}

	public static ComponentInstanceProperty getCompPropInstListByInstIdAndPropName(Component component,
			ComponentInstance componentInstanceDetails, String name, String type) {
		List<ComponentInstanceProperty> propList = component.getComponentInstancesProperties()
				.get(componentInstanceDetails.getUniqueId());
		if (propList != null) {
			return getPropFromListByPropNameAndType(propList, name, type);
		}
		return null;
	}

	private static void updatePropertyListWithPathParameter(Resource resource, List<String> path,
			List<ComponentInstanceProperty> expectedPropertyList) {
		List<PropertyDefinition> propertyList = resource.getProperties();
		for (PropertyDefinition propertyDefinition : propertyList) {
			ComponentInstanceProperty propDetailsToRemove = PropertyRestUtils.getPropFromListByPropNameAndType(
					expectedPropertyList, propertyDefinition.getName(), propertyDefinition.getType());
			ComponentInstanceProperty propDetailsToAdd = propDetailsToRemove;
			propDetailsToAdd.setPath(path);
			expectedPropertyList.remove(propDetailsToRemove);
			expectedPropertyList.add(propDetailsToAdd);
		}
	}

	private static void updatePropertyListWithPathParameterOnCompInst(Service service, List<String> path,
			List<ComponentInstanceProperty> expectedPropertyList) {
		List<ComponentInstanceProperty> servicePropertyList = new ArrayList<>();
		servicePropertyList = PropertyRestUtils.addComponentInstPropertiesToList(service, servicePropertyList,
				path.get(0));

		for (ComponentInstanceProperty serviceCompInstProperty : servicePropertyList) {
			ComponentInstanceProperty propDetailsToRemove = PropertyRestUtils.getPropFromListByPropNameTypeAndPath(
					expectedPropertyList, serviceCompInstProperty.getName(), serviceCompInstProperty.getType(),
					serviceCompInstProperty.getPath());
			ComponentInstanceProperty propDetailsToAdd = propDetailsToRemove;
			List<String> tempPathList = new ArrayList<String>();
			for (String tempPath : path) {
				tempPathList.add(tempPath);
			}
			// path parameter can not contain the same service unique ID twice
			if (propDetailsToAdd.getPath() != null
					&& !propDetailsToAdd.getPath().get(0).contains(service.getUniqueId())) {
				if (!propDetailsToAdd.getPath().containsAll(tempPathList)) {
					tempPathList.addAll(propDetailsToAdd.getPath());
				}
			}
			propDetailsToAdd.setPath(tempPathList);
			expectedPropertyList.remove(propDetailsToRemove);
			expectedPropertyList.add(propDetailsToAdd);
		}
	}

	public static void updatePropertyListWithPathOnResource(ComponentInstance componentInstDetails, Resource resource,
			List<ComponentInstanceProperty> list, Component container) {
		List<String> path = new ArrayList<>();
		if (container != null) {
			List<ComponentInstance> componentInstances = container.getComponentInstances();
			for (ComponentInstance componentInstance : componentInstances) {
				if (componentInstance.getNormalizedName().equals(componentInstDetails.getNormalizedName())) {
					path.add(componentInstance.getUniqueId());
					break;
				}
			}

		} else {
			path.add(componentInstDetails.getUniqueId());
		}
		updatePropertyListWithPathParameter(resource, path, list);
	}

	public static void updatePropertyListWithPathOnComponentInstance(ComponentInstance componentInstDetails,
			Service service, List<ComponentInstanceProperty> list) {
		List<String> path = new ArrayList<>();
		path.add(componentInstDetails.getUniqueId());
		updatePropertyListWithPathParameterOnCompInst(service, path, list);
	}

	public static RestResponse declareProporties(Component componentObject, Map<String, List<ComponentInstanceInput>> componentInstancesInputs, User sdncModifierDetails)
			throws Exception {
		Config config = Config.instance();
		String url = String.format(Urls.DECLARE_PROPERTIES, config.getCatalogBeHost(), config.getCatalogBePort(), ComponentTypeEnum.findParamByType(componentObject.getComponentType()), componentObject.getUniqueId());
		String userId = sdncModifierDetails.getUserId();
		Map<String, String> headersMap = prepareHeadersMap(userId);
		Map<String, Object> jsonBuilder = new HashMap<>();
		jsonBuilder.put("componentInstanceInputsMap", componentInstancesInputs);
		Gson gson = new Gson();
		String userBodyJson = gson.toJson(jsonBuilder);
		String calculateMD5 = GeneralUtility.calculateMD5Base64EncodedByString(userBodyJson);
		headersMap.put(HttpHeaderEnum.Content_MD5.getValue(), calculateMD5);
		HttpRequest http = new HttpRequest();
		// System.out.println(url);
		// System.out.println(userBodyJson);
		RestResponse declareProportiesResponse = http.httpSendPost(url, userBodyJson, headersMap);
		if (declareProportiesResponse.getErrorCode() == STATUS_CODE_GET_SUCCESS) {

		}
		return declareProportiesResponse;
	}
}
