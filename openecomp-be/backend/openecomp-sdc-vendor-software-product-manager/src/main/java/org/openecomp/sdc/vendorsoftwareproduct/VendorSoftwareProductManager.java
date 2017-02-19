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

package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.MibUploadStatus;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

public interface VendorSoftwareProductManager {

  Version checkout(String vendorSoftwareProductId, String user);

  Version undoCheckout(String vendorSoftwareProductId, String user);

  Version checkin(String vendorSoftwareProductId, String user);

  ValidationResponse submit(String vendorSoftwareProductId, String user) throws IOException;


  VspDetails createNewVsp(VspDetails vspDetails, String user);

  List<VersionedVendorSoftwareProductInfo> getVspList(String versionFilter, String user);

  void updateVsp(VspDetails vspDetails, String user);

  VersionedVendorSoftwareProductInfo getVspDetails(String vspId, Version version, String user);

  void deleteVsp(String vspIdToDelete, String user);


  UploadFileResponse uploadFile(String vspId, InputStream heatFileToUpload, String user);

  PackageInfo createPackage(String vspId, String user) throws IOException;

  List<PackageInfo> listPackages(String category, String subCategory);

  File getTranslatedFile(String vspId, Version version, String user);

  File getLatestHeatPackage(String vspId, String user);

  QuestionnaireResponse getVspQuestionnaire(String vspId, Version version, String user);

  void updateVspQuestionnaire(String vspId, String questionnaireData, String user);


  Collection<NetworkEntity> listNetworks(String vspId, Version version, String user);

  NetworkEntity createNetwork(NetworkEntity network, String user);

  CompositionEntityValidationData updateNetwork(NetworkEntity networkEntity, String user);

  CompositionEntityResponse<Network> getNetwork(String vspId, Version version, String networkId,
                                                String user);

  void deleteNetwork(String vspId, String networkId, String user);


  QuestionnaireResponse getComponentQuestionnaire(String vspId, Version version, String componentId,
                                                  String user);

  void updateComponentQuestionnaire(String vspId, String componentId, String questionnaireData,
                                    String user);


  Collection<ComponentEntity> listComponents(String vspId, Version version, String user);

  void deleteComponents(String vspId, String user);

  ComponentEntity createComponent(ComponentEntity componentEntity, String user);

  CompositionEntityValidationData updateComponent(ComponentEntity componentEntity, String user);

  CompositionEntityResponse<ComponentData> getComponent(String vspId, Version version,
                                                        String componentId, String user);

  void deleteComponent(String vspId, String componentId, String user);


  Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity> listProcesses(
      String vspId, Version version, String componentId,
      String user);

  void deleteProcesses(String vspId, String componentId, String user);

  org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity createProcess(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity processEntity, String user);

  org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity getProcess(String vspId,
                                                                            Version version,
                                                                            String componentId,
                                                                            String processId,
                                                                            String user);

  void updateProcess(org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity processEntity,
                     String user);

  void deleteProcess(String vspId, String componentId, String processId, String user);


  File getProcessArtifact(String vspId, Version version, String componentId, String processId,
                          String user);

  void deleteProcessArtifact(String vspId, String componentId, String processId, String user);

  void uploadProcessArtifact(InputStream uploadFile, String fileName, String vspId,
                             String componentId, String processId, String user);


  Collection<org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity> listNics(String vspId,
                                                                                 Version version,
                                                                                 String componentId,
                                                                                 String user);

  org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity createNic(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nic, String user);

  CompositionEntityValidationData updateNic(
      org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity nicEntity, String user);

  CompositionEntityResponse<Nic> getNic(String vspId, Version version, String componentId,
                                        String nicId, String user);

  void deleteNic(String vspId, String componentId, String nicId, String user);

  QuestionnaireResponse getNicQuestionnaire(String vspId, Version version, String componentId,
                                            String nicId, String user);

  void updateNicQuestionnaire(String vspId, String componentId, String nicId,
                              String questionnaireData, String user);

  void deleteComponentMib(String vspId, String componentId, boolean isTrap, String user);

  void uploadComponentMib(InputStream object, String filename, String vspId, String componentId,
                          boolean isTrap, String user);

  MibUploadStatus listMibFilenames(String vspId, String componentId, String user);
}
