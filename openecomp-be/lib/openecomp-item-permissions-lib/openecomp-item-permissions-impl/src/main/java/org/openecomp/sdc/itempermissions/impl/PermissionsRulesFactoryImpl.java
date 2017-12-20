package org.openecomp.sdc.itempermissions.impl;

import org.openecomp.sdc.itempermissions.PermissionsRules;
import org.openecomp.sdc.itempermissions.PermissionsRulesFactory;

/**
 * Created by ayalaben on 6/26/2017.
 */
public class PermissionsRulesFactoryImpl extends PermissionsRulesFactory {

   @Override
  public PermissionsRules createInterface() {
    return new PermissionsRulesImpl();
  }

}
