package org.openecomp.sdc.healing.healers.testHealers.structure;

import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

public class TestNewEntityInVspHealer implements Healer {

  @Override
  public Object heal(String vspId, Version version) throws Exception {
    VspDetails vspDetails = new VspDetails(vspId, version);
    vspDetails.setDescription("This is a structure healer");

    return vspDetails;
  }
}
