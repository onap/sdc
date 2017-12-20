package org.openecomp.core.zusammen.plugin.dao;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.Map;
import java.util.Optional;

public interface ElementRepository {

  Map<Id,Id> listIds(SessionContext context, ElementEntityContext elementContext);

/*  void createVersionData(SessionContext context, ElementEntityContext elementContext, VersionDataElement element);*/

  void create(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  void update(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  void delete(SessionContext context, ElementEntityContext elementContext, ElementEntity element);

  Optional<ElementEntity> get(SessionContext context, ElementEntityContext elementContext,
                              ElementEntity element);

  Optional<ElementEntity> getDescriptor(SessionContext context, ElementEntityContext elementContext,
                                        ElementEntity element);

  void createNamespace(SessionContext context, ElementEntityContext elementContext,
                       ElementEntity element);

  Optional<Id> getHash(SessionContext context, ElementEntityContext elementEntityContext,
               ElementEntity element);




/*  Collection<SynchronizationStateEntity> listSynchronizationStates(SessionContext context,
                                              ElementEntityContext elementContext);

  void updateSynchronizationState(SessionContext context, ElementEntityContext elementContext,
              SynchronizationStateEntity elementSyncState);

  void markAsDirty(SessionContext context, ElementEntityContext elementContext,
                   SynchronizationStateEntity elementSyncState);

  Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                           ElementEntityContext elementContext,
                                           SynchronizationStateEntity elementSyncState);*/
}
