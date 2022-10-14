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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import java.util.Collection;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ComponentDependencyModelManagerImpl implements ComponentDependencyModelManager {

    private ComponentManager componentManager;
    private ComponentDependencyModelDao componentDependencyModelDao;

    public ComponentDependencyModelManagerImpl(ComponentManager componentManager, ComponentDependencyModelDao componentDependencyModelDao) {
        this.componentManager = componentManager;
        this.componentDependencyModelDao = componentDependencyModelDao;
    }

    @Override
    public Collection<ComponentDependencyModelEntity> list(String vspId, Version version) {
        return componentDependencyModelDao.list(new ComponentDependencyModelEntity(vspId, version, null));
    }

    @Override
    public ComponentDependencyModelEntity createComponentDependency(ComponentDependencyModelEntity entity, String vspId, Version version) {
        validateComponentDependency(entity);
        entity.setId(CommonMethods.nextUuId());
        componentDependencyModelDao.create(entity);
        return entity;
    }

    private void validateComponentDependency(ComponentDependencyModelEntity entity) {
        if (!StringUtils.isEmpty(entity.getSourceComponentId())) {
            componentManager.validateComponentExistence(entity.getVspId(), entity.getVersion(), entity.getSourceComponentId());
            if (entity.getSourceComponentId().equals(entity.getTargetComponentId())) {
                ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder();
                throw new CoreException(errorCode);
            }
        } else {
            ErrorCode errorCode = ComponentDependencyModelErrorBuilder.getNoSourceComponentErrorBuilder();
            throw new CoreException(errorCode);
        }
        if (!StringUtils.isEmpty(entity.getTargetComponentId())) {
            componentManager.validateComponentExistence(entity.getVspId(), entity.getVersion(), entity.getTargetComponentId());
        }
    }

    @Override
    public void delete(String vspId, Version version, String dependencyId) {
        ComponentDependencyModelEntity componentDependencyEntity = getComponentDependency(vspId, version, dependencyId);
        if (componentDependencyEntity != null) {
            componentDependencyModelDao.delete(componentDependencyEntity);
        }
    }

    @Override
    public void update(ComponentDependencyModelEntity entity) {
        getComponentDependency(entity.getVspId(), entity.getVersion(), entity.getId());
        validateComponentDependency(entity);
        componentDependencyModelDao.update(entity);
    }

    @Override
    public ComponentDependencyModelEntity get(String vspId, Version version, String dependencyId) {
        return getComponentDependency(vspId, version, dependencyId);
    }

    private ComponentDependencyModelEntity getComponentDependency(String vspId, Version version, String dependencyId) {
        ComponentDependencyModelEntity retrieved = componentDependencyModelDao.get(new ComponentDependencyModelEntity(vspId, version, dependencyId));
        VersioningUtil.validateEntityExistence(retrieved, new ComponentDependencyModelEntity(vspId, version, dependencyId), VspDetails.ENTITY_TYPE);
        return retrieved;
    }
}
