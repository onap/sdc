package org.openecomp.sdc.itempermissions.dao.impl;


import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDao;
import org.openecomp.sdc.itempermissions.dao.ItemPermissionsDaoFactory;


/**
 * Created by ayalaben on 6/18/2017.
 */
public class ItemPermissionsDaoFactoryImpl extends ItemPermissionsDaoFactory {

  private static ItemPermissionsDao INSTANCE =new ItemPermissionsDaoImpl();

  @Override
  public ItemPermissionsDao createInterface() {
    return INSTANCE;
  }
}
