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

import java.util.Collection;
import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.CompositionEditNotAllowedErrorBuilder;
import org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator.SchemaGenerator;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Network;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.NetworkCompositionSchemaInput;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

public class NetworkManagerImpl implements NetworkManager {

    private final NetworkDao networkDao;
    private final CompositionEntityDataManager compositionEntityDataManager;
    private final VendorSoftwareProductInfoDao vspInfoDao;

    public NetworkManagerImpl(NetworkDao networkDao, CompositionEntityDataManager compositionEntityDataManager,
                              VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao) {
        this.networkDao = networkDao;
        this.compositionEntityDataManager = compositionEntityDataManager;
        this.vspInfoDao = vendorSoftwareProductInfoDao;
    }

    @Override
    public Collection<NetworkEntity> listNetworks(String vspId, Version version) {
        return networkDao.list(new NetworkEntity(vspId, version, null));
    }

    @Override
    public NetworkEntity createNetwork(NetworkEntity network) {
        if (!vspInfoDao.isManual(network.getVspId(), network.getVersion())) {
            throw new CoreException(new CompositionEditNotAllowedErrorBuilder(network.getVspId(), network.getVersion()).build());
        }
        return null;
    }

    @Override
    public CompositionEntityValidationData updateNetwork(NetworkEntity network) {
        NetworkEntity retrieved = getValidatedNetwork(network.getVspId(), network.getVersion(), network.getId());
        NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(network.getVspId(), network.getVersion()));
        schemaInput.setNetwork(retrieved.getNetworkCompositionData());
        CompositionEntityValidationData validationData = compositionEntityDataManager
            .validateEntity(network, SchemaTemplateContext.composition, schemaInput);
        if (CollectionUtils.isEmpty(validationData.getErrors())) {
            networkDao.update(network);
        }
        return validationData;
    }

    @Override
    public CompositionEntityResponse<Network> getNetwork(String vspId, Version version, String networkId) {
        NetworkEntity networkEntity = getValidatedNetwork(vspId, version, networkId);
        Network network = networkEntity.getNetworkCompositionData();
        NetworkCompositionSchemaInput schemaInput = new NetworkCompositionSchemaInput();
        schemaInput.setManual(vspInfoDao.isManual(vspId, version));
        schemaInput.setNetwork(network);
        CompositionEntityResponse<Network> response = new CompositionEntityResponse<>();
        response.setId(networkId);
        response.setData(network);
        response.setSchema(getCompositionSchema(schemaInput));
        return response;
    }

    private NetworkEntity getValidatedNetwork(String vspId, Version version, String networkId) {
        NetworkEntity retrieved = networkDao.get(new NetworkEntity(vspId, version, networkId));
        VersioningUtil.validateEntityExistence(retrieved, new NetworkEntity(vspId, version, networkId), VspDetails.ENTITY_TYPE);
        return retrieved;
    }

    @Override
    public void deleteNetwork(String vspId, Version version, String networkId) {
        if (!vspInfoDao.isManual(vspId, version)) {
            throw new CoreException(new CompositionEditNotAllowedErrorBuilder(vspId, version).build());
        }
    }

    protected String getCompositionSchema(NetworkCompositionSchemaInput schemaInput) {
        return SchemaGenerator.generate(SchemaTemplateContext.composition, CompositionEntityType.network, schemaInput);
    }
}
