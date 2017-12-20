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

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CompositionEditNotAllowedErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public class NetworkManagerImpl implements NetworkManager {
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  private NetworkDao networkDao;
  private CompositionEntityDataManager compositionEntityDataManager;

  public NetworkManagerImpl(NetworkDao networkDao,
                            CompositionEntityDataManager compositionEntityDataManager) {
    this.networkDao = networkDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
  }

  @Override
  public Collection<NetworkEntity> listNetworks(String vspId, Version version) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", vspId);
    mdcDataDebugMessage.debugExitMessage("VSP id", vspId);

    return networkDao.list(new NetworkEntity(vspId, version, null));
  }

  @Override
  public NetworkEntity createNetwork(NetworkEntity network) {
    mdcDataDebugMessage.debugEntryMessage("VSP id", network.getVspId());

    if (!isManual(network.getVspId(), network.getVersion())) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_NETWORK, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't create network");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(network.getVspId(), network.getVersion())
              .build());
    }

/*    networkDao
        .updateVspLatestModificationTime(network.getVspId(), network.getVersion());*/

    mdcDataDebugMessage.debugExitMessage("VSP id", network.getVspId());

    return null;
  }

  @Override
  public CompositionEntityValidationData updateNetwork(NetworkEntity network) {
    mdcDataDebugMessage
        .debugEntryMessage("VSP id, network id", network.getVspId(), network.getId());

    NetworkEntity retrieved = getValidatedNetwork(network.getVspId(), network.getVersion(), network.getId());

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(isManual(network.getVspId(), network.getVersion()));
    schemaInput.setNetwork(retrieved.getNetworkCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(network, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      networkDao.update(network);
    }

/*    networkDao
        .updateVspLatestModificationTime(network.getVspId(), network.getVersion());*/

    mdcDataDebugMessage
        .debugExitMessage("VSP id, network id", network.getVspId(), network.getId());

    return validationData;
  }

  @Override
  public CompositionEntityResponse<Network> getNetwork(String vspId, Version version,
                                                       String networkId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, network id", vspId, networkId);

    NetworkEntity networkEntity = getValidatedNetwork(vspId, version, networkId);
    Network network = networkEntity.getNetworkCompositionData();

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(isManual(vspId, version));
    schemaInput.setNetwork(network);

    CompositionEntityResponse<Network> response = new CompositionEntityResponse<>();
    response.setId(networkId);
    response.setData(network);
    response.setSchema(getCompositionSchema(schemaInput));

    mdcDataDebugMessage.debugExitMessage("VSP id, network id", vspId, networkId);

    return response;
  }


  private NetworkEntity getValidatedNetwork(String vspId, Version version, String networkId) {
    NetworkEntity retrieved = networkDao.get(new NetworkEntity(vspId, version, networkId));
    VersioningUtil.validateEntityExistence(retrieved, new NetworkEntity(vspId, version, networkId),
        VspDetails.ENTITY_TYPE);
    return retrieved;
  }

  @Override
  public void deleteNetwork(String vspId, Version version, String networkId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, network id", vspId, networkId);

    if (!isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_NETWORK, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete network");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
    }

    //networkDao.updateVspLatestModificationTime(vspId, version);

    mdcDataDebugMessage.debugExitMessage("VSP id, network id", vspId, networkId);
  }

  // todo *************************** move to reusable place! *************************

  private boolean isManual(String vspId, Version version) {
    return false;
  }

  protected String getCompositionSchema(NetworkCompositionSchemaInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.network, schemaInput);
  }
}
