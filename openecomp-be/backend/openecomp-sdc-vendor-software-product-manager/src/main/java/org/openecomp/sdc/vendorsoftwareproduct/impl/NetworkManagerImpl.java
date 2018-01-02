/*
 * Copyright © 2016-2017 European Support Limited
 *
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
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
  private static final MdcDataDebugMessage MDC_DATA_DEBUG_MESSAGE = new MdcDataDebugMessage();

  private final NetworkDao networkDao;
  private final CompositionEntityDataManager compositionEntityDataManager;
  private final VendorSoftwareProductInfoDao VSPInfoDao;

  private static final  String VSP_ID = "VSP id";
  private static final String VSP_ID_NETWORK_ID = "VSP id, network id";

  public NetworkManagerImpl(NetworkDao networkDao,
                            CompositionEntityDataManager compositionEntityDataManager,
                            VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao) {
    this.networkDao = networkDao;
    this.compositionEntityDataManager = compositionEntityDataManager;
    this.VSPInfoDao = vendorSoftwareProductInfoDao;
  }

  @Override
  public Collection<NetworkEntity> listNetworks(String vspId, Version version) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(VSP_ID, vspId);
    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID, vspId);

    return networkDao.list(new NetworkEntity(vspId, version, null));
  }

  @Override
  public NetworkEntity createNetwork(NetworkEntity network) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(VSP_ID, network.getVspId());

    if (!VSPInfoDao.isManual(network.getVspId(), network.getVersion())) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_NETWORK, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't create network");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(network.getVspId(), network.getVersion())
              .build());
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID, network.getVspId());

    return null;
  }

  @Override
  public CompositionEntityValidationData updateNetwork(NetworkEntity network) {
    MDC_DATA_DEBUG_MESSAGE
        .debugEntryMessage(VSP_ID_NETWORK_ID, network.getVspId(), network.getId());

    NetworkEntity retrieved = getValidatedNetwork(network.getVspId(), network.getVersion(), network.getId());

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(!VSPInfoDao.isManual(network.getVspId(), network.getVersion()));
    schemaInput.setNetwork(retrieved.getNetworkCompositionData());

    CompositionEntityValidationData validationData = compositionEntityDataManager
        .validateEntity(network, SchemaTemplateContext.composition, schemaInput);
    if (CollectionUtils.isEmpty(validationData.getErrors())) {
      networkDao.update(network);
    }

    MDC_DATA_DEBUG_MESSAGE
        .debugExitMessage(VSP_ID_NETWORK_ID, network.getVspId(), network.getId());

    return validationData;
  }

  @Override
  public CompositionEntityResponse<Network> getNetwork(String vspId, Version version,
                                                       String networkId) {
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(VSP_ID_NETWORK_ID, vspId, networkId);

    NetworkEntity networkEntity = getValidatedNetwork(vspId, version, networkId);
    Network network = networkEntity.getNetworkCompositionData();

    NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
    schemaInput.setManual(!VSPInfoDao.isManual(vspId, version));
    schemaInput.setNetwork(network);

    CompositionEntityResponse<Network> response = new CompositionEntityResponse<>();
    response.setId(networkId);
    response.setData(network);
    response.setSchema(getCompositionSchema(schemaInput));

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID_NETWORK_ID, vspId, networkId);

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
    MDC_DATA_DEBUG_MESSAGE.debugEntryMessage(VSP_ID_NETWORK_ID, vspId, networkId);

    if (!VSPInfoDao.isManual(vspId, version)) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.DELETE_NETWORK, ErrorLevel.ERROR.name(),
          LoggerErrorCode.PERMISSION_ERROR.getErrorCode(), "Can't delete network");
      throw new CoreException(
          new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
    }

    MDC_DATA_DEBUG_MESSAGE.debugExitMessage(VSP_ID_NETWORK_ID, vspId, networkId);
  }


  protected String getCompositionSchema(NetworkCompositionSchemaInput schemaInput) {
    return SchemaGenerator
        .generate(SchemaTemplateContext.composition, CompositionEntityType.network, schemaInput);
  }
}
