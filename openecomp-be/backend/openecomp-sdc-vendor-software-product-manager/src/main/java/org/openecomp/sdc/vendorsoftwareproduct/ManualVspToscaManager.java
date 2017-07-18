package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.generator.datatypes.tosca.VspModelInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ManualVspToscaManager {

  public VspModelInfo gatherVspInformation(String vspId, Version version, String user);

  public ToscaServiceModel generateToscaModel(VspModelInfo vspModelInfo);
}
