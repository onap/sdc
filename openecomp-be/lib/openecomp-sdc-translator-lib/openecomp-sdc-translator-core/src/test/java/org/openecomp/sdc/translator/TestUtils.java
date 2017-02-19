package org.openecomp.sdc.translator;

import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.utilities.file.FileUtils;
import org.junit.Assert;

import java.io.*;

public class TestUtils {
  private static final String MANIFEST_NAME = AsdcCommon.MANIFEST_NAME;
  private static String zipFilename = "VSP.zip";
  private static String validationFilename = "validationOutput.json";

  private TestUtils() {
  }


  public static void addFilesToTranslator(HeatToToscaTranslator heatToToscaTranslator, String path)
      throws IOException {
    File manifestFile = new File(path);
    File[] files = manifestFile.listFiles();
    byte[] fileContent;

    Assert.assertNotNull("manifest files is empty", files);

    for (File file : files) {

      try (FileInputStream fis = new FileInputStream(file)) {

        fileContent = FileUtils.toByteArray(fis);

        if (file.getName().equals(MANIFEST_NAME)) {
          heatToToscaTranslator.addManifest(MANIFEST_NAME, fileContent);
        } else {
          if (!file.getName().equals(zipFilename) && (!file.getName().equals(validationFilename))) {
            heatToToscaTranslator.addFile(file.getName(), fileContent);
          }
        }
      }
    }
  }
}
