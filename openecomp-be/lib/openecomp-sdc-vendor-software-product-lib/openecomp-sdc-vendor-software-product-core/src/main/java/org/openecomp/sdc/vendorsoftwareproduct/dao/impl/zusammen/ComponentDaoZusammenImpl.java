package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

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
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(component.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(component.getVersion()));

    return listComponents(zusammenAdaptor, context, elementContext, component.getVspId(),
        component.getVersion());
  }

  static Collection<ComponentEntity> listComponents(ZusammenAdaptor zusammenAdaptor,
                                                    SessionContext context,
                                                    ElementContext elementContext,
                                                    String vspId, Version version) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, null, StructureElement.Components.name())
        .stream().map(elementInfo -> mapElementInfoToComponent(vspId, version, elementInfo))
        .collect(Collectors.toList());
  }

  private static ComponentEntity mapElementInfoToComponent(String vspId, Version version,
                                                           ElementInfo elementInfo) {
    ComponentEntity componentEntity =
        new ComponentEntity(vspId, version, elementInfo.getId().getValue());
    componentEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return componentEntity;
  }

  @Override
  public void create(ComponentEntity component) {
    ZusammenElement componentElement = componentToZusammen(component, Action.CREATE);
    ZusammenElement componentsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Components, null);
    componentsElement.getSubElements().add(componentElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(component.getVspId());
    Optional<Element> savedElement = zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentsElement, "Create component");
    savedElement.ifPresent(element ->
        component.setId(element.getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(ComponentEntity component) {
    ZusammenElement componentElement = componentToZusammen(component, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(component.getVspId());
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentElement, String.format("Update component with id %s", component.getId()));
  }

  @Override
  public ComponentEntity get(ComponentEntity component) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(component.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(component.getVersion()));

    Optional<Element> element =
        zusammenAdaptor.getElement(context, elementContext, component.getId());

    if (element.isPresent()) {
      component.setCompositionData(new String(FileUtils.toByteArray(element.get().getData())));
      return component;
    }
    return null;
  }

  @Override
  public void delete(ComponentEntity component) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(component.getId()));
    componentElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(component.getVspId());
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentElement, String.format("Delete component with id %s", component.getId()));
  }

  @Override
  public ComponentEntity getQuestionnaireData(String vspId, Version version, String componentId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    return getQuestionnaire(context, elementContext,
        new ComponentEntity(vspId, version, componentId));
  }

  private ComponentEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                           ComponentEntity component) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(component.getId()),
            StructureElement.Questionnaire.name());
    return questionnaireElement.map(
        element -> element.getData() == null
            ? null
            : new String(FileUtils.toByteArray(element.getData())))
        .map(questionnaireData -> {
          component.setQuestionnaireData(questionnaireData);
          return component;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String questionnaireData) {
    ZusammenElement questionnaireElement =
        componentQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setAction(Action.IGNORE);
    componentElement.setElementId(new Id(componentId));
    componentElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentElement, "Update component questionnaire");
  }

  @Override
  public Collection<ComponentEntity> listQuestionnaires(String vspId, Version version) {
    return listCompositionAndQuestionnaire(vspId, version);
  }

  @Override
  public Collection<ComponentEntity> listCompositionAndQuestionnaire(String vspId,
                                                                     Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Collection<ComponentEntity> components =
        listComponents(zusammenAdaptor, context, elementContext, vspId, version);

    components.forEach(component -> getQuestionnaire(context, elementContext, component));
    return components;
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    ZusammenElement componentsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Components, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        componentsElement, "Delete all components");
  }

  private ZusammenElement componentToZusammen(ComponentEntity component, Action action) {
    ZusammenElement componentElement = buildComponentElement(component, action);
    if (action == Action.CREATE) {
      componentElement
          .setSubElements(Arrays.asList(
              componentQuestionnaireToZusammen(component.getQuestionnaireData(), Action.CREATE),
              VspZusammenUtil.buildStructuralElement(StructureElement.Nics, Action.CREATE),
              VspZusammenUtil.buildStructuralElement(StructureElement.Processes, Action.CREATE),
              VspZusammenUtil.buildStructuralElement(StructureElement.Mibs, Action.CREATE),
              VspZusammenUtil.buildStructuralElement(StructureElement.Computes, Action.CREATE),
              VspZusammenUtil.buildStructuralElement(StructureElement.Images, Action.CREATE)));


    }
    return componentElement;
  }

  private ZusammenElement componentQuestionnaireToZusammen(String questionnaireData,
                                                           Action action) {
    ZusammenElement questionnaireElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildComponentElement(ComponentEntity component, Action action) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setAction(action);
    if (component.getId() != null) {
      componentElement.setElementId(new Id(component.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Component);
    info.addProperty(ElementPropertyName.compositionData.name(), component.getCompositionData());
    componentElement.setInfo(info);
    componentElement.setData(new ByteArrayInputStream(component.getCompositionData().getBytes()));
    return componentElement;
  }
}
