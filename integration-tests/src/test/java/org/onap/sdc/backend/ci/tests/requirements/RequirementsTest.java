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

package org.onap.sdc.backend.ci.tests.requirements;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.onap.sdc.backend.ci.tests.datatypes.enums.UserRoleEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.RestResponse;
import org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.backend.ci.tests.utils.rest.BaseRestUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.RequirementsUtils;
import org.onap.sdc.backend.ci.tests.utils.rest.ServiceRestUtils;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.onap.sdc.backend.ci.tests.api.ComponentBaseTest;
import org.onap.sdc.backend.ci.tests.datatypes.RequirementDetails;
import org.onap.sdc.backend.ci.tests.datatypes.ServiceReqDetails;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.onap.sdc.backend.ci.tests.utils.general.AtomicOperationUtils.getServiceObject;

public class RequirementsTest extends ComponentBaseTest {
    @Rule
    public static final TestName name = new TestName();

    private  ServiceReqDetails component;
    private  User user = null;

    @BeforeTest
    public void init() throws Exception {
        user = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        component = ElementFactory.getDefaultService();
        component.setName("comp_req" + Math.random());
        ServiceRestUtils.createService(component, user);
    }

    @Test
    public void createRequirementTest() throws Exception {

        RequirementDetails requirement = createRequirement();
        RestResponse restResponse = RequirementsUtils.createRequirement(component.getUniqueId(),
                Collections.singletonList(requirement), user);
        logger.info("createRequirement Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "createRequirementTest")
    public void updateRequirementTest() throws Exception {

        RequirementDetails requirement = createRequirement();
        requirement.setMaxOccurrences("10");
        requirement.setMinOccurrences("4");
        RestResponse restResponse = RequirementsUtils.updateRequirement(component.getUniqueId(),
                Collections.singletonList(requirement), user);
        logger.info("updateRequirement Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "updateRequirementTest")
    public void getRequirementTest() throws Exception {
        Service service = AtomicOperationUtils.getServiceObject(component.getUniqueId());

        List<RequirementDefinition> requirementDefinitionList = service.getRequirements().values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = RequirementsUtils.getRequirement(component.getUniqueId(),
                requirementDefinitionList.get(0).getUniqueId(),  user);
        logger.info("getRequirementTest Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    @Test(dependsOnMethods = "getRequirementTest")
    public void deleteRequirementTest() throws Exception {
        Service service = AtomicOperationUtils.getServiceObject(component.getUniqueId());

        List<RequirementDefinition> requirementDefinitionList = service.getRequirements().values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList());

        RestResponse restResponse = RequirementsUtils.deleteRequirement(component.getUniqueId(),
                requirementDefinitionList.get(0).getUniqueId(),  user);
        logger.info("deleteRequirementTest Response Code:" + restResponse.getErrorCode());
        Assert.assertEquals((int) restResponse.getErrorCode(), BaseRestUtils.STATUS_CODE_SUCCESS);
    }

    private  RequirementDetails createRequirement() {
        RequirementDetails  requirementDetails = new RequirementDetails();
        requirementDetails.setName("req" + Math.random());
        requirementDetails.setCapability("tosca.capabilities.network.Bindable");

        return requirementDetails;
    }
}
