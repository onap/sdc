package org.openecomp.sdc.asdctool.impl.validator.executers;

import java.util.List;
import java.util.Map;

import org.openecomp.sdc.asdctool.impl.validator.tasks.TopologyTemplateValidationTask;
import org.openecomp.sdc.be.dao.jsongraph.GraphVertex;
import org.openecomp.sdc.be.dao.jsongraph.types.VertexTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.GraphPropertyEnum;

public interface IArtifactValidatorExecuter {
    boolean executeValidations();
    String getName();
    
  
    
}
