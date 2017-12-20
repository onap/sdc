package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ComponentDependencyModelManager {

  Collection<ComponentDependencyModelEntity> list(String vspId, Version version);

  ComponentDependencyModelEntity createComponentDependency(ComponentDependencyModelEntity entity,
                                                           String vspId, Version version);

  void delete(String vspId, Version version, String dependencyId);

  void update(ComponentDependencyModelEntity entity);

  ComponentDependencyModelEntity get(String vspId, Version version, String dependencyId);
}
