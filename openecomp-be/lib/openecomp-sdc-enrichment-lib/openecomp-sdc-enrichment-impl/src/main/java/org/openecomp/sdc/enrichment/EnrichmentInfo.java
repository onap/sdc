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

package org.openecomp.sdc.enrichment;

import org.openecomp.core.enrichment.types.EntityInfo;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrichmentInfo {
    Map<String, List<Object>> additionalInfo = new HashMap<>();
    Map<String, EntityInfo> entitiesInfo = new HashMap<>();
    String key;
    Version version;

    public Map<String, List<Object>> getAdditionalInfo() {
        return additionalInfo;
    }

    public Map<String, EntityInfo> getEntityInfo() {
        return entitiesInfo;
    }

    public void addEntityInfo(String entityKey, EntityInfo entityInfo) {
        this.entitiesInfo.put(entityKey, entityInfo);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }
}
