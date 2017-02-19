package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.translator.factory.HeatToToscaTranslatorFactory;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.types.MessageContainerUtil;
import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.Assert.assertEquals;

public class BaseResourceTranslationTest {

  protected String inputFilesPath;
  protected String outputFilesPath;
  private HeatToToscaTranslator heatToToscaTranslator;
  private File translatedZipFile;

  private Map<String, byte[]> expectedResultMap = new HashMap<>();
  private Set<String> expectedResultFileNameSet = new HashSet<>();

  @Before
  public void setUp() throws IOException {
    initTranslatorAndTranslate();
  }

  protected void initTranslatorAndTranslate() throws IOException {
    heatToToscaTranslator = HeatToToscaTranslatorFactory.getInstance().createInterface();
    translatedZipFile = translateZipFile();
  }

  protected void testTranslation() throws IOException {

    URL url = BaseResourceTranslationTest.class.getResource(outputFilesPath);

    String path = url.getPath();
    File pathFile = new File(path);
    File[] files = pathFile.listFiles();
    Assert.assertNotNull("manifest files is empty", files);
    for (File expectedFile : files) {
      expectedResultFileNameSet.add(expectedFile.getName());
      try (FileInputStream input = new FileInputStream(expectedFile)) {
        expectedResultMap.put(expectedFile.getName(), FileUtils.toByteArray(input));
      }
    }

    try (FileInputStream fis = new FileInputStream(translatedZipFile);
         ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {
      ZipEntry entry;
      String name;
      String expected;
      String actual;

      while ((entry = zis.getNextEntry()) != null) {

        name = entry.getName()
            .substring(entry.getName().lastIndexOf(File.separator) + 1, entry.getName().length());
        if (expectedResultFileNameSet.contains(name)) {
          expected = new String(expectedResultMap.get(name)).trim().replace("\r", "");
          actual = new String(FileUtils.toByteArray(zis)).trim().replace("\r", "");
          assertEquals("difference in file: " + name, expected, actual);

          expectedResultFileNameSet.remove(name);
        }
      }
      if (expectedResultFileNameSet.isEmpty()) {
        expectedResultFileNameSet.forEach(System.out::println);
      }
    }
    assertEquals(0, expectedResultFileNameSet.size());
  }

  private File translateZipFile() throws IOException {
    String zipFilename = "VSP.zip";
    URL inputFilesUrl = this.getClass().getResource(inputFilesPath);
    String path = inputFilesUrl.getPath();
    TestUtils.addFilesToTranslator(heatToToscaTranslator, path);
    TranslatorOutput translatorOutput = heatToToscaTranslator.translate();
    Assert.assertNotNull(translatorOutput);
    if (MapUtils.isNotEmpty(translatorOutput.getErrorMessages()) && MapUtils.isNotEmpty(
        MessageContainerUtil
            .getMessageByLevel(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, translatorOutput.getErrorMessages()))) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder()).withMessage(
          "Error in validation " + getErrorAsString(translatorOutput.getErrorMessages()))
          .withId("Validation Error").withCategory(ErrorCategory.APPLICATION).build());
    }
    File file = new File(path + "/" + zipFilename);
    file.createNewFile();

    try (FileOutputStream fos = new FileOutputStream(file)) {
      ToscaFileOutputService toscaFileOutputService = new ToscaFileOutputServiceCsarImpl();
      fos.write(
          toscaFileOutputService.createOutputFile(translatorOutput.getToscaServiceModel(), null));
    }

    return file;
  }

  private String getErrorAsString(Map<String, List<org.openecomp.sdc.datatypes.error.ErrorMessage>> errorMessages) {
    StringBuilder sb = new StringBuilder();
    errorMessages.entrySet().forEach(
        entry -> sb.append("File:").append(entry.getKey()).append(System.lineSeparator())
            .append(getErrorList(entry.getValue())));

    return sb.toString();
  }

  private String getErrorList(List<org.openecomp.sdc.datatypes.error.ErrorMessage> errors) {
    StringBuilder sb = new StringBuilder();
    errors.stream().forEach(
        error -> sb.append(error.getMessage()).append("[").append(error.getLevel()).append("]")
            .append(System.lineSeparator()));
    return sb.toString();
  }

}
