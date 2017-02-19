package org.openecomp.sdc.heat.services.tree;

import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class HeatTreeManagerTest {

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
    Assert.assertEquals(tree.getHEAT().size(), 2);
  }

  private byte[] getFileContent(File file) {
    try {
      return FileUtils.toByteArray(new FileInputStream(file));
    } catch (IOException e) {
      e.printStackTrace();
    }

    return new byte[0];
  }


}
