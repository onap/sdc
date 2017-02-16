package org.openecomp.sdc.vendorsoftwareproduct.tree;

import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.core.utilities.file.FileUtils;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class TreeBaseTest {

  String INPUT_DIR;


  HeatTreeManager initHeatTreeManager() {
    HeatTreeManager heatTreeManager = new HeatTreeManager();

    URL url = TreeBaseTest.class.getResource(INPUT_DIR);

    File inputDir = null;
    try {
      inputDir = new File(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
    File[] files = inputDir.listFiles();
    for (File inputFile : files) {
      heatTreeManager.addFile(inputFile.getName(), FileUtils.loadFileToInputStream(
          INPUT_DIR.replace("/", File.separator) + File.separator + inputFile.getName()));
    }
    return heatTreeManager;
  }
}
