package org.openecomp.sdc.enrichment.impl.external.artifact;

import org.openecomp.core.enrichment.types.ArtifactCategory;
import org.openecomp.core.enrichment.types.ComponentProcessInfo;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceModelDaoFactory;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.inter.ExternalArtifactEnricherInterface;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ProcessDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessType;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProcessArtifactEnricher implements ExternalArtifactEnricherInterface {

  private VendorSoftwareProductDao vendorSoftwareProductDao;
  //private ProcessArtifactDao processArtifactDao;
  private ProcessDao processDao;
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private EnrichedServiceModelDao enrichedServiceModelDao;

  @Override
  public Map<String, List<ErrorMessage>> enrich(EnrichmentInfo enrichmentInfo) throws IOException {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    String vspId = enrichmentInfo.getKey();
    Version version = enrichmentInfo.getVersion();

    Collection<ComponentEntity> components =
        getVendorSoftwareProductDao().listComponents(vspId, version);
    components.stream()
        .forEach(componentEntry -> errors.putAll(enrichComponent(componentEntry,
            vspId, version)));

    return errors;
  }

  Map<String, List<ErrorMessage>> enrichComponent(ComponentEntity componentEntry, String vspId,
                                                  Version version) {
    mdcDataDebugMessage.debugEntryMessage("LifeCycleOperationArtifactEnricher vspId ",
        vspId);

    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    enrichComponentProcessArtifact(componentEntry, vspId, version, errors);

    mdcDataDebugMessage.debugExitMessage("LifeCycleOperationArtifactEnricher vspId ",
        vspId);
    return errors;
  }

  void enrichComponentProcessArtifact(ComponentEntity componentEntity,
                                      String vspId, Version version,
                                      Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    String componentId = componentEntity.getId();
    ProcessEntity processEntity = new ProcessEntity(vspId, version, componentId, null);
    final Collection<ProcessEntity> processes = getProcessDao().list(processEntity);

    /*processes.stream()
        .filter(entity -> entity.getType().equals(ProcessType.Lifecycle_Operations))
        .forEach(entity -> {
          ProcessArtifactEntity artifactEntity = new ProcessArtifactEntity(vspId, version,
                  componentId, entity.getId());*/

    processes.stream()
        .forEach(entity -> {
          ProcessEntity artifactEntity = new ProcessEntity(vspId, version,
              componentId, entity.getId());

          ProcessEntity artifactProcessEntity = getProcessDao().get(artifactEntity);
          //ProcessArtifactEntity artifact = getProcessArtifactDao().get(artifactEntity);
          if (artifactProcessEntity != null && ProcessType.Lifecycle_Operations.equals(
              artifactProcessEntity.getType())
              && artifactProcessEntity.getArtifactName() != null ) {
            String componentName = componentEntity.getComponentCompositionData().getName();
            String path = componentName + File.separator
                + ArtifactCategory.DEPLOYMENT.getDisplayName() + File.separator
                + "Lifecycle Operations" + File.separator + artifactProcessEntity.getArtifactName();

            ComponentProcessInfo componentProcessInfo = new ComponentProcessInfo();
            componentProcessInfo.setName(path);
            componentProcessInfo.setContent(artifactProcessEntity.getArtifact().array());

            ServiceArtifact processServiceArtifact = new ServiceArtifact();
            processServiceArtifact.setVspId(vspId);
            processServiceArtifact.setVersion(version);
            enrichServiceArtifact(componentProcessInfo, processServiceArtifact, errors);
          }
        });

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  void enrichServiceArtifact(ComponentProcessInfo componentProcessInfo,
                             ServiceArtifact processServiceArtifact,
                             Map<String, List<ErrorMessage>> errors) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    processServiceArtifact.setName(componentProcessInfo.getName());
    processServiceArtifact.setContentData(FileUtils.toByteArray(componentProcessInfo.getContent()));
    getEnrichedServiceModelDao().storeExternalArtifact(processServiceArtifact);
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private VendorSoftwareProductDao getVendorSoftwareProductDao() {
    if (vendorSoftwareProductDao == null) {
      vendorSoftwareProductDao = VendorSoftwareProductDaoFactory.getInstance().createInterface();
    }
    return vendorSoftwareProductDao;
  }

  private ProcessDao getProcessDao() {
    if (processDao == null) {
      processDao = ProcessDaoFactory.getInstance().createInterface();
    }
    return processDao;
  }

  private EnrichedServiceModelDao getEnrichedServiceModelDao() {

    if (enrichedServiceModelDao == null) {
      enrichedServiceModelDao = EnrichedServiceModelDaoFactory.getInstance().createInterface();
    }
    return enrichedServiceModelDao;
  }
}
