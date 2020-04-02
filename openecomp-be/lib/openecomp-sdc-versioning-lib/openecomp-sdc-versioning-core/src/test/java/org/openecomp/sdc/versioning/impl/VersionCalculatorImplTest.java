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

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;
import org.openecomp.sdc.versioning.types.VersionCreationMethod;

public class VersionCalculatorImplTest {

    private VersionCalculatorImpl versionCalculator;

    @Before
    public void setUp() {
        versionCalculator = new VersionCalculatorImpl();
    }

    @Test
    public void testCalculateNullBaseVer() {
        String result = versionCalculator.calculate(null, VersionCreationMethod.major);

        assertThat(result, is("1.0"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCalculateFail() {
        versionCalculator.calculate("2", VersionCreationMethod.major);

        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void testCalculateNotNullBaseVerMajor() {
        String result = versionCalculator.calculate("2.1", VersionCreationMethod.major);

        assertThat(result, is("3.0"));
    }

    @Test
    public void testCalculateNotNullBaseVerMinor() {
        String result = versionCalculator.calculate("2.1", VersionCreationMethod.minor);

        assertThat(result, is("2.2"));
    }

    @Test
    public void testInjectAdditionalInfo() {
        Version version = new Version();
        version.setAdditionalInfo(new HashMap<>());
        version.setStatus(VersionStatus.Certified);

        Set<String> versions = new HashSet<>();
        versions.add("3.0");

        versionCalculator.injectAdditionalInfo(version, versions);

        assertThat(version.getAdditionalInfo(), notNullValue());
        assertThat(version.getAdditionalInfo().size(), is(1));
        assertThat(version.getAdditionalInfo().get("OptionalCreationMethods"),
                is(ImmutableSet.of(VersionCreationMethod.major, VersionCreationMethod.minor)));
    }
}