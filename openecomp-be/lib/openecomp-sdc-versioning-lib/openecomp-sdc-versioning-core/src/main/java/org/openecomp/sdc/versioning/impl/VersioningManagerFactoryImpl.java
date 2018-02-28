/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openecomp.sdc.versioning.impl;

import org.openecomp.sdc.versioning.ItemManagerFactory;
import org.openecomp.sdc.versioning.VersionCalculatorFactory;
import org.openecomp.sdc.versioning.VersioningManager;
import org.openecomp.sdc.versioning.VersioningManagerFactory;
import org.openecomp.sdc.versioning.dao.VersionDaoFactory;

public class VersioningManagerFactoryImpl extends VersioningManagerFactory {


  @Override
  public VersioningManager createInterface() {
    return new VersioningManagerImpl(
        VersionDaoFactory.getInstance().createInterface(),
        VersionCalculatorFactory.getInstance().createInterface(),
        ItemManagerFactory.getInstance().createInterface());
  }
}
