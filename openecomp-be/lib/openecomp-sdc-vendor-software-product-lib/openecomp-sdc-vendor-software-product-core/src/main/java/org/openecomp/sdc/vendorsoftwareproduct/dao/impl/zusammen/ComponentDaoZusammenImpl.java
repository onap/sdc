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
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComponentQuestionnnaireConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class ComponentDaoZusammenImpl implements ComponentDao {

  private ZusammenAdaptor zusammenAdaptor;

  public ComponentDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
  }

  @Override
  public Collection<ComponentEntity> list(ComponentEntity component) {
    SessionContext context = createSessionContext();

    return listComponents(zusammenAdaptor, context, component.getVspId(), component.getVersion());
  }

  static Collection<ComponentEntity> listComponents(ZusammenAdaptor zusammenAdaptor,
                                                    SessionContext context,
                                                    String vspId, Version version) {
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return new ArrayList<>();
    }

    ElementToComponentConvertor convertor = new ElementToComponentConvertor();
    return zusammenAdaptor.listElementsByName(context, elementContext, vspModel.get().getId(),
        ElementType.Components.name()).stream()
        .map(elementInfo -> {
          ComponentEntity entity = convertor.convert(elementInfo);
          entity.setVspId(vspId);
          entity.setVersion(version);
          return entity;
        })
        .collect(Collectors.toList());
  }


  @Override
  public void create(ComponentEntity component) {
    ZusammenElement componentElement = componentToZusammen(component, Action.CREATE);

    ZusammenElement componentsElement =
        buildStructuralElement(ElementType.Components, Action.IGNORE);
    componentsElement.getSubElements().add(componentElement);

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(componentsElement);

    SessionContext context = createSessionContext();
    Element savedVspModel = zusammenAdaptor.saveElement(context,
        new ElementContext(component.getVspId(), component.getVersion().getId()),
        vspModel, "Create component");
    component.setId(savedVspModel.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(ComponentEntity component) {
    ZusammenElement componentElement = componentToZusammen(component, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(component.getVspId(), component.getVersion().getId()),
        componentElement, String.format("Update component with id %s", component.getId()));
  }

  @Override
  public ComponentEntity get(ComponentEntity component) {
    SessionContext context = createSessionContext();

    Optional<Element> element =
        zusammenAdaptor.getElement(context,
            new ElementContext(component.getVspId(), component.getVersion().getId()),
            component.getId());

    if (element.isPresent()) {
      ComponentEntity entity = new ElementToComponentConvertor().convert(element.get());
      entity.setVspId(component.getVspId());
      entity.setVersion(component.getVersion());
      return entity;
    }
    return null;
  }

  @Override
  public void delete(ComponentEntity component) {
    ZusammenElement componentElement = buildElement(new Id(component.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context,
        new ElementContext(component.getVspId(), component.getVersion().getId()),
        componentElement, String.format("Delete component with id %s", component.getId()));
  }

  @Override
  public ComponentEntity getQuestionnaireData(String vspId, Version version, String componentId) {
    SessionContext context = createSessionContext();

    return getQuestionnaire(context, new ElementContext(vspId, version.getId()),
        new ComponentEntity(vspId, version, componentId));
  }

  private ComponentEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                           ComponentEntity component) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(component.getId()),
            ElementType.ComponentQuestionnaire.name());
    return questionnaireElement.map(new ElementToComponentQuestionnnaireConvertor()::convert)
        .map(entity -> {
          entity.setVspId(component.getVspId());
          entity.setVersion(component.getVersion());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String questionnaireData) {
    ZusammenElement questionnaireElement =
        componentQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement componentElement = buildElement(new Id(componentId), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context, new ElementContext(vspId, version.getId()),
        componentElement, "Update component questionnaire");
  }

  @Override
  public Collection<ComponentEntity> listQuestionnaires(String vspId, Version version) {
    return listCompositionAndQuestionnaire(vspId, version);
  }

  @Override
  public Collection<ComponentEntity> listCompositionAndQuestionnaire(String vspId,
                                                                     Version version) {
    SessionContext context = createSessionContext();

    Collection<ComponentEntity> components =
        listComponents(zusammenAdaptor, context, vspId, version);

    ElementContext elementContext = new ElementContext(vspId, version.getId());
    components.forEach(component -> component.setQuestionnaireData(
        getQuestionnaire(context, elementContext, component).getQuestionnaireData()));
    return components;
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Collection<ElementInfo> vspModelSubs = zusammenAdaptor
        .listElementsByName(context, elementContext, null, ElementType.VspModel.name());

    Optional<ElementInfo> componentsElement = vspModelSubs.stream()
        .filter(elementInfo -> elementInfo.getInfo() != null
            && ElementType.Components.name().equals(elementInfo.getInfo().getName()))
        .findFirst();
    if (!componentsElement.isPresent()) {
      return;
    }

    ZusammenElement components = buildElement(componentsElement.get().getId(), Action.IGNORE);
    components.setSubElements(componentsElement.get().getSubElements().stream()
        .map(component -> buildElement(component.getId(), Action.DELETE))
        .collect(Collectors.toList()));

    zusammenAdaptor.saveElement(context, elementContext, components, "Delete all components");
  }

  private ZusammenElement componentToZusammen(ComponentEntity component, Action action) {
    ZusammenElement componentElement = buildComponentElement(component, action);

    if (action == Action.CREATE) {
      ZusammenElement mibs = buildStructuralElement(ElementType.Mibs, Action.CREATE);
      mibs.addSubElement(buildStructuralElement(ElementType.SNMP_TRAP, Action.CREATE));
      mibs.addSubElement(buildStructuralElement(ElementType.VES_EVENTS, Action.CREATE));
      mibs.addSubElement(buildStructuralElement(ElementType.SNMP_POLL, Action.CREATE));

      componentElement.addSubElement(mibs);
      componentElement.addSubElement(
          componentQuestionnaireToZusammen(component.getQuestionnaireData(), Action.CREATE));
      componentElement.addSubElement(buildStructuralElement(ElementType.Nics, Action.CREATE));
      componentElement.addSubElement(buildStructuralElement(ElementType.Processes, Action.CREATE));
      componentElement.addSubElement(buildStructuralElement(ElementType.Computes, Action.CREATE));
      componentElement.addSubElement(buildStructuralElement(ElementType.Images, Action.CREATE));
    }
    return componentElement;
  }

  private ZusammenElement componentQuestionnaireToZusammen(String questionnaireData,
                                                           Action action) {
    ZusammenElement questionnaireElement =
        buildStructuralElement(ElementType.ComponentQuestionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildComponentElement(ComponentEntity component, Action action) {
    ZusammenElement componentElement =
        buildElement(component.getId() == null ? null : new Id(component.getId()), action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Component);
    info.addProperty(ElementPropertyName.compositionData.name(), component.getCompositionData());
    componentElement.setInfo(info);
    componentElement.setData(new ByteArrayInputStream(component.getCompositionData().getBytes()));
    return componentElement;
  }


}
