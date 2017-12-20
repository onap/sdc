package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class OrchestrationTemplateDaoZusammenImpl implements OrchestrationTemplateDao {

  private ZusammenAdaptor zusammenAdaptor;

  public OrchestrationTemplateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public OrchestrationTemplateEntity getInfo(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return null;
    }

    Optional<ElementInfo> elementInfo = zusammenAdaptor
        .getElementInfoByName(context, elementContext, vspModel.get().getId(),
            ElementType.OrchestrationTemplate.name());
    if (!elementInfo.isPresent()) {
      return null;
    }

    Optional<Element> element =
        zusammenAdaptor.getElementByName(context, elementContext, elementInfo.get().getId(),
            ElementType.OrchestrationTemplateValidationData.name());

    OrchestrationTemplateEntity orchestrationTemplate = new OrchestrationTemplateEntity();
    if (!element.isPresent()) {
      return orchestrationTemplate;
    }
    orchestrationTemplate
        .setFileSuffix(element.get().getInfo().getProperty(InfoPropertyName.fileSuffix.name()));
    orchestrationTemplate
        .setFileName(element.get().getInfo().getProperty(InfoPropertyName.fileName.name()));
    if (!hasEmptyData(element.get().getData())) {
      orchestrationTemplate
          .setValidationData(new String(FileUtils.toByteArray(element.get().getData())));
    }
    return orchestrationTemplate;
  }

  @Override
  public OrchestrationTemplateEntity get(String vspId, Version version) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    OrchestrationTemplateEntity orchestrationTemplate = new OrchestrationTemplateEntity();

    Optional<ElementInfo> vspModel = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null, ElementType.VspModel.name());
    if (!vspModel.isPresent()) {
      return orchestrationTemplate;
    }

    Optional<Element> orchestrationTemplateElement = zusammenAdaptor
        .getElementByName(context, elementContext, vspModel.get().getId(),
            ElementType.OrchestrationTemplate.name());
    if (!orchestrationTemplateElement.isPresent()) {
      return orchestrationTemplate;
    }

    if (!hasEmptyData(orchestrationTemplateElement.get().getData())) {
      orchestrationTemplate.setContentData(
          ByteBuffer.wrap(FileUtils.toByteArray(orchestrationTemplateElement.get().getData())));
    }

    Optional<Element> validationDataElement =
        zusammenAdaptor.getElementByName(context, elementContext,
            orchestrationTemplateElement.get().getElementId(),
            ElementType.OrchestrationTemplateValidationData.name());
    if (validationDataElement.isPresent()) {
      orchestrationTemplate.setFileSuffix(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.fileSuffix.name()));
      orchestrationTemplate.setFileName(validationDataElement.get().getInfo()
          .getProperty(InfoPropertyName.fileName.name()));
      if (!hasEmptyData(validationDataElement.get().getData())) {
        orchestrationTemplate.setValidationData(
            new String(FileUtils.toByteArray(validationDataElement.get().getData())));
      }
    }
    return orchestrationTemplate;
  }

  @Override
  public void update(String vspId, Version version,
                     OrchestrationTemplateEntity orchestrationTemplate) {
    SessionContext context = createSessionContext();
    ElementContext elementContext = new ElementContext(vspId, version.getId());

    ZusammenElement validationData =
        buildStructuralElement(ElementType.OrchestrationTemplateValidationData, Action.UPDATE);
    validationData
        .setData(new ByteArrayInputStream(orchestrationTemplate.getValidationData().getBytes()));
    validationData.getInfo()
        .addProperty(InfoPropertyName.fileSuffix.name(), orchestrationTemplate.getFileSuffix());
    validationData.getInfo()
        .addProperty(InfoPropertyName.fileName.name(), orchestrationTemplate.getFileName());

    ZusammenElement orchestrationTemplateElement =
        buildStructuralElement(ElementType.OrchestrationTemplate, Action.UPDATE);
    orchestrationTemplateElement
        .setData(new ByteArrayInputStream(orchestrationTemplate.getContentData().array()));
    orchestrationTemplateElement.addSubElement(validationData);

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(orchestrationTemplateElement);

    zusammenAdaptor.saveElement(context, elementContext, vspModel, "Update Orchestration Template");
  }

  private boolean hasEmptyData(InputStream elementData) {
    String emptyData = "{}";
    byte[] byteElementData;
    try {
      byteElementData = IOUtils.toByteArray(elementData);
    } catch (IOException ex) {
      ex.printStackTrace();
      return false;
    }
    if (Arrays.equals(emptyData.getBytes(), byteElementData)) {
      return true;
    }
    return false;
  }

  private enum InfoPropertyName {
    fileSuffix,
    fileName
  }
}
