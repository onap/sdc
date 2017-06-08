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
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Avrahamg.
 * @since March 23, 2017
 */
public class ProcessDaoZusammenImpl implements ProcessDao {

  private static final String NAME = "name";
  private static final String ELEMENT_TYPE = "type";
  private static final String ARTIFACT_NAME = "artifactName";
  private static final String DESCRIPTION = "description";
  private static final String PROCESS_TYPE = "processType";

  private ZusammenAdaptor zusammenAdaptor;

  public ProcessDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public void create(ProcessEntity processEntity) {
    ZusammenElement processElement = buildProcessElement(processEntity, Action.CREATE);

    ZusammenElement processesElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Processes, null);
    ZusammenElement aggregatedElement = VspZusammenUtil.aggregateElements(processesElement,
        processElement);
    ZusammenElement componentElement;
    if (processEntity.getComponentId() != null) {
      componentElement = createParentElement(processEntity);
      aggregatedElement =
          VspZusammenUtil.aggregateElements(componentElement, aggregatedElement);
    }

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(processEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> savedElement =
        zusammenAdaptor.saveElement(context, elementContext, aggregatedElement, "Create process");
    savedElement.ifPresent(element -> {
      if (processEntity.getComponentId() == null) {
        processEntity.setId(element.getSubElements().iterator().next()
            .getElementId().getValue());
      } else {
        processEntity.setId(element.getSubElements().iterator().next()
            .getSubElements().iterator().next().getElementId().getValue());
      }
    });
  }

  @Override
  public ProcessEntity get(ProcessEntity process) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(process.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(process.getVersion()));

    Optional<Element> elementOptional =
        zusammenAdaptor.getElement(context, elementContext, process.getId());

    if (elementOptional.isPresent()) {
      Element element = elementOptional.get();
      process.setName(element.getInfo().getProperty(NAME));
      process.setArtifactName(element.getInfo().getProperty(ARTIFACT_NAME));
      process.setDescription(element.getInfo().getProperty(DESCRIPTION));
      process.setType(element.getInfo().getProperty
          (PROCESS_TYPE) != null ? ProcessType.valueOf(element.getInfo().getProperty
          (PROCESS_TYPE)) : null);

      process.setArtifact(ByteBuffer.wrap(FileUtils.toByteArray(element.getData())));
      return process;
    } else {
      return null;
    }
  }

  @Override
  public void update(ProcessEntity processEntity) {
    ZusammenElement processElement = buildProcessElement(processEntity, Action.UPDATE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(processEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    Optional<Element> element =
        zusammenAdaptor.saveElement(context, elementContext, processElement, "Create process");
    System.out.println(element.get().getElementId());
  }

  @Override
  public void delete(ProcessEntity processEntity) {
    ZusammenElement processElement = new ZusammenElement();
    processElement.setElementId(new Id(processEntity.getId()));
    processElement.setAction(Action.DELETE);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(processEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext, processElement,
        String.format("Delete process with id %s", processEntity.getId()));
  }

  @Override
  public void deleteAll(ProcessEntity processEntity) {
    ZusammenElement aggregatedElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.Processes, Action.DELETE);

    if (processEntity.getComponentId() != null) {
      ZusammenElement componentElement = createParentElement(processEntity);
      aggregatedElement = VspZusammenUtil.aggregateElements(componentElement,
          aggregatedElement);
    }

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(processEntity.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));
    zusammenAdaptor.saveElement(context, elementContext, aggregatedElement, "Delete All processes");
  }

  @Override
  public void deleteVspAll(String vspId, Version version) {
    ProcessEntity processEntity = new ProcessEntity();
    processEntity.setVersion(version);
    processEntity.setVspId(vspId);
    deleteAll(processEntity);
  }

  @Override
  public Collection<ProcessEntity> list(ProcessEntity process) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(process.getVspId());
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(process.getVersion()));

    Optional<ElementInfo> processesOptional =
        zusammenAdaptor.getElementInfoByName(context, elementContext,
            extractParentElementId(process), StructureElement.Processes.name());
    if (!processesOptional.isPresent()) {
      return new ArrayList<>();
    }
    return zusammenAdaptor.listElements(context, elementContext, processesOptional.get().getId())
        .stream()
        .map(elementInfo -> mapElementInfoToProcess(
            process.getVspId(), process.getVersion(), process.getComponentId(),
            elementInfo))
        .collect(Collectors.toList());
  }

  private Id extractParentElementId(ProcessEntity processEntity) {
    return processEntity.getComponentId() == null ? null : new Id(processEntity.getComponentId());
  }

  private ProcessEntity mapElementInfoToProcess(String vspId, Version version,
                                                String componentId,
                                                ElementInfo elementInfo) {
    ProcessEntity processEntity = new ProcessEntity(vspId, version, componentId, elementInfo
        .getId().getValue());
    processEntity.setName((String) elementInfo.getInfo().getProperties().get(NAME));
    processEntity
        .setArtifactName((String) elementInfo.getInfo().getProperties().get(ARTIFACT_NAME));
    processEntity.setDescription((String) elementInfo.getInfo().getProperties().get(DESCRIPTION));
    return processEntity;
  }

  private ZusammenElement buildProcessElement(ProcessEntity process, Action action) {

    Info info = new Info();
    info.setName(process.getName());
    info.addProperty(NAME, process.getName());
    info.addProperty(ELEMENT_TYPE, ElementType.Process);
    info.addProperty(ARTIFACT_NAME, process.getArtifactName());
    info.addProperty(DESCRIPTION, process.getDescription());
    info.addProperty(PROCESS_TYPE, process.getType() != null ? process.getType().name() : null);

    ZusammenElement processElement = new ZusammenElement();
    processElement.setElementId(new Id(process.getId()));
    processElement.setAction(action);
    processElement.setInfo(info);
    if (Objects.nonNull(process.getArtifact())) {
      processElement.setData(new ByteArrayInputStream(process.getArtifact().array()));
    }
    return processElement;
  }

  private ZusammenElement createParentElement(ProcessEntity entity) {
    ZusammenElement componentElement = new ZusammenElement();
    componentElement.setElementId(new Id(entity.getComponentId()));
    componentElement.setAction(Action.IGNORE);
    return componentElement;
  }
}
