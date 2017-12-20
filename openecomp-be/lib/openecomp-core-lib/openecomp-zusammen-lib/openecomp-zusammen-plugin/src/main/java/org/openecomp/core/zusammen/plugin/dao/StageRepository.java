package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.dao.types.StageEntity;

import java.util.Optional;

public interface StageRepository<C, E> {

  Optional<StageEntity<E>> get(SessionContext context, C entityContext, E entity);

  void create(SessionContext context, C entityContext, StageEntity<E> stageEntity);

  void delete(SessionContext context, C entityContext, E entity);
}
