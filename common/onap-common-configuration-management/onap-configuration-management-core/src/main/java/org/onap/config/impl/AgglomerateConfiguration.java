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

package org.onap.config.impl;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.configuration2.DatabaseConfiguration;

public class AgglomerateConfiguration extends DatabaseConfiguration {

    private final Map<String, Object> store = Collections.synchronizedMap(new WeakHashMap<>());

    public Object getPropertyValue(String key) {

        Object objToReturn = store.get(key);
        if (objToReturn == null && !store.containsKey(key)) {
            objToReturn = super.getProperty(key);
            store.put(key, objToReturn);
        }

        return objToReturn;
    }

}
