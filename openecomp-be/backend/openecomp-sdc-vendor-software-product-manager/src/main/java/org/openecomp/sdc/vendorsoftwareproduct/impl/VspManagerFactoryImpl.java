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

import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.sdc.vendorlicense.VendorLicenseArtifactServiceFactory;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.PackageInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.InformationArtifactGeneratorFactory;

public class VspManagerFactoryImpl extends VspManagerFactory {
  private static final VendorSoftwareProductManager INSTANCE =
      new VendorSoftwareProductManagerImpl(
          OrchestrationTemplateDaoFactory.getInstance().createInterface(),
          VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
          VendorLicenseFacadeFactory.getInstance().createInterface(),
          ServiceModelDaoFactory.getInstance().createInterface(),
          EnrichedServiceModelDaoFactory.getInstance().createInterface(),
          VendorLicenseArtifactServiceFactory.getInstance().createInterface(),
          InformationArtifactGeneratorFactory.getInstance().createInterface(),
          PackageInfoDaoFactory.getInstance().createInterface(),
          DeploymentFlavorDaoFactory.getInstance().createInterface(),
          ComponentDaoFactory.getInstance().createInterface(),
          ComponentDependencyModelDaoFactory.getInstance().createInterface(),
          NicDaoFactory.getInstance().createInterface(),
          ComputeDaoFactory.getInstance().createInterface(),
          ImageDaoFactory.getInstance().createInterface(),
          new ManualVspToscaManagerImpl());

  @Override
  public VendorSoftwareProductManager createInterface() {
    return INSTANCE;
  }
}
