package org.openecomp.sdcrests.vsp.rest.services;

import org.openecomp.sdc.logging.context.MdcUtil;
import org.openecomp.sdc.logging.types.LoggerServiceName;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentDependencyModelManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyCreationDto;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyModel;
import org.openecomp.sdcrests.vendorsoftwareproducts.types.ComponentDependencyResponseDto;
import org.openecomp.sdcrests.vsp.rest.ComponentDependencies;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDependencyEntityToCreationDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDependencyEntityToDto;
import org.openecomp.sdcrests.vsp.rest.mapping.MapComponentDependencyModelRequestToEntity;
import org.openecomp.sdcrests.wrappers.GenericCollectionWrapper;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.inject.Named;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named
@Service("componentDependencies")
@Scope(value = "prototype")
public class ComponentDependenciesImpl implements ComponentDependencies {

  private ComponentDependencyModelManager componentDependencyModelManager =
      ComponentDependencyModelManagerFactory.getInstance().createInterface();

  @Override
  public Response create(ComponentDependencyModel request, String vspId, String versionId,
                         String user) {
    MdcUtil.initMdc(LoggerServiceName.CREATE_COMPONENT_DEPENDENCY_MODEL.toString());
    List<ComponentDependencyModelEntity> modelEntities = new
        ArrayList<ComponentDependencyModelEntity>();

    final Version version = new Version(versionId);

    ComponentDependencyModelEntity modelEntity =
        new MapComponentDependencyModelRequestToEntity().applyMapping(request,
            ComponentDependencyModelEntity.class);

    modelEntity.setVspId(vspId);
    modelEntity.setVersion(version);

    ComponentDependencyModelEntity componentDependency =
        componentDependencyModelManager.createComponentDependency(modelEntity, vspId, version);

    MapComponentDependencyEntityToCreationDto mapping =
        new MapComponentDependencyEntityToCreationDto();
    ComponentDependencyCreationDto createdComponentDependencyDto = mapping.applyMapping(
        componentDependency, ComponentDependencyCreationDto.class);
    return Response.ok(componentDependency != null ? createdComponentDependencyDto : null)
        .build();
  }

  @Override
  public Response list(String vspId, String versionId, String user) {
    MdcUtil.initMdc(LoggerServiceName.GET_LIST_COMPONENT_DEPENDENCY.toString());
    Version vspVersion = new Version(versionId);

    Collection<ComponentDependencyModelEntity> componentDependencies =
        componentDependencyModelManager.list(vspId, vspVersion);

    MapComponentDependencyEntityToDto mapper = new MapComponentDependencyEntityToDto();
    GenericCollectionWrapper<ComponentDependencyResponseDto> results = new GenericCollectionWrapper
        <ComponentDependencyResponseDto>();
    for (ComponentDependencyModelEntity entity : componentDependencies) {
      results.add(mapper.applyMapping(entity, ComponentDependencyResponseDto.class));
    }

    return Response.ok(results).build();
  }

  @Override
  public Response delete(String vspId, String versionId, String dependencyId, String user) {
    MdcUtil.initMdc(LoggerServiceName.DELETE_COMPONENT_DEPENDENCY.toString());
    Version vspVersion = new Version(versionId);
    componentDependencyModelManager.delete(vspId, vspVersion, dependencyId);
    return Response.ok().build();
  }

  @Override
  public Response update(ComponentDependencyModel request, String vspId, String versionId, String
      dependencyId, String user) {

    MdcUtil.initMdc(LoggerServiceName.UPDATE_COMPONENT_DEPENDENCY.toString());

    final Version version = new Version(versionId);
    ComponentDependencyModelEntity modelEntity =
        new MapComponentDependencyModelRequestToEntity().applyMapping(request,
            ComponentDependencyModelEntity.class);

    modelEntity.setId(dependencyId);
    modelEntity.setVspId(vspId);
    modelEntity.setVersion(version);
    componentDependencyModelManager.update(modelEntity);
    return Response.ok().build();
  }

  @Override
  public Response get(String vspId, String version, String dependencyId, String user) {
    MdcUtil.initMdc(LoggerServiceName.GET_COMPONENT_DEPENDENCY.toString());
    ComponentDependencyModelEntity componentDependencyModelEntity = componentDependencyModelManager
        .get(vspId, new Version(version), dependencyId);

    MapComponentDependencyEntityToDto mapper = new MapComponentDependencyEntityToDto();
    ComponentDependencyResponseDto componentDependencyResponseDto =
        mapper.applyMapping(componentDependencyModelEntity, ComponentDependencyResponseDto.class);

    return Response.ok(componentDependencyModelEntity != null ? componentDependencyResponseDto :
        null).build();
  }

}