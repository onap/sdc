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

package org.openecomp.sdc.vendorsoftwareproduct.dao.impl;

import org.openecomp.core.model.dao.EnrichedServiceArtifactDao;
import org.openecomp.core.model.dao.EnrichedServiceArtifactDaoFactory;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceArtifactDaoFactory;
import org.openecomp.core.model.dao.ServiceArtifactDaoInter;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceTemplateDaoFactory;
import org.openecomp.core.model.dao.ServiceTemplateDaoInter;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public class VendorSoftwareProductDaoImpl implements VendorSoftwareProductDao {

  private static final VendorSoftwareProductInfoDao vspInfoDao = VendorSoftwareProductInfoDaoFactory
      .getInstance().createInterface();
  private static final PackageInfoDao packageInfoDao =
      PackageInfoDaoFactory.getInstance().createInterface();
  private static final OrchestrationTemplateCandidateDao orchestrationTemplateCandidateDataDao =
      OrchestrationTemplateCandidateDaoFactory.getInstance().createInterface();
  private static final NetworkDao networkDao = NetworkDaoFactory.getInstance().createInterface();
  private static final ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();
  private static final NicDao nicDao = NicDaoFactory.getInstance().createInterface();
  private static final ProcessDao processDao = ProcessDaoFactory.getInstance().createInterface();
  private static final ServiceArtifactDaoInter
      artifactDao = ServiceArtifactDaoFactory.getInstance().createInterface();
  public static final ServiceTemplateDaoInter
      templateDao = ServiceTemplateDaoFactory.getInstance().createInterface();
  private static final EnrichedServiceArtifactDao enrichArtifactDao =
      EnrichedServiceArtifactDaoFactory.getInstance().createInterface();
  private static final EnrichedServiceModelDao enrichedServiceModelDao =
      EnrichedServiceModelDaoFactory.getInstance().createInterface();
  private static final ServiceModelDao serviceModelDao =
      ServiceModelDaoFactory.getInstance().createInterface();
  private static final ComponentDependencyModelDao componentDependencyModelDao =
      ComponentDependencyModelDaoFactory.getInstance().createInterface();
  private static final DeploymentFlavorDao
      deploymentFlavorDao = DeploymentFlavorDaoFactory.getInstance().createInterface();
  private static final ImageDao imageDao = ImageDaoFactory.getInstance().createInterface();
  private static final ComputeDao computeDao = ComputeDaoFactory.getInstance().createInterface();


  @Override
  public void registerVersioning(String versionableEntityType) {
    vspInfoDao.registerVersioning(versionableEntityType);
    networkDao.registerVersioning(versionableEntityType);
    componentDao.registerVersioning(versionableEntityType);
    nicDao.registerVersioning(versionableEntityType);
    processDao.registerVersioning(versionableEntityType);
    orchestrationTemplateCandidateDataDao.registerVersioning(versionableEntityType);
    componentDependencyModelDao.registerVersioning(versionableEntityType);
    computeDao.registerVersioning(versionableEntityType);
    deploymentFlavorDao.registerVersioning(versionableEntityType);
    imageDao.registerVersioning(versionableEntityType);
  }

  @Override
  public Collection<ComponentEntity> listComponents(String vspId, Version version) {
    return componentDao.list(new ComponentEntity(vspId, version, null));
  }

  @Override
  public Collection<ComponentEntity> listComponentsQuestionnaire(String vspId, Version version) {
    return componentDao.listQuestionnaires(vspId, version);
  }

  @Override
  public Collection<ComponentEntity> listComponentsCompositionAndQuestionnaire(String vspId,
                                                                               Version version) {
    return componentDao.listCompositionAndQuestionnaire(vspId, version);
  }

  @Override
  public Collection<ProcessEntity> listProcesses(String vspId, Version version,
                                                 String componentId) {
    return processDao.list(new ProcessEntity(vspId, version, componentId, null));
  }

  @Override
  public void deleteProcesses(String vspId, Version version, String componentId) {
    processDao.delete(new ProcessEntity(vspId, version, componentId, null));
  }

  @Override
  public ProcessEntity getProcess(String vspId, Version version, String componentId,
                                  String processId) {
    return processDao.get(new ProcessEntity(vspId, version, componentId, processId));
  }

  @Override
  public void createProcess(ProcessEntity processEntity) {
    processDao.create(processEntity);
  }

  @Override
  public void updateProcess(ProcessEntity processEntity) {
    ProcessEntity process = processDao.get(new ProcessEntity(processEntity.getVspId(), processEntity.getVersion(),
            processEntity.getComponentId(), processEntity.getId()));
    processEntity.setArtifact(process.getArtifact());
    processEntity.setArtifactName(process.getArtifactName());
    processDao.update(processEntity);
  }

  @Override
  public void deleteProcess(String vspId, Version version, String componentId, String processId) {
    processDao.delete(new ProcessEntity(vspId, version, componentId, processId));
  }

  @Override
  public void uploadProcessArtifact(String vspId, Version version, String componentId,
                                    String processId, byte[] artifact, String artifactName) {
    ProcessEntity
        processArtifact = new ProcessEntity(vspId, version, componentId, processId);
    processArtifact = processDao.get(processArtifact);
    processArtifact.setArtifact(ByteBuffer.wrap(artifact));
    processArtifact.setArtifactName(artifactName);
    processDao.update(processArtifact);
  }

  @Override
  public ProcessEntity getProcessArtifact(String vspId, Version version, String componentId,
                                          String processId) {
    return processDao
        .get(new ProcessEntity(vspId, version, componentId, processId));
  }

  @Override
  public void deleteProcessArtifact(String vspId, Version version, String componentId,
                                    String processId) {
    processDao.delete(new ProcessEntity(vspId, version, componentId, processId));
  }

  @Override
  public Collection<NicEntity> listNicsByVsp(String vspId, Version version) {
    return nicDao.listByVsp(vspId, version);
  }

  @Override
  public void deleteUploadData(String vspId, Version version) {

    networkDao.deleteAll(vspId, version);
    //nicDao.deleteByVspId(vspId, version);
    //artifactDao.delete(vspId, version);
    //templateDao.deleteAll(vspId, version);
    enrichedServiceModelDao.deleteAll(vspId, version);
    serviceModelDao.deleteAll(vspId, version);
    //processDao.deleteVspAll(vspId,version);
    componentDao.deleteAll(vspId, version);
    //Retain VSP information on heat upload
    //vspInfoDao.deleteAll(vspId, version);

//    uploadDataDao.deleteContentDataAndValidationData(vspId, version);

//    enrichArtifactDao.deleteAll(vspId, version);
//    artifactDao.deleteAll(vspId, version);
  }

  /* @Override
   public void updateVspLatestModificationTime(String vspId, Version version) {
   *//*  if (Objects.isNull(vspId) || Objects.isNull(version)) {
      return;
    }

    VspDetails retrieved = getVendorSoftwareProductInfo(new VspDetails(vspId, version));
    updateVendorSoftwareProductInfo(retrieved);*//*
  }
*/
  @Override
  public void createComponentDependencyModel(List<ComponentDependencyModelEntity>
                                                 componentDependencyModel, String vspId,
                                             Version version) {
    componentDependencyModelDao.deleteAll(vspId, version);
    for (ComponentDependencyModelEntity entity : componentDependencyModel) {
      entity.setId(CommonMethods.nextUuId());
      componentDependencyModelDao.create(entity);
    }
  }

  @Override
  public Collection<ComponentDependencyModelEntity> listComponentDependencies(String vspId,
                                                                              Version version) {
    return componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId, version,
        null));
  }

  @Override
  public void createDeploymentFlavor(DeploymentFlavorEntity deploymentFlavor) {
    deploymentFlavorDao.create(deploymentFlavor);
  }

  @Override
  public Collection<DeploymentFlavorEntity> listDeploymentFlavors(String vspId, Version version) {
    return deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
  }

  @Override
  public DeploymentFlavorEntity getDeploymentFlavor(String vspId, Version version,
                                                    String deploymentFlavorId) {
    return deploymentFlavorDao.get(new DeploymentFlavorEntity(vspId, version, deploymentFlavorId));
  }

  @Override
  public void deleteDeploymentFlavor(String vspId, Version version, String deploymentFlavorId) {
    deploymentFlavorDao.delete(new DeploymentFlavorEntity(vspId,version,
        deploymentFlavorId));
  }

  @Override
  public void createImage(ImageEntity imageEntity) {
    imageDao.create(imageEntity);
  }

  @Override
  public Collection<ImageEntity> listImages(String vspId, Version version, String componentId) {
    return imageDao.list(new ImageEntity(vspId, version, componentId, null));
  }

  @Override
  public ImageEntity getImage(String vspId, Version version, String componentId, String imageId) {
    return imageDao.get(new ImageEntity(vspId, version, componentId, imageId));
  }

  @Override
  public Collection<ImageEntity> listImagesByVsp(String vspId, Version version) {
    return imageDao.listByVsp(vspId, version);
  }

  @Override
  public void createCompute(ComputeEntity computeEntity) {
    computeDao.create(computeEntity);
  }

  @Override
  public Collection<ComputeEntity> listComputes(String vspId, Version version, String componentId) {
    return computeDao.list(new ComputeEntity(vspId, version, componentId, null));
  }

  @Override
  public Collection<ComputeEntity> listComputesByVsp(String vspId, Version version) {
    return computeDao.listByVsp(vspId, version);
  }

  @Override
  public ComputeEntity getCompute(String vspId, Version version, String componentId,
                                  String computeFlavorId) {
    return computeDao.get(new ComputeEntity(vspId, version, componentId, computeFlavorId));
  }

  @Override
  public void deleteImage(String vspId, Version version, String componentId, String imageId) {
    ImageEntity imageEntity = new ImageEntity(vspId, version, componentId, imageId);
    imageDao.delete(imageEntity);
  }

  @Override
  public void updateDeploymentFlavor(DeploymentFlavorEntity deploymentFlavorEntity) {
    deploymentFlavorDao.update(deploymentFlavorEntity);
  }

  @Override
  public void updateImage(ImageEntity imageEntity) {
    imageDao.update(imageEntity);
  }

  @Override
  public void updateImageQuestionnaire(String vspId, Version version, String componentId,
                                       String imageId, String questionnaireData) {
    imageDao.updateQuestionnaireData(vspId, version, componentId, imageId, questionnaireData);
  }

  @Override
  public void updateComputeQuestionnaire(String vspId, Version version, String componentId,
                                         String computeId, String questionnaireData) {
    computeDao.updateQuestionnaireData(vspId, version, componentId, computeId, questionnaireData);
  }

  @Override
  public void updateCompute(ComputeEntity compute) {
    computeDao.update(compute);
  }

  @Override
  public void deleteCompute(String vspId, Version version, String componentId, String
      computeFlavorId) {
    computeDao.delete(new ComputeEntity(vspId, version, componentId, computeFlavorId));
  }
}
