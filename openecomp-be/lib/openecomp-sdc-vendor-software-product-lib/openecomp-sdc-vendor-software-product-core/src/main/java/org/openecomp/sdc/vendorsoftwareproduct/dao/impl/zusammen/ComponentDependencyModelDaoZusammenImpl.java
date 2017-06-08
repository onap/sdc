package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDependencyModelDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentDependencyModelEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by ayalaben on 5/16/2017.
 */
public class ComponentDependencyModelDaoZusammenImpl implements ComponentDependencyModelDao {

  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateDaoZusammenImpl.class);

  private ZusammenAdaptor zusammenAdaptor;

  public ComponentDependencyModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public ComponentDependencyModelEntity get(ComponentDependencyModelEntity entity) {

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(entity.getVspId()); // entity.getId()?
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(entity.getVersion()));

    Optional<ElementInfo> componentDependencyElement =
        zusammenAdaptor.getElementInfo(context, elementContext, new Id(entity.getId()));

    if (componentDependencyElement.isPresent()) {
      addComponentDependencyData(entity, componentDependencyElement.get());
      return entity;
    }

    return null;
  }

  @Override
  public void create(ComponentDependencyModelEntity entity) {

    ZusammenElement componentDependencies =
        VspZusammenUtil.buildStructuralElement(StructureElement.ComponentDependencies, null);

    ZusammenElement componentDependency = buildComponentDependencyElement(entity);
    componentDependency.setAction(Action.CREATE);

    Id itemId = new Id(entity.getVspId());
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement = zusammenAdaptor.saveElement(context, elementContext,
        VspZusammenUtil.aggregateElements(componentDependencies, componentDependency),
        "Create component dependency model");

    savedElement.ifPresent(element ->
        entity.setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(ComponentDependencyModelEntity entity) {
    ZusammenElement componentDependencyElement = buildComponentDependencyElement(entity);
    componentDependencyElement.setAction(Action.UPDATE);

    Id itemId = new Id(entity.getVspId());
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext,
        componentDependencyElement,
        String.format("Update component dependency model with id %s", entity.getId()));
  }

  @Override
  public void delete(ComponentDependencyModelEntity entity) {
    ZusammenElement componentDependencyElement = new ZusammenElement();
    componentDependencyElement.setElementId(new Id(entity.getId()));
    componentDependencyElement.setAction(Action.DELETE);

    Id itemId = new Id(entity.getVspId());
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));


    zusammenAdaptor.saveElement(context, elementContext,
        componentDependencyElement,
        String.format("Delete component dependency model with id %s", entity.getId()));
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    ZusammenElement componentDependenciesElement =
        VspZusammenUtil
            .buildStructuralElement(StructureElement.ComponentDependencies, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context, new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentDependenciesElement, "Delete all component dependencies");
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
    //not implemented?
  }

  @Override
  public Collection<ComponentDependencyModelEntity> list(ComponentDependencyModelEntity entity) {

    Id itemId = new Id(entity.getVspId());
    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(entity.getVersion()));

    return zusammenAdaptor.listElementsByName(context, elementContext,
        null, StructureElement.ComponentDependencies.name())
        .stream().map(elementInfo -> mapElementInfoToComponentDependencyModel(entity.getVspId(),
            entity.getVersion(), elementInfo))
        .collect(Collectors.toList());
  }

  private static ComponentDependencyModelEntity mapElementInfoToComponentDependencyModel(
      String vspId, Version version,
      ElementInfo elementInfo) {
    ComponentDependencyModelEntity componentDependencyModelEntity =
        new ComponentDependencyModelEntity(vspId, version, elementInfo.getId().getValue());
    componentDependencyModelEntity.setSourceComponentId(elementInfo.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.sourcecomponent_id.name()));
    componentDependencyModelEntity.setTargetComponentId(elementInfo.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.targetcomponent_id.name()));
    componentDependencyModelEntity.setRelation(elementInfo.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.relation.name()));

    return componentDependencyModelEntity;
  }


  private ZusammenElement buildComponentDependencyElement(ComponentDependencyModelEntity entity) {
    ZusammenElement componentDependencyElement = new ZusammenElement();

    if (entity.getId() != null) {
      componentDependencyElement.setElementId(new Id(entity.getId()));
    }

    Info info = new Info();
    info.addProperty(ComponentDependencyModelPropertyName.id.name(), entity.getId());
    info.addProperty(ComponentDependencyModelPropertyName.relation.name(), entity.getRelation());
    info.addProperty(ComponentDependencyModelPropertyName.sourcecomponent_id.name(),
        entity.getSourceComponentId());
    info.addProperty(ComponentDependencyModelPropertyName.targetcomponent_id.name(),
        entity.getTargetComponentId());

    componentDependencyElement.setInfo(info);

    return componentDependencyElement;
  }

  private void addComponentDependencyData(ComponentDependencyModelEntity componentDependency,
                                          ElementInfo componentDependencyElement) {
    componentDependency.setId(componentDependencyElement.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.id.name()));
    componentDependency.setRelation(componentDependencyElement.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.id.name()));
    componentDependency.setSourceComponentId(componentDependencyElement.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.sourcecomponent_id.name()));
    componentDependency.setTargetComponentId(componentDependencyElement.getInfo()
        .getProperty(ComponentDependencyModelPropertyName.targetcomponent_id.name()));
  }


  private enum ComponentDependencyModelPropertyName {
    id,
    relation,
    sourcecomponent_id,
    targetcomponent_id,
  }

}
