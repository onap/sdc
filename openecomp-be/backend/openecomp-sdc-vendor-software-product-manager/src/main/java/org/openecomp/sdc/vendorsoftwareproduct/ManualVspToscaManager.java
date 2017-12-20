package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ManualVspToscaManager {

  VspModelInfo gatherVspInformation(String vspId, Version version);

  ToscaServiceModel generateToscaModel(VspModelInfo vspModelInfo);
}
