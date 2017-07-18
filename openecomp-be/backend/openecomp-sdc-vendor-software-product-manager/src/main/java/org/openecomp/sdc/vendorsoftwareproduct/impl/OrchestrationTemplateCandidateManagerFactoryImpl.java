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

import org.openecomp.core.model.dao.ServiceModelDaoFactory;
import org.openecomp.sdc.activityLog.ActivityLogManagerFactory;
import org.openecomp.sdc.healing.factory.HealingManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.MonitoringUploadDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionDataExtractorFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CompositionEntityDataManagerFactory;

public class OrchestrationTemplateCandidateManagerFactoryImpl extends
    OrchestrationTemplateCandidateManagerFactory {
  private static final OrchestrationTemplateCandidateManager INSTANCE =
      new OrchestrationTemplateCandidateManagerImpl(
          VendorSoftwareProductDaoFactory.getInstance().createInterface(),
          VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
          OrchestrationTemplateDaoFactory.getInstance().createInterface(),
          CandidateServiceFactory.getInstance().createInterface(),
          HealingManagerFactory.getInstance().createInterface(),
          CompositionDataExtractorFactory.getInstance().createInterface(),
          ServiceModelDaoFactory.getInstance().createInterface(),
          CompositionEntityDataManagerFactory.getInstance().createInterface(),
          NicDaoFactory.getInstance().createInterface(),
          ComponentDaoFactory.getInstance().createInterface(),
          MonitoringUploadDaoFactory.getInstance().createInterface(),
          ProcessDaoFactory.getInstance().createInterface(),
          ActivityLogManagerFactory.getInstance().createInterface());


  @Override
  public OrchestrationTemplateCandidateManager createInterface() {
    return INSTANCE;
  }
}
