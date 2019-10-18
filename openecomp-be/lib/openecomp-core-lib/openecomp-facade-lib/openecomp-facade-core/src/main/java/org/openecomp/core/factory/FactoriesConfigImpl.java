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

package org.openecomp.core.factory;


import org.openecomp.core.factory.api.FactoriesConfiguration;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.errors.SdcConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FactoriesConfigImpl implements FactoriesConfiguration {

  private final Map factoryConfigurationMap = new HashMap();

  public FactoriesConfigImpl() {
    init();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, String> getFactoriesMap() {
    return factoryConfigurationMap;
  }

  private void init() {
    final List<URL> factoryConfigUrlList = FileUtils.getAllLocations("factoryConfiguration.json");
    for (final URL factoryConfigUrl : factoryConfigUrlList) {
      try (InputStream stream = factoryConfigUrl.openStream()) {
        factoryConfigurationMap.putAll(JsonUtil.json2Object(stream, Map.class));
      } catch (final IOException e) {
        throw new SdcConfigurationException("Failed to initialize Factory from '" + factoryConfigUrl.getPath() +"'", e);
      }
    }
  }
}

