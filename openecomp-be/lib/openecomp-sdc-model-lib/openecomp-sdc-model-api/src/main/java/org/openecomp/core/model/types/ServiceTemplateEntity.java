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
package org.openecomp.core.model.types;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Frozen;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.versioning.dao.types.Version;

@Table(keyspace = "dox", name = "vsp_service_template")
public class ServiceTemplateEntity implements ServiceElementEntity {

    private static final String ENTITY_TYPE;

    static {
        ENTITY_TYPE = "Vendor Software Product Service model";
    }

    @PartitionKey
    @Column(name = "vsp_id")
    public String id;
    @PartitionKey(value = 1)
    @Frozen
    public Version version;
    @ClusteringColumn
    @Column(name = "name")
    public String name;
    @Column(name = "content_data")
    public ByteBuffer contentData;
    @Column(name = "base_name")
    private String baseName;

    /**
     * Every entity class must have a default constructor according to
     * <a href="http://docs.datastax.com/en/developer/java-driver/2.1/manual/object_mapper/creating/">
     * Definition of mapped classes</a>.
     */
    public ServiceTemplateEntity() {
        // Don't delete! Default constructor is required by DataStax driver
    }

    /**
     * Instantiates a new Service template entity.
     *
     * @param entity the entity
     */
    public ServiceTemplateEntity(ServiceTemplate entity) {
        this.id = entity.getVspId();
        this.version = entity.getVersion();
        this.name = entity.getName();
        this.setBaseName(entity.getBaseName());
        try {
            this.contentData = ByteBuffer.wrap(ByteStreams.toByteArray(entity.getContent()));
        } catch (IOException ioException) {
            throw new SdcRuntimeException(ioException);
        }
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getId();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public void setVersion(Version version) {
        this.version = version;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ByteBuffer getContentData() {
        return contentData;
    }

    public void setContentData(ByteBuffer contentData) {
        this.contentData = contentData;
    }

    public ServiceTemplate getServiceTemplate() {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        serviceTemplate.setName(getName());
        serviceTemplate.setVersion(getVersion());
        serviceTemplate.setContentData(getContentData().array());
        serviceTemplate.setVspId(getId());
        serviceTemplate.setBaseName(getBaseName());
        return serviceTemplate;
    }
}
