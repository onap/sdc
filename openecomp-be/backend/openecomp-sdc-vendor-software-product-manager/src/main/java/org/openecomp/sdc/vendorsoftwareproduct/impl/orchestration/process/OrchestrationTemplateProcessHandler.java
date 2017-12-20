package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.process;


import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.OrchestrationTemplateActionResponse;

public interface OrchestrationTemplateProcessHandler {

  OrchestrationTemplateActionResponse process(VspDetails vspDetails,
                                              OrchestrationTemplateCandidateData candidateData);
}
