/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
package org.onap.sdc.tosca.services;

import java.util.LinkedHashSet;
import java.util.Set;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class MyPropertyUtils extends PropertyUtils {

    //Unsorted properties
    @Override
    protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bnAccess) {
        return new LinkedHashSet<>(getPropertiesMap(type, BeanAccess.FIELD).values());
    }

    @Override
    public Property getProperty(Class<?> type, String name) {
        String updatedName = name;
        if (YamlUtil.DEFAULT.equals(updatedName)) {
            updatedName = YamlUtil.DEFAULT_STR;
        }
        return super.getProperty(type, updatedName);
    }
}
