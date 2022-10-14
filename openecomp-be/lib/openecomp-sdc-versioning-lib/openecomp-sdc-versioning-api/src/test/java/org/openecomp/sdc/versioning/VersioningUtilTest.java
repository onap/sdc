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

package org.openecomp.sdc.versioning;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;

public class VersioningUtilTest {

    @Test
    public void testResolveVersion() {
        Version version = new Version();
        version.setMajor(1);
        version.setMinor(1);

        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(version));
        versionInfo.setLockingUser("user");
        Version resolveVersion = VersioningUtil.resolveVersion(version, versionInfo, "user");
        Assert.assertNotNull(resolveVersion);
        Assert.assertEquals(1, resolveVersion.getMajor());
        Assert.assertEquals(1, resolveVersion.getMinor());

    }

    @Test(expected = CoreException.class)
    public void testResolveVersionNegative() {
        Version version = new Version();
        version.setMajor(1);
        version.setMinor(1);

        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(new Version()));
        versionInfo.setLockingUser("user");
        VersioningUtil.resolveVersion(null, versionInfo, "user");

    }

    @Test
    public void testResolveVersionFinalOnly() {
        Version version = new Version();
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(version));
        versionInfo.setLatestFinalVersion(version);
        Assert.assertNotNull(VersioningUtil.resolveVersion(null, versionInfo, true));

    }

    @Test(expected = CoreException.class)
    public void testResolveVersionWithNonFinalVersions() {
        Version version = new Version();
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(new Version()));
        versionInfo.setLatestFinalVersion(version);
        VersioningUtil.resolveVersion(version, versionInfo, true);
    }

    @Test
    public void testResolveVersionWithoutFinal() {
        Version version = new Version();
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(version));
        Assert.assertNotNull(VersioningUtil.resolveVersion(null, versionInfo, false));

    }

    @Test(expected = CoreException.class)
    public void testResolveVersionWithoutFinalVersion() {
        Version version = new Version();
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(version));
        VersioningUtil.resolveVersion(null, versionInfo, true);

    }

    @Test(expected = CoreException.class)
    public void testResolveVersionFinalOnlyNegative() {
        Version version = new Version();
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setActiveVersion(version);
        versionInfo.setViewableVersions(Arrays.asList(version));
        versionInfo.setLatestFinalVersion(version);
        VersioningUtil.resolveVersion(version, versionInfo, true);

    }

    @Test(expected = CoreException.class)
    public void testValidateEntityExistence() {
        VersioningUtil.validateEntityExistence(null, new VersionableEntityImplStub(), "firstClassCitizenType");
    }

    @Test(expected = CoreException.class)
    public void testValidateEntitiesExistence() {
        Set<String> entityIds = new HashSet<>();
        entityIds.add("id1");
        entityIds.add("id2");
        VersioningUtil.validateEntitiesExistence(entityIds, new VersionableEntityImplStub(),
            new VersionInfoDaoImplStub(), "firstClassCitizenType");
    }

    @Test(expected = CoreException.class)
    public void testValidateEntitiesExistenceSizeOne() {
        Set<String> entityIds = new HashSet<>();
        entityIds.add("id1");
        VersioningUtil.validateEntitiesExistence(entityIds, new VersionableEntityImplStub(),
            new VersionInfoDaoImplStub(), "firstClassCitizenType");
    }

    @Test(expected = CoreException.class)
    public void testValidateContainedEntitiesExistence() {
        Set<String> inputContainedEntityIds = new HashSet<>();
        inputContainedEntityIds.add("id1");
        inputContainedEntityIds.add("id2");
        Set<String> retrievedContainedEntityIds = new HashSet<>();
        VersioningUtil.validateContainedEntitiesExistence("containedEntityType",
            inputContainedEntityIds, new VersionableEntityImplStub(), retrievedContainedEntityIds);
    }

    @Test(expected = CoreException.class)
    public void testValidateContainedEntitiesExistenceWithIdOne() {
        Set<String> inputContainedEntityIds = new HashSet<>();
        inputContainedEntityIds.add("id1");
        inputContainedEntityIds.add("id2");
        Set<String> retrievedContainedEntityIds = new HashSet<>();
        retrievedContainedEntityIds.add("id1");
        VersioningUtil.validateContainedEntitiesExistence("containedEntityType",
            inputContainedEntityIds, new VersionableEntityImplStub(), retrievedContainedEntityIds);
    }
}
