 
/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Network Service Descriptor known versions
 */
@Getter
@AllArgsConstructor
public enum EtsiVersion {
    VERSION_2_5_1("2.5.1"), VERSION_2_7_1("2.7.1"), VERSION_3_3_1("3.3.1");
    private final String version;

    public static EtsiVersion convertOrNull(final String etsiVersion) {
        if (StringUtils.isEmpty(etsiVersion)) {
            return null;
        }
        if (VERSION_2_5_1.getVersion().equals(etsiVersion)) {
            return VERSION_2_5_1;
        }
        if (VERSION_2_7_1.getVersion().equals(etsiVersion)) {
            return VERSION_2_7_1;
        }
        if (VERSION_3_3_1.getVersion().equals(etsiVersion)) {
            return VERSION_3_3_1;
        }
        return null;
    }

    public static EtsiVersion getDefaultVersion() {
        return VERSION_2_5_1;
    }
}
