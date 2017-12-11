/*
 * Copyright © 2016-2017 European Support Limited
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

package org.openecomp.sdc.logging.api;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Contains common functionality for factories used in the logging framework.
 *
 * <p>In order to use the factory, a particular (e.g. framework-specific) implementation of a service must be
 * configured as described in
 * <a href="http://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">java.util.ServiceLoader</a>).</p>
 *
 * @author evitaliy
 * @since 13/09/2016.
 *
 * @see ServiceLoader
 */
public class BaseFactory {

    protected static <T> T locateService(Class<T> clazz) {

        T service;
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        Iterator<T> iterator = loader.iterator();
        if (iterator.hasNext()) {
            service = iterator.next();

            return service;
        }

        throw new IllegalArgumentException(String.format("No implementations configured for %s", clazz.getName()));
    }

    @Override
    public String toString() {
        return "BaseFactory{}";
    }
}
