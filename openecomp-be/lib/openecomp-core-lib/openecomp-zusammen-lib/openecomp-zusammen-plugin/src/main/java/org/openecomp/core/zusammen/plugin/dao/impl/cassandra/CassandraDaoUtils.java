package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.commons.db.api.cassandra.CassandraConnectorFactory;
import com.amdocs.zusammen.commons.db.api.cassandra.types.CassandraContext;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.datastax.driver.core.Session;

class CassandraDaoUtils {

  static <T> T getAccessor(SessionContext context, Class<T> classOfT) {
    return CassandraConnectorFactory.getInstance().createInterface(getCassandraContext(context))
        .getMappingManager()
        .createAccessor(classOfT);
  }

  static Session getSession(SessionContext context) {
    return CassandraConnectorFactory.getInstance().createInterface(getCassandraContext(context))
        .getMappingManager()
        .getSession();
  }

  private static CassandraContext getCassandraContext(SessionContext context) {
    CassandraContext cassandraContext = new CassandraContext();
    cassandraContext.setTenant(context.getTenant());
    return cassandraContext;
  }
}
