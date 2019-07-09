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

package org.openecomp.sdcrests.vsp.rest.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileStatus;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.UploadFileResponseDto;

/**
 * This class was generated.
 */
public class MapUploadFileResponseToUploadFileResponseDtoTest {

    @Test()
    public void testConversion() {

        final UploadFileResponse source = new UploadFileResponse();

        final UploadFileStatus status = UploadFileStatus.Failure;
        source.setStatus(status);

        final OnboardingTypesEnum onboardingOrigin = OnboardingTypesEnum.MANUAL;
        source.setOnboardingType(onboardingOrigin);

        final String networkPackageName = "2527ac7d-76bd-4263-9472-e767e1c25fbb";
        source.setNetworkPackageName(networkPackageName);

        final UploadFileResponseDto target = new UploadFileResponseDto();
        final MapUploadFileResponseToUploadFileResponseDto mapper = new MapUploadFileResponseToUploadFileResponseDto();
        mapper.doMapping(source, target);

        assertSame(status, target.getStatus());
        assertEquals(onboardingOrigin.toString(), target.getOnboardingOrigin());
        assertEquals(networkPackageName, target.getNetworkPackageName());
    }
}
