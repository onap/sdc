/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * Modifications copyright (c) 2019 Nokia
 * ================================================================================
 */

package org.openecomp.sdc.generator.datatypes.tosca;

@SuppressWarnings("CheckStyle")
public class LicenseFlavor {

  String feature_group_uuid;

  public String getFeature_group_uuid() {
    return feature_group_uuid;
  }

  public void setFeature_group_uuid(String feature_group_uuid) {
    this.feature_group_uuid = feature_group_uuid;
  }

  @Override
  public String toString() {
    return "LicenseFlavor{"
        + "feature_group_uuid='" + feature_group_uuid + '\''
        + '}';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null || getClass() != obj.getClass())
      return false;
    LicenseFlavor other = (LicenseFlavor) obj;

      if (this.feature_group_uuid == null) {
        if (other.feature_group_uuid != null)
          return false;
      } else if (!feature_group_uuid.equals(other.feature_group_uuid))
        return false;

    return true;
  }

  @Override
  public int hashCode() {
    return feature_group_uuid != null ? feature_group_uuid.hashCode() : 0;
  }
}
