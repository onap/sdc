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

package org.onap.sdc.backend.ci.tests.servicefilter;

import java.util.Collections;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceFilterDetails;
import org.onap.sdc.backend.ci.tests.datatypes.enums.LifeCycleStatesEnum;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.ComponentInstanceReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.PropertyReqDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ComponentInstanceRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.LifecycleRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.PropertyRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ResponseParser;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceFilterUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ServiceFilterTests extends ComponentBaseTest {
    @Rule
    public static TestName name = new TestName();

    private static ServiceReqDetails externalService;
    private static ComponentInstanceReqDetails componentInstanceReqDetails;
    private static User user = null;

    @BeforeTest
    public void init() throws Exception {
        user = new ElementFactory().getDefaultUser(UserRoleEnum.DESIGNER);

        ServiceReqDetails internalService;
        //Create External Service
        externalService = new ElementFactory().getDefaultService();
        externalService.setName("ExternalService" + Math.random());
        new ServiceRestUtils().createService(externalService, user);

        //Create Internal Service
        internalService = new ElementFactory().getDefaultService();
        internalService.setName("InternalService" + Math.random());
        new ServiceRestUtils().createService(internalService, user);

        //Add property services
        //#PropertyOne
        PropertyReqDetails propertyReqDetails = new ElementFactory().getDefaultStringProperty();
        propertyReqDetails.setName("StringProp1");
        String body = propertyReqDetails.propertyToJsonString();
        PropertyRestUtils.createServiceProperty(externalService.getUniqueId(), body, user);
        PropertyRestUtils.createServiceProperty(internalService.getUniqueId(), body, user);
        //#PropertyTwo
        propertyReqDetails.setName("StringProp2");
        body = propertyReqDetails.propertyToJsonString();
        RestResponse response = PropertyRestUtils.createServiceProperty(externalService.getUniqueId(), body, user);
        response = PropertyRestUtils.createServiceProperty(internalService.getUniqueId(), body, user);

        //CheckIn internal Service
        response = new LifecycleRestUtils().changeServiceState(internalService, user, "0.1",
                LifeCycleStatesEnum.CHECKIN,
                "{\"userRemarks\":\"CheckIn\"}");
        BaseRestUtils.checkSuccess(response);
        if (response.getErrorCode() == BaseRestUtils.STATUS_CODE_SUCCESS) {
            internalService.setUniqueId(ResponseParser.getUniqueIdFromResponse(response));
        }
        //Make internal service as component instance
        componentInstanceReqDetails =
                new ElementFactory().getDefaultComponentInstance(internalService.getUniqueId(), "ServiceProxy");
        response = ComponentInstanceRestUtils.createComponentInstance(componentInstanceReqDetails,
                user, externalService.getUniqueId(), ComponentTypeEnum.SERVICE);
        BaseRestUtils.checkCreateResponse(response);
        if (response.getErrorCode() == BaseRestUtils.STATUS_CODE_CREATED) {
            componentInstanceReqDetails.setUniqueId(ResponseParser.getUniqueIdFromResponse(response));
            componentInstanceReqDetails.setName(ResponseParser.getNameFromResponse(response));
        }
        //Mark as dependent
        componentInstanceReqDetails.setDirectives(Collections.singletonList("selectable"));
        response = ComponentInstanceRestUtils.updateComponentInstance(componentInstanceReqDetails,
                user, externalService.getUniqueId(), ComponentTypeEnum.SERVICE);
        BaseRestUtils.checkSuccess(response);
    }

    @Test
    public void createServiceFilter() throws Exception {
        //Add Service Filter
        ServiceFilterDetails serviceFilterDetails = new ElementFactory().getDefaultEqualOperatorFilter("StringProp1", "value");
        RestResponse restResponse = ServiceFilterUtils.createServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), serviceFilterDetails, user);
        logger.info("CreateServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createServiceFilter")
    public void updateServiceFilter() throws Exception {
        //Update Service Filter
        ServiceFilterDetails serviceFilterDetails =
                new ElementFactory().getDefaultEqualOperatorFilter("StringProp1", "updated");
        RestResponse restResponse = ServiceFilterUtils.updateServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), Collections.singletonList(serviceFilterDetails),  user);
        logger.info("UpdateServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    //    @Test(dependsOnMethods = "updateServiceFilter")
    public void deleteServiceFilter() throws Exception {
        //Delete Service Filter
        RestResponse restResponse = ServiceFilterUtils.deleteServiceFilter(externalService.getUniqueId(),
                componentInstanceReqDetails.getUniqueId(), 0, user);
        logger.info("DeleteServiceFilter Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }
}
