package org.openecomp.sdcrests.vsp.rest.mapping;


import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRelationType;

public class MapComponentDependencyModelRequestToEntity extends
    MappingBase<ComponentDependencyModel, ComponentDependencyModelEntity> {
  private static final Logger logger =
      LoggerFactory.getLogger(MapComponentDependencyModelRequestToEntity.class);

  @Override
  public void doMapping(ComponentDependencyModel source,
                        ComponentDependencyModelEntity target) {
    target.setSourceComponentId(source.getSourceId());
    target.setTargetComponentId(source.getTargetId());
    try {
      ComponentRelationType.valueOf(source.getRelationType());
      target.setRelation(source.getRelationType());
    } catch (IllegalArgumentException exception) {
      logger.debug("",exception);
      ErrorCode errorCode =
          ComponentDependencyModelErrorBuilder.getInvalidRelationTypeErrorBuilder();
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.CREATE_COMPONENT_DEPENDENCY_MODEL, ErrorLevel.ERROR.name(),
          errorCode.id(), errorCode.message() );
      logger.error(errorCode.message(), exception);
      throw new CoreException(errorCode);
    }
  }
}
