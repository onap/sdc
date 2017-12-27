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

import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManager;
import org.openecomp.sdc.vendorsoftwareproduct.OrchestrationTemplateCandidateManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.factory.CandidateServiceFactory;

public class OrchestrationTemplateCandidateManagerFactoryImpl extends
    OrchestrationTemplateCandidateManagerFactory {

  private static final OrchestrationTemplateCandidateManager INSTANCE =
      new OrchestrationTemplateCandidateManagerImpl(
          VendorSoftwareProductInfoDaoFactory.getInstance().createInterface(),
          CandidateServiceFactory.getInstance().createInterface()
      );

  @Override
  public OrchestrationTemplateCandidateManager createInterface() {
    return INSTANCE;
  }
}
