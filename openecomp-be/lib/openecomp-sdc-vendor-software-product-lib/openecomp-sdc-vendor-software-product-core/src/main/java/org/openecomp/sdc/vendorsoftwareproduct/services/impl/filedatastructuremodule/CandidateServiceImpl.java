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

package org.openecomp.sdc.vendorsoftwareproduct.services.impl.filedatastructuremodule;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.common.errors.Messages;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.structure.Artifact;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.utils.ErrorsUtil;
import org.openecomp.sdc.vendorsoftwareproduct.services.HeatFileAnalyzer;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.ManifestCreator;
import org.openecomp.sdc.vendorsoftwareproduct.services.utils.CandidateServiceValidator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CandidateDataEntityTo;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.AnalyzedZipHeatFiles;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.Module;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CandidateServiceImpl implements CandidateService {
  protected static final Logger logger = LoggerFactory.getLogger(CandidateServiceImpl.class);
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private CandidateServiceValidator candidateServiceValidator = new CandidateServiceValidator();
  private ManifestCreator manifestCreator;
  private OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDao;

  public CandidateServiceImpl(ManifestCreator manifestCreator,
                              OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDao) {
    this.manifestCreator = manifestCreator;
    this.orchestrationTemplateCandidateDao = orchestrationTemplateCandidateDao;

  }

  public CandidateServiceImpl() {
  }

  @Override
  public Optional<ErrorMessage> validateNonEmptyFileToUpload(InputStream fileToUpload) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (Objects.isNull(fileToUpload)) {
      return Optional.of(new ErrorMessage(ErrorLevel.ERROR,
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
    } else {
      try {
        int available = fileToUpload.available();
        if (available == 0) {
          mdcDataDebugMessage.debugExitMessage(null);
          return Optional.of(new ErrorMessage(ErrorLevel.ERROR,
              Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
        }
      } catch (IOException e) {
        logger.debug(e.getMessage(), e);
        mdcDataDebugMessage.debugExitMessage(null);
        return Optional.of(new ErrorMessage(ErrorLevel.ERROR,
            Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return Optional.empty();
  }

  @Override
  public Optional<ErrorMessage> validateRawZipData(byte[] uploadedFileData) {
    if (Objects.isNull(uploadedFileData)) {
      return Optional.of(new ErrorMessage(ErrorLevel.ERROR,
          Messages.NO_ZIP_FILE_WAS_UPLOADED_OR_ZIP_NOT_EXIST.getErrorMessage()));
    }
    return Optional.empty();
  }

  private String heatStructureTreeToFileDataStructure(HeatStructureTree tree,
                                                      FileContentHandler zipContentMap,
                                                      Map<String, List<ErrorMessage>> uploadErrors,
                                                      AnalyzedZipHeatFiles analyzedZipHeatFiles)
      throws Exception {
    FilesDataStructure structure = new FilesDataStructure();
    Set<String> usedEnvFiles = new HashSet<>();
    addHeatsToFileDataStructure(tree, usedEnvFiles, structure, uploadErrors,
        analyzedZipHeatFiles);
    handleOtherResources(tree, usedEnvFiles, structure);
    FilesDataStructure fileDataStructureFromManifest =
        createFileDataStructureFromManifest(zipContentMap.getFileContent
            (SdcCommon.MANIFEST_NAME));
    List<String> structureArtifacts = structure.getArtifacts();
    structureArtifacts.addAll(fileDataStructureFromManifest.getArtifacts().stream().filter
        (artifact -> isNotStrctureArtifact(structureArtifacts, artifact))
        .collect((Collectors.toList())));
    handleArtifactsFromTree(tree, structure);

    return JsonUtil.object2Json(structure);
  }

  private boolean isNotStrctureArtifact(List<String> structureArtifacts, String artifact) {
    return !structureArtifacts.contains(artifact);
  }

  @Override
  public OrchestrationTemplateCandidateData createCandidateDataEntity(
      CandidateDataEntityTo candidateDataEntityTo, InputStream zipFileManifest,
      AnalyzedZipHeatFiles analyzedZipHeatFiles) throws Exception {


    mdcDataDebugMessage.debugEntryMessage(null);

    FileContentHandler zipContentMap = candidateDataEntityTo.getContentMap();
    FilesDataStructure filesDataStructure;
    String dataStructureJson;

    if (zipFileManifest != null) {
      // create data structure from manifest
      filesDataStructure = createFileDataStructureFromManifest(zipFileManifest);
      Set<String> zipFileList = zipContentMap.getFileList();
      balanceManifestFilesWithZipFiles(filesDataStructure,
          zipContentMap, analyzedZipHeatFiles);
      Set<String> filesDataStructureFiles = getFlatFileNames(filesDataStructure);
      filesDataStructure.getUnassigned().addAll(zipFileList.stream()
          .filter(fileName -> (!filesDataStructureFiles.contains(fileName) &&
              !filesDataStructure.getNested().contains(fileName) &&
              !fileName.equals(SdcCommon.MANIFEST_NAME)))
          .collect(Collectors.toList()));
      dataStructureJson = JsonUtil.object2Json(filesDataStructure);
    } else {
      // create data structure from based on naming convention
      dataStructureJson =
          heatStructureTreeToFileDataStructure(candidateDataEntityTo.getTree(), zipContentMap,
              candidateDataEntityTo.getErrors(), analyzedZipHeatFiles);
    }

    OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
    candidateData.setContentData(ByteBuffer.wrap(candidateDataEntityTo.getUploadedFileData()));
    candidateData.setFilesDataStructure(dataStructureJson);

    mdcDataDebugMessage.debugExitMessage(null);
    return candidateData;
  }

  private void balanceManifestFilesWithZipFiles(
      FilesDataStructure filesDataStructure,
      FileContentHandler fileContentHandler, AnalyzedZipHeatFiles analyzedZipHeatFiles)
      throws Exception {
    Set<String> zipFileList = fileContentHandler.getFileList();
    filesDataStructure.getNested().addAll(analyzedZipHeatFiles.getNestedFiles());
    List<Module> modules = filesDataStructure.getModules();
    if (CollectionUtils.isEmpty(modules)) {
      return;
    }

    for (int i = 0; i < modules.size(); i++) {
      Module module = modules.get(i);
      if (!isFileExistInZipContains(zipFileList, module.getYaml())) {
        addFileToUnassigned(filesDataStructure, zipFileList, module.getEnv());
        addFileToUnassigned(filesDataStructure, zipFileList, module.getVol());
        addFileToUnassigned(filesDataStructure, zipFileList, module.getVolEnv());
        modules.remove(i--);
      } else if (Objects.nonNull(module.getVol()) && !zipFileList.contains(module.getVol())) {
        module.setVol(null);
        CollectionUtils
            .addIgnoreNull(filesDataStructure.getUnassigned(), module.getVolEnv());
      } else {
        if (filesDataStructure.getNested().contains(module.getYaml())) {
          moveModuleFileToNested(filesDataStructure, i--, module);
        }
      }
    }
  }

  private void addFileToUnassigned(FilesDataStructure filesDataStructure, Set<String> zipFileList,
                                   String fileName) {
    if (isFileExistInZipContains(zipFileList, fileName)) {
      filesDataStructure.getUnassigned().add(fileName);
    }
  }

  private boolean isFileExistInZipContains(Set<String> zipFileList, String fileName) {
    return Objects.nonNull(fileName) && zipFileList.contains(fileName);
  }

  private void moveModuleFileToNested(FilesDataStructure filesDataStructure, int i,
                                      Module module) {
    if (!filesDataStructure.getNested().contains(module.getYaml())) {
      filesDataStructure.getNested().add(module.getYaml());
    }
    if (Objects.nonNull(module.getEnv())) {
      filesDataStructure.getNested().add(module.getEnv());
    }
    if (Objects.nonNull(module.getVol())) {
      filesDataStructure.getNested().add(module.getVol());
    }
    if (Objects.nonNull(module.getVolEnv())) {
      filesDataStructure.getNested().add(module.getVolEnv());
    }
    filesDataStructure.getModules().remove(i);
  }

  private Set<String> getFlatFileNames(FilesDataStructure filesDataStructure) {
    Set<String> fileNames = new HashSet<>();
    if (!CollectionUtils.isEmpty(filesDataStructure.getModules())) {
      for (Module module : filesDataStructure.getModules()) {
        CollectionUtils.addIgnoreNull(fileNames, module.getEnv());
        CollectionUtils.addIgnoreNull(fileNames, module.getVol());
        CollectionUtils.addIgnoreNull(fileNames, module.getVolEnv());
        CollectionUtils.addIgnoreNull(fileNames, module.getYaml());
      }
    }
    fileNames.addAll(filesDataStructure.getArtifacts().stream().collect(Collectors.toSet()));
    fileNames.addAll(filesDataStructure.getNested().stream().collect(Collectors.toSet()));
    fileNames.addAll(filesDataStructure.getUnassigned().stream().collect(Collectors.toSet()));

    return fileNames;
  }

  private FilesDataStructure createFileDataStructureFromManifest(InputStream isManifestContent) {


    mdcDataDebugMessage.debugEntryMessage(null);

    ManifestContent manifestContent =
        JsonUtil.json2Object(isManifestContent, ManifestContent.class);
    FilesDataStructure structure = new FilesDataStructure();
    for (FileData fileData : manifestContent.getData()) {
      if (Objects.nonNull(fileData.getType()) &&
          fileData.getType().equals(FileData.Type.HEAT)) {
        Module module = new Module();
        module.setYaml(fileData.getFile());
        module.setIsBase(fileData.getBase());
        addHeatDependenciesToModule(module, fileData.getData());
        structure.getModules().add(module);
      } else if (HeatFileAnalyzer.isYamlOrEnvFile(fileData.getFile()) &&
          !FileData.Type.isArtifact(fileData.getType())) {
        structure.getUnassigned().add(fileData.getFile());
      } else {
        structure.getArtifacts().add(fileData.getFile());
      }
    }

    mdcDataDebugMessage.debugExitMessage(null);
    return structure;
  }

  private void addHeatDependenciesToModule(Module module, List<FileData> data) {
    if (CollectionUtils.isEmpty(data)) {
      return;
    }

    for (FileData fileData : data) {
      if (fileData.getType().equals(FileData.Type.HEAT_ENV)) {
        module.setEnv(fileData.getFile());
      } else if (fileData.getType().equals(FileData.Type.HEAT_VOL))// must be volume
      {
        module.setVol(fileData.getFile());
        if (!CollectionUtils.isEmpty(fileData.getData())) {
          FileData volEnv = fileData.getData().get(0);
          if (volEnv.getType().equals(FileData.Type.HEAT_ENV)) {
            module.setVolEnv(volEnv.getFile());
          } else {
            throw new CoreException((new ErrorCode.ErrorCodeBuilder())
                .withMessage(Messages.ILLEGAL_MANIFEST.getErrorMessage())
                .withId(Messages.ILLEGAL_MANIFEST.getErrorMessage())
                .withCategory(ErrorCategory.APPLICATION).build());
          }
        }
      } else {
        throw new CoreException((new ErrorCode.ErrorCodeBuilder())
            .withMessage(Messages.FILE_TYPE_NOT_LEGAL.getErrorMessage())
            .withId(Messages.FILE_TYPE_NOT_LEGAL.getErrorMessage())
            .withCategory(ErrorCategory.APPLICATION).build());
      }
    }
  }

  @Override
  public void updateCandidateUploadData(String vspId, Version version,
                                        OrchestrationTemplateCandidateData uploadData) {
    mdcDataDebugMessage.debugEntryMessage(null);

    orchestrationTemplateCandidateDao.update(vspId, version, uploadData);

    mdcDataDebugMessage.debugExitMessage(null);
  }

  @Override
  public Optional<FilesDataStructure> getOrchestrationTemplateCandidateFileDataStructure(
      String vspId, Version version) {

    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspId);

    Optional<String> jsonFileDataStructure =
        orchestrationTemplateCandidateDao.getStructure(vspId, version);

    if (jsonFileDataStructure.isPresent() && JsonUtil.isValidJson(jsonFileDataStructure.get())) {
      mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);
      return Optional
          .of(JsonUtil.json2Object(jsonFileDataStructure.get(), FilesDataStructure.class));
    } else {
      mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);
      return Optional.empty();
    }
  }

  @Override
  public void updateOrchestrationTemplateCandidateFileDataStructure(String vspId, Version version,
                                                                    FilesDataStructure fileDataStructure) {
    OrchestrationTemplateCandidateDaoFactory.getInstance().createInterface()
        .updateStructure(vspId, version, fileDataStructure);
  }

  @Override
  public OrchestrationTemplateCandidateData getOrchestrationTemplateCandidate(String vspId,
                                                                              Version version) {
    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspId);
    mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);

    return orchestrationTemplateCandidateDao.get(vspId, version);
  }

  @Override
  public OrchestrationTemplateCandidateData getOrchestrationTemplateCandidateInfo(String vspId,
                                                                                  Version version) {
    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspId);
    mdcDataDebugMessage.debugExitMessage("VSP Id", vspId);

    return orchestrationTemplateCandidateDao.getInfo(vspId, version);
  }

  @Override
  public String createManifest(VspDetails vspDetails, FilesDataStructure structure) {

    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspDetails.getId());

    Optional<ManifestContent> manifest = manifestCreator.createManifest(vspDetails, structure);
    if (!manifest.isPresent()) {
      throw new RuntimeException(Messages.CREATE_MANIFEST_FROM_ZIP.getErrorMessage());
    }

    mdcDataDebugMessage.debugExitMessage("VSP Id", vspDetails.getId());
    return JsonUtil.object2Json(manifest.get());
  }

  @Override
  public Optional<ManifestContent> createManifest(VspDetails vspDetails,
                                                  FileContentHandler fileContentHandler,
                                                  AnalyzedZipHeatFiles analyzedZipHeatFiles) {


    mdcDataDebugMessage.debugEntryMessage("VSP Id", vspDetails.getId());

    mdcDataDebugMessage.debugExitMessage("VSP Id", vspDetails.getId());
    return manifestCreator.createManifest(vspDetails, fileContentHandler, analyzedZipHeatFiles);
  }

  @Override
  public Optional<ByteArrayInputStream> fetchZipFileByteArrayInputStream(String vspId,
                                                                         OrchestrationTemplateCandidateData candidateDataEntity,
                                                                         String manifest,
                                                                         OnboardingTypesEnum type,
                                                                         Map<String, List<ErrorMessage>> uploadErrors) {
    byte[] file;
    ByteArrayInputStream byteArrayInputStream = null;
    try {
      file = replaceManifestInZip(candidateDataEntity.getContentData(), manifest, vspId, type);
      byteArrayInputStream = new ByteArrayInputStream(
          Objects.isNull(file) ? candidateDataEntity.getContentData().array()
              : file);
    } catch (IOException e) {
      ErrorMessage errorMessage =
          new ErrorMessage(ErrorLevel.ERROR,
              Messages.CANDIDATE_PROCESS_FAILED.getErrorMessage());
      logger.error(errorMessage.getMessage(), e);
      ErrorsUtil
          .addStructureErrorToErrorMap(SdcCommon.UPLOAD_FILE, errorMessage, uploadErrors);
    }
    return Optional.ofNullable(byteArrayInputStream);
  }

  @Override
  public byte[] replaceManifestInZip(ByteBuffer contentData, String manifest, String vspId,
                                     OnboardingTypesEnum type)
      throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    try (final ZipOutputStream zos = new ZipOutputStream(baos);
         ZipInputStream zipStream = new ZipInputStream(
             new ByteArrayInputStream(contentData.array()))) {
      ZipEntry zipEntry;
      boolean manifestWritten = false;
      while ((zipEntry = zipStream.getNextEntry()) != null) {
        if (!zipEntry.getName().equalsIgnoreCase(SdcCommon.MANIFEST_NAME)) {
          ZipEntry loc_ze = new ZipEntry(zipEntry.getName());
          zos.putNextEntry(loc_ze);
          byte[] buf = new byte[1024];
          int len;
          while ((len = zipStream.read(buf)) > 0) {
            zos.write(buf, 0, (len < buf.length) ? len : buf.length);
          }
        } else {
          manifestWritten = true;
          writeManifest(manifest, type, zos);
        }
        zos.closeEntry();
      }
      if (!manifestWritten) {
        writeManifest(manifest, type, zos);
        zos.closeEntry();
      }
    }
    return baos.toByteArray();
  }

  @Override
  public Optional<List<ErrorMessage>> validateFileDataStructure(
      FilesDataStructure filesDataStructure) {
    return candidateServiceValidator.validateFileDataStructure(filesDataStructure);
  }

  private void writeManifest(String manifest,
                             OnboardingTypesEnum type,
                             ZipOutputStream zos) throws IOException {

    if (isManifestNeedsToGetWritten(type)) {
      return;
    }

    zos.putNextEntry(new ZipEntry(SdcCommon.MANIFEST_NAME));
    try (InputStream manifestStream = new ByteArrayInputStream(
        manifest.getBytes(StandardCharsets.UTF_8))) {
      byte[] buf = new byte[1024];
      int len;
      while ((len = (manifestStream.read(buf))) > 0) {
        zos.write(buf, 0, (len < buf.length) ? len : buf.length);
      }
    }
  }

  private boolean isManifestNeedsToGetWritten(OnboardingTypesEnum type) {
    return type.equals(OnboardingTypesEnum.CSAR);
  }

  private void handleArtifactsFromTree(HeatStructureTree tree, FilesDataStructure structure) {

    if (Objects.isNull(tree) || Objects.isNull(tree.getArtifacts())) {
      return;
    }

    if (CollectionUtils.isNotEmpty(tree.getArtifacts())) {
      structure.getArtifacts().addAll(
          tree.getArtifacts()
              .stream()
              .map(Artifact::getFileName)
              .filter(fileName -> !structure.getArtifacts().contains(fileName))
              .collect(Collectors.toList()));
    }
  }

  private void handleOtherResources(HeatStructureTree tree, Set<String> usedEnvFiles,
                                    FilesDataStructure structure) {
    Set<HeatStructureTree> others = tree.getOther();
    if (Objects.isNull(others)) {
      return;
    }

    List<String> artifacts = new ArrayList<>();
    List<String> unassigned = new ArrayList<>();
    for (HeatStructureTree other : others) {
      if (HeatFileAnalyzer.isYamlOrEnvFile(other.getFileName())) {
        if (isEnvFileUsedByHeatFile(usedEnvFiles, other)) {
          continue;
        }
        unassigned.add(other.getFileName());
      } else {
        artifacts.add(other.getFileName());
      }
      handleArtifactsFromTree(other, structure);
    }
    structure.getArtifacts().addAll(artifacts);
    structure.getUnassigned().addAll(unassigned);
  }

  private boolean isEnvFileUsedByHeatFile(Set<String> usedEnvFiles, HeatStructureTree other) {
    if (HeatFileAnalyzer.isEnvFile(other.getFileName())) {
      if (usedEnvFiles.contains(other.getFileName())) {
        return true;
      }
    }
    return false;
  }

  private void addHeatsToFileDataStructure(HeatStructureTree tree, Set<String> usedEnvFiles,
                                           FilesDataStructure structure,
                                           Map<String, List<ErrorMessage>> uploadErrors,
                                           AnalyzedZipHeatFiles analyzedZipHeatFiles)
      throws Exception {
    List<Module> modules = new ArrayList<>();
    Set<HeatStructureTree> heatsSet = tree.getHeat();
    if (Objects.isNull(heatsSet)) {
      return;
    }
    for (HeatStructureTree heat : heatsSet) {
      if (isFileBaseFile(heat.getFileName())) {
        handleSingleHeat(structure, modules, heat, uploadErrors);
      } else if (isFileModuleFile(heat.getFileName(),
          analyzedZipHeatFiles.getModuleFiles())) {
        handleSingleHeat(structure, modules, heat, uploadErrors);
      } else {
        structure.getUnassigned().add(heat.getFileName());
        addNestedToFileDataStructure(heat, structure);
      }
      if (!Objects.isNull(heat.getEnv())) {
        usedEnvFiles.add(heat.getEnv() == null ? null : heat.getEnv().getFileName());
      }
    }
    structure.setModules(modules);

  }

  private boolean isFileModuleFile(String fileName, Set<String> modulesFileNames) {
    return modulesFileNames.contains(fileName);
  }

  private boolean isFileBaseFile(String fileName) {
    return manifestCreator.isFileBaseFile(fileName);
  }

  private void handleSingleHeat(FilesDataStructure structure, List<Module> modules,
                                HeatStructureTree heat,
                                Map<String, List<ErrorMessage>> uploadErrors) {


    mdcDataDebugMessage.debugEntryMessage(null);

    Module module = new Module();
    module.setYaml(heat.getFileName());
    module.setIsBase(heat.getBase());
    addNestedToFileDataStructure(heat, structure);
    Set<HeatStructureTree> volumeSet = heat.getVolume();
    int inx = 0;
    if (Objects.nonNull(volumeSet)) {
      handleVolumes(module, volumeSet, structure, inx, uploadErrors);
    }
    handleEnv(module, heat, false, structure);
    modules.add(module);

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void handleVolumes(Module module, Set<HeatStructureTree> volumeSet,
                             FilesDataStructure structure, int inx,
                             Map<String, List<ErrorMessage>> uploadErrors) {


    mdcDataDebugMessage.debugEntryMessage(null);

    for (HeatStructureTree volume : volumeSet) {
      if (inx++ > 0) {
        ErrorsUtil.addStructureErrorToErrorMap(SdcCommon.UPLOAD_FILE,
            new ErrorMessage(ErrorLevel.WARNING,
                Messages.MORE_THEN_ONE_VOL_FOR_HEAT.getErrorMessage()), uploadErrors);
        break;
      }
      handleArtifactsFromTree(volume, structure);
      module.setVol(volume.getFileName());
      handleEnv(module, volume, true, structure);
      addNestedToFileDataStructure(volume, structure);
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void handleEnv(Module module, HeatStructureTree tree, boolean isVolEnv,
                         FilesDataStructure structure) {


    mdcDataDebugMessage.debugEntryMessage(null);

    if (Objects.nonNull(tree.getEnv())) {
      if (isVolEnv) {
        module.setVolEnv(tree.getEnv().getFileName());
      } else {
        module.setEnv(tree.getEnv().getFileName());
      }
      handleArtifactsFromTree(tree.getEnv(), structure);
    }

    mdcDataDebugMessage.debugExitMessage(null);
  }

  private void addNestedToFileDataStructure(HeatStructureTree heat,
                                            FilesDataStructure structure) {
    Set<HeatStructureTree> nestedSet = heat.getNested();
    if (Objects.isNull(nestedSet)) {
      return;
    }
    for (HeatStructureTree nested : nestedSet) {
      if (structure.getNested().contains(nested.getFileName())) {
        continue;
      }
      structure.getNested().add(nested.getFileName());
      if (CollectionUtils.isNotEmpty(nested.getArtifacts())) {
        handleArtifactsFromTree(nested, structure);
      }
      addNestedToFileDataStructure(nested, structure);
    }
  }
}
