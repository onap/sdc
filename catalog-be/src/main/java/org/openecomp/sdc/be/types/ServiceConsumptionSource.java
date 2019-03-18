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

package org.openecomp.sdc.be.types;

import java.util.HashMap;
import java.util.Map;

public enum  ServiceConsumptionSource {
  SERVICE_INPUT("ServiceInput"),
  STATIC("Static");


  private static Map<String, ServiceConsumptionSource> sourceToValue;

  static {
    sourceToValue = new HashMap<>();
    for(ServiceConsumptionSource sourceName : ServiceConsumptionSource.values()) {
      sourceToValue.put(sourceName.source, sourceName);
    }
  }

  private String source;

  ServiceConsumptionSource(String source) {
    this.source = source;
  }

  public static ServiceConsumptionSource getSourceValue(String source) {
    return sourceToValue.get(source);
  }

  public String getSource() {
    return source;
  }
}
