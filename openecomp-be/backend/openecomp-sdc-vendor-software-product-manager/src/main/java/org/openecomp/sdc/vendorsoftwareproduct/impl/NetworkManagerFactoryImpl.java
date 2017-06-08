/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.openecomp.sdc.vendorsoftwareproduct.NetworkManager;
import org.openecomp.sdc.vendorsoftwareproduct.NetworkManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;

public class NetworkManagerFactoryImpl extends NetworkManagerFactory {
  private static final NetworkManager INSTANCE =
      new NetworkManagerImpl(
          NetworkDaoFactory.getInstance().createInterface(),
          CompositionEntityDataManagerFactory.getInstance().createInterface());

  @Override
  public NetworkManager createInterface() {
    return INSTANCE;
  }
}
