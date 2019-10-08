/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import org.openecomp.sdc.TestUtils;

public class SoftwareInformationArtifactYamlParserTest {

    @Test
    public void parse() throws IOException {
        //given
        final byte[] resourceAsByteArray = TestUtils
            .getResourceAsByteArray("artifacts/pnfSoftwareInformation/pnf-sw-information.yaml");
        //when
        final Optional<PnfSoftwareInformation> pnfSoftwareInformation = SoftwareInformationArtifactYamlParser
            .parse(resourceAsByteArray);
        //then
        final PnfSoftwareVersion expectedPnfSoftwareVersion1 = new PnfSoftwareVersion("version1",
            "first software version of PNF");
        final PnfSoftwareVersion expectedPnfSoftwareVersion2 = new PnfSoftwareVersion("version2",
            "second software version of PNF");
        assertThat("The software information should be parsed", pnfSoftwareInformation.isPresent(), is(true));
        pnfSoftwareInformation.ifPresent(softwareInformation -> {
            assertThat("The software information provider should be as expected",
                softwareInformation.getProvider(), is(equalTo("Ericsson")));
            assertThat("The software information description should be as expected",
                softwareInformation.getDescription(), is(equalTo("pnf software information")));
            assertThat("The software information version should be as expected",
                softwareInformation.getVersion(), is(equalTo("1.0")));
            assertThat("The software versions should contain expected versions",
                softwareInformation.getSoftwareVersionSet(),
                hasItems(expectedPnfSoftwareVersion1, expectedPnfSoftwareVersion2));
        });
    }

}