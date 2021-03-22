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
package org.openecomp.sdc.vendorsoftwareproduct.dao.type;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityId;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.versioning.dao.types.Version;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@Table(keyspace = "dox", name = "vsp_deployment_flavor")
public class DeploymentFlavorEntity implements CompositionEntity {

    private static final String ENTITY_TYPE = "Vendor Software Product Deployment Flavor";
    @PartitionKey
    @Column(name = "vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    @Frozen
    private Version version;
    @ClusteringColumn
    @Column(name = "deployment_flavor_id")
    private String id;
    @Column(name = "composition_data")
    private String compositionData;
    @Column(name = "questionnaire_data")
    private String questionnaireData;

    /**
     * Instantiates a new DeploymentFlavor entity.
     *
     * @param vspId   the vsp id
     * @param version the version
     * @param id      the id
     */
    public DeploymentFlavorEntity(String vspId, Version version, String id) {
        this.vspId = vspId;
        this.version = version;
        this.id = id;
    }

    @Override
    public CompositionEntityType getType() {
        return CompositionEntityType.deployment;
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

    public DeploymentFlavor getDeploymentFlavorCompositionData() {
        return compositionData == null ? null : JsonUtil.json2Object(compositionData, DeploymentFlavor.class);
    }

    public void setDeploymentFlavorCompositionData(DeploymentFlavor deploymentFlavor) {
        this.compositionData = deploymentFlavor == null ? null : JsonUtil.object2Json(deploymentFlavor);
    }
}
