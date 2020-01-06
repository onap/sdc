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

package org.openecomp.sdc.ci.tests.validation;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import com.aventstack.extentreports.Status;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.datatypes.ComponentReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.execute.setup.ExtentTestActions;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.RestCDUtils;

public final class ServiceValidation {

    private ServiceValidation() {
    }

    public static void verifyNumOfComponentInstances(ComponentReqDetails component, String version, int numOfVFC,
                                                     User user) {
        SetupCDTest.getExtendTest()
            .log(Status.INFO, String.format("Verifying the number of components on the canvas; should be %s", numOfVFC));
        String responseAfterDrag = null;
        component.setVersion(version);
        if (component instanceof ServiceReqDetails) {
            responseAfterDrag = RestCDUtils.getService((ServiceReqDetails) component, user).getResponse();
        } else if (component instanceof ResourceReqDetails) {
            responseAfterDrag = RestCDUtils.getResource((ResourceReqDetails) component, user).getResponse();
        }
        int size = 0;
        JSONObject jsonResource = (JSONObject) JSONValue.parse(responseAfterDrag);
        if (jsonResource.get("componentInstances") != null) {
            size = ((JSONArray) jsonResource.get("componentInstances")).size();
            assertEquals(numOfVFC, size,
                "Expected number of component instances is " + numOfVFC + ", but actual is " + size);
            ExtentTestActions.log(Status.INFO, "The number of components on the canvas was verified.");
        } else {
            fail("Expected number of component instances is " + numOfVFC + ", but actual is " + size);
        }
    }

    public static void verifyServiceUpdatedInUi(final ServiceReqDetails service) {
        assertEquals(ResourceGeneralPage.getNameText(), service.getName());
        assertEquals(ResourceGeneralPage.getDescriptionText(), service.getDescription());
        assertEquals(ServiceGeneralPage.getCategoryText(), service.getCategory());
        assertEquals(ServiceGeneralPage.getProjectCodeText(), service.getProjectCode());
        for (final String tag : ServiceGeneralPage.getTags()) {
            assertTrue(service.getTags().contains(tag), String.format("Could not find expected tag '%s'", tag));
        }
        assertEquals(ResourceGeneralPage.getContactIdText(), service.getContactId());
    }

}
