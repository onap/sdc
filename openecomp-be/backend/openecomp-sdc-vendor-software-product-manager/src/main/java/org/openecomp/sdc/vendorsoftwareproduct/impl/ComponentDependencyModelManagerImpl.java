package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.core.utilities.CommonMethods;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.versioning.VersioningUtil;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public class ComponentDependencyModelManagerImpl implements ComponentDependencyModelManager {

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  protected static final Logger logger =
      LoggerFactory.getLogger(ComponentDependencyModelManagerImpl.class);

  private ComponentManager componentManager;
  private ComponentDependencyModelDao componentDependencyModelDao;

  public ComponentDependencyModelManagerImpl(
      ComponentManager componentManager, ComponentDependencyModelDao componentDependencyModelDao) {
    this.componentManager = componentManager;
    this.componentDependencyModelDao = componentDependencyModelDao;
  }

  private void raiseException(ErrorCode errorCode) {
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
        LoggerTragetServiceName.CREATE_COMPONENT_DEPENDENCY_MODEL, ErrorLevel.ERROR.name(),
        errorCode.id(), errorCode.message());
    throw new CoreException(errorCode);
  }

  @Override
  public Collection<ComponentDependencyModelEntity> list(String vspId, Version version) {

    mdcDataDebugMessage.debugExitMessage("VSP id, version", vspId, version.toString());

    return componentDependencyModelDao
        .list(new ComponentDependencyModelEntity(vspId, version, null));
  }

  @Override
  public ComponentDependencyModelEntity createComponentDependency(ComponentDependencyModelEntity
                                                                      entity, String vspId,
                                                                  Version version) {

    validateComponentDependency(entity);
    entity.setId(CommonMethods.nextUuId());
    componentDependencyModelDao.create(entity);
    return entity;
  }

  private void validateComponentDependency(ComponentDependencyModelEntity entity) {
    if (!StringUtils.isEmpty(entity.getSourceComponentId())) {
      componentManager.validateComponentExistence(entity.getVspId(), entity.getVersion(),
          entity.getSourceComponentId());
      if (entity.getSourceComponentId().equals(entity.getTargetComponentId())) {
        ErrorCode errorCode =
            ComponentDependencyModelErrorBuilder.getSourceTargetComponentEqualErrorBuilder();
        raiseException(errorCode);
      }
    } else {
      ErrorCode errorCode = ComponentDependencyModelErrorBuilder
          .getNoSourceComponentErrorBuilder();
      raiseException(errorCode);
    }

    if (!StringUtils.isEmpty(entity.getTargetComponentId())) {
      componentManager.validateComponentExistence(entity.getVspId(), entity.getVersion(),
          entity.getTargetComponentId());
    }
  }

  @Override
  public void delete(String vspId, Version version, String dependencyId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, dependencyId", vspId, dependencyId);
    ComponentDependencyModelEntity componentDependencyEntity = getComponentDependency(vspId,
        version, dependencyId);
    if (componentDependencyEntity != null) {
      componentDependencyModelDao.delete(componentDependencyEntity);
    }

    mdcDataDebugMessage.debugExitMessage("VSP id, dependencyId", vspId, dependencyId);
  }

  @Override
  public void update(ComponentDependencyModelEntity entity) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, dependencyId", entity.getVspId(),
        entity.getId());
    ComponentDependencyModelEntity componentDependencyEntity = getComponentDependency(
        entity.getVspId(), entity.getVersion(), entity.getId());
    validateComponentDependency(entity);
    componentDependencyModelDao.update(entity);
  }

  @Override
  public ComponentDependencyModelEntity get(String vspId, Version version, String dependencyId) {
    mdcDataDebugMessage.debugEntryMessage("VSP id, dependencyId", vspId, dependencyId);
    ComponentDependencyModelEntity componentDependency =
        getComponentDependency(vspId, version, dependencyId);
    return componentDependency;
  }

  private ComponentDependencyModelEntity getComponentDependency(String vspId, Version version,
                                                                String dependencyId) {
    ComponentDependencyModelEntity retrieved = componentDependencyModelDao.get(
        new ComponentDependencyModelEntity(vspId, version, dependencyId));
    VersioningUtil.validateEntityExistence(retrieved, new ComponentDependencyModelEntity(
        vspId, version, dependencyId), VspDetails.ENTITY_TYPE);
    return retrieved;
  }
}
