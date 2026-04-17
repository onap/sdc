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


import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.common.errors.SdcRuntimeException;
import org.openecomp.sdc.versioning.dao.types.Version;

@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("vsp_enriched_service_template")
public class EnrichedServiceTemplateEntity implements ServiceElementEntity {

    private static final String ENTITY_TYPE = "Vendor Software Product Service model";

    @PartitionKey
    @CqlName("vsp_id")
    public String id;
    @PartitionKey(value = 1)
    public Version version;
    @ClusteringColumn
    @CqlName("name")
    public String name;
    @CqlName("content_data")
    public ByteBuffer contentData;
    @CqlName("base_name")
    private String baseName;

    /**
     * Instantiates a new Enriched service template entity.
     *
     * @param entity the entity
     */
    public EnrichedServiceTemplateEntity(ServiceTemplate entity) {
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

    @Override
    public String getEntityType() {
        return ENTITY_TYPE;
    }

    @Override
    public String getFirstClassCitizenId() {
        return getId();
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
