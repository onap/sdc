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

package org.openecomp.sdc.ci.tests.utilities;

import static org.testng.AssertJUnit.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ProductReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.CatalogRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CategoryRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ProductRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.UserRestUtils;

import com.aventstack.extentreports.Status;

public class RestCDUtils {

	private static void setResourceUniqueIdAndUUID(ComponentReqDetails element, RestResponse getResourceResponse) {
		element.setUniqueId(ResponseParser.getUniqueIdFromResponse(getResourceResponse));
		element.setUUID(ResponseParser.getUuidFromResponse(getResourceResponse));
	}

	public static RestResponse getResource(ResourceReqDetails resource, User user) {
		final String getResourceMsg = "Trying to get resource named " + resource.getName() + " with version " + resource.getVersion();
		final String succeedGetResourceMsg = "Succeeded to get resource named " + resource.getName() + " with version " + resource.getVersion();
		final String failedGetResourceMsg = "Failed to get resource named " + resource.getName() + " with version " + resource.getVersion();
		try {
			ExtentTestActions.log(Status.INFO, getResourceMsg);
			System.out.println(getResourceMsg);
			GeneralUIUtils.sleep(1000);
			RestResponse getResourceResponse = null;
			String reourceUniqueId = resource.getUniqueId();
			if (reourceUniqueId != null) {
				getResourceResponse = ResourceRestUtils.getResource(reourceUniqueId);
				if (getResourceResponse.getErrorCode().intValue() == 200) {
					ExtentTestActions.log(Status.INFO, succeedGetResourceMsg);
					System.out.println(succeedGetResourceMsg);
				}
				return getResourceResponse;
			}
			JSONObject getResourceJSONObject = null;
			getResourceResponse = ResourceRestUtils.getResourceByNameAndVersion(user.getUserId(), resource.getName(), resource.getVersion());
			if (getResourceResponse.getErrorCode().intValue() == 200) {
//				JSONArray jArray = new JSONArray(getResourceResponse.getResponse());
//				for (int i = 0; i < jArray.length(); i++) {
//					getResourceJSONObject = jArray.getJSONObject(i);
//					String resourceType = ResponseParser.getValueFromJsonResponse(getResourceJSONObject.toString(), "resourceType");
//					if (resourceType.equals(resource.getResourceType())) {
//						getResourceResponse.setResponse(getResourceJSONObject.toString());
						setResourceUniqueIdAndUUID(resource, getResourceResponse);
						ExtentTestActions.log(Status.INFO, succeedGetResourceMsg);
						System.out.println(succeedGetResourceMsg);
						return getResourceResponse;
//					}
//				}
			}
			ExtentTestActions.log(Status.INFO, failedGetResourceMsg);
			return getResourceResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static RestResponse getService(ServiceReqDetails service, User user) {
		try {
			Thread.sleep(3500);
			RestResponse getServiceResponse = ServiceRestUtils.getServiceByNameAndVersion(user, service.getName(),
					service.getVersion());
			if (getServiceResponse.getErrorCode().intValue() == 200) {
				setResourceUniqueIdAndUUID(service, getServiceResponse);
			}
			return getServiceResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static RestResponse getProduct(ProductReqDetails product, User user) {
		try {
			Thread.sleep(3500);
			RestResponse getProductResponse = ProductRestUtils.getProductByNameAndVersion(product.getName(),
					product.getVersion(), user.getUserId());
			if (getProductResponse.getErrorCode().intValue() == 200) {
				setResourceUniqueIdAndUUID(product, getProductResponse);
			}
			return getProductResponse;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Map<String, String> getAllElementVersionsFromResponse(RestResponse getResource) throws Exception {
		Map<String, String> versionsMap = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();

			JSONObject object = new JSONObject(getResource.getResponse());
			versionsMap = mapper.readValue(object.get("allVersions").toString(), Map.class);

		} catch (Exception e) {
			e.printStackTrace();
			return versionsMap;

		}

		return versionsMap;
	}

	public static void deleteElementVersions(Map<String, String> elementVersions, boolean isBeforeTest, Object clazz,
			User user) throws Exception {
		Iterator<String> iterator = elementVersions.keySet().iterator();
		while (iterator.hasNext()) {
			String singleVersion = iterator.next();
			String uniqueId = elementVersions.get(singleVersion);
			RestResponse deleteResponse = null;
			if (clazz instanceof ServiceReqDetails) {
				deleteResponse = ServiceRestUtils.deleteServiceById(uniqueId, user.getUserId());
			} else if (clazz instanceof ResourceReqDetails) {
				deleteResponse = ResourceRestUtils.deleteResource(uniqueId, user.getUserId());
			} else if (clazz instanceof ProductReqDetails) {
				deleteResponse = ProductRestUtils.deleteProduct(uniqueId, user.getUserId());
			}

			if (isBeforeTest) {
				assertTrue(deleteResponse.getErrorCode().intValue() == 204
						|| deleteResponse.getErrorCode().intValue() == 404);
			} else {
				assertTrue(deleteResponse.getErrorCode().intValue() == 204);
			}
		}
	}

	public static void deleteAllResourceVersionsAfterTest(ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) {
		try {
			deleteAllComponentVersion(false, componentDetails, getObjectResponse, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteAllResourceVersionsBeforeTest(ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) throws Exception {
		deleteAllComponentVersion(true, componentDetails, getObjectResponse, user);
	}

	public static void deleteAllComponentVersion(boolean isBeforeTest, ComponentReqDetails componentDetails,
			RestResponse getObjectResponse, User user) throws Exception {
		if (getObjectResponse.getErrorCode().intValue() == 404)
			return;
		Map<String, String> componentVersionsMap = getAllElementVersionsFromResponse(getObjectResponse);
		System.out.println("deleting...");
		deleteElementVersions(componentVersionsMap, isBeforeTest, componentDetails, user);
		componentDetails.setUniqueId(null);
	}

	
	
	public static  String getExecutionHostAddress() {
		
		String computerName = null;
		try {
			   computerName = InetAddress.getLocalHost().getHostAddress().replaceAll("\\.", "&middot;");
			   System.out.println(computerName);
			  if (computerName.indexOf(".") > -1)
			    computerName = computerName.substring(0,
			        computerName.indexOf(".")).toUpperCase();
			} catch (UnknownHostException e) {
				System.out.println("Uknown hostAddress");
			}
			return computerName != null ? computerName : "Uknown hostAddress";
	}

	public static Map<String, List<Component>> getCatalogAsMap() throws IOException {
		User defaultAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		RestResponse catalog = CatalogRestUtils.getCatalog(defaultAdminUser.getUserId());
		Map<String, List<Component>> convertCatalogResponseToJavaObject = ResponseParser
				.convertCatalogResponseToJavaObject(catalog.getResponse());
		return convertCatalogResponseToJavaObject;
	}

	public static Map<String, List<CategoryDefinition>> getCategories() throws Exception {
		
		User defaultAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		
		Map<String,List<CategoryDefinition>> map = new HashMap<String,List<CategoryDefinition>>();
				
		
		RestResponse allResourceCategories = CategoryRestUtils.getAllCategories(defaultAdminUser, ComponentTypeEnum.RESOURCE_PARAM_NAME);
		RestResponse allServiceCategories = CategoryRestUtils.getAllCategories(defaultAdminUser, ComponentTypeEnum.SERVICE_PARAM_NAME);
	
		List<CategoryDefinition> parsedResourceCategories = ResponseParser.parseCategories(allResourceCategories);
		List<CategoryDefinition> parsedServiceCategories = ResponseParser.parseCategories(allServiceCategories);
		
		map.put(ComponentTypeEnum.RESOURCE_PARAM_NAME, parsedResourceCategories);
		map.put(ComponentTypeEnum.SERVICE_PARAM_NAME, parsedServiceCategories);
		
		return map;
	}

	public static void deleteCreatedComponents(Map<String, List<Component>> map) throws IOException {
		
		System.out.println("going to delete all created components...");
		
		User defaultAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);
		final String userId = defaultAdminUser.getUserId();
		
		List<Component> resourcesArrayList = map.get("products");
		List<String>  collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith(ElementFactory.getProductPrefix())).
																										 map(e -> e.getUniqueId()).
				                                                                                         collect(Collectors.toList());
		for (String uId : collect) {
			ProductRestUtils.deleteProduct(uId, userId);
		}
		
		resourcesArrayList = map.get("services");
		collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith(ElementFactory.getServicePrefix())).
				                                                                            map(e -> e.getUniqueId()).
				                                                                            collect(Collectors.toList());
		for (String uId : collect) {
			ServiceRestUtils.markServiceToDelete(uId, userId);
		}
		ServiceRestUtils.deleteMarkedServices(userId);		
		
		resourcesArrayList = map.get("resources");
		collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith(ElementFactory.getResourcePrefix())).
				                                                                            map(e -> e.getUniqueId()).
				                                                                            collect(Collectors.toList());
		for (String uId : collect) {			
			ResourceRestUtils.markResourceToDelete(uId, userId);
		}
		ResourceRestUtils.deleteMarkedResources(userId);
	
	
	

	
	}

	public static void deleteCategoriesByList(List<CategoryDefinition> listCategories, String componentType, User user) throws Exception {
		
		for (CategoryDefinition categoryDefinition : listCategories) {
			if (categoryDefinition.getName().toLowerCase().startsWith("ci")) {
				List<SubCategoryDefinition> subcategories = categoryDefinition.getSubcategories();
				if (subcategories != null) {
					for (SubCategoryDefinition subCategoryDefinition : subcategories) {
	
						CategoryRestUtils.deleteSubCategory(subCategoryDefinition.getUniqueId(),
								categoryDefinition.getUniqueId(), user.getUserId(),
								componentType);
					}
				}
	
				CategoryRestUtils.deleteCategory(categoryDefinition.getUniqueId(), user.getUserId(),
						componentType);
	
			}
		}
	}
	
	public static String getUserRole(User reqUser, User user){
		try{
			RestResponse getUserRoleResp = UserRestUtils.getUserRole(reqUser, user);
			JSONObject jObject = new JSONObject(getUserRoleResp.getResponse());
			return jObject.getString("role");
		}
		catch(Exception e){
			return null;
		}
	}
	
	public static RestResponse getUser(User reqUser, User user){
		try{
			return UserRestUtils.getUser(reqUser, user);
		}
		catch(Exception e){
			return null;
		}
	}

	/*************************************/
	
	public static void deleteOnDemand() throws IOException {
		Config config = DriverFactory.getConfig();
		if(!config.getSystemUnderDebug()){
			deleteCreatedComponents(getCatalogAsMap());
		}else{
			System.out.println("Accordindig to configuration components will not be deleted, in case to unable option to delete, please change systemUnderDebug parameter value to false ...");
		}
	}

	public static void deleteCategories(User user) throws Exception {
		Map<String, List<CategoryDefinition>> categoriesMap = getCategories();
		List<CategoryDefinition> listCategories = categoriesMap.get(ComponentTypeEnum.RESOURCE_PARAM_NAME);
		deleteCategoriesByList(listCategories, ComponentTypeEnum.RESOURCE_PARAM_NAME, user);
		listCategories = categoriesMap.get(ComponentTypeEnum.SERVICE_PARAM_NAME);
		deleteCategoriesByList(listCategories, ComponentTypeEnum.SERVICE_PARAM_NAME, user);
	}

}
