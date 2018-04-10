/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
