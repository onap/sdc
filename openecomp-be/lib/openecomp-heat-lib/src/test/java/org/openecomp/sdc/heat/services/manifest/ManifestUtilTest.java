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
 * /
 *
 */

package org.openecomp.sdc.heat.services.manifest;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;

public class ManifestUtilTest {

    @Test
    public void testGetFileAndItsEnvNull() {
        Assert.assertTrue(ManifestUtil.getFileAndItsEnv(new ManifestContent()).isEmpty());
    }

    @Test
    public void testGetFileAndItsEnv() {

        Assert.assertEquals(ManifestUtil.getFileAndItsEnv(createManifestContent()).size(), 1);
    }

    @Test
    public void testGetFileTypeMapDataNull() {
        Assert.assertTrue(ManifestUtil.getFileTypeMap(new ManifestContent()).isEmpty());
    }

    @Test
    public void testGetFileTypeMap() {
        Map<String, FileData.Type> fileTypeMap = ManifestUtil.getFileTypeMap(createManifestContent());

        Assert.assertEquals(fileTypeMap.size(), 2);
        Assert.assertTrue(fileTypeMap.containsKey("Main.yml") && fileTypeMap.containsKey("Test.yml"));
    }

    @Test
    public void testGetFileTypeMapDataBlank() {
        ManifestContent manifestContent = new ManifestContent();
        manifestContent.setData(Collections.emptyList());

        Assert.assertTrue(ManifestUtil.getFileTypeMap(manifestContent).isEmpty());
    }

    @Test
    public void testGetArtifactsDataNull() {
        Assert.assertTrue(ManifestUtil.getArtifacts(new ManifestContent()).isEmpty());
    }

    @Test
    public void testGetArtifacts() {
        ManifestContent manifestContent = createManifestContent();
        manifestContent.getData().get(0).getData().get(0).setType(FileData.Type.OTHER);

        Set<String> typeSet = ManifestUtil.getArtifacts(manifestContent);
        Assert.assertEquals(typeSet.size(), 1);
        Assert.assertTrue(typeSet.contains("Test.yml"));
    }

    @Test
    public void testGetBaseFilesDataNull() {
        Assert.assertTrue(ManifestUtil.getArtifacts(new ManifestContent()).isEmpty());
    }

    @Test
    public void testGetBaseFiles() {
        Set<String> typeSet = ManifestUtil.getBaseFiles(createManifestContent());
        Assert.assertEquals(typeSet.size(), 1);
        Assert.assertTrue(typeSet.contains("Main.yml"));
    }

    private ManifestContent createManifestContent() {

        FileData fileData1 = new FileData();
        fileData1.setFile("Test.yml");
        fileData1.setType(FileData.Type.HEAT_ENV);

        FileData fileData = new FileData();
        fileData.setFile("Main.yml");
        fileData.setType(FileData.Type.HEAT_ENV);
        fileData.setBase(true);

        fileData.setData(Collections.singletonList(fileData1));

        ManifestContent manifestContent = new ManifestContent();
        manifestContent.setData(Collections.singletonList(fileData));

        return manifestContent;
    }
}
