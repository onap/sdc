/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.impl.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.openecomp.sdc.be.datatypes.elements.OperationDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.Service;

public final class ComponentUtils {

    private ComponentUtils() {
    }

    public static boolean checkArtifactInComponent(Component component, String artifactId) {
        Predicate<ArtifactDefinition> hasSameArtifactId = ad -> ad != null && ad.getUniqueId().equals(artifactId);
        return exists(component.getArtifacts(), hasSameArtifactId) || exists(component.getDeploymentArtifacts(), hasSameArtifactId) || exists(
            component.getToscaArtifacts(), hasSameArtifactId) || hasOperationImplementationWithUniqueId(component, artifactId)
            || isServiceAndHasApiArtifactWithUniqueId(component, hasSameArtifactId);
    }

    private static boolean isServiceAndHasApiArtifactWithUniqueId(Component component, Predicate<ArtifactDefinition> hasSameArtifactId) {
        return component.getComponentType() == ComponentTypeEnum.SERVICE && exists(((Service) component).getServiceApiArtifacts(), hasSameArtifactId);
    }

    private static boolean hasOperationImplementationWithUniqueId(Component component, String artifactId) {
        return findFirst(
            valueStream(component.getInterfaces()).flatMap(v -> valueStream(v.getOperationsMap())).map(OperationDataDefinition::getImplementation),
            e -> e != null && e.getUniqueId().equals(artifactId)).isPresent();
    }

    public static boolean checkArtifactInResourceInstance(Component component, String resourceInstanceId, String artifactId) {
        Predicate<ComponentInstance> hasSameResourceId = ri -> ri != null && ri.getUniqueId().equals(resourceInstanceId);
        Predicate<ArtifactDefinition> hasSameArtifactId = ad -> ad != null && ad.getUniqueId().equals(artifactId);
        return findFirst(component.getComponentInstances(), hasSameResourceId)
            .map(ri -> exists(ri.getDeploymentArtifacts(), hasSameArtifactId) || exists(ri.getArtifacts(), hasSameArtifactId)).isPresent();
    }

    private static <V> Optional<V> findFirst(List<V> ovs, Predicate<V> p) {
        return Optional.ofNullable(ovs).flatMap(vs -> findFirst(vs.stream(), p));
    }

    private static <K, V> boolean exists(Map<K, V> okvs, Predicate<V> p) {
        return Optional.ofNullable(okvs).flatMap(kvs -> findFirst(kvs.values().stream(), p)).isPresent();
    }

    private static <V> Optional<V> findFirst(Stream<V> vs, Predicate<V> p) {
        return Optional.ofNullable(vs).flatMap(ms -> ms.filter(p).findFirst());
    }

    private static <K, V> Stream<V> valueStream(Map<K, V> okvs) {
        return Optional.ofNullable(okvs).map(kvs -> kvs.values().stream()).orElse(Stream.empty());
    }
}
