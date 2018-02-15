/*
 * Copyright © 2016-2018 European Support Limited
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

package org.openecomp.sdc.tosca.services.impl;

import org.apache.commons.io.FilenameUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.core.validation.ErrorMessageCode;
import org.openecomp.core.validation.errors.ErrorMessagesFormatBuilder;
import org.openecomp.sdc.common.togglz.ToggleableFeature;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.parser.config.ConfigurationManager;
import org.openecomp.sdc.tosca.parser.exceptions.SdcToscaParserException;
import org.openecomp.sdc.tosca.parser.impl.SdcToscaParserFactory;
import org.openecomp.sdc.tosca.services.ToscaValidationService;
import org.openecomp.sdc.toscaparser.api.common.JToscaValidationIssue;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToscaValidationServiceImpl implements ToscaValidationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ToscaValidationServiceImpl.class);
  private static final String SDCPARSER_JTOSCA_VALIDATIONISSUE_CONFIG =
      "SDCParser_jtosca-validation-issue-configuration.yaml";
  private static final String SDCPARSER_ERROR_CONFIG = "SDCParser_error-configuration.yaml";
  private static final String TOSCA_DEFINITION_VERSION = "tosca_definitions_version";

  @Override
  public Map<String, List<ErrorMessage>> validateTranslatedTosca(FileContentHandler
                                                                        fileContentHandler)
      throws IOException {
    Map<String, List<ErrorMessage>> issues = new HashMap<>();

    // Toggle Feature - When Toggle needs to be removed; just remove the IF statement and directly
    // call the validate method or remove this method altogether and replace its call in calling
    // method with validate method
    if (ToggleableFeature.VALIDATE_TRANSLATED_TOSCA.isActive()) {
      issues = validate(fileContentHandler);
    }
    return issues;
  }

  @Override
  public Map<String, List<ErrorMessage>> validateInputTosca(FileContentHandler fileContentHandler)
      throws IOException {
    Map<String, List<ErrorMessage>> issues = new HashMap<>();

    // Toggle Feature - When Toggle needs to be removed; just remove the IF statement and directly
    // call the validate method or remove this method altogether and replace its call in calling
    // method with validate method
    if (ToggleableFeature.VALIDATE_INPUT_TOSCA.isActive()) {
      issues = validate(fileContentHandler);
    }
    return issues;
  }

  @Override
  public Map<String, List<ErrorMessage>> validate(FileContentHandler fileContentHandler)
      throws IOException {

    Map<String, List<ErrorMessage>> issues = new HashMap<>();
    Path dir =
        Files.createTempDirectory(OnboardingTypesEnum.CSAR + "_" + System.currentTimeMillis());
    try {
      // Write temporary files and folders to File System
      Map<String, String> filePaths = FileUtils.writeFilesFromFileContentHandler
          (fileContentHandler, dir);
      // Process Tosca Yaml validation
      issues = processToscaYamls(filePaths);
    } finally {
      // Cleanup temporary files and folders from file system
      FileUtils.deleteFilesAndFolders(dir.toAbsolutePath().toString());
    }
    return issues;
  }

  private Map<String, List<ErrorMessage>> processToscaYamls(Map<String, String> filePaths) {
    Map<String, String> validFilePaths = getValidFilePaths(filePaths);
    Map<String, List<ErrorMessage>> validationIssues = new HashMap<>();

    // Override default SDC Parser configuration and process Yaml Files
    ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    configurationManager.setJtoscaValidationIssueConfiguration(SDCPARSER_JTOSCA_VALIDATIONISSUE_CONFIG);
    configurationManager.setErrorConfiguration(SDCPARSER_ERROR_CONFIG);
    SdcToscaParserFactory.setConfigurationManager(configurationManager);

    for (Map.Entry<String, String> fileEntry : validFilePaths.entrySet()) {
      try {
        SdcToscaParserFactory factory = SdcToscaParserFactory.getInstance();
        factory.getSdcCsarHelper(fileEntry.getValue());
        processValidationIssues(fileEntry.getKey(), factory, validationIssues);
      } catch (SdcToscaParserException stpe) {
        LOGGER.error("SDC Parser Exception from SDC Parser Library : " + stpe);
        ErrorMessage.ErrorMessageUtil.addMessage(fileEntry.getKey(), validationIssues).add(
            new ErrorMessage(ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(new ErrorMessageCode("JE000"), "Unexpected Error "
                    + "occurred")));
      }
      catch (RuntimeException rte) {
        LOGGER.error("Runtime Exception from SDC Parser Library : " + rte);
        ErrorMessage.ErrorMessageUtil.addMessage(fileEntry.getKey(), validationIssues).add(
            new ErrorMessage(ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(new ErrorMessageCode("JE000"), "Unexpected Error "
                    + "occurred")));
      }
    }
    return validationIssues;
  }

  private Map<String, String> getValidFilePaths(Map<String, String> filePaths) {
    return filePaths.entrySet()
        .stream()
        .filter(map -> FileUtils.isValidYamlExtension(FilenameUtils.getExtension(map.getKey())))
        .filter(map -> isToscaYaml(map.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private boolean isToscaYaml(String filePath) {
   boolean retValue = false;

    try (InputStream input = new FileInputStream(new File(filePath));) {
      Yaml yaml = new Yaml();
      LinkedHashMap<String,Object> data = (LinkedHashMap) yaml.load(input);
      if(data.get(TOSCA_DEFINITION_VERSION) != null) {
        retValue = true;
      }
    }
    catch(Exception e){
      LOGGER.info("Ignore the exception as the input file may not be a Tosca Yaml; let the " +
          "default value return", e);
    }
    return retValue;
  }

  private void processValidationIssues(String fileName, SdcToscaParserFactory factory, Map<String,
      List<ErrorMessage>> validationIssues) {

    List<JToscaValidationIssue> criticalsReport = factory.getCriticalExceptions();
    criticalsReport.stream().forEach(err ->
        ErrorMessage.ErrorMessageUtil.addMessage(fileName, validationIssues).add(
            new ErrorMessage(ErrorLevel.ERROR, ErrorMessagesFormatBuilder
                .getErrorWithParameters(new ErrorMessageCode(err.getCode()), err.getMessage()))));

    List<JToscaValidationIssue> warningsReport = factory.getWarningExceptions();
    warningsReport.stream().forEach(err ->
        ErrorMessage.ErrorMessageUtil.addMessage(fileName, validationIssues).add(
            new ErrorMessage(ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(new ErrorMessageCode(err.getCode()), err.getMessage()))));

    List<JToscaValidationIssue> notAnalyzedReport = factory.getNotAnalyzadExceptions();
    notAnalyzedReport.stream().forEach(err ->
        ErrorMessage.ErrorMessageUtil.addMessage(fileName, validationIssues).add(
            new ErrorMessage(ErrorLevel.WARNING, ErrorMessagesFormatBuilder
                .getErrorWithParameters(new ErrorMessageCode(err.getCode()), err.getMessage()))));

  }

}
