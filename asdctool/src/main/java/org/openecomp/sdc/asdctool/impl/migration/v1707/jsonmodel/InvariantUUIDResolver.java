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

package org.openecomp.sdc.asdctool.impl.migration.v1707.jsonmodel;

import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.operations.migration.MigrationMalformedDataLogger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InvariantUUIDResolver <T extends Component> {

    public String resolveInvariantUUID(List<T> components, T missingInvariantCmpt) {
        String uuid = missingInvariantCmpt.getUUID();
        String systemName = missingInvariantCmpt.getSystemName();
        String invariantUid = findInvariantUUidByAllVersionsMap(missingInvariantCmpt, components).orElseGet(() -> findInvariantUUidByUUIDOrSystemName(components, uuid, systemName));
        if (invariantUid == null) {
            MigrationMalformedDataLogger.logMalformedDataMsg(String.format("could not find invariant uuid for component %s with id %s", missingInvariantCmpt.getName(), missingInvariantCmpt.getUniqueId()));
        }
        return invariantUid;
    }

    private String findInvariantUUidByUUIDOrSystemName(List<T> components, String uuid, String systemName) {
        return components.stream()
                .filter(c -> c.getUUID().equals(uuid) || c.getSystemName().equals(systemName))
                .map(Component::getInvariantUUID)
                .filter(c -> c != null)
                .findAny().orElse(null);
    }

    private Optional<String> findInvariantUUidByAllVersionsMap(T component, List<T> allComponents) {
        if (component.getAllVersions() == null) return Optional.empty();
        Collection<String> allVersionsComponentIds = component.getAllVersions().values();
        return allComponents.stream().filter(c -> allVersionsComponentIds.contains(c.getUniqueId()))
                .map(Component::getInvariantUUID)
                .filter(c -> c != null)
                .findAny();


    }

}
