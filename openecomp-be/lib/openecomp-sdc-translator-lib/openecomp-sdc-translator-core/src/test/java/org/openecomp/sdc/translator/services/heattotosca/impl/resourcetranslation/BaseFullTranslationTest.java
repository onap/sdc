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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import static org.junit.Assert.assertEquals;

import org.apache.commons.collections4.MapUtils;
import org.junit.Assert;
import org.junit.Before;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.translator.factory.HeatToToscaTranslatorFactory;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.util.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;
import org.openecomp.sdc.tosca.services.impl.ToscaFileOutputServiceCsarImpl;
import org.openecomp.sdc.translator.TestUtils;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class BaseFullTranslationTest {

  protected String inputFilesPath;
  protected String outputFilesPath;
  protected TranslationContext translationContext;

  private String zipFilename = "VSP.zip";
  private HeatToToscaTranslator heatToToscaTranslator;
  private File translatedZipFile;

  private Map<String, byte[]> expectedResultMap = new HashMap<>();
  private Set<String> expectedResultFileNameSet = new HashSet<>();

  @Before
  public void setUp() throws IOException {
    initTranslatorAndTranslate();
  }

  protected void testTranslationWithInit() throws IOException {
      initTranslatorAndTranslate();
      testTranslation();
  }

  protected void initTranslatorAndTranslate() throws IOException {
    heatToToscaTranslator = HeatToToscaTranslatorFactory.getInstance().createInterface();
    translatedZipFile = translateZipFile();
  }

  protected void testTranslation() throws IOException {

    URL url = BaseFullTranslationTest.class.getResource(outputFilesPath);
    expectedResultFileNameSet = new HashSet<>();

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
    URL inputFilesUrl = this.getClass().getResource(inputFilesPath);
    String path = inputFilesUrl.getPath();
    TestUtils.addFilesToTranslator(heatToToscaTranslator, path);
    TranslatorOutput translatorOutput = heatToToscaTranslator.translate();
    Assert.assertNotNull(translatorOutput);
    if (MapUtils.isNotEmpty(translatorOutput.getErrorMessages()) && MapUtils.isNotEmpty(
        MessageContainerUtil
            .getMessageByLevel(ErrorLevel.ERROR, translatorOutput.getErrorMessages()))) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.VALIDATE_HEAT_BEFORE_TRANSLATE, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Can't translate HEAT file");
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

  private String getErrorAsString(Map<String, List<ErrorMessage>> errorMessages) {
    StringBuilder sb = new StringBuilder();
    errorMessages.entrySet().forEach(
        entry -> sb.append("File:").append(entry.getKey()).append(System.lineSeparator())
            .append(getErrorList(entry.getValue())));

    return sb.toString();
  }

  private String getErrorList(List<ErrorMessage> errors) {
    StringBuilder sb = new StringBuilder();
    errors.forEach(
        error -> sb.append(error.getMessage()).append("[").append(error.getLevel()).append("]")
            .append(System.lineSeparator()));
    return sb.toString();
  }

}
