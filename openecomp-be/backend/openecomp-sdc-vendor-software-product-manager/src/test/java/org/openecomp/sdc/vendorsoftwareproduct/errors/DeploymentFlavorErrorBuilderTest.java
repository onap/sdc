/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct.errors;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;

public class DeploymentFlavorErrorBuilderTest {

    @Test
    public void testGetInvalidComponentIdErrorBuilder() {
        //when
        ErrorCode errorCode = DeploymentFlavorErrorBuilder.getInvalidComponentIdErrorBuilder();

        //then
        assertEquals(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_ID, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Invalid request, Component provided does not exist for this VSP.", errorCode.message());
    }

    @Test
    public void testGetInvalidComponentComputeAssociationErrorBuilder() {
        //when
        ErrorCode errorCode = DeploymentFlavorErrorBuilder.getInvalidComponentComputeAssociationErrorBuilder("");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.INVALID_COMPONENT_COMPUTE_ASSOCIATION, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals(
            "VSP cannot be submitted with an invalid Deployment Flavor. All Deployment Flavor should have atleast a VFC included with it's required Compute needs. "
                + "Please fix the Deployment Flavor  and re-submit the VSP.", errorCode.message());
    }

    @Test
    public void testGetFeatureGroupMandatoryErrorBuilder() {
        //when
        ErrorCode errorCode = DeploymentFlavorErrorBuilder.getFeatureGroupMandatoryErrorBuilder("");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.FEATUREGROUP_REQUIRED_IN_DEPLOYMENT_FLAVOR, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("VSP cannot be submitted with an invalid Deployment Flavor. All Deployment Flavor should have "
                + "FeatureGroup. Please fix the Deployment Flavor  and re-submit the VSP.",
            errorCode.message());
    }

    @Test
    public void testGetDeploymentFlavorNameFormatErrorBuilder() {
        //when
        ErrorCode errorCode = DeploymentFlavorErrorBuilder.getDeploymentFlavorNameFormatErrorBuilder("");

        //then
        assertEquals(VendorSoftwareProductErrorCodes.DEPLOYMENT_FLAVOR_NAME_FORMAT_NOT_ALLOWED, errorCode.id());
        assertEquals(ErrorCategory.APPLICATION, errorCode.category());
        assertEquals("Field does not conform to predefined criteria: name : must match ", errorCode.message());
    }
}
