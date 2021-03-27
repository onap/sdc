/*
 * Copyright © 2016-2018 European Support Limited
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
package org.onap.config;

public class Constants {

    public static final String DEFAULT_TENANT = "GLOBAL";
    public static final String DEFAULT_NAMESPACE = "COMMON";
    public static final String DB_NAMESPACE = "SYSTEM";
    public static final String KEY_ELEMENTS_DELIMITER = "-";
    public static final String TENANT_NAMESPACE_SEPARATOR = ":";
    public static final String NAMESPACE_KEY = "_config.namespace";
    public static final String MODE_KEY = "_config.mergeStrategy";
    public static final String MBEAN_NAME = "org.openecomp.jmx:name=SystemConfig";
    public static final String LOAD_ORDER_KEY = "_config.loadOrder";

    private Constants() {
        // prevent instantiation
    }
}
