package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;


import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor.ElementToComputeConvertor;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
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
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(compute.getVspId(), compute.getVersion().getId());

    return listComputes(context, elementContext, compute);
  }

  private Collection<ComputeEntity> listComputes(SessionContext context,
                                                 ElementContext elementContext,
                                                 ComputeEntity compute) {
    ElementToComputeConvertor convertor = new ElementToComputeConvertor();
    return zusammenAdaptor
        .listElementsByName(context, elementContext, new Id(compute.getComponentId()),
            ElementType.Computes.name())
        .stream().map(elementInfo -> convertor.convert(elementInfo))
        .map(computeEntity -> {
          computeEntity.setComponentId(compute.getComponentId());
          computeEntity.setVspId(compute.getVspId());
          computeEntity.setVersion(compute.getVersion());
          return computeEntity;
        })
        .collect(Collectors.toList());
  }

  @Override
  public void create(ComputeEntity compute) {
    ZusammenElement computeElement = computeToZusammen(compute, Action.CREATE);

    ZusammenElement computesElement = buildStructuralElement(ElementType.Computes, Action.IGNORE);
    computesElement.setSubElements(Collections.singletonList(computeElement));

    ZusammenElement componentElement =
        buildElement(new Id(compute.getComponentId()), Action.IGNORE);
    componentElement.setSubElements(Collections.singletonList(computesElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(compute.getVspId(), compute.getVersion().getId());

    Element savedElement =
        zusammenAdaptor.saveElement(context, elementContext, componentElement, "Create compute");
    compute.setId(savedElement.getSubElements().iterator().next()
        .getSubElements().iterator().next().getElementId().getValue());
  }

  @Override
  public void update(ComputeEntity compute) {
    ZusammenElement computeElement = computeToZusammen(compute, Action.UPDATE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(compute.getVspId(), compute.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, computeElement,
        String.format("Update compute with id %s", compute.getId()));
  }

  @Override
  public ComputeEntity get(ComputeEntity compute) {
    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(compute.getVspId(), compute.getVersion().getId());
    Optional<Element> element =
        zusammenAdaptor.getElement(context, elementContext, compute.getId());

    if (element.isPresent()) {

      ElementToComputeConvertor convertor = new ElementToComputeConvertor();
      ComputeEntity entity = convertor.convert(element.get());
      entity.setVspId(compute.getVspId());
      entity.setVersion(compute.getVersion());
      entity.setComponentId(compute.getComponentId());
      return entity;
    } else {
      return null;
    }
  }

  @Override
  public void delete(ComputeEntity compute) {
    ZusammenElement computeElement = buildElement(new Id(compute.getId()), Action.DELETE);

    SessionContext context = createSessionContext();
    ElementContext elementContext =
        new ElementContext(compute.getVspId(), compute.getVersion().getId());
    zusammenAdaptor.saveElement(context, elementContext, computeElement,
        String.format("Delete compute with id %s", compute.getId()));
  }

  @Override
  public ComputeEntity getQuestionnaireData(String vspId, Version version, String componentId,
                                            String computeId) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    return getQuestionnaire(context, elementContext,
        new ComputeEntity(vspId, version, componentId, computeId));
  }

  private ComputeEntity getQuestionnaire(SessionContext context, ElementContext elementContext,
                                         ComputeEntity compute) {
    Optional<Element> questionnaireElement = zusammenAdaptor
        .getElementByName(context, elementContext, new Id(compute.getId()),
            ElementType.ComputeQuestionnaire.name());
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

    ZusammenElement computeElement = buildElement(new Id(computeId), Action.IGNORE);
    computeElement.setSubElements(Collections.singletonList(questionnaireElement));

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());
    zusammenAdaptor.saveElement(context, elementContext, computeElement, "Update compute "
        + "questionnaire");
  }

  @Override
  public Collection<ComputeEntity> listByVsp(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Collection<ComponentEntity> components = ComponentDaoZusammenImpl
        .listComponents(zusammenAdaptor, context, vspId, version);

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

    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<Element> optionalElement = zusammenAdaptor.getElementByName(context,
        elementContext, null, ElementType.Computes.name());

    if (optionalElement.isPresent()) {
      Element computesElement = optionalElement.get();
      Collection<Element> computes = computesElement.getSubElements();

      computes.forEach(compute -> {
        ZusammenElement computeElement = buildElement(compute.getElementId(), Action.DELETE);
        zusammenAdaptor.saveElement(context, elementContext, computeElement,
            "Delete compute with id " + compute.getElementId());
      });
    }
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
        buildStructuralElement(ElementType.ComputeQuestionnaire, action);
    questionnaireElement.setData(new ByteArrayInputStream(questionnaireData.getBytes()));
    return questionnaireElement;
  }

  private ZusammenElement buildComputeElement(ComputeEntity compute, Action action) {
    ZusammenElement computeElement =
        buildElement(compute.getId() == null ? null : new Id(compute.getId()), action);
    Info info = new Info();
    info.addProperty(ElementPropertyName.elementType.name(), ElementType.Compute);
    info.addProperty(ElementPropertyName.compositionData.name(), compute.getCompositionData());
    computeElement.setInfo(info);
    computeElement.setData(new ByteArrayInputStream(compute.getCompositionData().getBytes()));
    return computeElement;
  }
}
