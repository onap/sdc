package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.List;

public interface ComponentDependencyModelManager {

  void createComponentDependencyModel(List<ComponentDependencyModelEntity> entities, String
      vspId, Version version, String user);

  Collection<ComponentDependencyModelEntity> list(String vspId, Version version, String user);
}
