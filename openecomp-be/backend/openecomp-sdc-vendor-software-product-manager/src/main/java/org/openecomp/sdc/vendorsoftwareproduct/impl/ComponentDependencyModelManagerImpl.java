package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.apache.commons.lang3.StringUtils;
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
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.List;

public class ComponentDependencyModelManagerImpl implements ComponentDependencyModelManager {

  private VendorSoftwareProductDao vendorSoftwareProductDao;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  protected static final Logger logger =
      LoggerFactory.getLogger(ComponentDependencyModelManagerImpl.class);

  private ComponentManager componentManager;

  public ComponentDependencyModelManagerImpl(VendorSoftwareProductDao vendorSoftwareProductDao, ComponentManager componentManager) {
    this.vendorSoftwareProductDao = vendorSoftwareProductDao;
    this.componentManager = componentManager;
  }

  @Override
  public void createComponentDependencyModel(List<ComponentDependencyModelEntity> entities,
                                             String vspId, Version version, String user) {

    mdcDataDebugMessage.debugEntryMessage("createComponentDependencyModel");
    for(ComponentDependencyModelEntity entity : entities) {
      if (!StringUtils.isEmpty(entity.getSourceComponentId())) {
        componentManager.validateComponentExistence(entity.getVspId(), entity.getVersion(),
            entity.getSourceComponentId(), user);
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
            entity.getTargetComponentId(), user);
      }
    }

    vendorSoftwareProductDao.createComponentDependencyModel(entities, vspId, version );

    mdcDataDebugMessage.debugExitMessage("createComponentDependencyModel");
  }

  private void raiseException(ErrorCode errorCode) {
    MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
        LoggerTragetServiceName.CREATE_COMPONENT_DEPENDENCY_MODEL, ErrorLevel.ERROR.name(),
        errorCode.id(), errorCode.message());
    throw new CoreException(errorCode);
  }

  @Override
  public Collection<ComponentDependencyModelEntity> list(String vspId, Version version, String
      user) {

    mdcDataDebugMessage.debugExitMessage("VSP id, version", vspId, version.toString());

    return vendorSoftwareProductDao.listComponentDependencies(vspId, version);
  }
}
