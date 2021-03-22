/*
 * Copyright © 2016-2017 European Support Limited
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
package org.openecomp.sdc.vendorsoftwareproduct.impl;

import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;

public class DeploymentFlavorManagerFactoryImpl extends DeploymentFlavorManagerFactory {

    private static final DeploymentFlavorManager INSTANCE = new DeploymentFlavorManagerImpl(
        VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(), DeploymentFlavorDaoFactory.getInstance().createInterface(),
        CompositionEntityDataManagerFactory.getInstance().createInterface(), ComputeDaoFactory.getInstance().createInterface(),
        ComponentDaoFactory.getInstance().createInterface());

    @Override
    public DeploymentFlavorManager createInterface() {
        return INSTANCE;
    }
}
