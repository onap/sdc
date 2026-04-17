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


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@EqualsAndHashCode
@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("vsp_component_dependency_modelComponentEntityComponentEntity")
public class ComponentDependencyModelEntity implements VersionableEntity {

    public static final String ENTITY_TYPE = "Vendor Software Product Component Dependency Model";
    @PartitionKey
    @CqlName("vsp_id")
    private String vspId;
    @PartitionKey(value = 1)

    private Version version;
    @ClusteringColumn
    @CqlName("dependency_id")
    private String id;
    @CqlName("sourcecomponent_id")
    private String sourceComponentId;
    @CqlName("targetcomponent_id")
    private String targetComponentId;
    @CqlName("relation")
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
