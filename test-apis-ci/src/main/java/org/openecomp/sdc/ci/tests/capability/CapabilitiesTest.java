/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

package org.openecomp.sdc.ci.tests.capability;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.CapabilityDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.CapabilityRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.openecomp.sdc.ci.tests.utils.general.AtomicOperationUtils.getServiceObject;

public class CapabilitiesTest extends ComponentBaseTest {
    @Rule
    public static TestName name = new TestName();

    private static ServiceReqDetails component;
    private static User user = null;

    public CapabilitiesTest() {
        super(name, CapabilitiesTest.class.getName());
    }

    @BeforeTest
    public void init() throws Exception {
        user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        component = ElementFactory.getDefaultService();
        component.setName("comp_cap" + Math.random());
        ServiceRestUtils.createService(component, user);
    }

    @Test
    public void createCapabilityTest() throws Exception {

        CapabilityDetails capability = createCapability();
        RestResponse restResponse = CapabilityRestUtils.createCapability(component.getUniqueId(),
                Collections.singletonList(capability), user);
        logger.info("createCapability Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }



    @Test(dependsOnMethods = "createCapabilityTest")
    public void updateCapabilityTest() throws Exception {

        CapabilityDetails capability = createCapability();
        capability.setMaxOccurrences("10");
        capability.setMinOccurrences("4");
        RestResponse restResponse = CapabilityRestUtils.updateCapability(component.getUniqueId(),
                Collections.singletonList(capability), user);
        logger.info("updateCapability Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateCapabilityTest")
    public void getCapabilityTest() throws Exception {
        Service service = getServiceObject(component.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = service.getCapabilities().values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.getCapability(component.getUniqueId(),
                capabilityDefinitionList.get(0).getUniqueId(),  user);
        logger.info("getCapabilityTest Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getCapabilityTest")
    public void deleteCapabilityTest() throws Exception {
        Service service = getServiceObject(component.getUniqueId());

        List<org.openecomp.sdc.be.model.CapabilityDefinition> capabilityDefinitionList = service.getCapabilities().values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = CapabilityRestUtils.deleteCapability(component.getUniqueId(),
                capabilityDefinitionList.get(0).getUniqueId(),  user);
        logger.info("deleteCapabilityTest Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    private  CapabilityDetails createCapability() {
        CapabilityDetails  capabilityDetails = new CapabilityDetails();
        capabilityDetails.setName("cap" + Math.random());
        capabilityDetails.setType("tosca.capabilities.network.Bindable");

        return capabilityDetails;
    }
}
