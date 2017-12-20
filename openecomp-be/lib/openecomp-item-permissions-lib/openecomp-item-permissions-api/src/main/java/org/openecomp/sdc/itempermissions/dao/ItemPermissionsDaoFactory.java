package org.openecomp.sdc.itempermissions.dao;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 6/18/2017.
 */
public abstract class ItemPermissionsDaoFactory extends AbstractComponentFactory<ItemPermissionsDao> {

  public static ItemPermissionsDaoFactory getInstance() {
    return AbstractFactory.getInstance(ItemPermissionsDaoFactory.class);
  }

}
