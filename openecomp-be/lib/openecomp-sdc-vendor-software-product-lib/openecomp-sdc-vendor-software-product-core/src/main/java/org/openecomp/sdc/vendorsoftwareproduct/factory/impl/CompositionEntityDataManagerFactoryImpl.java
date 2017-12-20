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

package org.openecomp.sdc.vendorsoftwareproduct.factory.impl;

import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NetworkDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.services.impl.composition.CompositionEntityDataManagerImpl;

public class CompositionEntityDataManagerFactoryImpl extends CompositionEntityDataManagerFactory {

  @Override
  public CompositionEntityDataManager createInterface() {
    // this class is stateful! it must be recreated from scratch on every use!!!
    return new CompositionEntityDataManagerImpl(
        VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
        ComponentDaoFactory.getInstance().createInterface(),
        NicDaoFactory.getInstance().createInterface(),
        NetworkDaoFactory.getInstance().createInterface(),
        ImageDaoFactory.getInstance().createInterface(),
        ComputeDaoFactory.getInstance().createInterface(),
        DeploymentFlavorDaoFactory.getInstance().createInterface());
  }
}
