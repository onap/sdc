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
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.validation.types.GlobalValidationContext;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.manifest.ManifestContent;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.structure.Artifact;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.services.YamlUtil;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


public class HeatTreeManager {

  private static Logger logger = (Logger) LoggerFactory.getLogger(HeatTreeManager.class);


  private FileContentHandler heatContentMap = new FileContentHandler();
  private byte[] manifest;
  private HeatStructureTree tree = new HeatStructureTree();
  private Map<String, HeatStructureTree> fileTreeRef = new HashMap<>();
  private Map<String, Artifact> artifactRef = new HashMap<>();
  private Map<String, Artifact> candidateOrphanArtifacts = new HashMap<>();
  private Map<String, HeatStructureTree> nestedFiles = new HashMap<>();
  private Map<HeatStructureTree, HeatStructureTree> volumeFileToParent = new HashMap<>();
  private Map<HeatStructureTree, HeatStructureTree> networkFileToParent = new HashMap<>();
  private Set<String> manifestFiles = new HashSet<>();

  /**
   * Add file.
   *
   * @param fileName the file name
   * @param content  the content
   */
  public void addFile(String fileName, InputStream content) {
    if (fileName.equals(SdcCommon.MANIFEST_NAME)) {
      manifest = FileUtils.toByteArray(content);

    } else {
      heatContentMap.addFile(fileName, content);
    }
  }

  /**
   * Create tree.
   */
  public void createTree() {
    if (manifest == null) {
      logger.error("Missing manifest file in the zip.");
      return;
    }
    ManifestContent manifestData =
        JsonUtil.json2Object(new String(manifest), ManifestContent.class);
    scanTree(null, manifestData.getData());
    addNonNestedVolumeNetworkToTree(volumeFileToParent, nestedFiles.keySet(), true);
    addNonNestedVolumeNetworkToTree(networkFileToParent, nestedFiles.keySet(), false);
    handleOrphans();

    tree = fileTreeRef.get(SdcCommon.PARENT);
  }

  private void handleOrphans() {
    tree = fileTreeRef.get(SdcCommon.PARENT);
    candidateOrphanArtifacts.entrySet().stream()
        .forEach(entry -> tree.addArtifactToArtifactList(entry.getValue()));
    nestedFiles
        .values().stream().filter(heatStructureTree -> tree.getHeat().contains(heatStructureTree))
        .forEach(heatStructureTree -> tree.getHeat().remove(heatStructureTree));

    heatContentMap.getFileList().stream().filter(fileName -> isNotInManifestFiles(fileName))
        .forEach(fileName -> addTreeOther(fileName));
  }

  private boolean isNotInManifestFiles(String fileName) {
    return !manifestFiles.contains(fileName);
  }

  private void addTreeOther(String fileName) {
    if (tree.getOther() == null) {
      tree.setOther(new HashSet<>());
    }
    HeatStructureTree other = new HeatStructureTree(fileName, false);
    fileTreeRef.put(fileName, other);
    tree.getOther().add(other);
  }


  private void handleHeatContentReference(HeatStructureTree fileHeatStructureTree,
                                          GlobalValidationContext globalContext) {

    String fileName = fileHeatStructureTree.getFileName();

    try (InputStream fileContent = this.heatContentMap.getFileContent(fileName)) {
      HeatOrchestrationTemplate hot =
          new YamlUtil().yamlToObject(fileContent, HeatOrchestrationTemplate.class);

      Set<String> nestedSet = HeatTreeManagerUtil.getNestedFiles(fileName, hot, globalContext);
      addHeatNestedFiles(fileHeatStructureTree, nestedSet);

      Set<String> artifactSet = HeatTreeManagerUtil.getArtifactFiles(fileName, hot, globalContext);
      addHeatArtifactFiles(fileHeatStructureTree, artifactSet);
    } catch (Exception ignore) { /* invalid yaml no need to process reference */
      logger.debug("",ignore);
    }
  }


  private void addHeatArtifactFiles(HeatStructureTree fileHeatStructureTree,
                                    Set<String> artifactSet) {
    Artifact artifact;
    for (String artifactName : artifactSet) {
      FileData.Type type =
          candidateOrphanArtifacts.get(artifactName) != null ? candidateOrphanArtifacts
              .get(artifactName).getType() : null;
      artifact = new Artifact(artifactName, type);
      artifactRef.put(artifactName, artifact);
      candidateOrphanArtifacts.remove(artifactName);
      fileHeatStructureTree.addArtifactToArtifactList(artifact);
    }
  }


  private void addHeatNestedFiles(HeatStructureTree fileHeatStructureTree, Set<String> nestedSet) {
    HeatStructureTree childHeatStructureTree;
    for (String nestedName : nestedSet) {
      childHeatStructureTree = fileTreeRef.get(nestedName);
      if (childHeatStructureTree == null) {
        childHeatStructureTree = new HeatStructureTree();
        childHeatStructureTree.setFileName(nestedName);
        fileTreeRef.put(nestedName, childHeatStructureTree);
      }
      fileHeatStructureTree.addHeatStructureTreeToNestedHeatList(childHeatStructureTree);
      nestedFiles.put(childHeatStructureTree.getFileName(), childHeatStructureTree);
    }
  }


