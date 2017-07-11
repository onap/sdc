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

import fj.data.Either;
import jersey.repackaged.com.google.common.collect.Sets;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.tosca.ToscaPropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NormativesMigration extends ComponentMigration<Resource> {

    private static Logger LOGGER = LoggerFactory.getLogger(NormativesMigration.class);
    private static final String JCP_VERSION_PROPERTY = "jcp-version";
    private static final Set<String> e2eMalformedVfcs = Sets.newHashSet("59da26b4-edd0-4412-a2e6-d6711f376340");

    @javax.annotation.Resource(name = "normatives-resolver")
    private NormativesResolver normativesResolver;

    @javax.annotation.Resource(name = "resource-version-migration")
    private VersionMigration<Resource> versionMigration;

    @Override
    public String description() {
        return "migration of node types";
    }

    @Override
    Either<List<Resource>, ?> getElementsToMigrate() {
        return normativesResolver.getAllNodeTypeNormatives();
    }

    @Override
    boolean save(Resource element) {
        if (e2eMalformedVfcs.contains(element.getUniqueId())) {
            replaceJcpVersionPropertyTypeToVersion(element);
        }
        return super.save(element);
    }

    private void replaceJcpVersionPropertyTypeToVersion(Resource element) {
        getJcpIntegerProperty(element).ifPresent(propertyDefinition -> {
            LOGGER.info("resource {} with id {}: found property jcp-version with type 'integer', changing type to 'version'", element.getName(), element.getUniqueId());
            propertyDefinition.setType(ToscaPropertyType.VERSION.getType());
        });
    }

    private Optional<PropertyDefinition> getJcpIntegerProperty(Resource element) {
        if (element.getProperties() == null) return Optional.empty();
        return element.getProperties().stream()
                               .filter(prop -> prop.getName().equals(JCP_VERSION_PROPERTY))
                               .filter(prop -> prop.getType().equals(ToscaPropertyType.INTEGER.getType()))
                               .findAny();

    }

    @Override
    boolean doPostMigrateOperation(List<Resource> elements) {
        LOGGER.info("migrating node types versions");
        return versionMigration.buildComponentsVersionChain(elements);
    }
}
