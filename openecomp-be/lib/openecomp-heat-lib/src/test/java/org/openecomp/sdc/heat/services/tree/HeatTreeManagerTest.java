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

package org.openecomp.sdc.heat.services.tree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;

public class HeatTreeManagerTest {

    @Test
    public void testCreateTree() throws IOException {
        HeatTreeManager heatTreeManager = new HeatTreeManager();
        addFile(heatTreeManager, "mock/model/MANIFEST.json", SdcCommon.MANIFEST_NAME);
        addFile(heatTreeManager, "mock/model/first.yaml", "first.yaml");
        addFile(heatTreeManager, "mock/model/second.yaml", "second.yaml");
        addFile(heatTreeManager, "mock/model/first.env", "first.env");
        addFile(heatTreeManager, "mock/model/base_cscf_volume.yaml", "base_cscf_volume.yaml");
        addFile(heatTreeManager, "mock/model/network.yml", "network.yml");
        addFile(heatTreeManager, "mock/model/testHeat.yml", "testHeat.yml");
        addFile(heatTreeManager, "mock/model/nested.yml", "nested.yml");
        addFile(heatTreeManager, "mock/model/base_cscf_volume.env", "base_cscf_volume.env");

        heatTreeManager.createTree();

        HeatStructureTree heatStructureTree = heatTreeManager.getTree();
        Assert.assertNotNull(heatStructureTree);
        Assert.assertEquals(1, heatStructureTree.getHeat().size());
        Assert.assertEquals(1, heatStructureTree.getNetwork().size());
    }

    private void addFile(HeatTreeManager heatTreeManager, String fileLocation, String fileName)
            throws IOException {

        List<URL> urlList = FileUtils.getAllLocations(fileLocation);
        try (InputStream inputStream = new FileInputStream(new File(urlList.get(0).getPath()))) {
            heatTreeManager.addFile(fileName, inputStream);
        }
    }
}
