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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComputeDaoZusammenImpl implements ComputeDao {

  private ZusammenAdaptor zusammenAdaptor;

  public ComputeDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {
  }

  @Override
  public Collection<ComputeEntity> list(ComputeEntity compute) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(compute.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(compute.getVersion()));

    return listComputes(context, elementContext, compute);
  }

  private Collection<ComputeEntity> listComputes(SessionContext context,
                                            ElementContext elementContext, ComputeEntity compute) {
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(compute.getComponentId()),
            StructureElement.Computes.name())
        .stream().map(elementInfo -> mapElementInfoToCompute(
            compute.getVspId(), compute.getVersion(), compute.getComponentId(), elementInfo))
        .collect(Collectors.toList());
  }

  private static ComputeEntity mapElementInfoToCompute(String vspId, Version version,
                                                     String componentId, ElementInfo elementInfo) {
    ComputeEntity componentEntity =
        new ComputeEntity(vspId, version, componentId, elementInfo.getId().getValue());
    componentEntity.setCompositionData(
        elementInfo.getInfo().getProperty(ElementPropertyName.compositionData.name()));
    return componentEntity;
  }

  @Override
  public void create(ComputeEntity compute) {
    ZusammenElement computeElement = computeToZusammen(compute, Action.CREATE);

    ZusammenElement computesElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Computes, null);
    computesElement.setSubElements(Collections.singletonList(computeElement));

    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(compute.getComponentId()));
    componentElement.setAction(Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(computesElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(compute.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create compute");
    savedElement.ifPresent(element ->
        compute.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue()));
  }

  @Override
  public void update(ComputeEntity compute) {
    ZusammenElement computeElement = computeToZusammen(compute, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(compute.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, computeElement,
        String.format("Update compute with id %s", compute.getId()));
  }

  @Override
  public ComputeEntity get(ComputeEntity compute) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(compute.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(compute.getVersion()));
    Optional<Element> element = zusammenAdaptor.getElement(context, elementContext, compute.getId());

    if (element.isPresent()) {
      compute.setCompositionData(new String(FileUtils.toByteArray(element.get().getData())));
      return compute;
    } else {
      return null;
    }
  }

  @Override
  public void delete(ComputeEntity compute) {
    ZusammenElement computeElement = new ZusammenElement();
    computeElement.setElementId(new Id(compute.getId()));
    computeElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(compute.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, computeElement,
        String.format("Delete compute with id %s", compute.getId()));
  }

  @Override
  public ComputeEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                        String computeId) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    return getQuestionnaire(context, elementContext,
        new ComputeEntity(vspId, version, componentId, computeId));
  }

  private ComputeEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                     ComputeEntity compute) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(compute.getId()),
            StructureElement.Questionnaire.name());
    return questionnaireElement.map(
        element -> element.getData() == null
            ? null
            : new String(FileUtils.toByteArray(element.getData())))
        .map(questionnaireData -> {
          compute.setQuestionnaireData(questionnaireData);
          return compute;
        })
        .orElse(null);
  }

  @Override
  public void updateQuestionnaireData(String vspId, Version version, String componentId,
                                      String computeId, String questionnaireData) {
    ZusammenElement questionnaireElement =
        computeQuestionnaireToZusammen(questionnaireData, Action.UPDATE);

    ZusammenElement computeElement = new ZusammenElement();
    computeElement.setAction(Action.IGNORE);
    computeElement.setElementId(new Id(computeId));
    computeElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, computeElement, "Update compute "
        + "questionnaire");
  }

  @Override
  public Collection<ComputeEntity> listByVsp(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, elementContext, vspId, version);

    return components.stream()
        .map(component ->
            listComputes(context, elementContext,
                new ComputeEntity(vspId, version, component.getId(), null)).stream()
                .map(compute -> getQuestionnaire(context, elementContext, compute))
                .collect(Collectors.toList()))
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  @Override
  public void deleteAll(String vspId, Version version) {
    ZusammenElement computesElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Computes, Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    zusammenAdaptor.saveElement(context,
        new ElementContext(itemId,
            VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor)),
        computesElement, "Delete all computes");
  }

  private ZusammenElement computeToZusammen(ComputeEntity compute, Action action) {
    ZusammenElement computeElement = buildComputeElement(compute, action);
    if (action == Action.CREATE) {
      computeElement.setSubElements(Collections.singletonList(
          computeQuestionnaireToZusammen(compute.getQuestionnaireData(), Action.CREATE)));
    }
    return computeElement;
  }

  private ZusammenElement computeQuestionnaireToZusammen(String questionnaireData,
                                                     Action action) {
    ZusammenElement questionnaireElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Questionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildComputeElement(ComputeEntity compute, Action action) {
    ZusammenElement computeElement = new ZusammenElement();
    computeElement.setAction(action);
    if (compute.getId() != null) {
      computeElement.setElementId(new Id(compute.getId()));
    }
    Info info = new Info();
    info.addProperty(ElementPropertyName.type.name(), ElementType.Compute);
    info.addProperty(ElementPropertyName.compositionData.name(), compute.getCompositionData());
    computeElement.setInfo(info);
    computeElement.setData(new ByteArrayInputStream(compute.getCompositionData().getBytes()));
    return computeElement;
  }



}
