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
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.openecomp.sdc.versioning.dao.types;

import org.junit.Assert;
import org.junit.Test;

public class VersionTest {
    @Test
    public void testValueOfPositive() {
        Version version = Version.valueOf("1.1");
        Assert.assertEquals(1, version.getMajor());
        Assert.assertEquals(1, version.getMinor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfWihLengthOne() {
        Version version = Version.valueOf("1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValueOfNegative() {
        Version version = Version.valueOf("1a.1");
    }
    @Test
    public void testValueOfNullVersion() {
        Version version = Version.valueOf(null);
        Assert.assertNull(version);
    }

    @Test
    public void testCalculateNextCandidate() {
        Version version = new Version();
        version.setMinor(1);
        Assert.assertEquals(2, version.calculateNextCandidate().getMinor());
    }

    @Test
    public void testCalculateNextCandidateFinal() {
        Version version = new Version();
        version.setMajor(0);
        Assert.assertEquals(1, version.calculateNextFinal().getMajor());
    }

    @Test
    public void testCompareTo() {
        Version version = new Version();
        version.setMajor(1);

        Version versionToCompare = new Version();
        versionToCompare.setMajor(1);
        Assert.assertEquals(0, version.compareTo(versionToCompare));
    }

    @Test
    public void testVersionEquals() {
        Version version = new Version();
        version.setMajor(0);
        version.setMinor(2);
        Version versionToCompare = new Version();
        versionToCompare.setMajor(0);
        versionToCompare.setMinor(2);

        Assert.assertTrue(version.equals(versionToCompare));
    }

    @Test
    public void testVersionClone() {
        Version version = new Version("1.0");
        Assert.assertEquals(version, version.clone());
    }

    @Test
    public void testVersionHashcode() {
        Version version = new Version("1.0");
        Assert.assertEquals((31 * (version.getMajor())+ version.getMinor()), version.hashCode());
    }
}
