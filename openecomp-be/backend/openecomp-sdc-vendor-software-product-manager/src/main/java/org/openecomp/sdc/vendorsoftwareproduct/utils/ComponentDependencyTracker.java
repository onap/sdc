package org.openecomp.sdc.vendorsoftwareproduct.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComponentDependencyTracker {
  Map<String, Set<String>> store = new HashMap<>();

  /**
   * Add dependency.
   *
   * @param dependent the dependent
   * @param dependsOn the depends on
   */
  public void addDependency(String dependent, String dependsOn) {
    if (dependent != null && dependsOn != null && dependent.trim().length() > 0 && dependsOn.trim()
        .length() > 0) {
      dependent = dependent.toLowerCase();
      dependsOn = dependsOn.toLowerCase();
      Set<String> dependsOnList = store.get(dependent);
      if (dependsOnList == null) {
        dependsOnList = new HashSet<>();
        store.put(dependent, dependsOnList);
      }
      dependsOnList.add(dependsOn);
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
