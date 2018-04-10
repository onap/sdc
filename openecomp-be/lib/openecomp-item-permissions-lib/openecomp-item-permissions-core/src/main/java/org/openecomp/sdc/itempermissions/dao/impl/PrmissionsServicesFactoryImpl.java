package org.openecomp.sdc.itempermissions.dao.impl;

import org.openecomp.sdc.itempermissions.PermissionsRulesFactory;
import org.openecomp.sdc.itempermissions.PermissionsServices;
import org.openecomp.sdc.itempermissions.PermissionsServicesFactory;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDaoFactory;
import org.openecomp.sdc.itempermissions.dao.UserPermissionsDaoFactory;

/**
 * Created by ayalaben on 6/22/2017
 */
public class PrmissionsServicesFactoryImpl  extends PermissionsServicesFactory {

  private static final PermissionsServices INSTANCE =
      new PermissionsServicesImpl(PermissionsRulesFactory.getInstance().createInterface(),
          ItemPermissionsDaoFactory.getInstance().createInterface(), UserPermissionsDaoFactory.getInstance().createInterface());

  @Override
  public PermissionsServices createInterface() {
    return INSTANCE;
  }
}
