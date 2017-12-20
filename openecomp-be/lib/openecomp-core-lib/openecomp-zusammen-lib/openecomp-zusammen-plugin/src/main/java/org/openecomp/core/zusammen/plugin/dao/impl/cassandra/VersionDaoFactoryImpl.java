package org.openecomp.core.zusammen.plugin.dao.impl.cassandra;

import com.amdocs.zusammen.datatypes.SessionContext;
import org.openecomp.core.zusammen.plugin.dao.VersionDao;
import org.openecomp.core.zusammen.plugin.dao.VersionDaoFactory;

public class VersionDaoFactoryImpl extends VersionDaoFactory {

  private static final VersionDao INSTANCE = new VersionDaoImpl();

  @Override
  public VersionDao createInterface(SessionContext context) {
    return INSTANCE;
  }
}
