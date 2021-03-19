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
 */
package org.openecomp.sdc.be.components.upgrade;

import org.openecomp.sdc.be.dao.api.ActionStatus;

public class ServiceInfo {

    private String uniqueId;
    private String version;
    private String name;
    private ActionStatus status;

    public ServiceInfo() {
        status = ActionStatus.OK;
    }

    public ServiceInfo(String id, ActionStatus status) {
        this.uniqueId = id;
        this.status = status;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ActionStatus getStatus() {
        return status;
    }

    public void setStatus(ActionStatus status) {
        this.status = status;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
     if (this == obj) {
      return true;
     }
     if (obj == null) {
      return false;
     }
     if (getClass() != obj.getClass()) {
      return false;
     }
        ServiceInfo other = (ServiceInfo) obj;
        if (name == null) {
         if (other.name != null) {
          return false;
         }
        } else if (!name.equals(other.name)) {
         return false;
        }
     if (status != other.status) {
      return false;
     }
        if (uniqueId == null) {
         if (other.uniqueId != null) {
          return false;
         }
        } else if (!uniqueId.equals(other.uniqueId)) {
         return false;
        }
        if (version == null) {
         if (other.version != null) {
          return false;
         }
        } else if (!version.equals(other.version)) {
         return false;
        }
        return true;
    }
}
