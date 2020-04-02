/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 Samsung Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.versioning.impl;

import static junit.framework.TestCase.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class MajorVersionCalculatorImplTest {
    private MajorVersionCalculatorImpl majorVersionCalculator;

    @Before
    public void setUp() {
        majorVersionCalculator = new MajorVersionCalculatorImpl();
    }

    @Test
    public void testCalculateNullBaseVer() {
        String result = majorVersionCalculator.calculate(null, VersionCreationMethod.major);

        assertThat(result, is("1.0"));
    }

    @Test
    public void testCalculateNotNullBaseVer() {
        String result = majorVersionCalculator.calculate("2.0", VersionCreationMethod.major);

        assertThat(result, is("3.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateFail() {
        majorVersionCalculator.calculate("1", VersionCreationMethod.major);

        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void testInjectAdditionalInfo() {
        Version version = new Version();
        version.setAdditionalInfo(new HashMap<>());
        version.setStatus(VersionStatus.Certified);

        Set<String> versions = new HashSet<>();
        versions.add("3.0");

        majorVersionCalculator.injectAdditionalInfo(version, versions);

        assertThat(version.getAdditionalInfo(), notNullValue());
        assertThat(version.getAdditionalInfo().size(), is(1));
        assertThat(version.getAdditionalInfo().get("OptionalCreationMethods"), is(Collections.singleton(VersionCreationMethod.major)));
    }
}