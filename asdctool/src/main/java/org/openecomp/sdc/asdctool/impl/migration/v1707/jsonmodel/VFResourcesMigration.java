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

import java.util.List;

import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.operations.api.IResourceOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.data.Either;

public class VFResourcesMigration extends ComponentMigration<Resource> {

    private static Logger LOGGER = LoggerFactory.getLogger(VFResourcesMigration.class);

    @javax.annotation.Resource(name = "resource-operation")
    private IResourceOperation resourceOperation;

    @javax.annotation.Resource(name = "resource-version-migration")
    private VersionMigration<Resource> versionMigration;

    @Override
    public String description() {
        return "migrate VFs";
    }

    @Override
    Either<List<Resource>, ?> getElementsToMigrate() {
        return resourceOperation.getVFResources();
    }

    @Override
    boolean save(Resource element) {
        requirementsCapabilitiesMigrationService.overrideInstanceCapabilitiesRequirements(element);
        return super.save(element);
    }

    @Override
    boolean doPostSaveOperation(Resource element) {
        return element.getComponentInstances() == null ||
                (requirementsCapabilitiesMigrationService.associateFulfilledRequirements(element, NodeTypeEnum.Resource) &&
                 requirementsCapabilitiesMigrationService.associateFulfilledCapabilities(element, NodeTypeEnum.Resource));
    }

    @Override
    boolean doPostMigrateOperation(List<Resource> elements) {
        LOGGER.info("migrating VFs versions");
        return versionMigration.buildComponentsVersionChain(elements);
    }

    @Override
    void doPreMigrationOperation(List<Resource> elements) {
        super.doPreMigrationOperation(elements);
        setMissingTemplateInfo(elements);
    }


}
