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
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class NicDaoZusammenImpl implements NicDao {

  private ZusammenAdaptor zusammenAdaptor;

  public NicDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public Collection<NicEntity> list(NicEntity nic) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(nic.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(nic.getVersion()));

    return listNics(context, elementContext, nic);
  }

  private Collection<NicEntity> listNics(SessionContext context, ElementContext elementContext,
                                         NicEntity nic) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(nic.getComponentId()),
            StructureElement.Nics.name())
        .stream().map(elementInfo -> mapElementInfoToNic(
            nic.getVspId(), nic.getVersion(), nic.getComponentId(), elementInfo))
        .collect(Collectors.toList());
  }

  private NicEntity mapElementInfoToNic(String vspId, Version version,
                                        String componentId, ElementInfo elementInfo) {
    NicEntity nicEntity =
        new NicEntity(vspId, version, componentId, elementInfo.getId().getValue());
    nicEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return nicEntity;
  }

  @Override
  public void create(NicEntity nic) {
    ZusammenElement nicElement = nicToZusammen(nic, Action.CREATE);

    ZusammenElement nicsElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Nics, null);
    nicsElement.setSubElements(Collections.singletonList(nicElement));

    ZusammenElement componentElement =
        buildZusammenElement(new Id(nic.getComponentId()), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(nicsElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(nic.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create nic");
    savedElement.ifPresent(element ->
        nic.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(NicEntity nic) {
    ZusammenElement nicElement = nicToZusammen(nic, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(nic.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, nicElement,
        String.format("Update nic with id %s", nic.getId()));
  }

  @Override
  public NicEntity get(NicEntity nic) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(nic.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(nic.getVersion()));
    Optional<Element> element = zusammenAdaptor.getElement(context, elementContext, nic.getId());

    if (element.isPresent()) {
      nic.setCompositionData(new String(FileUtils.toByteArray(element.get().getData())));
      return nic;
    } else {
      return null;
    }
  }

  @Override
  public void delete(NicEntity nic) {
    ZusammenElement nicElement = buildZusammenElement(new Id(nic.getId()), Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(nic.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, nicElement,
        String.format("Delete nic with id %s", nic.getId()));
  }

  @Override
  public NicEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                        String nicId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    return getQuestionnaire(context, elementContext,
        new NicEntity(vspId, version, componentId, nicId));
  }

  private NicEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                     NicEntity nic) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(nic.getId()),
            StructureElement.Questionnaire.name());
    return questionnaireElement.map(
        element -> element.getData() == null
            ? null
            : new String(FileUtils.toByteArray(element.getData())))
        .map(questionnaireData -> {
          nic.setQuestionnaireData(questionnaireData);
          return nic;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String nicId, String questionnaireData) {
    ZusammenElement questionnaireElement =
        nicQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement nicElement = new ZusammenElement();
    nicElement.setAction(Action.IGNORE);
    nicElement.setElementId(new Id(nicId));
    nicElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, nicElement, "Update nic questionnaire");
  }

  @Override
  public Collection<NicEntity> listByVsp(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, elementContext, vspId, version);

    return components.stream()
        .map(component ->
            listNics(context, elementContext,
                new NicEntity(vspId, version, component.getId(), null)).stream()
                .map(nic -> getQuestionnaire(context, elementContext, nic))
                .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteByComponentId(String vspId, Version version, String componentId) {
    ZusammenElement componentElement = buildZusammenElement(new Id(componentId), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(
        VspZusammenUtil.buildStructuralElement(StructureElement.Nics, Action.DELETE)));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, componentElement,
        String.format("Delete all nics of component %s", componentId));
  }

  @Override
  public void deleteByVspId(String vspId, Version version) {

  }

  private ZusammenElement nicToZusammen(NicEntity nic, Action action) {
    ZusammenElement nicElement = buildNicElement(nic, action);
    if (action == Action.CREATE) {
      nicElement.setSubElements(Collections.singletonList(
          nicQuestionnaireToZusammen(nic.getQuestionnaireData(), Action.CREATE)));
    }
    return nicElement;
  }

  private ZusammenElement nicQuestionnaireToZusammen(String questionnaireData,
                                                     Action action) {
    ZusammenElement questionnaireElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildZusammenElement(Id elementId, Action action) {
    ZusammenElement element = new ZusammenElement();
    element.setElementId(elementId);
    element.setAction(action);
    return element;
  }

  private ZusammenElement buildNicElement(NicEntity nic, Action action) {
    ZusammenElement nicElement = new ZusammenElement();
    nicElement.setAction(action);
    if (nic.getId() != null) {
      nicElement.setElementId(new Id(nic.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Nic);
    info.addProperty(ElementPropertyName.compositionData.name(), nic.getCompositionData());
    nicElement.setInfo(info);
    nicElement.setData(new ByteArrayInputStream(nic.getCompositionData().getBytes()));
    return nicElement;
  }
}
