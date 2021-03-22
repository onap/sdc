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
package org.openecomp.sdcrests.conflict.types;

import java.util.ArrayList;
import java.util.Collection;

public class ItemVersionConflictDto {

    private ConflictDto conflict;
    private Collection<ConflictInfoDto> conflictInfoList = new ArrayList<>();

    public ConflictDto getConflict() {
        return conflict;
    }

    public void setConflict(ConflictDto conflict) {
        this.conflict = conflict;
    }

    public Collection<ConflictInfoDto> getConflictInfoList() {
        return conflictInfoList;
    }

    public void setConflictInfoList(Collection<ConflictInfoDto> conflictInfoList) {
        this.conflictInfoList = conflictInfoList;
    }

    public void addConflictInfo(ConflictInfoDto conflictInfo) {
        conflictInfoList.add(conflictInfo);
    }
}
