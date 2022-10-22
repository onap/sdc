/*
 *
 *  Copyright © 2017-2018 European Support Limited
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  * Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
 
package org.openecomp.sdc.versioning.types;

import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

@Getter
@Setter
public class Item {

    private String id;
    private String type;
    private String name;
    private String owner;
    private String tenant;
    private ItemStatus status;
    private String description;
    private Map<String, Object> properties = new HashMap<>();
    private Map<VersionStatus, Integer> versionStatusCounters = new EnumMap<>(VersionStatus.class);
    private Date creationTime;
    private Date modificationTime;



    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    public void addVersionStatus(VersionStatus versionStatus) {
        Integer counter = versionStatusCounters.get(versionStatus);
        versionStatusCounters.put(versionStatus, counter == null ? 1 : counter + 1);
    }

    public void removeVersionStatus(VersionStatus versionStatus) {
        Integer counter = versionStatusCounters.get(versionStatus);
        if (counter != null) {
            if (counter == 1) {
                versionStatusCounters.remove(versionStatus);
            } else {
                versionStatusCounters.put(versionStatus, counter - 1);
            }
        }
    }
}
