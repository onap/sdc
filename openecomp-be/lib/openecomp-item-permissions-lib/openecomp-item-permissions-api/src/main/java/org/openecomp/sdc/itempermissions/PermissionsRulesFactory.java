package org.openecomp.sdc.itempermissions;

import org.openecomp.core.factory.api.AbstractComponentFactory;
import org.openecomp.core.factory.api.AbstractFactory;

/**
 * Created by ayalaben on 6/26/2017
 */
public abstract class PermissionsRulesFactory extends
    AbstractComponentFactory<PermissionsRules> {

  public static PermissionsRulesFactory getInstance() {
    return AbstractFactory.getInstance(PermissionsRulesFactory.class);
  }

}
