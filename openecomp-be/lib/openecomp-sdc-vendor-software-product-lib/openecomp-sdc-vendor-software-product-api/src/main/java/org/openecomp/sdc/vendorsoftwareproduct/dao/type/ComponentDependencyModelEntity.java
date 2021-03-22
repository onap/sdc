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
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@Table(keyspace = "dox", name = "vsp_component_dependency_model")
public class ComponentDependencyModelEntity implements VersionableEntity {

    public static final String ENTITY_TYPE = "Vendor Software Product Component Dependency Model";
    @PartitionKey
    @Column(name = "vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    @Frozen
    private Version version;
    @ClusteringColumn
    @Column(name = "dependency_id")
    private String id;
    @Column(name = "sourcecomponent_id")
    private String sourceComponentId;
    @Column(name = "targetcomponent_id")
    private String targetComponentId;
    @Column(name = "relation")
    private String relation;

    /**
     * Instantiates a new ComponentDependencyModelEntity entity.
     *
     * @param vspId        the vsp id
     * @param version      the version
     * @param dependencyId the dependencyId
     */
    public ComponentDependencyModelEntity(String vspId, Version version, String dependencyId) {
        this.vspId = vspId;
        this.version = version;
        this.id = dependencyId;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getVspId();
    }
}
