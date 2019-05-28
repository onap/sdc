/*
 * Copyright (C) 2019 Samsung. All rights reserved.
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

package org.onap.config.util;

import org.onap.config.api.Config;

import java.util.Map;

@Config(key = "")
public class TestImplementationConfiguration {
    @Config(key = "enable")
    Boolean enable = true;
    @Config(key = "implementationClass")
    String implementationClass;
    @Config(key = "properties")
    Map<String, Object> properties;

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    public Boolean isEnable() {
        return enable;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

}
