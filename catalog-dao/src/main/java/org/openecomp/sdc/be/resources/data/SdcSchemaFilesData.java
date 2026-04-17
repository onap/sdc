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
package org.openecomp.sdc.be.resources.data;

import java.nio.ByteBuffer;
import java.time.Instant;

import com.datastax.oss.driver.api.mapper.annotations.ClusteringColumn;
import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.datastax.oss.driver.api.mapper.annotations.Transient;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity(defaultKeyspace = "sdcartifact")
@CqlName("sdcschemafiles")
public class SdcSchemaFilesData {

    @PartitionKey(0)
    @CqlName("sdcreleasenum")
    private String sdcReleaseNum;
    @ClusteringColumn
    @CqlName("timestamp")
    private Instant timestamp;
    @PartitionKey(1)
    @CqlName("conformanceLevel")
    private String conformanceLevel;
    @CqlName("fileName")
    private String fileName;
    @CqlName("payload")
    @Setter(AccessLevel.NONE)
    private ByteBuffer payload;
    @CqlName("checksum")
    private String checksum;

    public SdcSchemaFilesData(String sdcReleaseNum, Instant timestamp, String conformanceLevel, String fileName, byte[] payload, String checksum) {
        this.sdcReleaseNum = sdcReleaseNum;
        this.timestamp = timestamp;
        this.conformanceLevel = conformanceLevel;
        this.fileName = fileName;
        if (payload != null) {
            this.payload = ByteBuffer.wrap(payload.clone());
        }
        this.checksum = checksum;
    }

    public void setPayload(ByteBuffer payload) {
        if (payload != null) {
            this.payload = payload.duplicate();
        }
    }

    @Transient
    public byte[] getPayloadAsArray() {
        return payload != null ? payload.array() : null;
    }

    public void setPayloadAsArray(byte[] payload) {
        if (payload != null) {
            this.payload = ByteBuffer.wrap(payload.clone());
        }
    }

    @Override
    public String toString() {
        return "SdcSchemaFilesData [sdcReleaseNum=" + sdcReleaseNum + ", timestamp=" + timestamp + ", conformanceLevel=" + conformanceLevel
            + ", fileName=" + fileName + ", checksum=" + checksum + "]";
    }
}
