package org.openecomp.sdc.model.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

public class EnrichedServiceModelDaoZusammenImpl extends ServiceModelDaoZusammenImpl implements
    EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> {

  private static final Logger logger =
      LoggerFactory.getLogger(EnrichedServiceModelDaoZusammenImpl.class);

  public EnrichedServiceModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    super(zusammenAdaptor);
    this.name = StructureElement.EnrichedServiceModel.name();
  }

  @Override
  public void storeExternalArtifact(ServiceArtifact serviceArtifact) {
    ZusammenElement artifactElement = buildArtifactElement(serviceArtifact.getName(),
        FileUtils.toByteArray(serviceArtifact.getContent()), Action.CREATE);

    ZusammenElement artifactsElement =
        buildStructuralElement(StructureElement.Artifacts.name(), null);
    artifactsElement.addSubElement(artifactElement);

    ZusammenElement enrichedServiceModelElement = buildStructuralElement(name, null);
    enrichedServiceModelElement.addSubElement(artifactsElement);

    SessionContext context = ZusammenUtil.createSessionContext();
    Id itemId = new Id(serviceArtifact.getVspId());
    ElementContext elementContext = new ElementContext(itemId, getFirstVersionId(context, itemId));
    zusammenAdaptor
        .saveElement(context, elementContext, enrichedServiceModelElement, "add service artifact.");

    logger.info("Finished adding artifact to service model for vsp id -> " +
        elementContext.getItemId().getValue());
  }
}
