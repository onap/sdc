package org.openecomp.sdcrests.conflict.rest.services;

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
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("conflicts")
@Scope(value = "prototype")
public class ConflictsImpl implements Conflicts {

  @Override
  public Response getConflict(String itemId, String versionId, String user) {

    ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
    ItemVersionConflict itemVersionConflict = conflictsManager.getConflict
        (itemId, new Version(versionId));
    ItemVersionConflictDto result = (new MapItemVersionConflictToDto()).applyMapping
        (itemVersionConflict, ItemVersionConflictDto.class);
    return Response.ok(result).build();
  }

  @Override
  public Response getConflict(String itemId, String versionId, String conflictId, String user) {
    ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();
    Conflict conflict = conflictsManager.getConflict(itemId, new Version(versionId), conflictId);

    ConflictDto result = new MapConflictToDto().applyMapping(conflict, ConflictDto.class);

    return Response.ok(result).build();

  }

  @Override
  public Response resolveConflict(ConflictResolutionDto conflictResolution, String itemId,
                                  String versionId, String conflictId, String user) {
    ConflictsManager conflictsManager = ConflictsManagerFactory.getInstance().createInterface();

    Version version = new Version(versionId);
    conflictsManager.resolveConflict(itemId, version, conflictId,
        new MapDtoToConflictResolution()
            .applyMapping(conflictResolution, ConflictResolution.class));
    conflictsManager.finalizeMerge(itemId, version);

    return Response.ok().build();
  }
}
