package org.openecomp.sdcrests.vsp.rest.services;

import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionableEntityAction;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModelRequestDto;
import org.openecomp.sdcrests.vsp.rest.ComponentDependencyModels;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDependencyModelEntityToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDependencyModelRequestToEntity;

import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Named;
import javax.ws.rs.core.Response;

@Named
@Service("componentDependencyModel")
@Scope(value = "prototype")
public class ComponentDependencyModelsImpl implements ComponentDependencyModels {

  private ComponentDependencyModelManager componentDependencyModelManager =
      ComponentDependencyModelManagerFactory.getInstance().createInterface();

  @Override
  public Response create(ComponentDependencyModelRequestDto request, String vspId,
                         String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.CREATE_COMPONENT_DEPENDENCY_MODEL.toString());
    List<ComponentDependencyModelEntity> modelEntities = new
        ArrayList<ComponentDependencyModelEntity>();

    final Version version = resolveVspVersion(vspId, null, user, VersionableEntityAction.Write);

    if (request.getComponentDependencyModels() != null) {
      for(ComponentDependencyModel model : request.getComponentDependencyModels()) {
        ComponentDependencyModelEntity modelEntity =
            new MapComponentDependencyModelRequestToEntity().applyMapping(model,
                ComponentDependencyModelEntity.class);

        modelEntity.setVspId(vspId);
        modelEntity.setVersion(version);
        modelEntities.add(modelEntity);
      }
    }

    componentDependencyModelManager
        .createComponentDependencyModel(modelEntities, vspId, version, user);

    return Response.ok().build();
  }

  @Override
  public Response list(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.GET_COMPONENT_DEPENDENCY_MODEL.toString());
    Version vspVersion = resolveVspVersion(vspId, versionId, user, VersionableEntityAction.Read);

    Collection<ComponentDependencyModelEntity> componentDependencies =
        componentDependencyModelManager.list(vspId, vspVersion, user);

    MapComponentDependencyModelEntityToDto mapper = new MapComponentDependencyModelEntityToDto();
    GenericCollectionWrapper<ComponentDependencyModel> results = new GenericCollectionWrapper
        <ComponentDependencyModel>();
    for (ComponentDependencyModelEntity entity : componentDependencies) {
      results.add(mapper.applyMapping(entity, ComponentDependencyModel.class));
    }

    return Response.ok(results).build();
  }
}
