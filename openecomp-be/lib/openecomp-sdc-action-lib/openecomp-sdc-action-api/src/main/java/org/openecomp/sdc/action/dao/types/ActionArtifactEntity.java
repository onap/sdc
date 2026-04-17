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
package org.openecomp.sdc.action.dao.types;


import java.nio.ByteBuffer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.openecomp.sdc.action.types.ActionArtifact;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;

@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("action_artifact")
public class ActionArtifactEntity {

    @PartitionKey
    @CqlName("artifactuuid")
    private String artifactUuId;
    @ClusteringColumn
    @CqlName("effective_version")
    private int effectiveVersion;
    @CqlName("artifact")
    private ByteBuffer artifact;

    public ActionArtifactEntity(String artifactUuId, int effectiveVersion) {
        this.artifactUuId = artifactUuId;
        this.effectiveVersion = effectiveVersion;
    }

    /**
     * To dto action artifact.
     *
     * @return the action artifact
     */
    public ActionArtifact toDto() {
        ActionArtifact destination = new ActionArtifact();
        destination.setArtifactUuId(this.getArtifactUuId());
        destination.setEffectiveVersion(this.getEffectiveVersion());
        destination.setArtifact(this.getArtifact().array());
        return destination;
    }
}
