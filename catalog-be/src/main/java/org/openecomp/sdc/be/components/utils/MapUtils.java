package org.openecomp.sdc.be.components.utils;

import java.util.List;
import java.util.Map;

public class MapUtils {
  public static boolean compareMaps(Map<String, Object> source, Map<String, Object> target) {
    if (source == null && target == null) {
      return true;
    }
    if ((source == null && target != null) || source != null && target == null
        || source.keySet().size() != target.keySet().size()) {
      return false;
    }

    for (Map.Entry<String, Object> entry : source.entrySet()) {
      Object sourceObj = entry.getValue();
      Object targetObj = target.get(entry.getKey());
      if ((sourceObj == null && targetObj != null) || (sourceObj != null && targetObj == null)) {
        return false;
      }
      if (!handleSourceAndTargetObjects(sourceObj, targetObj)) {
        return false;
      }
    }
    return true;

  }

  public static boolean compareLists(List<Object> source, List<Object> target) {
    if (source == null && target == null) {
      return true;
    }
    if ((source == null && target != null) || source != null && target == null ||
        source.size() != target.size()) {
      return false;
    }
    for (int i = 0; i < source.size(); i++) {
      Object sourceObj = source.get(i);
      Object targetObj = target.get(i);
      if ((sourceObj == null && targetObj != null) || sourceObj != null && targetObj == null) {
        return false;
      }
      if (!handleSourceAndTargetObjects(sourceObj, targetObj)) {
        return false;
      }
    }
    return true;
  }

  public static boolean handleSourceAndTargetObjects(Object sourceObj, Object targetObj) {

    if (sourceObj == null && targetObj == null) {
      return true;
    }

    if (sourceObj == null && targetObj != null) {
      return false;
    }
    if (sourceObj.getClass().equals(targetObj.getClass())) {
      if (sourceObj instanceof Map) {
        if (!compareMaps((Map<String, Object>) sourceObj, (Map<String, Object>) targetObj)) {
          return false;
        }
      } else if (sourceObj instanceof List) {
        if (!compareLists((List<Object>) sourceObj, (List<Object>) targetObj)) {
          return false;
        }
      } else  {

        if(sourceObj.getClass() != targetObj.getClass()){
          return false;
        }

        if (!sourceObj.equals(targetObj)) {
          return false;
        }
      }
    }
    return true;
  }


}
