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

package org.onap.sdc.tosca.datatypes.model.heatextend;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The enum Property type ext.
 */
public enum PropertyTypeExt {

    /**
     * Json property type ext.
     */
    JSON("json");

    private static final Map<String, PropertyTypeExt> M_MAP =
            Collections.unmodifiableMap(initializeMapping());
    private String displayName;

    PropertyTypeExt(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Initialize mapping map.
     *
     * @return the map
     */
    public static Map<String, PropertyTypeExt> initializeMapping() {
        Map<String, PropertyTypeExt> typeMap = new HashMap<>();
        for (PropertyTypeExt v : PropertyTypeExt.values()) {
            typeMap.put(v.displayName, v);
        }
        return typeMap;
    }

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }


}
