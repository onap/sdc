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

package org.openecomp.sdc.ci.tests.execute.sanity;

import java.util.Arrays;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.GeneralPageElements;
import org.openecomp.sdc.ci.tests.pages.ServiceGeneralPage;
import org.openecomp.sdc.ci.tests.utilities.ServiceUIUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.validation.ServiceValidation;
import org.testng.annotations.Test;

public class Service extends SetupCDTest {

    @Test
    public void updateService() {
        // Create Service
        ServiceReqDetails serviceMetadata = ElementFactory.getDefaultService();
        ServiceUIUtils.createService(serviceMetadata);

        // Update Service
        ServiceGeneralPage.deleteOldTags(serviceMetadata);
        serviceMetadata.setName(ElementFactory.getServicePrefix() + "UpdatedName" + serviceMetadata.getName());
        serviceMetadata.setDescription("updatedDescriptionSanity");
        serviceMetadata.setProjectCode("654321");
        serviceMetadata.setContactId("cs6543");
        serviceMetadata.getTags().addAll(Arrays.asList("updatedTag", "oneMoreUpdatedTag", "lastOne UpdatedTag"));
        ServiceUIUtils.setServiceCategory(serviceMetadata, ServiceCategoriesEnum.VOIP);
        ServiceUIUtils.fillServiceGeneralPage(serviceMetadata);
        GeneralPageElements.clickCreateButton();

        ServiceValidation.verifyServiceUpdatedInUi(serviceMetadata);
    }

    @Override
    protected UserRoleEnum getRole() {
        return UserRoleEnum.DESIGNER;
    }

}
