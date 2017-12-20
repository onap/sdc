package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Resolution;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;

import java.util.Collection;
import java.util.Optional;

public interface ElementStageStore {

  Collection<ElementEntity> listIds(SessionContext context, ElementContext elementContext);

  boolean hasConflicts(SessionContext context, ElementContext elementContext);

  Collection<StageEntity<ElementEntity>> listConflictedDescriptors(SessionContext context,
                                                                   ElementContext elementContext);

  Optional<StageEntity<ElementEntity>> get(SessionContext context, ElementContext elementContext,
                                           ElementEntity element);

  Optional<StageEntity<ElementEntity>> getConflicted(SessionContext context,
                                                     ElementContext elementContext,
                                                     ElementEntity element);

  void create(SessionContext context, ElementContext elementContext,
              StageEntity<ElementEntity> elementStage);

  void delete(SessionContext context, ElementContext elementContext, ElementEntity element);

  void resolveConflict(SessionContext context, ElementContext elementContext, ElementEntity element,
                       Resolution resolution);
}
