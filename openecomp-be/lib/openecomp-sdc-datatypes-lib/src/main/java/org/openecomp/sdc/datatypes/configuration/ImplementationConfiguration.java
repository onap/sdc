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
package org.openecomp.sdc.datatypes.configuration;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.onap.config.api.Config;

/**
 * @author shiria
 * @since November 30, 2016.
 */
@Config(key = "")
@Getter
@Setter
public class ImplementationConfiguration {

    @Config(key = "enable")
    Boolean enable = true;
    @Config(key = "implementationClass")
    String implementationClass;
    @Config(key = "properties")
    Map<String, Object> properties;

    public Boolean isEnable() {
        return enable;
    }
}
