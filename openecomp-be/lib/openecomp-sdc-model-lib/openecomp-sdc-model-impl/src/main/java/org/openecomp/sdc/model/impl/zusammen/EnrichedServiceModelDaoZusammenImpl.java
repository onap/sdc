package org.openecomp.sdc.model.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;
import org.openecomp.core.zusammen.api.ZusammenUtil;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;

public class EnrichedServiceModelDaoZusammenImpl extends ServiceModelDaoZusammenImpl implements
    EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> {

  private static final Logger logger =
      LoggerFactory.getLogger(EnrichedServiceModelDaoZusammenImpl.class);

  public EnrichedServiceModelDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
    super(zusammenAdaptor);
    this.elementType = ElementType.EnrichedServiceModel;
  }

  @Override
  public void storeExternalArtifact(ServiceArtifact serviceArtifact) {
    ZusammenElement artifact = buildArtifactElement(serviceArtifact.getName(),
        FileUtils.toByteArray(serviceArtifact.getContent()), Action.CREATE);

    ZusammenElement artifacts = buildStructuralElement(ElementType.Artifacts, Action.IGNORE);
    artifacts.addSubElement(artifact);

    ZusammenElement enrichedServiceModel = buildStructuralElement(elementType, Action.IGNORE);
    enrichedServiceModel.addSubElement(artifacts);

    ZusammenElement vspModel = buildStructuralElement(ElementType.VspModel, Action.IGNORE);
    vspModel.addSubElement(enrichedServiceModel);

    SessionContext context = ZusammenUtil.createSessionContext();
    ElementContext elementContext =
        new ElementContext(serviceArtifact.getVspId(), serviceArtifact.getVersion().getId());
    zusammenAdaptor
        .saveElement(context, elementContext, vspModel, "add service external artifact.");

    logger.info(
        "Finished adding artifact to enriched service model for VendorSoftwareProduct id -> {}",
        elementContext.getItemId().getValue());
  }
}
