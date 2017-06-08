package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.Collection;
import java.util.Optional;

public interface ElementRepository {

  Collection<ElementEntity> list(SessionContext context, ElementEntityContext elementContext);

  void create(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  void update(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  void delete(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  Optional<ElementEntity> get(SessionContext context, ElementEntityContext elementContext,
                              ElementEntity element);

  void createNamespace(SessionContext context, ElementEntityContext elementContext,
                       ElementEntity element);
}
