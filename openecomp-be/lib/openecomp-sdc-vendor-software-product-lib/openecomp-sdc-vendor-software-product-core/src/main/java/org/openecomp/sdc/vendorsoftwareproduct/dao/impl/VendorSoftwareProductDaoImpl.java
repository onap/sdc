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
import org.openecomp.core.model.dao.ServiceArtifactDaoFactory;
import org.openecomp.core.model.dao.ServiceArtifactDaoInter;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentArtifactDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessArtifactDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessArtifactDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.UploadDataDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.UploadDataDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspQuestionnaireDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspQuestionnaireDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class VendorSoftwareProductDaoImpl implements VendorSoftwareProductDao {

  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static final PackageInfoDao packageInfoDao =
      PackageInfoDaoFactory.getInstance().createInterface();
  private static final UploadDataDao uploadDataDao =
      UploadDataDaoFactory.getInstance().createInterface();
  private static final VspQuestionnaireDao vspQuestionnaireDao =
      VspQuestionnaireDaoFactory.getInstance().createInterface();
  private static final NetworkDao networkDao = NetworkDaoFactory.getInstance().createInterface();
  private static final ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();
  private static final NicDao nicDao = NicDaoFactory.getInstance().createInterface();
  private static final ProcessDao processDao = ProcessDaoFactory.getInstance().createInterface();
  private static final ProcessArtifactDao processArtifactDao =
      ProcessArtifactDaoFactory.getInstance().createInterface();
  private static final ComponentArtifactDao componentArtifactDao =
      ComponentArtifactDaoFactory.getInstance().createInterface();
  private static final ServiceArtifactDaoInter artifactDao =
      ServiceArtifactDaoFactory.getInstance().createInterface();
  private static final EnrichedServiceArtifactDao enrichArtifactDao =
      EnrichedServiceArtifactDaoFactory.getInstance().createInterface();

  @Override
  public void registerVersioning(String versionableEntityType) {
    vspInfoDao.registerVersioning(versionableEntityType);
    vspQuestionnaireDao.registerVersioning(versionableEntityType);
    networkDao.registerVersioning(versionableEntityType);
    componentDao.registerVersioning(versionableEntityType);
    nicDao.registerVersioning(versionableEntityType);
    processDao.registerVersioning(versionableEntityType);
  }

  @Override
  public void createVendorSoftwareProductInfo(VspDetails vspDetails) {
    vspInfoDao.create(vspDetails);
  }

  @Override
  public Collection<VspDetails> listVendorSoftwareProductsInfo() {
    return vspInfoDao.list(new VspDetails());
  }

  public VspDetails getVendorSoftwareProductInfo(VspDetails vspDetails) {
    return vspInfoDao.get(vspDetails);
  }


  @Override
  public void updateVendorSoftwareProductInfo(VspDetails vspDetails) {
    vspInfoDao.update(vspDetails);
  }

  @Override
  public void deleteVendorSoftwareProductInfo(VspDetails vspDetails) {
    vspInfoDao.delete(vspDetails);
  }

  @Override
  public void updateUploadData(UploadDataEntity uploadData) {
    uploadDataDao.update(uploadData);
  }

  @Override
  public UploadDataEntity getUploadData(UploadDataEntity uploadData) {
    return uploadDataDao.get(uploadData);
  }

  @Override
  public ByteBuffer getContentData(UploadDataEntity uploadDataEntity) {
    return uploadDataDao.getContentData(uploadDataEntity.getId(), uploadDataEntity.getVersion());
  }

  @Override
  public void insertPackageDetails(PackageInfo packageInfo) {
    packageInfoDao.update(packageInfo);
  }

  @Override
  public PackageInfo getPackageInfo(PackageInfo packageInfo) {
    return packageInfoDao.get(packageInfo);
  }

  @Override
  public void deletePackageInfo(PackageInfo packageInfo) {
    packageInfoDao.delete(packageInfo);
  }

  @Override
  public Collection<NetworkEntity> listNetworks(String vspId, Version version) {
    return networkDao.list(new NetworkEntity(vspId, version, null));
  }

  @Override
  public void createNetwork(NetworkEntity network) {
    networkDao.create(network);
  }

  @Override
  public void updateNetwork(NetworkEntity networkEntity) {
    networkDao.update(networkEntity);
  }

  @Override
  public NetworkEntity getNetwork(String vspId, Version version, String networkId) {
    return networkDao.get(new NetworkEntity(vspId, version, networkId));
  }

  @Override
  public void deleteNetwork(String vspId, Version version) {
    NetworkEntity networkEntity = new NetworkEntity(vspId, version, null);
    networkDao.delete(networkEntity);
  }

  @Override
  public List<PackageInfo> listPackages(String category, String subCategory) {
    return packageInfoDao.listByCategory(category, subCategory);
  }


  @Override
  public Collection<ComponentEntity> listComponents(String vspId, Version version) {
    return componentDao.list(new ComponentEntity(vspId, version, null));
  }

  @Override
  public void createComponent(ComponentEntity component) {
    componentDao.create(component);
  }

  @Override
  public void updateComponent(ComponentEntity component) {
    componentDao.update(component);
  }

  @Override
  public ComponentEntity getComponent(String vspId, Version version, String componentId) {
    return componentDao.get(new ComponentEntity(vspId, version, componentId));
  }

  @Override
  public Collection<ComponentEntity> listComponentsQuestionnaire(String vspId, Version version) {
    return componentDao.listQuestionnaires(vspId, version);
  }

  @Override
  public void updateComponentQuestionnaire(String vspId, Version version, String componentId,
                                           String questionnaireData) {
    componentDao.updateQuestionnaireData(vspId, version, componentId, questionnaireData);
  }

  @Override
  public void deleteComponent(String vspId, Version version) {
    ComponentEntity componentEntity = new ComponentEntity(vspId, version, null);
    componentDao.delete(componentEntity);
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
    processDao.update(processEntity);
  }

  @Override
  public void deleteProcess(String vspId, Version version, String componentId, String processId) {
    processDao.delete(new ProcessEntity(vspId, version, componentId, processId));
  }

  @Override
  public void uploadProcessArtifact(String vspId, Version version, String componentId,
                                    String processId, byte[] artifact, String artifactName) {
    ProcessArtifactEntity processArtifact =
        new ProcessArtifactEntity(vspId, version, componentId, processId);
    processArtifact.setArtifact(ByteBuffer.wrap(artifact));
    processArtifact.setArtifactName(artifactName);
    processArtifactDao.update(processArtifact);
  }

  @Override
  public ProcessArtifactEntity getProcessArtifact(String vspId, Version version, String componentId,
                                                  String processId) {
    return processArtifactDao
        .get(new ProcessArtifactEntity(vspId, version, componentId, processId));
  }

  @Override
  public void deleteProcessArtifact(String vspId, Version version, String componentId,
                                    String processId) {
    processArtifactDao.delete(new ProcessArtifactEntity(vspId, version, componentId, processId));
  }

  @Override
  public VspQuestionnaireEntity getQuestionnaire(String vspId, Version version) {
    return vspQuestionnaireDao.get(new VspQuestionnaireEntity(vspId, version));
  }

  @Override
  public void updateQuestionnaire(String vspId, Version version, String questionnaireData) {
    vspQuestionnaireDao.updateQuestionnaireData(vspId, version, questionnaireData);
  }

  @Override
  public Collection<NicEntity> listNics(String vspId, Version version, String componentId) {
    return nicDao.list(new NicEntity(vspId, version, componentId, null));
  }

  @Override
  public void createNic(NicEntity nic) {
    nicDao.create(nic);
  }

  @Override
  public NicEntity getNic(String vspId, Version version, String componentId, String nicId) {
    return nicDao.get(new NicEntity(vspId, version, componentId, nicId));
  }

  @Override
  public void updateNic(NicEntity nicEntity) {
    nicDao.update(nicEntity);
  }

  @Override
  public void updateNicQuestionnaire(String vspId, Version version, String componentId,
                                     String nicId, String questionnaireData) {
    nicDao.updateQuestionnaireData(vspId, version, componentId, nicId, questionnaireData);
  }

  @Override
  public Collection<NicEntity> listNicsByVsp(String vspId, Version version) {
    return nicDao.listByVsp(vspId, version);
  }

  @Override
  public void deleteNic(String vspId, Version version, String componentId) {
    NicEntity nicEntity = new NicEntity(vspId, version, componentId, null);
    nicDao.delete(nicEntity);
  }

  @Override
  public void deleteUploadData(String vspId, Version version) {
    networkDao.deleteAll(vspId, version);
    nicDao.deleteByVspId(vspId, version);
    artifactDao.delete(vspId, version);
    enrichArtifactDao.delete(vspId, version);
    ComponentArtifactEntity componentArtifactEntity =
        new ComponentArtifactEntity(vspId, version, null, null);
    ProcessEntity processEntity = new ProcessEntity(vspId, version, null, null);
    componentArtifactDao.deleteAll(componentArtifactEntity);
    processDao.deleteAll(processEntity);
    componentDao.deleteAll(vspId, version);
    uploadDataDao.deleteContentDataAndValidationData(vspId, version);
  }

  @Override
  public void updateVspLatestModificationTime(String vspId, Version version) {
    if (Objects.isNull(vspId) || Objects.isNull(version)) {
      return;
    }

    VspDetails retrieved = getVendorSoftwareProductInfo(new VspDetails(vspId, version));
    updateVendorSoftwareProductInfo(retrieved);
  }
}
