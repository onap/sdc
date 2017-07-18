package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ComputeManager {

  Collection<ListComputeResponse> listCompute(String vspId, Version version, String
      componentId, String user);

  ComputeEntity createCompute(ComputeEntity compute, String user);

  CompositionEntityResponse<ComputeData> getCompute(String vspId, Version version, String
      componentId, String computeFlavorId, String user);

  QuestionnaireResponse getComputeQuestionnaire(String vspId, Version version, String
      componentId, String computeFlavorId, String user);

  void updateComputeQuestionnaire(String vspId, Version version, String componentId, String
      computeId, String
      questionnaireData, String user);

  CompositionEntityValidationData updateCompute(ComputeEntity compute, String user);

  void deleteCompute(String vspId, Version version,String componentId, String computeFlavorId,
                     String user);
}
