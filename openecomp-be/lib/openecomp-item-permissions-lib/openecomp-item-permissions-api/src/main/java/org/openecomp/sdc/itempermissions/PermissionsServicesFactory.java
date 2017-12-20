package org.openecomp.sdc.itempermissions;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 6/22/2017.
 */
public abstract class PermissionsServicesFactory extends
    AbstractComponentFactory<PermissionsServices> {

  public static PermissionsServicesFactory getInstance() {
    return AbstractFactory.getInstance(PermissionsServicesFactory.class);
  }

}
