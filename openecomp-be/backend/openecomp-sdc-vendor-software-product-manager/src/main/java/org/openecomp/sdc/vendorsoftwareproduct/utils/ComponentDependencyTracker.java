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

package org.openecomp.sdc.vendorsoftwareproduct.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComponentDependencyTracker {
  private final Map<String, Set<String>> store = new HashMap<>();

  /**
   * Add dependency.
   *
   * @param dependent the dependent
   * @param dependsOn the depends on
   */
  public void addDependency(String dependent, String dependsOn) {
    if (dependent != null && dependsOn != null && dependent.trim().length() > 0 && dependsOn.trim()
        .length() > 0) {
      Set<String> dependsOnList = store
              .computeIfAbsent(dependent.toLowerCase(), k -> new HashSet<>());
      dependsOnList.add(dependsOn.toLowerCase());
    }
  }

  /**
   * Is cyclic dependency present boolean.
   *
   * @return the boolean
   */
  public boolean isCyclicDependencyPresent() {
    Set<Map.Entry<String, Set<String>>> entries = store.entrySet();
    for (Map.Entry<String, Set<String>> entry : entries) {
      for (String dependentOn : entry.getValue()) {
        if (!entry.getKey().equals(dependentOn) && isCyclicDependencyPresent(entry.getKey(),
            dependentOn)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isCyclicDependencyPresent(String root, String dependentOn) {
    Set<String> dependentOnList = store.get(dependentOn);
    if (dependentOnList != null && dependentOnList.contains(root)) {
      return true;
    } else if (dependentOnList != null) {
      for (String item : dependentOnList) {
        return isCyclicDependencyPresent(root, item);
      }
    }
    return false;
  }

}
