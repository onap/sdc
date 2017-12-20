package org.openecomp.sdc.healing.healers;


import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.NetworkType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public class NicDataHealer implements Healer {
  /*private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory.getInstance().createInterface();*/

  private static final NicDao nicDao = NicDaoFactory.getInstance().createInterface();

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public NicDataHealer(){

  }
  @Override
  public Object heal(String vspId, Version version) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    Collection<NicEntity> nics = nicDao.listByVsp(vspId, version);
    for (NicEntity nicEntity : nics) {
      Nic nic = nicEntity.getNicCompositionData();
      if (nic != null && nic.getNetworkType()==null) {
        nic.setNetworkType(NetworkType.Internal);
        nicEntity.setNicCompositionData(nic);
        //vendorSoftwareProductDao.updateNic(nicEntity);
        nicDao.update(nicEntity);
      }
    }
    return nics;
  }
}
