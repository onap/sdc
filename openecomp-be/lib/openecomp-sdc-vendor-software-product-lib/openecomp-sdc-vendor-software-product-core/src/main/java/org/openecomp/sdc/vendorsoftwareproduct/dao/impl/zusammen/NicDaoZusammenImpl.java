package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToNicConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToNicQuestionnaireConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.types.ElementPropertyName;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

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
    SessionContext context = createSessionContext();
    return listNics(context, new ElementContext(nic.getVspId(), nic.getVersion().getId()), nic);
  }

  private Collection<NicEntity> listNics(SessionContext context, ElementContext elementContext,
                                         NicEntity nic) {
    ElementToNicConvertor convertor = new ElementToNicConvertor();
    return zusammenAdaptor.listElementsByName(context, elementContext, new Id(nic.getComponentId()),
        ElementType.Nics.name())
        .stream().map(convertor::convert)
        .map(nicEntity -> {
          nicEntity.setComponentId(nicEntity.getComponentId());
          nicEntity.setVspId(nic.getVspId());
          nicEntity.setVersion(nic.getVersion());
          return nicEntity;
        })
        .collect(Collectors.toList());
  }


  @Override
  public void create(NicEntity nic) {
    ZusammenElement nicElement = nicToZusammen(nic, Action.CREATE);

    ZusammenElement nicsElement = buildStructuralElement(ElementType.Nics, Action.IGNORE);
    nicsElement.setSubElements(Collections.singletonList(nicElement));

    ZusammenElement componentElement = buildElement(new Id(nic.getComponentId()), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(nicsElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(nic.getVspId(), nic.getVersion().getId());

    Element savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create nic");
    nic.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(NicEntity nic) {
    ZusammenElement nicElement = nicToZusammen(nic, Action.UPDATE);

    SessionContext context = createSessionContext();
    zusammenAdaptor
        .saveElement(context, new ElementContext(nic.getVspId(), nic.getVersion().getId()),
            nicElement, String.format("Update nic with id %s", nic.getId()));
  }

  @Override
  public NicEntity get(NicEntity nic) {
    SessionContext context = createSessionContext();
    ElementToNicConvertor convertor = new ElementToNicConvertor();
    Optional<Element> element = zusammenAdaptor
        .getElement(context, new ElementContext(nic.getVspId(), nic.getVersion().getId()),
            nic.getId());

    if (element.isPresent()) {
      NicEntity entity = convertor.convert(element.get());
      entity.setVspId(nic.getVspId());
      entity.setVersion(nic.getVersion());
      entity.setComponentId(nic.getComponentId());

      return entity;
    } else {
      return null;
    }
  }

  @Override
  public void delete(NicEntity nic) {
    ZusammenElement nicElement = buildElement(new Id(nic.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    zusammenAdaptor
        .saveElement(context, new ElementContext(nic.getVspId(), nic.getVersion().getId()),
            nicElement, String.format("Delete nic with id %s", nic.getId()));
  }

  @Override
  public NicEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                        String nicId) {
    SessionContext context = createSessionContext();

    return getQuestionnaire(context, new ElementContext(vspId, version.getId()),
        new NicEntity(vspId, version, componentId, nicId));
  }

  private NicEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                     NicEntity nic) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(nic.getId()),
            ElementType.NicQuestionnaire.name());
    return questionnaireElement.map(new ElementToNicQuestionnaireConvertor()::convert)
        .map(entity -> {
          entity.setVspId(nic.getVspId());
          entity.setVersion(nic.getVersion());
          entity.setComponentId(nic.getComponentId());
          return entity;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String nicId, String questionnaireData) {
    ZusammenElement questionnaireElement =
        nicQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement nicElement = buildElement(new Id(nicId), Action.IGNORE);
    nicElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = createSessionContext();
    zusammenAdaptor.saveElement(context, new ElementContext(vspId, version.getId()), nicElement,
        "Update nic questionnaire");
  }

  @Override
  public Collection<NicEntity> listByVsp(String vspId, Version version) {
    SessionContext context = createSessionContext();

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, vspId, version);

    ElementContext elementContext = new ElementContext(vspId, version.getId());
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
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> optionalElement = zusammenAdaptor.getElementByName(context,
        elementContext, new Id(componentId), ElementType.Nics.name());

    if (optionalElement.isPresent()) {
      Element nicsElement = optionalElement.get();
      Collection<Element> nics = nicsElement.getSubElements();

      nics.forEach(nic -> {
        ZusammenElement nicZusammenElement = buildElement(nic.getElementId(), Action.DELETE);
        zusammenAdaptor.saveElement(context, elementContext, nicZusammenElement,
            "Delete nic with id " + nic.getElementId());
      });
    }
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
        buildStructuralElement(ElementType.NicQuestionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildNicElement(NicEntity nic, Action action) {
    ZusammenElement nicElement =
        buildElement(nic.getId() == null ? null : new Id(nic.getId()), action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Nic);
    info.addProperty(ElementPropertyName.compositionData.name(), nic.getCompositionData());
    nicElement.setInfo(info);
    nicElement.setData(new ByteArrayInputStream(nic.getCompositionData().getBytes()));
    return nicElement;
  }
}
