package org.openecomp.sdc.itempermissions;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 6/18/2017.
 */
public abstract class PermissionsManagerFactory extends
    AbstractComponentFactory<PermissionsManager> {

  public static PermissionsManagerFactory getInstance() {
    return AbstractFactory.getInstance(PermissionsManagerFactory.class);
  }
}
