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

package org.openecomp.sdc.vendorsoftwareproduct.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.ComponentArtifactType;
import org.openecomp.core.translator.api.HeatToToscaTranslator;
import org.openecomp.core.translator.datatypes.TranslatorOutput;
import org.openecomp.core.translator.factory.HeatToToscaTranslatorFactory;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.validation.errors.Messages;
import org.openecomp.core.validation.types.MessageContainerUtil;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.utils.AsdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.heat.services.tree.HeatTreeManager;
import org.openecomp.sdc.heat.services.tree.HeatTreeManagerUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The type Vendor software product utils.
 */
public class VendorSoftwareProductUtils {

  private static org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao
      vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();

  /**
   * Load upload file content file content handler.
   *
   * @param uploadedFileData the uploaded file data
   * @return the file content handler
   * @throws IOException the io exception
   */
  public static FileContentHandler loadUploadFileContent(byte[] uploadedFileData)
      throws IOException {
    return getFileContentMapFromZip(uploadedFileData);
  }

  private static FileContentHandler getFileContentMapFromZip(byte[] uploadFileData)
      throws IOException, CoreException {
    ZipEntry zipEntry;
    List<String> folderList = new ArrayList<>();
    FileContentHandler mapFileContent = new FileContentHandler();
    try {
      ZipInputStream inputZipStream;

      byte[] fileByteContent;
      String currentEntryName;
      inputZipStream = new ZipInputStream(new ByteArrayInputStream(uploadFileData));

      while ((zipEntry = inputZipStream.getNextEntry()) != null) {
        currentEntryName = zipEntry.getName();
        // else, get the file content (as byte array) and save it in a map.
        fileByteContent = FileUtils.toByteArray(inputZipStream);

        int index = lastIndexFileSeparatorIndex(currentEntryName);
        String currSubstringWithoutSeparator =
            currentEntryName.substring(index + 1, currentEntryName.length());
        if (index != -1) { //todo ?
          folderList.add(currentEntryName);
        } else {
          mapFileContent.addFile(currentEntryName, fileByteContent);
        }

      }

    } catch (RuntimeException e0) {
      throw new IOException(e0);
    }

    if (CollectionUtils.isNotEmpty(folderList)) {
      throw new CoreException((new ErrorCode.ErrorCodeBuilder())
          .withMessage(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withId(Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage())
          .withCategory(ErrorCategory.APPLICATION).build());
    }

    return mapFileContent;
  }

  /**
   * Load and translate template data translator output.
   *
   * @param fileNameContentMap the file name content map
   * @return the translator output
   */
  public static TranslatorOutput loadAndTranslateTemplateData(
      FileContentHandler fileNameContentMap) {
    HeatToToscaTranslator heatToToscaTranslator =
        HeatToToscaTranslatorFactory.getInstance().createInterface();
    InputStream fileContent = fileNameContentMap.getFileContent(AsdcCommon.MANIFEST_NAME);

    heatToToscaTranslator.addManifest(AsdcCommon.MANIFEST_NAME, FileUtils.toByteArray(fileContent));

    fileNameContentMap.getFileList().stream()
        .filter(fileName -> !(fileName.equals(AsdcCommon.MANIFEST_NAME))).forEach(
          fileName -> heatToToscaTranslator
        .addFile(fileName, FileUtils.toByteArray(fileNameContentMap.getFileContent(fileName))));

    Map<String, List<ErrorMessage>> errors = heatToToscaTranslator.validate();
    if (MapUtils.isNotEmpty(MessageContainerUtil.getMessageByLevel(
        org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR, errors))) {
      TranslatorOutput translatorOutput = new TranslatorOutput();
      translatorOutput.setErrorMessages(errors);
      return translatorOutput;
    }

    InputStream structureFile = getHeatStructureTreeFile(fileNameContentMap);
    heatToToscaTranslator.addExternalArtifacts(AsdcCommon.HEAT_META, structureFile);
    return heatToToscaTranslator.translate();
  }

  private static InputStream getHeatStructureTreeFile(FileContentHandler fileNameContentMap) {
    HeatTreeManager heatTreeManager = HeatTreeManagerUtil.initHeatTreeManager(fileNameContentMap);
    heatTreeManager.createTree();
    HeatStructureTree tree = heatTreeManager.getTree();
    ValidationStructureList validationStructureList = new ValidationStructureList(tree);
    return FileUtils.convertToInputStream(validationStructureList, FileUtils.FileExtension.JSON);
  }


  private static int lastIndexFileSeparatorIndex(String filePath) {
    int length = filePath.length() - 1;

    for (int i = length; i >= 0; i--) {
      char currChar = filePath.charAt(i);
      if (currChar == '/' || currChar == File.separatorChar || currChar == File.pathSeparatorChar) {
        return i;
      }
    }
    // if we've reached to the start of the string and didn't find file separator - return -1
    return -1;
  }

  /**
   * Add file names to upload file response.
   *
   * @param fileContentMap     the file content map
   * @param uploadFileResponse the upload file response
   */
  public static void addFileNamesToUploadFileResponse(FileContentHandler fileContentMap,
                                                      UploadFileResponse uploadFileResponse) {
    uploadFileResponse.setFileNames(new ArrayList<>());
    for (String filename : fileContentMap.getFileList()) {
      if (!new File(filename).isDirectory()) {
        uploadFileResponse.addNewFileToList(filename);
      }
    }
    uploadFileResponse.removeFileFromList(AsdcCommon.MANIFEST_NAME);
  }

  /**
   * Validate raw zip data.
   *
   * @param uploadedFileData the uploaded file data
   * @param errors           the errors
   */
  public static void validateRawZipData(byte[] uploadedFileData,
                                        Map<String, List<ErrorMessage>> errors) {
    if (uploadedFileData.length == 0) {
      ErrorMessage.ErrorMessageUtil.addMessage(AsdcCommon.UPLOAD_FILE, errors).add(
          new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR,
              Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
    }
  }

  /**
   * Validate content zip data.
   *
   * @param contentMap the content map
   * @param errors     the errors
   */
  public static void validateContentZipData(FileContentHandler contentMap,
                                            Map<String, List<ErrorMessage>> errors) {
    if (contentMap == null) {
      ErrorMessage.ErrorMessageUtil.addMessage(AsdcCommon.UPLOAD_FILE, errors).add(
          new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR,
              Messages.ZIP_SHOULD_NOT_CONTAIN_FOLDERS.getErrorMessage()));

    } else if (contentMap.getFileList().size() == 0) {
      ErrorMessage.ErrorMessageUtil.addMessage(AsdcCommon.UPLOAD_FILE, errors)
          .add(new ErrorMessage(org.openecomp.sdc.datatypes.error.ErrorLevel.ERROR,
              Messages.INVALID_ZIP_FILE.getErrorMessage()));
    }
  }


  /**
   * Filter non trap or poll artifacts map.
   *
   * @param artifacts the artifacts
   * @return the map
   */
  public static Map<ComponentArtifactType, String> filterNonTrapOrPollArtifacts(
      Collection<ComponentArtifactEntity> artifacts) {
    Map<ComponentArtifactType, String> artifactTypeToFilename = new HashMap<>();

    for (ComponentArtifactEntity entity : artifacts) {
      if (isTrapOrPoll(entity.getType())) {
        artifactTypeToFilename.put(entity.getType(), entity.getArtifactName());
      }
    }

    return artifactTypeToFilename;
  }


  private static boolean isTrapOrPoll(ComponentArtifactType type) {
    return type.equals(ComponentArtifactType.SNMP_POLL)
        || type.equals(ComponentArtifactType.SNMP_TRAP);
  }


}
