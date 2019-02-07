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

package org.openecomp.sdc.be.components.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.PropertyDefinition;

public class ProxyServicePropertiesUtils {

    private ProxyServicePropertiesUtils() {
    }

    public static List<PropertyDefinition> getProperties(Component service) {
        List<PropertyDefinition> properties = service.getProperties();
        if (properties == null) {
            properties = new ArrayList<>();
        }
        Set<PropertyDefinition> serviceProperties = new HashSet<>(properties);
        if (service.getInputs() != null) {
            Set<PropertyDefinition> inputs = service.getInputs().stream().map(input -> new PropertyDefinition(input))
                                                    .collect(Collectors.toSet());
            serviceProperties.addAll(inputs);
        }
        serviceProperties =
                serviceProperties.stream().filter(distinctByKey(PropertyDefinition::getName)).collect(Collectors.toSet());
        return new ArrayList<>(serviceProperties);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = new HashSet<>();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
