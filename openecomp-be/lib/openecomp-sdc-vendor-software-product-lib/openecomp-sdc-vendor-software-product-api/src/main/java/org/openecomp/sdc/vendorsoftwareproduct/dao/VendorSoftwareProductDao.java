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

package org.openecomp.sdc.vendorsoftwareproduct.dao;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.List;


public interface VendorSoftwareProductDao extends VersionableDao {

  //VspDetails getVendorSoftwareProductInfo(VspDetails vspDetails);

  Collection<ComponentEntity> listComponents(String vspId, Version version);

  Collection<ComponentEntity> listComponentsQuestionnaire(String vspId, Version version);

  Collection<ComponentEntity> listComponentsCompositionAndQuestionnaire(String vspId,
                                                                        Version version);


  Collection<ProcessEntity> listProcesses(String vspId, Version version, String componentId);

  void deleteProcesses(String vspId, Version version, String componentId);

  ProcessEntity getProcess(String vspId, Version version, String componentId, String processId);

  void createProcess(ProcessEntity processEntity);

  void updateProcess(ProcessEntity processEntity);

  void deleteProcess(String vspId, Version version, String componentId, String processId);


  void uploadProcessArtifact(String vspId, Version version, String componentId, String processId,
                             byte[] artifact, String artifactFileName);

  ProcessEntity getProcessArtifact(String vspId, Version version, String componentId,
                                   String processId);

  void deleteProcessArtifact(String vspId, Version version, String componentId, String processId);


  Collection<NicEntity> listNicsByVsp(String vspId, Version version);


  void deleteUploadData(String vspId, Version version);

  //void updateVspLatestModificationTime(String vspId, Version version);
  void createComponentDependencyModel(
      List<ComponentDependencyModelEntity> componentDependencyModelEntity, String vspId,
      Version version);

  Collection<ComponentDependencyModelEntity> listComponentDependencies(String vspId,Version
      version);

  void createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavor);

  Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version);

  DeploymentFlavorEntity getDeploymentFlavor(String vspId, Version version, String
      deploymentFlavorId);
  void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId);


  void createImage(ImageEntity imageEntity);

  Collection<ImageEntity> listImages(String vspId, Version version, String componentId);

  ImageEntity getImage(String vspId, Version version, String componentId, String imageId);

  Collection<ImageEntity> listImagesByVsp(String vspId, Version version);

  void createCompute(ComputeEntity computeEntity);

  Collection<ComputeEntity> listComputes(String vspId, Version version, String componentId);

  Collection<ComputeEntity> listComputesByVsp(String vspId, Version version);

  ComputeEntity getCompute(String vspId, Version version, String componentId, String
      computeFlavorId);

  void deleteImage(String vspId, Version version, String componentId, String imageId);

  void updateDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity);

  void updateImage(ImageEntity imageEntity);

  void updateImageQuestionnaire(String vspId, Version activeVersion, String componentId,
                                String imageId, String questionnaireData);

  void updateComputeQuestionnaire(String vspId, Version activeVersion, String componentId,
                                  String computeId, String questionnaireData);

  void updateCompute(ComputeEntity compute);

  void deleteCompute(String vspId, Version version, String componentId, String computeFlavorId);

}
