package org.openecomp.sdc.healing.healers.testHealers.data;

import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

public class TestDescriptionHealer implements Healer {

  @Override
  public Object heal(String vspId, Version version) throws Exception {
    VspDetails vspDetails = new VspDetails(vspId, version);
    vspDetails.setDescription("This is a data healer");

    return vspDetails;
  }
}
