package org.openecomp.sdcrests.vsp.rest.mapping;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.ComponentDependencyModelErrorBuilder;
import org.openecomp.sdcrests.mapping.MappingBase;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentRelationType;

public class MapComponentDependencyModelEntityToDto extends
    MappingBase<ComponentDependencyModelEntity, ComponentDependencyModel> {

  @Override
  public void doMapping(ComponentDependencyModelEntity source,
                        ComponentDependencyModel target) {
   target.setSourceId(source.getSourceComponentId());
    target.setTargetId(source.getTargetComponentId());
    target.setRelationType(source.getRelation());
  }
}
