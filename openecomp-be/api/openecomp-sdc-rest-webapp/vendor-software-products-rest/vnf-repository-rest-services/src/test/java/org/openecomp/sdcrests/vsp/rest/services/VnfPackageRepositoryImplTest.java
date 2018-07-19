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

package org.openecomp.sdcrests.vsp.rest.services;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.openecomp.sdc.versioning.dao.types.Version;

/**
 * @author evitaliy
 * @since 19 Jul 2018
 */
public class VnfPackageRepositoryImplTest {

    @Test
    public void versionFoundWhenInList() {
        VnfPackageRepositoryImpl vnfRepository = new VnfPackageRepositoryImpl();
        List<Version> versions = new ArrayList<>(3);
        versions.add(new Version("1243"));
        versions.add(new Version("3434"));
        versions.add(new Version("398"));
        assertTrue("Expected to find the version", vnfRepository.findVersion(versions, "3434").isPresent());
    }

    @Test
    public void versionNotFoundWhenInList() {
        VnfPackageRepositoryImpl vnfRepository = new VnfPackageRepositoryImpl();
        List<Version> versions = new ArrayList<>(1);
        versions.add(new Version("1243"));
        assertFalse("Did not expect to find the version", vnfRepository.findVersion(versions, "3434").isPresent());
    }

    @Test
    public void configurationLoadedFromFile() {
        final String prefix = "http://10.57.30.20:1111/";
        assertEquals(prefix + "download-vnf-31", VnfPackageRepositoryImpl.Configuration.INSTANCE.getDownloadUri());
        assertEquals(prefix + "get-vnf-13", VnfPackageRepositoryImpl.Configuration.INSTANCE.getGetUri());
    }
}