  /**
   * Add errors.
   *
   * @param validationErrors the validation errors
   */
  public void addErrors(Map<String, List<ErrorMessage>> validationErrors) {

    validationErrors.entrySet().stream().filter(entry -> {
      return fileTreeRef.get(entry.getKey()) != null;
    }).forEach(entry -> entry.getValue().stream().forEach(error ->
        fileTreeRef.get(entry.getKey()).addErrorToErrorsList(error)));

    validationErrors.entrySet().stream().filter(entry -> {
      return artifactRef.get(entry.getKey()) != null;
    }).forEach(entry -> artifactRef.get(entry.getKey()).setErrors(entry.getValue()));

  }

  /**
   * Scan tree.
   *
   * @param parent the parent
   * @param data   the data
   */
  public void scanTree(String parent, List<FileData> data) {
    String fileName;
    FileData.Type type;
    HeatStructureTree parentHeatStructureTree;
    HeatStructureTree fileHeatStructureTree;
    HeatStructureTree childHeatStructureTree;
    Artifact artifact;
    if (parent == null) {
      parentHeatStructureTree = new HeatStructureTree();
      fileTreeRef.put(SdcCommon.PARENT, parentHeatStructureTree);
    } else {
      parentHeatStructureTree = fileTreeRef.get(parent);
    }

    for (FileData fileData : data) {
      fileName = fileData.getFile();
      manifestFiles.add(fileName);
      type = fileData.getType();

      if (Objects.nonNull(type) && FileData.Type.HEAT.equals(type)) {
        fileHeatStructureTree = fileTreeRef.get(fileName);
        if (fileHeatStructureTree == null) {
          fileHeatStructureTree = new HeatStructureTree();
          fileTreeRef.put(fileName, fileHeatStructureTree);
        }
        fileHeatStructureTree.setFileName(fileName);
        fileHeatStructureTree.setBase(fileData.getBase());
        fileHeatStructureTree.setType(type);
        handleHeatContentReference(fileHeatStructureTree, null);
        parentHeatStructureTree.addHeatToHeatList(fileHeatStructureTree);
        if (fileData.getData() != null) {
          scanTree(fileName, fileData.getData());
        }
      } else {
        childHeatStructureTree = new HeatStructureTree();
        childHeatStructureTree.setFileName(fileName);
        childHeatStructureTree.setBase(fileData.getBase());
        childHeatStructureTree.setType(type);
        fileTreeRef.put(childHeatStructureTree.getFileName(), childHeatStructureTree);

        if (type == null) {
          parentHeatStructureTree.addOtherToOtherList(childHeatStructureTree);
        } else if (FileData.Type.HEAT_NET.equals(type)) {
          //parentHeatStructureTree.addNetworkToNetworkList(childHeatStructureTree);
          networkFileToParent.put(childHeatStructureTree, parentHeatStructureTree);
          if (fileData.getData() != null) {
            scanTree(fileName, fileData.getData());
          }
          handleHeatContentReference(childHeatStructureTree, null);

        } else if (FileData.Type.HEAT_VOL.equals(type)) {
          //parentHeatStructureTree.addVolumeFileToVolumeList(childHeatStructureTree);
          volumeFileToParent.put(childHeatStructureTree, parentHeatStructureTree);
          if (fileData.getData() != null) {
            scanTree(fileName, fileData.getData());
          }
          handleHeatContentReference(childHeatStructureTree, null);
        } else if (FileData.Type.HEAT_ENV.equals(type)) {
          if (parentHeatStructureTree != null && parentHeatStructureTree.getFileName() != null) {
            parentHeatStructureTree.setEnv(childHeatStructureTree);
          } else {
            if (parentHeatStructureTree.getOther() == null) {
              parentHeatStructureTree.setOther(new HashSet<>());
            }
            parentHeatStructureTree.getOther().add(childHeatStructureTree);
          }
        } else {
          artifact = new Artifact(fileName, type);
          if (!artifactRef.keySet().contains(fileName)) {
            artifactRef.put(fileName, artifact);
            candidateOrphanArtifacts.put(fileName, artifact);
          }
        }
      }
    }
  }


  private void addNonNestedVolumeNetworkToTree(
      Map<HeatStructureTree, HeatStructureTree> netVolToParent, Set<String> nestedFileNames,
      boolean isVolume) {
    for (Map.Entry<HeatStructureTree, HeatStructureTree> entry : netVolToParent.entrySet()) {
      HeatStructureTree netOrVolNode = entry.getKey();
      HeatStructureTree parent = entry.getValue();
      if (!nestedFileNames.contains(netOrVolNode.getFileName())) {
        if (isVolume) {
          parent.addVolumeFileToVolumeList(netOrVolNode);
        } else {
          parent.addNetworkToNetworkList(netOrVolNode);
        }
      }
    }
  }


  public HeatStructureTree getTree() {
    return tree;
  }
}
