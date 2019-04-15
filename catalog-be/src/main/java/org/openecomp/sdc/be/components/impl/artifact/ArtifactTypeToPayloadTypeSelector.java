/*
 * -
 *  * ============LICENSE_START=======================================================
 *  *  Copyright (C) 2019 Nordix Foundation.
 *  * ================================================================================
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * SPDX-License-Identifier: Apache-2.0
 *  * ============LICENSE_END=========================================================
 *
 */

package org.openecomp.sdc.be.components.impl.artifact;

import org.openecomp.sdc.common.api.ArtifactTypeEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.HEAT_YAML;
import static org.openecomp.sdc.be.components.impl.artifact.PayloadTypeEnum.NOT_DEFINED;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.DCAE_INVENTORY_JSON;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.DCAE_INVENTORY_TOSCA;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.HEAT;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.HEAT_ENV;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.HEAT_NET;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.HEAT_VOL;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.LIFECYCLE_OPERATIONS;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.MODEL_INVENTORY_PROFILE;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.MODEL_QUERY_SPEC;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.PM_DICTIONARY;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.UCPE_LAYER_2_CONFIGURATION;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.VENDOR_LICENSE;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.VES_EVENTS;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.VF_LICENSE;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.VNF_CATALOG;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.YANG_XML;

public class ArtifactTypeToPayloadTypeSelector {

    private static final Map<ArtifactTypeWithExtension, PayloadTypeEnum> artifactTypeWithExtension2PayloadType = new HashMap<>();
    private static final Map<String, PayloadTypeEnum> artifactType2PayloadType = new HashMap<>();
    private static final String XML = "xml";
    private static final String JSON = "json";
    private static final String YML = "yml";
    private static final String YAML = "yaml";

    static {
        populateArtifactTypeWithExtensionMap();
        populateArtifactsTypeOnlyMap();
    }

    private static void populateArtifactTypeWithExtensionMap() {
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_JSON, XML), PayloadTypeEnum.XML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_JSON, JSON), PayloadTypeEnum.JSON);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_JSON, YML), PayloadTypeEnum.YAML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_JSON, YAML), PayloadTypeEnum.YAML);

        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_TOSCA, XML), PayloadTypeEnum.XML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_TOSCA, JSON), PayloadTypeEnum.JSON);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_TOSCA, YML), PayloadTypeEnum.YAML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(DCAE_INVENTORY_TOSCA, YAML), PayloadTypeEnum.YAML);

        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(VES_EVENTS, XML), PayloadTypeEnum.XML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(VES_EVENTS, JSON), PayloadTypeEnum.JSON);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(VES_EVENTS, YML), PayloadTypeEnum.YAML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(VES_EVENTS, YAML), PayloadTypeEnum.YAML);

        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(LIFECYCLE_OPERATIONS, XML), PayloadTypeEnum.XML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(LIFECYCLE_OPERATIONS, JSON), PayloadTypeEnum.JSON);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(LIFECYCLE_OPERATIONS, YML), PayloadTypeEnum.YAML);
        artifactTypeWithExtension2PayloadType.put(createArtifactTypeWithExtension(LIFECYCLE_OPERATIONS, YAML), PayloadTypeEnum.YAML);
    }

    private static void populateArtifactsTypeOnlyMap() {
        artifactType2PayloadType.put(HEAT.getType().toLowerCase(), HEAT_YAML);
        artifactType2PayloadType.put(HEAT_VOL.getType().toLowerCase(), HEAT_YAML);
        artifactType2PayloadType.put(HEAT_NET.getType().toLowerCase(), HEAT_YAML);
        artifactType2PayloadType.put(HEAT_ENV.getType().toLowerCase(), PayloadTypeEnum.HEAT_ENV);

        artifactType2PayloadType.put(YANG_XML.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(VNF_CATALOG.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(VF_LICENSE.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(VENDOR_LICENSE.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(MODEL_INVENTORY_PROFILE.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(MODEL_QUERY_SPEC.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(UCPE_LAYER_2_CONFIGURATION.getType().toLowerCase(), PayloadTypeEnum.XML);
        artifactType2PayloadType.put(PM_DICTIONARY.getType().toLowerCase(), PayloadTypeEnum.YAML);
    }

    private static ArtifactTypeWithExtension createArtifactTypeWithExtension(ArtifactTypeEnum artifactTypeEnum, String extension) {
        return createArtifactTypeWithExtension(artifactTypeEnum.getType(), extension);
    }

    private static ArtifactTypeWithExtension createArtifactTypeWithExtension(String artifactType, String extension) {
        return new ArtifactTypeToPayloadTypeSelector().new ArtifactTypeWithExtension(artifactType.toLowerCase(), extension.toLowerCase());
    }

    public static PayloadTypeEnum getPayloadType(String artifactType, String fileExtension) {
        PayloadTypeEnum payloadType = artifactTypeWithExtension2PayloadType.get(createArtifactTypeWithExtension(artifactType, fileExtension));
        payloadType = payloadType != null ? payloadType : artifactType2PayloadType.get(artifactType.toLowerCase());
        return payloadType != null ? payloadType : NOT_DEFINED;
    }

    private class ArtifactTypeWithExtension {
        private String artifactType;
        private String fileExtension;

        public ArtifactTypeWithExtension(String artifactType, String fileExtension) {
            this.artifactType = artifactType;
            this.fileExtension = fileExtension;
        }

        public String getArtifactType() {
            return artifactType;
        }

        public String getFileExtension() {
            return fileExtension;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ArtifactTypeWithExtension)) {
                return false;
            }
            ArtifactTypeWithExtension otherArtifactTypeWithExtension = (ArtifactTypeWithExtension) other;

            return isArtifactTypeEqual(otherArtifactTypeWithExtension.getArtifactType()) &&
                    isFileExtensionEqual(otherArtifactTypeWithExtension.getFileExtension());
        }

        @Override
        public int hashCode() {
            return Objects.hash(artifactType, fileExtension);
        }

        private boolean isArtifactTypeEqual(String otherArtifactType) {
            if (artifactType == null) {
                return otherArtifactType == null;
            }
            return artifactType.equalsIgnoreCase(otherArtifactType);
        }

        private boolean isFileExtensionEqual(String otherFileExtension) {
            if (fileExtension == null) {
                return otherFileExtension == null;
            }
            return fileExtension.equalsIgnoreCase(otherFileExtension);
        }
    }
}
