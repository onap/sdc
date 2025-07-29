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
package org.openecomp.sdcrests.conflict.rest.services;

import javax.inject.Named;
import org.openecomp.conflicts.types.Conflict;
import org.openecomp.conflicts.types.ConflictResolution;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.sdc.conflicts.ConflictsManager;
import org.openecomp.sdc.conflicts.ConflictsManagerFactory;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.conflict.rest.Conflicts;
import org.openecomp.sdcrests.conflict.rest.mapping.MapConflictToDto;
import org.openecomp.sdcrests.conflict.rest.mapping.MapDtoToConflictResolution;
import org.openecomp.sdcrests.conflict.rest.mapping.MapItemVersionConflictToDto;
import org.openecomp.sdcrests.conflict.types.ConflictDto;
import org.openecomp.sdcrests.conflict.types.ConflictResolutionDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.context.annotation.ScopedProxyMode;
@Named
@Service("conflicts")
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ConflictsImpl implements Conflicts {

    @Override
    public ResponseEntity getConflict(String itemId, String versionId, String user) {
        ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
        ItemVersionConflict itemVersionConflict = conflictsManager.getConflict(itemId, new Version(versionId));
        ItemVersionConflictDto result = (new MapItemVersionConflictToDto()).applyMapping(itemVersionConflict, ItemVersionConflictDto.class);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity getConflict(String itemId, String versionId, String conflictId, String user) {
        ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
        Conflict conflict = conflictsManager.getConflict(itemId, new Version(versionId), conflictId);
        ConflictDto result = new MapConflictToDto().applyMapping(conflict, ConflictDto.class);
        return ResponseEntity.ok(result);
    }

    @Override
    public ResponseEntity resolveConflict(ConflictResolutionDto conflictResolution, String itemId, String versionId, String conflictId, String user) {
        ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
        Version version = new Version(versionId);
        conflictsManager.resolveConflict(itemId, version, conflictId,
            new MapDtoToConflictResolution().applyMapping(conflictResolution, ConflictResolution.class));
        conflictsManager.finalizeMerge(itemId, version);
        return ResponseEntity.ok().build();
    }
}
