package org.openecomp.core.tools.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.core.zusammen.plugin.dao.types.ElementEntity;

public class ElementHandler {

  private static final String GLOBAL_USER = "GLOBAL_USER";

  public static void update(SessionContext context,

                            String itemId, String versionId,
                            ElementEntity elementEntity) {

    ElementEntityContext elementContext;
    elementContext = new ElementEntityContext(GLOBAL_USER, new Id(itemId),
        new Id(versionId));
    CassandraElementRepository cassandraElementRepository = new CassandraElementRepository();
    cassandraElementRepository.update(context, elementContext, elementEntity);

  }
}

