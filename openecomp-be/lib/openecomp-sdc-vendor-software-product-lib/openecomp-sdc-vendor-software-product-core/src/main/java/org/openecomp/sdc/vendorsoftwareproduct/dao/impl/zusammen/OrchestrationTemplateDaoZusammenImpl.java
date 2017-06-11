package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

public class OrchestrationTemplateDaoZusammenImpl implements OrchestrationTemplateDao {

  private ZusammenAdaptor zusammenAdaptor;

  public OrchestrationTemplateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public String getValidationData(String vspId, Version version) {
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Optional<ElementInfo> elementInfo = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null,
            StructureElement.OrchestrationTemplate.name());
    if (elementInfo.isPresent()) {
      Optional<Element> element =
          zusammenAdaptor.getElementByName(context, elementContext, elementInfo.get().getId(),
              StructureElement.OrchestrationTemplateValidationData.name());
      if (element.isPresent()) {
        return new String(FileUtils.toByteArray(element.get().getData()));
      }
    }

    return null;
  }

  @Override
  public UploadDataEntity getOrchestrationTemplate(String vspId, Version version) {

    UploadDataEntity uploadData = new UploadDataEntity();
    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor),
        VspZusammenUtil.getVersionTag(version));

    Optional<ElementInfo> elementInfo = zusammenAdaptor
        .getElementInfoByName(context, elementContext, null,
            StructureElement.OrchestrationTemplate.name());
    if (elementInfo.isPresent()) {
      Optional<Element> element =
          zusammenAdaptor.getElementByName(context, elementContext, elementInfo.get().getId(),
              StructureElement.OrchestrationTemplateValidationData.name());
      element.ifPresent(element1 -> uploadData
          .setValidationData(new String(FileUtils.toByteArray(element1.getData()))));
      element =
          zusammenAdaptor.getElementByName(context, elementContext, elementInfo.get().getId(),
              StructureElement.OrchestrationTemplateContent.name());
      element.ifPresent(element1 -> uploadData
          .setContentData(ByteBuffer.wrap(FileUtils.toByteArray(element1.getData()))));
    }
    return uploadData;
  }

  @Override
  public void updateOrchestrationTemplateData(String vspId, UploadData uploadData) {
    ZusammenElement orchestrationTemplateElement =
        VspZusammenUtil.buildStructuralElement(StructureElement.OrchestrationTemplate, null);
    ZusammenElement orchestrationTemplateValidationDataElement =
        VspZusammenUtil
            .buildStructuralElement(StructureElement.OrchestrationTemplateValidationData, Action.UPDATE);
    orchestrationTemplateValidationDataElement.setData(new ByteArrayInputStream(uploadData
        .getValidationData().getBytes()));
    ZusammenElement orchestrationTemplateContent =
        VspZusammenUtil.buildStructuralElement(StructureElement.OrchestrationTemplateContent, Action.UPDATE);
    orchestrationTemplateContent
        .setData(new ByteArrayInputStream(uploadData.getContentData().array()));
    orchestrationTemplateElement.addSubElement(orchestrationTemplateValidationDataElement);
    orchestrationTemplateElement.addSubElement(orchestrationTemplateContent);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    ElementContext elementContext = new ElementContext(itemId,
        VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor));

    zusammenAdaptor.saveElement(context, elementContext, orchestrationTemplateElement, "Update " +
        "Orchestration Template");
  }

}
