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
package org.openecomp.sdc.vendorsoftwareproduct.dao.type;


import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("vsp_component")
public class ComponentEntity implements CompositionEntity {

    public static final String ENTITY_TYPE = "Vendor Software Product Component";
    @PartitionKey
    @CqlName("vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    private Version version;
    @ClusteringColumn
    @CqlName("component_id")
    private String id;
    @CqlName("composition_data")
    private String compositionData;
    @CqlName("questionnaire_data")
    private String questionnaireData;
    @Transient
    private List<NicEntity> nics = new ArrayList<>();

    /**
     * Instantiates a new Component entity.
     *
     * @param vspId   the vsp id
     * @param version the version
     * @param id      the id
     */
    public ComponentEntity(String vspId, Version version, String id) {
        this.vspId = vspId;
        this.version = version;
        this.id = id;
    }

    @Override
    public CompositionEntityType getType() {
        return CompositionEntityType.component;
    }

    @Override
    public CompositionEntityId getCompositionEntityId() {
        return new CompositionEntityId(getId(), new CompositionEntityId(getVspId(), null));
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getVspId();
    }

    public ComponentData getComponentCompositionData() {
        return compositionData == null ? null : JsonUtil.json2Object(compositionData, ComponentData.class);
    }

    public void setComponentCompositionData(ComponentData component) {
        this.compositionData = component == null ? null : JsonUtil.object2Json(component);
    }

    
}
