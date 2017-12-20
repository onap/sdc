package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;
import org.openecomp.core.zusammen.plugin.dao.types.SynchronizationStateEntity;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

public interface ElementPrivateStore extends ElementStore {

  Map<Id, Id> listIds(SessionContext context, ElementContext elementContext);

  Collection<ElementEntity> listSubs(SessionContext context, ElementContext elementContext,
                                     Id elementId);

  Optional<SynchronizationStateEntity> getSynchronizationState(SessionContext context,
                                                               ElementContext elementContext,
                                                               Id elementId);

  void create(SessionContext context, ElementContext elementContext, ElementEntity element);

  boolean update(SessionContext context, ElementContext elementContext, ElementEntity element);

  void delete(SessionContext context, ElementContext elementContext, ElementEntity element);

  void markAsPublished(SessionContext context, ElementContext elementContext, Id elementId,
                       Date publishTime);

  void markDeletionAsPublished(SessionContext context, ElementContext elementContext, Id elementId,
                               Date publishTime);

  void commitStagedCreate(SessionContext context, ElementContext elementContext,
                          ElementEntity element, Date publishTime);

  void commitStagedUpdate(SessionContext context, ElementContext elementContext,
                          ElementEntity element, Date publishTime);

  void commitStagedDelete(SessionContext context, ElementContext elementContext,
                          ElementEntity element);

  void commitStagedIgnore(SessionContext context, ElementContext elementContext,
                          ElementEntity element, Date publishTime);
}
