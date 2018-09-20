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
import java.io.FileNotFoundException;
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
    public void testCreateTree() throws FileNotFoundException {
        HeatTreeManager heatTreeManager = new HeatTreeManager();
        addFiles(heatTreeManager);
        heatTreeManager.createTree();

        HeatStructureTree heatStructureTree = heatTreeManager.getTree();
        Assert.assertNotNull(heatStructureTree);
        Assert.assertEquals(1, heatStructureTree.getHeat().size());
        Assert.assertEquals(1, heatStructureTree.getNetwork().size());
    }

    private void addFiles(HeatTreeManager heatTreeManager) throws FileNotFoundException {
        List<URL> urlList = FileUtils.getAllLocations("mock/model/MANIFEST.json");
        InputStream inputStream = new FileInputStream(new File(urlList.get(0).getPath()));
        heatTreeManager.addFile(SdcCommon.MANIFEST_NAME, inputStream);

        List<URL> urlList1 = FileUtils.getAllLocations("mock/model/first.yaml");
        InputStream inputStream1 = new FileInputStream(new File(urlList1.get(0).getPath()));
        heatTreeManager.addFile("first.yaml", inputStream1);

        List<URL> urlList2 = FileUtils.getAllLocations("mock/model/second.yaml");
        InputStream inputStream2 = new FileInputStream(new File(urlList2.get(0).getPath()));
        heatTreeManager.addFile("second.yaml", inputStream2);

        List<URL> urlList3 = FileUtils.getAllLocations("mock/model/first.env");
        InputStream inputStream3 = new FileInputStream(new File(urlList3.get(0).getPath()));
        heatTreeManager.addFile("first.env", inputStream3);

        List<URL> urlList4 = FileUtils.getAllLocations("mock/model/base_cscf_volume.yaml");
        InputStream inputStream4 = new FileInputStream(new File(urlList4.get(0).getPath()));
        heatTreeManager.addFile("base_cscf_volume.yaml", inputStream4);

        List<URL> urlList5 = FileUtils.getAllLocations("mock/model/network.yml");
        InputStream inputStream5 = new FileInputStream(new File(urlList5.get(0).getPath()));
        heatTreeManager.addFile("network.yml", inputStream5);

        List<URL> urlList6 = FileUtils.getAllLocations("mock/model/testHeat.yml");
        InputStream inputStream6 = new FileInputStream(new File(urlList6.get(0).getPath()));
        heatTreeManager.addFile("testHeat.yml", inputStream6);

        List<URL> urlList7 = FileUtils.getAllLocations("mock/model/nested.yml");
        InputStream inputStream7 = new FileInputStream(new File(urlList7.get(0).getPath()));
        heatTreeManager.addFile("nested.yml", inputStream7);

        List<URL> urlList8 = FileUtils.getAllLocations("mock/model/base_cscf_volume.env");
        InputStream inputStream8 = new FileInputStream(new File(urlList8.get(0).getPath()));
        heatTreeManager.addFile("base_cscf_volume.env", inputStream8);
    }
}
