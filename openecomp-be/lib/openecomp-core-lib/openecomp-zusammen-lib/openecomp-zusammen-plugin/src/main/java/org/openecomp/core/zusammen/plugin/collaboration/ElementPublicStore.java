package org.openecomp.core.zusammen.plugin.collaboration;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

import java.util.Date;
import java.util.Map;

public interface ElementPublicStore extends ElementStore {

  void create(SessionContext context, ElementContext elementContext, ElementEntity element,
              Date publishTime);

  void update(SessionContext context, ElementContext elementContext, ElementEntity element,
              Date publishTime);

  void delete(SessionContext context, ElementContext elementContext, ElementEntity element,
              Date publishTime);

  Map<Id,Id> listIds(SessionContext context, ElementContext elementContext);
}
