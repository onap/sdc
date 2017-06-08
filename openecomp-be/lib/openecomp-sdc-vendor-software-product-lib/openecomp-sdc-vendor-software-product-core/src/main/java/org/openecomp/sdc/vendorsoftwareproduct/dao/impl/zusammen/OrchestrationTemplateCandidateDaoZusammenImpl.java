package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.utils.fileutils.FileUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateCandidateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionStatus;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.Optional;

public class OrchestrationTemplateCandidateDaoZusammenImpl
    implements OrchestrationTemplateCandidateDao {
  private static final Logger logger =
      LoggerFactory.getLogger(OrchestrationTemplateCandidateDaoZusammenImpl.class);

  private ZusammenAdaptor zusammenAdaptor;

  public OrchestrationTemplateCandidateDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    this.zusammenAdaptor = zusammenAdaptor;
  }

  @Override
  public void registerVersioning(String versionableEntityType) {

  }

  @Override
  public OrchestrationTemplateCandidateData get(String vspId, Version version) {
    logger.info("Getting orchestration template for vsp id -> " + vspId);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, versionId,
        VspZusammenUtil.getVersionTag(version));

    Optional<Element> candidateElement =
        zusammenAdaptor.getElementByName(context, elementContext, null,
            StructureElement.OrchestrationTemplateCandidate.name());
    if (candidateElement.isPresent()) {
      OrchestrationTemplateCandidateData candidateData = new OrchestrationTemplateCandidateData();
      candidateData.setFilesDataStructure(
          new String(FileUtils.toByteArray(candidateElement.get().getData())));

      zusammenAdaptor
          .getElementByName(context, elementContext, candidateElement.get().getElementId(),
              StructureElement.OrchestrationTemplateCandidateContent.name())
          .ifPresent(candidateContentElement -> candidateData.setContentData(
              ByteBuffer.wrap(FileUtils.toByteArray(candidateContentElement.getData()))));
      logger.info("Finished getting orchestration template for vsp id -> " + vspId);
      return candidateData;
    }
    logger.info(String.format("Orchestration template for vsp id %s does not exist", vspId));
    return null;
  }

  @Override
  public void update(String vspId, OrchestrationTemplateCandidateData candidateData) {
    logger.info("Uploading candidate data entity for vsp id -> " + vspId);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, versionId);

    ZusammenElement candidateElement = VspZusammenUtil
        .buildStructuralElement(StructureElement.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(candidateData.getFilesDataStructure().getBytes()));
    ZusammenElement candidateContentElement = VspZusammenUtil
        .buildStructuralElement(StructureElement.OrchestrationTemplateCandidateContent, Action.UPDATE);
    candidateContentElement
        .setData(new ByteArrayInputStream(candidateData.getContentData().array()));
    candidateElement.addSubElement(candidateContentElement);

    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate");
    logger.info("Finished uploading candidate data entity for vsp id -> " + vspId);
  }

  @Override
  public void updateStructure(String vspId, Version version, FilesDataStructure fileDataStructure) {
    logger.info("Updating orchestration template for VSP id -> " + vspId);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, versionId);

    ZusammenElement candidateElement = VspZusammenUtil
        .buildStructuralElement(StructureElement.OrchestrationTemplateCandidate, Action.UPDATE);
    candidateElement
        .setData(new ByteArrayInputStream(JsonUtil.object2Json(fileDataStructure).getBytes()));
    zusammenAdaptor.saveElement(context, elementContext, candidateElement,
        "Update Orchestration Template Candidate structure");
    logger.info("Finished uploading candidate data entity for vsp id -> " + vspId);
  }


  @Override
  public Optional<String> getStructure(String vspId, Version version) {
    logger.info("Getting orchestration template structure for vsp id -> " + vspId);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(vspId);
    Id versionId = VspZusammenUtil.getFirstVersionId(context, itemId, zusammenAdaptor);
    ElementContext elementContext = new ElementContext(itemId, versionId,
        VspZusammenUtil.getVersionTag(version));

    logger.info("Finished getting orchestration template structure for vsp id -> " + vspId);
    Optional<Element> element = zusammenAdaptor.getElementByName(context, elementContext, null,
        StructureElement.OrchestrationTemplateCandidate.name());
    if (element.isPresent()) {
      return Optional.of(new String(FileUtils.toByteArray(element.get().getData())));
    } else {
      return Optional.empty();
    }

  }
}
