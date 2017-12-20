package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.itempermissions.PermissionsRulesFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDaoFactory;

/**
 * Created by ayalaben on 6/22/2017
 */
public class PrmissionsServicesFactoryImpl  extends PermissionsServicesFactory {

  private static final PermissionsServices INSTANCE =
      new org.openecomp.sdc.itempermissions.dao.impl.PermissionsServicesImpl(PermissionsRulesFactory.getInstance().createInterface(),
          ItemPermissionsDaoFactory.getInstance().createInterface());

  @Override
  public PermissionsServices createInterface() {
    return INSTANCE;
  }
}
