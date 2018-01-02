package org.openecomp.core.tools.store;

import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.types.ElementEntityContext;
import com.amdocs.zusammen.plugin.dao.types.ElementEntity;

public class ElementHandler {



    public static void update(SessionContext context,
                              String itemId, String versionId, String revisionId,
                              ElementEntity elementEntity) {

       //todo - fix method
        /* ElementEntityContext elementContext;
        CassandraElementRepository cassandraElementRepository = new CassandraElementRepository();
        if (revisionId == null) {

            elementContext = new ElementEntityContext(GLOBAL_USER, new Id(itemId),
                new Id(versionId));

        }*/
    }
}

