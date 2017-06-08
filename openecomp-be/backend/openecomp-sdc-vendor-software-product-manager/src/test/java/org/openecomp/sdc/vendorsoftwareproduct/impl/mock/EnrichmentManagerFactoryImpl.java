package org.openecomp.sdc.vendorsoftwareproduct.impl.mock;


import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.enrichment.types.EntityInfo;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Map;


public class EnrichmentManagerFactoryImpl extends EnrichmentManagerFactory {

  @Override
  public EnrichmentManager createInterface() {
    return new EnrichmentManagerMock();
  }

  public class EnrichmentManagerMock implements EnrichmentManager {
    @Override
    public Map<String, List<ErrorMessage>> enrich() {
      return null;
    }

    @Override
    public void addEntityInfo(String entityKey, EntityInfo entityInfo) {

    }

    @Override
    public void init(String key, Version version) {

    }

    @Override
    public Object getModel() {
      return null;
    }

    @Override
    public void setModel(Object model) {

    }
  }
}
