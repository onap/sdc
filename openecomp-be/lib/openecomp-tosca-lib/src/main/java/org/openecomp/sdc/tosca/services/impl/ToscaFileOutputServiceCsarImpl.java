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

package org.openecomp.sdc.tosca.services.impl;

import org.apache.commons.io.IOUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.exceptions.CsarCreationErrorBuilder;
import org.openecomp.sdc.tosca.exceptions.CsarMissingEntryPointErrorBuilder;
import org.openecomp.sdc.tosca.services.ToscaFileOutputService;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ToscaFileOutputServiceCsarImpl implements ToscaFileOutputService {
  static final String EXTERNAL_ARTIFACTS_FOLDER_NAME = "Artifacts";
  private static final String DEFINITIONS_FOLDER_NAME = "Definitions";
  private static final String ARTIFACTS_FOLDER_NAME = "Artifacts";
  //todo currently duplicated, to be changed when external artifacts are separated from internal
  private static final String TOSCA_META_FOLDER_NAME = "TOSCA-Metadata";
  private static final String TOSCA_META_FILE_VERSION = "TOSCA-Meta-File-Version";
  private static final String TOSCA_META_FILE_VERSION_VALUE = "1.0";
  private static final String TOSCA_META_FILE_NAME = "TOSCA.meta";
  private static final String CSAR_VERSION = "CSAR-Version";
  private static final String CSAR_VERSION_VALUE = "1.1";
  private static final String CREATED_BY = "Created-By";
  private static final String CREATED_BY_VALUE = "ASDC Onboarding portal";
  private static final String ENTRY_DEFINITIONS = "Entry-Definitions";
  private static final String META_FILE_DELIMITER = ":";
  private static final String SPACE = " ";
  private static final String FILE_SEPARATOR = File.separator;

  @Override
  public byte[] createOutputFile(ToscaServiceModel toscaServiceModel,
                                 FileContentHandler externalArtifacts) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(baos))) {
      packDefinitions(zos, toscaServiceModel.getServiceTemplates());
      FileContentHandler artifactFiles = toscaServiceModel.getArtifactFiles();
      if (artifactFiles != null && !artifactFiles.isEmpty()) {
        packArtifacts(zos, artifactFiles);
      }
      if (toscaServiceModel.getEntryDefinitionServiceTemplate() == null) {
        throw new CoreException(new CsarMissingEntryPointErrorBuilder().build());
      }
      createAndPackToscaMetaFile(zos, toscaServiceModel.getEntryDefinitionServiceTemplate());
      if (externalArtifacts != null) {
        packExternalArtifacts(zos, externalArtifacts);
      }
    } catch (IOException exc) {
      throw new CoreException(new CsarCreationErrorBuilder().build(), exc);
    }
    return baos.toByteArray();
  }

  @Override
  public String createMetaFile(String entryDefinitionsFileName) {
    return TOSCA_META_FILE_VERSION + META_FILE_DELIMITER + SPACE + TOSCA_META_FILE_VERSION_VALUE
        + System.lineSeparator()
        + CSAR_VERSION + META_FILE_DELIMITER + SPACE + CSAR_VERSION_VALUE + System.lineSeparator()
        + CREATED_BY + META_FILE_DELIMITER + SPACE + CREATED_BY_VALUE + System.lineSeparator()
        + ENTRY_DEFINITIONS + META_FILE_DELIMITER + SPACE + DEFINITIONS_FOLDER_NAME
        + FILE_SEPARATOR + entryDefinitionsFileName;
  }

  @Override
  public String getArtifactsFolderName() {
    return ARTIFACTS_FOLDER_NAME;
  }

  private void createAndPackToscaMetaFile(ZipOutputStream zos, String entryDefinitionsFileName)
      throws IOException {
    String metaFile = createMetaFile(entryDefinitionsFileName);
    zos.putNextEntry(
        new ZipEntry((TOSCA_META_FOLDER_NAME + FILE_SEPARATOR + TOSCA_META_FILE_NAME)));
    writeBytesToZip(zos, new ByteArrayInputStream(metaFile.getBytes()));
  }

  private void packDefinitions(ZipOutputStream zos, Map<String, ServiceTemplate> serviceTemplates)
      throws IOException {
    for (Map.Entry<String, ServiceTemplate> serviceTemplate : serviceTemplates.entrySet()) {
      String fileName = serviceTemplate.getKey();
      zos.putNextEntry(new ZipEntry(DEFINITIONS_FOLDER_NAME + FILE_SEPARATOR + fileName));
      writeBytesToZip(zos,
          FileUtils.convertToInputStream(serviceTemplate.getValue(), FileUtils.FileExtension.YAML));
    }
  }

  private void packExternalArtifacts(ZipOutputStream zos, FileContentHandler externalArtifacts) {

    for (String filenameIncludingPath : externalArtifacts.getFileList()) {
      try {
        zos.putNextEntry(new ZipEntry(filenameIncludingPath));
        writeBytesToZip(zos, externalArtifacts.getFileContent(filenameIncludingPath));

      } catch (IOException exc) {
        throw new RuntimeException(exc);
      } finally {
        try {
          zos.closeEntry();
        } catch (IOException ignore) {
          //do nothing
        }
      }
    }

  }

  private void packArtifacts(ZipOutputStream zos, FileContentHandler artifacts) {

    for (String fileName : artifacts.getFileList()) {
      try {
        zos.putNextEntry(new ZipEntry((ARTIFACTS_FOLDER_NAME + FILE_SEPARATOR + fileName)));
        writeBytesToZip(zos, artifacts.getFileContent(fileName));

      } catch (IOException exc) {
        throw new RuntimeException(exc);
      } finally {
        try {
          zos.closeEntry();
        } catch (IOException ignore) {
          //do nothing
        }
      }
    }
  }

  private void writeBytesToZip(ZipOutputStream zos, InputStream is) throws IOException {
    if(is != null){
      IOUtils.copy(is, zos);
    }
  }
}
