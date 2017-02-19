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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessArtifactEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspQuestionnaireEntity;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public interface VendorSoftwareProductDao extends VersionableDao {

  void createVendorSoftwareProductInfo(VspDetails vspDetails);

  Collection<VspDetails> listVendorSoftwareProductsInfo();

  VspDetails getVendorSoftwareProductInfo(VspDetails vspDetails);

  void updateVendorSoftwareProductInfo(VspDetails vspDetails);

  void deleteVendorSoftwareProductInfo(VspDetails vspDetails);


  void updateUploadData(UploadDataEntity uploadData);

  UploadDataEntity getUploadData(UploadDataEntity uploadData);

  ByteBuffer getContentData(UploadDataEntity uploadDataEntity);


  List<PackageInfo> listPackages(String category, String subCategory);

  void insertPackageDetails(PackageInfo packageInfo);

  PackageInfo getPackageInfo(PackageInfo packageInfo);

  void deletePackageInfo(PackageInfo packageInfo);


  Collection<NetworkEntity> listNetworks(String vspId, Version version);

  void createNetwork(NetworkEntity network);

  void updateNetwork(NetworkEntity network);

  NetworkEntity getNetwork(String vspId, Version version, String networkId);

  void deleteNetwork(String vspId, Version version);


  Collection<ComponentEntity> listComponents(String vspId, Version version);

  void createComponent(ComponentEntity component);

  void updateComponent(ComponentEntity component);

  ComponentEntity getComponent(String vspId, Version version, String componentId);

  Collection<ComponentEntity> listComponentsQuestionnaire(String vspId, Version version);

  void updateComponentQuestionnaire(String vspId, Version version, String componentId,
                                    String questionnaireData);

  void deleteComponent(String vspId, Version version);


  Collection<ProcessEntity> listProcesses(String vspId, Version version, String componentId);

  void deleteProcesses(String vspId, Version version, String componentId);

  ProcessEntity getProcess(String vspId, Version version, String componentId, String processId);

  void createProcess(ProcessEntity processEntity);

  void updateProcess(ProcessEntity processEntity);

  void deleteProcess(String vspId, Version version, String componentId, String processId);


  void uploadProcessArtifact(String vspId, Version version, String componentId, String processId,
                             byte[] artifact, String artifactFileName);

  ProcessArtifactEntity getProcessArtifact(String vspId, Version version, String componentId,
                                           String processId);

  void deleteProcessArtifact(String vspId, Version version, String componentId, String processId);


  VspQuestionnaireEntity getQuestionnaire(String vspId, Version version);

  void updateQuestionnaire(String vspId, Version version, String questionnaireData);


  Collection<NicEntity> listNics(String vspId, Version version, String componentId);

  void createNic(NicEntity nic);

  void updateNic(NicEntity nicEntity);

  NicEntity getNic(String vspId, Version version, String componentId, String nicId);

  void updateNicQuestionnaire(String vspId, Version version, String componentId, String nicId,
                              String questionnaireData);

  Collection<NicEntity> listNicsByVsp(String vspId, Version version);

  void deleteNic(String vspId, Version version, String componentId);

  void deleteUploadData(String vspId, Version version);

  void updateVspLatestModificationTime(String vspId, Version version);
}
