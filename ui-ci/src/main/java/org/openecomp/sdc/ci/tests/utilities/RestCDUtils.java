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

import com.aventstack.extentreports.Status;
import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.ci.tests.config.Config;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.execute.setup.DriverFactory;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            String resourceUniqueId = resource.getUniqueId();
            if (resourceUniqueId != null) {
                getResourceResponse = ResourceRestUtils.getResource(resourceUniqueId);
                if (getResourceResponse.getErrorCode().intValue() == 200) {
                    ExtentTestActions.log(Status.INFO, succeedGetResourceMsg);
                    System.out.println(succeedGetResourceMsg);
                }
                return getResourceResponse;
            }
            JSONObject getResourceJSONObject = null;
            getResourceResponse = ResourceRestUtils.getResourceByNameAndVersion(user.getUserId(), resource.getName(), resource.getVersion());
            if (getResourceResponse.getErrorCode().intValue() == 200) {
                setResourceUniqueIdAndUUID(resource, getResourceResponse);
                ExtentTestActions.log(Status.INFO, succeedGetResourceMsg);
                System.out.println(succeedGetResourceMsg);
                return getResourceResponse;
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

    public static String getExecutionHostAddress() {

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
        return ResponseParser.convertCatalogResponseToJavaObject(catalog.getResponse());
    }

    public static Map<String, List<CategoryDefinition>> getCategories() throws Exception {

        User defaultAdminUser = ElementFactory.getDefaultUser(UserRoleEnum.ADMIN);

        Map<String, List<CategoryDefinition>> map = new HashMap<>();


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


        List<Component> resourcesArrayList = map.get("resources");
        List<String> collect = resourcesArrayList.stream().filter(s -> s.getName().startsWith(ElementFactory.getResourcePrefix())).
                map(e -> e.getUniqueId()).
                collect(Collectors.toList());
        for (String uId : collect) {
            ResourceRestUtils.markResourceToDelete(uId, userId);

        }
        ResourceRestUtils.deleteMarkedResources(userId);

        resourcesArrayList = map.get("services");
        collect = resourcesArrayList.stream().
                filter(e -> e != null).
                filter(e -> e.getName() != null).
                filter(s -> s.getName().startsWith(ElementFactory.getServicePrefix())).
                filter(e -> e.getUniqueId() != null).
                map(e -> e.getUniqueId()).
                collect(Collectors.toList());
        for (String uId : collect) {
            ServiceRestUtils.markServiceToDelete(uId, userId);
        }
        ServiceRestUtils.deleteMarkedServices(userId);

    }

    public static String getUserRole(User reqUser, User user) {
        try {
            RestResponse getUserRoleResp = UserRestUtils.getUserRole(reqUser, user);
            JSONObject jObject = new JSONObject(getUserRoleResp.getResponse());
            return jObject.getString("role");
        } catch (Exception e) {
            return null;
        }
    }

    public static RestResponse getUser(User reqUser, User user) {
        try {
            return UserRestUtils.getUser(reqUser, user);
        } catch (Exception e) {
            return null;
        }
    }

    /*************************************/

    public static void deleteOnDemand() throws IOException {
        Config config = DriverFactory.getConfig();
        if (!config.getSystemUnderDebug()) {
            deleteCreatedComponents(getCatalogAsMap());
        } else {
            System.out.println("According to configuration components will not be deleted, in case to unable option to delete, please change systemUnderDebug parameter value to false ...");
        }
    }

}
