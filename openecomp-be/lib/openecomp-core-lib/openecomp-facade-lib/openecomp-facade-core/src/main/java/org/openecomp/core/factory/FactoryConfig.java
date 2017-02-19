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

package org.openecomp.core.factory;

import org.openecomp.core.factory.api.FactoriesConfiguration;
import org.openecomp.core.utilities.CommonMethods;

import java.util.Map;

public final class FactoryConfig {

  private static final FactoriesConfiguration INSTANCE;

  static {

    try {
      INSTANCE = CommonMethods.newInstance(
          "org.openecomp.core.factory.FactoriesConfigImpl", FactoriesConfiguration.class);
    } catch (Exception exception) {
      exception.printStackTrace();
      throw exception;
    }
  }

  public static Map<String, String> getFactoriesMap() {
    return INSTANCE.getFactoriesMap();
  }
}
