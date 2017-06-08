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

package org.openecomp.sdc.validation.tos;

import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContrailResourcesMappingTo {
  private Map<String, List<String>> contrailV1Resources;
  private Map<String, List<String>> contrailV2Resources;

  /**
   * Add ContrailV1Resource.
   *
   * @param fileName       the file name
   * @param resourceName   the resource name
   */
  public void addContrailV1Resource(String fileName, String resourceName) {
    if (MapUtils.isEmpty(contrailV1Resources)) {
      contrailV1Resources = new HashMap<>();
    }
    contrailV1Resources.putIfAbsent(fileName, new ArrayList<>());
    contrailV1Resources.get(fileName).add(resourceName);
  }

  /**
   * Add ContrailV1Resource.
   *
   * @param fileName       the file name
   * @param resourceName   the resource name
   */
  public void addContrailV2Resource(String fileName, String resourceName) {
    if (MapUtils.isEmpty(contrailV2Resources)) {
      contrailV2Resources = new HashMap<>();
    }
    contrailV2Resources.putIfAbsent(fileName, new ArrayList<>());
    contrailV2Resources.get(fileName).add(resourceName);
  }

  public void addAll(ContrailResourcesMappingTo contrailResourcesMappingTo) {
    addContrailV1Resources(contrailResourcesMappingTo.getContrailV1Resources());
    addContrailV2Resources(contrailResourcesMappingTo.getContrailV2Resources());
  }

  public String fetchContrailV1Resources() {
    return fetchContrailResources(contrailV1Resources);
  }

  public String fetchContrailV2Resources() {
    return fetchContrailResources(contrailV2Resources);
  }

  private void addContrailV1Resources(Map<String, List<String>> contrailV1Resources) {
    if (!MapUtils.isEmpty(contrailV1Resources)) {
      for (Map.Entry<String, List<String>> fileResourcesEntry : contrailV1Resources.entrySet()) {
        for (String resourceName : fileResourcesEntry.getValue()) {
          this.addContrailV1Resource(fileResourcesEntry.getKey(), resourceName);
        }
      }
    }
  }

  private void addContrailV2Resources(Map<String, List<String>> contrailV2Resources) {
    if (!MapUtils.isEmpty(contrailV2Resources)) {
      for (Map.Entry<String, List<String>> fileResourcesEntry : contrailV2Resources.entrySet()) {
        for (String resourceName : fileResourcesEntry.getValue()) {
          this.addContrailV2Resource(fileResourcesEntry.getKey(), resourceName);
        }
      }
    }
  }

  private String fetchContrailResources(Map<String, List<String>> contrailResources) {
    StringBuilder buffer = new StringBuilder();
    if (MapUtils.isEmpty(contrailResources)) {
      return "";
    }
    for (Map.Entry<String, List<String>> fileResourcesEntry : contrailResources.entrySet()) {
      buffer.append(" file '").append(fileResourcesEntry.getKey()).append("' , resources :");
      for (String resourceName : fileResourcesEntry.getValue()) {
        buffer.append("'").append(resourceName).append("', ");
      }
    }
    buffer.deleteCharAt(buffer.lastIndexOf(","));
    return buffer.toString();
  }

  public Map<String, List<String>> getContrailV1Resources() {
    return contrailV1Resources;
  }

  public Map<String, List<String>> getContrailV2Resources() {
    return contrailV2Resources;
  }
}
