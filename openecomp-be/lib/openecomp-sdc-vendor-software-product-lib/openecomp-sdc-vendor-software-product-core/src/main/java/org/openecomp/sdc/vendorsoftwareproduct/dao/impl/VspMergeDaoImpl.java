/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Resolution;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeHintDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeMapper;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VspMergeMapperBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;

public class VspMergeDaoImpl implements VspMergeDao {

    private static final String VSP_MODEL_NOT_EXIST = "Vsp model does not exist for Vsp %s, version %s.";
    private final VspMergeHintDao dao;
    private ZusammenAdaptor zusammenAdaptor;

    public VspMergeDaoImpl(CqlSession session, ZusammenAdaptor adaptor) {

        VspMergeMapper mapper = new VspMergeMapperBuilder(session).build();
        this.dao = mapper.vspMergeHintDao();
        this.zusammenAdaptor = adaptor;
    }

    @Override
    public boolean isConflicted(String vspId, Version version) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(new Id(vspId), new Id(version.getId()));
        return listVspModels(context, elementContext).size() > 1;
    }

    @Override
    public void updateHint(String vspId, Version version) {
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(new Id(vspId), new Id(version.getId()));
        String vspModelId = zusammenAdaptor.getElementInfoByName(context, elementContext, null, ElementType.VspModel.name())
            .orElseThrow(() -> new IllegalStateException(String.format(VSP_MODEL_NOT_EXIST, vspId, version.getId()))).getId().getValue();
        updateVspModelId(vspId, version, vspModelId);
    }

    @Override
    public void deleteHint(String vspId, Version version) {
        dao.delete(getUser(), vspId, version.getId());
    }

    @Override
    public void updateConflictResolution(String vspId, Version version, Resolution resolution) {
        dao.updateModelResolution(resolution, getUser(), vspId, version.getId());
    }

    @Override
    public void applyConflictResolution(String vspId, Version version) {
        //called only when no conflicts
        SessionContext context = createSessionContext();
        ElementContext elementContext = new ElementContext(new Id(vspId), new Id(version.getId()));
        List<ElementInfo> vspModels = listVspModels(context, elementContext);
        if (vspModels.size() == 1) {
            updateVspModelId(vspId, version, vspModels.iterator().next().getId().getValue());
            return;
        }
        if (vspModels.size() != 2) {
            return;
        }
        String user = getUser();
        Row row = dao.get(user, vspId, version.getId());
        if (row == null) {
            throw new IllegalStateException("Vsp model id must exists if its conflict is being resolved");
        }
        String resolutionValue = row.getString("model_resolution");
        if (resolutionValue == null) {
            return; // model conflict is not resolved yet
        }
        Resolution resolution = Resolution.valueOf(resolutionValue);
        String localModelId = row.getString("model_id");
        String chosenModelId = keepOnlyChosenVspModel(context, elementContext, vspModels, resolution, localModelId);
        dao.update(chosenModelId, null, user, vspId, version.getId());
    }

    private String keepOnlyChosenVspModel(SessionContext context, ElementContext elementContext, List<ElementInfo> vspModels, Resolution resolution,
                                          String localModelId) {
        String newLocalModelId = null;
        for (ElementInfo vspModel : vspModels) {
            if (isRedundantModel(vspModel.getId().getValue(), localModelId, resolution)) {
                zusammenAdaptor.saveElement(context, elementContext, buildElement(vspModel.getId(), Action.DELETE), "Delete Redundant Vsp Model");
            } else {
                newLocalModelId = vspModel.getId().getValue();
            }
        }
        return newLocalModelId;
    }

    private boolean isRedundantModel(String modelId, String localModelId, Resolution resolution) {
        return resolution == Resolution.THEIRS && modelId.equals(localModelId) || resolution == Resolution.YOURS && !modelId.equals(localModelId);
    }

    private List<ElementInfo> listVspModels(SessionContext context, ElementContext elementContext) {
        return zusammenAdaptor.listElements(context, elementContext, null).stream()
            .filter(elementInfo -> ElementType.VspModel.name().equals(elementInfo.getInfo().getName())).collect(Collectors.toList());
    }

    private void updateVspModelId(String vspId, Version version, String vspModelId) {
        dao.update(vspModelId, null, getUser(), vspId, version.getId());
    }

    private String getUser() {
        return SessionContextProviderFactory.getInstance().createInterface().get().getUser().getUserId();
    }

}
