package org.openecomp.sdc.itempermissions;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 6/18/2017.
 */
public abstract class ItemPermissionsManagerFactory extends
    AbstractComponentFactory<ItemPermissionsManager> {

  public static ItemPermissionsManagerFactory getInstance() {
    return AbstractFactory.getInstance(ItemPermissionsManagerFactory.class);
  }
}
