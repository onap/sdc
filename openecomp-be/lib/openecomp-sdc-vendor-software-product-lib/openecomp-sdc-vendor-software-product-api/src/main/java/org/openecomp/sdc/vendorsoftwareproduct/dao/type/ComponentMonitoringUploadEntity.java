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

import java.nio.ByteBuffer;
import org.openecomp.core.enrichment.types.MonitoringUploadType;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;
import org.openecomp.sdc.versioning.dao.types.VersionableEntity;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Entity
@CqlName("vsp_component_artifact")
public class ComponentMonitoringUploadEntity implements VersionableEntity {

    public static final String ENTITY_TYPE = "Vendor Software Product Component Artifact";
    @PartitionKey
    @CqlName("vsp_id")
    private String vspId;
    @PartitionKey(value = 1)
    private Version version;
    @ClusteringColumn
    @CqlName("component_id")
    private String componentId;
    @ClusteringColumn(value = 1)
    @CqlName("artifact_type")
    private MonitoringUploadType type;
    @ClusteringColumn(value = 2)
    @CqlName("artifact_id")
    private String id;
    @CqlName("name")
    private String artifactName;
    private ByteBuffer artifact;

    /**
     * Every entity class must have a default constructor according to
     * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
     * Definition of mapped classes</a>.
     */
    public ComponentMonitoringUploadEntity() {
        // Don't delete! Default constructor is required by DataStax driver
    }

    /**
     * Instantiates a new Component artifact entity.
     *
     * @param vspId       the vsp id
     * @param version     the version
     * @param componentId the component id
     * @param id          the id
     */
    public ComponentMonitoringUploadEntity(String vspId, Version version, String componentId, String id) {
        this.vspId = vspId;
        this.version = version;
        this.componentId = componentId;
        this.id = id;
    }

    public String getVspId() {
        return vspId;
    }

    public void setVspId(String vspId) {
        this.vspId = vspId;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getVspId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public ByteBuffer getArtifact() {
        return artifact;
    }

    public void setArtifact(ByteBuffer artifact) {
        this.artifact = artifact;
    }

    public MonitoringUploadType getType() {
        return type;
    }

    public void setType(MonitoringUploadType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ComponentMonitoringUploadEntity{" + "vspId='" + vspId + '\'' + ", version=" + version + ", componentId='" + componentId + '\''
            + ", type=" + type + ", id='" + id + '\'' + ", artifactName='" + artifactName + '\'' + '}';
    }

   
}
