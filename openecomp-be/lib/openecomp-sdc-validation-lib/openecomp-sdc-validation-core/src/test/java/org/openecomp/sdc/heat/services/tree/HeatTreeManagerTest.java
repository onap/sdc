/*
 * Copyright © 2018 European Support Limited
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

package org.openecomp.sdc.heat.services.tree;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HeatTreeManagerTest {

  @Test
  public void testHeatTreeCreation() throws IOException {

    FileContentHandler fileContentMap = new FileContentHandler();
    URL url = this.getClass().getResource("/heatTreeValidationOutput");

    File templateDir = new File(url.getFile());
    File[] files = templateDir.listFiles();

    if (files == null || files.length == 0) {
      return;
    }

    for (File file : files) {
      fileContentMap.addFile(file.getName(), getFileContent(file));
    }

    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileContentMap);
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
    Assert.assertEquals(tree.getHeat().size(), 2);
  }

  @Test
  public void testHeatTreeArtifactsCreated() throws IOException {

    FileContentHandler fileContentMap = new FileContentHandler();
    URL url = this.getClass().getResource("/heatTreeArtifactsValidationOutput");

    File templateDir = new File(url.getFile());
    File[] files = templateDir.listFiles();

    if (files == null || files.length == 0) {
      return;
    }

    for (File file : files) {
      fileContentMap.addFile(file.getName(), getFileContent(file));
    }

    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileContentMap);
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();
    Assert.assertNotNull(tree);
    Assert.assertEquals(tree.getHeat().size(), 3);
    verifyHeatArtifacts(tree, "ocgmgr.yaml", 1);
    verifyHeatArtifacts(tree, "ocgapp.yaml", 0);
    verifyHeatArtifacts(tree, "base_ocg.yaml", 0);

  }

  private void verifyHeatArtifacts(HeatStructureTree tree, String heatName, int expectedArtifactNum ) {
    HeatStructureTree heat = HeatStructureTree.getHeatStructureTreeByName(tree.getHeat(), heatName);
    Assert.assertNotNull(heat);
    if (expectedArtifactNum > 0) {
      Assert.assertNotNull(heat.getArtifacts());
      Assert.assertEquals(heat.getArtifacts().size(), expectedArtifactNum);
    } else {
      Assert.assertNull(heat.getArtifacts());
    }
  }


  private byte[] getFileContent(File file) throws IOException {
    try(InputStream inputStream = new FileInputStream(file)) {
      return FileUtils.toByteArray(inputStream);
    }
  }
}
