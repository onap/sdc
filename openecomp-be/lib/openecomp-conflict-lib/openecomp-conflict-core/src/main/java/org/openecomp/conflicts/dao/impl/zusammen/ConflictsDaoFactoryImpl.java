package org.openecomp.conflicts.dao.impl.zusammen;


import org.openecomp.conflicts.dao.ConflictsDao;
import org.openecomp.conflicts.dao.ConflictsDaoFactory;
import org.openecomp.core.zusammen.api.ZusammenAdaptorFactory;

public class ConflictsDaoFactoryImpl extends ConflictsDaoFactory {

  private static final ConflictsDao INSTANCE = new
      ConflictsDaoImpl(ZusammenAdaptorFactory.getInstance().createInterface());

  @Override
  public ConflictsDao createInterface() {
    return INSTANCE;
  }
}
