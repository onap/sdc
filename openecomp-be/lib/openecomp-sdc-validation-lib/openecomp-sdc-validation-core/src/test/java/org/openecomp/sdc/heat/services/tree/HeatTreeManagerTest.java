/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.heat.services.tree;

import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;


public class HeatTreeManagerTest {

  private Logger logger = LoggerFactory.getLogger(HeatTreeManagerTest.class);

  @Test
  public void testHeatTreeCreation() {

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

  private byte[] getFileContent(File file) {
    try {
      return FileUtils.toByteArray(new FileInputStream(file));
    } catch (IOException e) {
      logger.debug("",e);
    }

    return new byte[0];
  }
}
