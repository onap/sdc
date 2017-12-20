package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;

import java.util.Collection;
import java.util.Optional;

public interface ElementStageRepository
    extends StageRepository<ElementEntityContext, ElementEntity> {

  Optional<StageEntity<ElementEntity>> getDescriptor(SessionContext context,
                                                     ElementEntityContext elementContext,
                                                     ElementEntity element);

  Collection<ElementEntity> listIds(SessionContext context,
                                    ElementEntityContext elementContext);

  Collection<ElementEntity> listConflictedIds(SessionContext context,
                                              ElementEntityContext elementContext);

  void markAsNotConflicted(SessionContext context, ElementEntityContext entityContext,
                           ElementEntity entity, Action action);

  void markAsNotConflicted(SessionContext context, ElementEntityContext entityContext,
                           ElementEntity entity);

  void update(SessionContext context, ElementEntityContext entityContext, ElementEntity entity,
              Action action, boolean conflicted);

}
