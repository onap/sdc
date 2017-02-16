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

package org.openecomp.sdc.vendorsoftwareproduct.types.composition;

public class CompositionEntityId {
  private String id;
  private CompositionEntityId parentId;

  public CompositionEntityId(String id, CompositionEntityId parentId) {
    this.id = id;
    this.parentId = parentId;
  }

  public String getId() {
    return id;
  }

  public CompositionEntityId getParentId() {
    return parentId;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    CompositionEntityId that = (CompositionEntityId) obj;

    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    return parentId != null ? parentId.equals(that.parentId) : that.parentId == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (parentId != null ? parentId.hashCode() : 0);
    return result;
  }
}
