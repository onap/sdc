package org.openecomp.core.migration;

import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.UserInfo;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.ItemCassandraDao;
import com.amdocs.zusammen.plugin.statestore.cassandra.dao.impl.VersionCassandraDao;
import org.openecomp.core.migration.convertors.ComponentConvertor;
import org.openecomp.core.migration.convertors.EntitlementPoolConvertor;
import org.openecomp.core.migration.convertors.FeatureGroupConvertor;
import org.openecomp.core.migration.convertors.LKGConvertor;
import org.openecomp.core.migration.convertors.LicenseAgreementConvertor;
import org.openecomp.core.migration.convertors.MibConvertor;
import org.openecomp.core.migration.convertors.NetworkConvertor;
import org.openecomp.core.migration.convertors.NicConvertor;
import org.openecomp.core.migration.convertors.OrchestrationTemplateCandidateConvertor;
import org.openecomp.core.migration.convertors.ProcessConvertor;
import org.openecomp.core.migration.convertors.VlmConvertor;
import org.openecomp.core.migration.convertors.VspInformationConvertor;
import org.openecomp.core.migration.convertors.VspServiceArtifactConvertor;
import org.openecomp.core.migration.convertors.VspServiceTemplateConvertor;
import org.openecomp.core.migration.loaders.ComponentCassandraLoader;
import org.openecomp.core.migration.loaders.EntitlementPoolCassandraLoader;
import org.openecomp.core.migration.loaders.FeatureGroupCassandraLoader;
import org.openecomp.core.migration.loaders.LKGCassandraLoader;
import org.openecomp.core.migration.loaders.LicenseAgreementCassandraLoader;
import org.openecomp.core.migration.loaders.MibCassandraLoader;
import org.openecomp.core.migration.loaders.NetworkCassandraLoader;
import org.openecomp.core.migration.loaders.NicCassandraLoader;
import org.openecomp.core.migration.loaders.OrchestrationTemplateCandidateCassandraLoader;
import org.openecomp.core.migration.loaders.ProcessCassandraLoader;
import org.openecomp.core.migration.loaders.ServiceArtifactCassandraLoader;
import org.openecomp.core.migration.loaders.ServiceTemplateCassandraLoader;
import org.openecomp.core.migration.loaders.VendorLicenseModelCassandraLoader;
import org.openecomp.core.migration.loaders.VendorSoftwareProductInfoLoader;
import org.openecomp.core.migration.loaders.VersionInfoCassandraLoader;
import org.openecomp.core.migration.loaders.VspInformation;
import org.openecomp.core.migration.store.ElementHandler;
import org.openecomp.core.migration.store.ItemHandler;
import org.openecomp.core.migration.util.marker.MigrationMarker;
import org.openecomp.core.model.types.ServiceArtifact;
import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.core.zusammen.plugin.dao.impl.CassandraElementRepository;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorlicense.dao.types.EntitlementPoolEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseAgreementEntity;
import org.openecomp.sdc.vendorlicense.dao.types.LicenseKeyGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.VendorLicenseModelEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.MibEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NetworkEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ProcessEntity;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.dao.types.VersionInfoEntity;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.openecomp.core.migration.util.Utils.printMessage;

public class MigrationMain {
  private static final String GLOBAL_USER = "GLOBAL_USER";
  private static Logger logger = LoggerFactory.getLogger(MigrationMain.class);
  private static int status = 0;

  public static Map<String, VersionInfoEntity> versionInfoMap = new HashMap<>();

  public static void main(String[] args) {
    CassandraElementRepository cassandraElementRepository = new CassandraElementRepository();
    CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();
    printMessage(logger, "Checking whether a migration has already been run.");
    if (MigrationMarker.isMigrated()) {
      printMessage(logger, "The DB has already been migrated, this script will now exit.");
      return;
    }
    ItemCassandraDao itemCassandraDao = new ItemCassandraDao();
    VersionCassandraDao versionCassandraDao = new VersionCassandraDao();
    SessionContext context = new SessionContext();
    context.setUser(new UserInfo(GLOBAL_USER));
    context.setTenant("dox");
    printMessage(logger, "Starting migration.\n");
    Instant startTime = Instant.now();

    migrateToZusammen(cassandraElementRepository, itemCassandraDao, versionCassandraDao, context);

    Instant stopTime = Instant.now();
    Duration duration = Duration.between(startTime, stopTime);
    long minutesPart = duration.toMinutes();
    long secondsPart = duration.minusMinutes(minutesPart).getSeconds();

    if (status == 0) {
      MigrationMarker.markMigrated();
    }
    printMessage(logger,
        "Migration finished . Total run time was : " + minutesPart + ":" + secondsPart
            + " minutes");
    System.exit(status);
  }


  private static void migrateToZusammen(CassandraElementRepository cassandraElementRepository,
                                        ItemCassandraDao itemCassandraDao,
                                        VersionCassandraDao versionCassandraDao,
                                        SessionContext context) {
    loadVersionInfo();


    try {
      convertVsp(context, itemCassandraDao, versionCassandraDao, cassandraElementRepository);
      printMessage(logger, "Converted VSPs\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for VSPs ,the error is :");
      e.printStackTrace();
      status = -1;
    }

    try {
      convertOrchestrationTemplateCandidate(context, cassandraElementRepository);
      printMessage(logger, "Converted OrchestrationTemplateCandidates\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for OrchestrationTemplateCandidates ,the error is :");
      e.printStackTrace();
      status = -1;
    }


    try {
      convertComponent(context, cassandraElementRepository);
      printMessage(logger, "Converted Components\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for Components ,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertNic(context, cassandraElementRepository);
      printMessage(logger, "Converted Nics\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for Nics ,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertNetwork(context, cassandraElementRepository);
      printMessage(logger, "Converted Networks\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for Networks ,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertMibs(context, cassandraElementRepository);
      printMessage(logger, "Converted MIBs\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for MIBs,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertServiceArtifact(context, cassandraElementRepository);
      printMessage(logger, "Converted Service Artifacts\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for Service Artifacts,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertServiceTemplate(context, cassandraElementRepository);
      printMessage(logger, "Converted Service Templates\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for Service Templates,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertProcesses(context, cassandraElementRepository);
      printMessage(logger, "Converted Processes\n");
    } catch (Exception e) {
      printMessage(logger,
          "Could not perform migration for Processes,the error is :");
      e.printStackTrace();
      status = -1;
    }


    try {
      convertVlm(context, itemCassandraDao, versionCassandraDao, cassandraElementRepository);
      printMessage(logger, "Converted VLMs\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for VLMs,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertLKG(context, cassandraElementRepository);
      printMessage(logger, "Converted LKGs\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for LKGs,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertFeatureGroup(context, cassandraElementRepository);
      printMessage(logger, "Converted Feature Groups\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for Feature Groups,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertEP(context, cassandraElementRepository);
      printMessage(logger, "Converted EPs\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for EPs,the error is :");
      e.printStackTrace();
      status = -1;
    }
    try {
      convertLicenseAgreement(context, cassandraElementRepository);
      printMessage(logger, "Converted License Agreements\n");
    } catch (Exception e) {
      printMessage(logger, "Could not perform migration for License Agreements,the error is :");
      e.printStackTrace();
      status = -1;
    }
  }

  private static void convertOrchestrationTemplateCandidate(SessionContext context,
                                                            CassandraElementRepository cassandraElementRepository) {
    OrchestrationTemplateCandidateCassandraLoader orchestrationTemplateCandidateCassandraLoader =
        new OrchestrationTemplateCandidateCassandraLoader();

    orchestrationTemplateCandidateCassandraLoader.list().stream()
        .filter(entity -> needMigration(entity.getId(), entity.getVersion()))
        .forEach(entity -> ElementHandler
            .save(context, cassandraElementRepository, entity.getId(), entity.getVersion(),
                OrchestrationTemplateCandidateConvertor
                    .convertOrchestrationTemplateCandidateToElement(entity)));
  }

  private static void loadVersionInfo() {

    VersionInfoCassandraLoader versionInfoCassandraLoader = new VersionInfoCassandraLoader();
    Collection<VersionInfoEntity> versions =
        versionInfoCassandraLoader.list();

    versions.forEach(versionInfoEntity -> versionInfoMap.put(versionInfoEntity.getEntityId
        (), versionInfoEntity));


  }

  private static void convertMibs(SessionContext context,
                                  CassandraElementRepository cassandraElementRepository) {
    MibCassandraLoader cassandraLoader = new MibCassandraLoader();
    Collection<MibEntity> mibs = cassandraLoader.list();
    mibs.stream().filter(mibEntity -> needMigration(mibEntity.getVspId(), mibEntity.getVersion()))
        .forEach
            (mibEntity -> {
              ElementHandler.save(context, cassandraElementRepository,
                  mibEntity
                      .getVspId(), mibEntity.getVersion(),
                  MibConvertor.convertMibToElement
                      (mibEntity));
            });
  }

  private static void convertProcesses(SessionContext context,
                                       CassandraElementRepository cassandraElementRepository) {
    ProcessCassandraLoader cassandraLoader = new ProcessCassandraLoader();
    Collection<ProcessEntity> processes = cassandraLoader.list();
    processes.stream()
        .filter(processEntity -> needMigration(processEntity.getVspId(), processEntity.getVersion
            ())).forEach(processEntity -> {
      ElementHandler.save(
          context,
          cassandraElementRepository,
          processEntity
              .getId(), processEntity.getVersion(),
          ProcessConvertor.convertProcessToElement(processEntity));
    });
  }

  private static void convertVsp(SessionContext context, ItemCassandraDao itemCassandraDao,
                                 VersionCassandraDao versionCassandraDao,
                                 CassandraElementRepository cassandraElementRepository) {
    VendorSoftwareProductInfoLoader vendorSoftwareProductInfoLoader = new
        VendorSoftwareProductInfoLoader();
    Collection<VspInformation> vsps =
        vendorSoftwareProductInfoLoader.list();
    vsps.stream().filter(vspInformation -> needMigration(vspInformation.getId(),
        vspInformation.getVersion())).forEach
        (vspInformation
            ->
            ItemHandler.save(context,
                itemCassandraDao,
                versionCassandraDao,
                vspInformation.getId(), vspInformation
                    .getVersion(),
                VspInformationConvertor
                    .getVspInfo
                        (vspInformation),
                VspInformationConvertor.getItemVersionData(vspInformation),
                vspInformation.getWritetimeMicroSeconds()));

    vsps.stream().filter(vspInformation -> needMigration(vspInformation.getId(),
        vspInformation.getVersion()))
        .forEach(vspInformation -> ElementHandler.save(context, cassandraElementRepository,
            vspInformation.getId(), vspInformation.getVersion(),
            VspInformationConvertor.convertVspToElement
                (vspInformation)));
  }

  private static void convertVlm(SessionContext context, ItemCassandraDao itemCassandraDao,
                                 VersionCassandraDao versionCassandraDao,
                                 CassandraElementRepository cassandraElementRepository) {
    VendorLicenseModelCassandraLoader
        vendorLicenseModelCassandraDao = new VendorLicenseModelCassandraLoader();
    Collection<VendorLicenseModelEntity> vlms =
        vendorLicenseModelCassandraDao.list();
    vlms.stream().filter(vlm -> needMigration(vlm.getId(), vlm.getVersion())).forEach(vlmEntity ->
        ItemHandler.save
            (context, itemCassandraDao,
                versionCassandraDao,
                vlmEntity.getId(), vlmEntity.getVersion(),
                VlmConvertor.getVlmInfo
                    (vlmEntity),
                VlmConvertor.getItemVersionData(vlmEntity), new Date().getTime()));
    vlms.stream().filter(vlm -> needMigration(vlm.getId(), vlm.getVersion()))
        .forEach(vlmEntity -> ElementHandler.save(context, cassandraElementRepository,
            vlmEntity.getId(), vlmEntity.getVersion(),
            VlmConvertor.convertVlmToElement
                (vlmEntity)));

  }

  private static void convertNic(SessionContext context,
                                 CassandraElementRepository cassandraElementRepository) {
    NicCassandraLoader nicCassandraLoader = new NicCassandraLoader();
    Collection<NicEntity> nics = nicCassandraLoader.list();
    nics.stream().filter(entity -> needMigration(entity.getVspId(), entity.getVersion
        ())).forEach(nicEntity -> ElementHandler.save(context, cassandraElementRepository,
        nicEntity.getVspId(), nicEntity.getVersion(), NicConvertor.convertNicToElement
            (nicEntity)));

  }

  private static void convertNetwork(SessionContext context,
                                     CassandraElementRepository cassandraElementRepository) {
    NetworkCassandraLoader networkCassandraLoader = new NetworkCassandraLoader();
    Collection<NetworkEntity> networks = networkCassandraLoader.list();
    networks.stream().filter(entity -> needMigration(entity.getVspId(), entity.getVersion
        ())).forEach(networkEntity -> ElementHandler.save(context, cassandraElementRepository,
        networkEntity.getVspId(), networkEntity.getVersion(), NetworkConvertor
            .convertNetworkToElement(networkEntity)));

  }

  private static void convertComponent(SessionContext context,
                                       CassandraElementRepository cassandraElementRepository) {
    ComponentCassandraLoader componentCassandraLoader = new ComponentCassandraLoader();
    Collection<ComponentEntity> components = componentCassandraLoader.list();
    components.stream().filter(entity -> needMigration(entity.getVspId(), entity.getVersion
        ())).forEach(componentEntity -> ElementHandler.save(context, cassandraElementRepository,
        componentEntity
            .getVspId(), componentEntity.getVersion(), ComponentConvertor
            .convertComponentToElement(componentEntity)));

  }

  private static void convertServiceArtifact(SessionContext context,
                                             CassandraElementRepository cassandraElementRepository) {
    ServiceArtifactCassandraLoader serviceArtifactCassandraLoader =
        new ServiceArtifactCassandraLoader();
    Collection<ServiceArtifact> serviceArtifacts = serviceArtifactCassandraLoader.list();
    serviceArtifacts.stream().filter(entity -> needMigration(entity.getVspId(), entity.getVersion
        ())).forEach(serviceArtifact -> ElementHandler.save(context,
        cassandraElementRepository,
        serviceArtifact
            .getVspId(), serviceArtifact.getVersion(),
        VspServiceArtifactConvertor
            .convertServiceArtifactToElement(serviceArtifact)));

  }

  private static void convertServiceTemplate(SessionContext context,
                                             CassandraElementRepository cassandraElementRepository) {
    ServiceTemplateCassandraLoader serviceTemplateCassandraLoader =
        new ServiceTemplateCassandraLoader();
    Collection<ServiceTemplate> serviceTemplates = serviceTemplateCassandraLoader.list();
    serviceTemplates.stream().filter(entity -> needMigration(entity.getVspId(), entity.getVersion
        ())).forEach(serviceTemplate -> ElementHandler.save(context,
        cassandraElementRepository,
        serviceTemplate
            .getVspId(), serviceTemplate.getVersion(),
        VspServiceTemplateConvertor
            .convertServiceTemplateToElement(serviceTemplate)));

  }

  private static void convertLKG(SessionContext context,
                                 CassandraElementRepository cassandraElementRepository) {
    LKGCassandraLoader LKGCassandraLoader = new LKGCassandraLoader();
    Collection<LicenseKeyGroupEntity> lkgs = LKGCassandraLoader.list();
    lkgs.stream().filter(entity -> needMigration(entity.getVendorLicenseModelId(), entity.getVersion
        ()))
        .forEach(licenseKeyGroupEntity -> ElementHandler.save(context, cassandraElementRepository,
            licenseKeyGroupEntity
                .getVendorLicenseModelId(), licenseKeyGroupEntity.getVersion(),
            LKGConvertor.convertLKGToElement
                (licenseKeyGroupEntity)));
  }

  private static void convertEP(SessionContext context,
                                CassandraElementRepository cassandraElementRepository) {
    EntitlementPoolCassandraLoader entitlementPoolCassandraLoader =
        new EntitlementPoolCassandraLoader();
    Collection<EntitlementPoolEntity> entitlementPools = entitlementPoolCassandraLoader.list();
    entitlementPools.stream()
        .filter(entity -> needMigration(entity.getVendorLicenseModelId(), entity.getVersion
            ()))
        .forEach(entitlementPoolEntity -> ElementHandler.save(context, cassandraElementRepository,
            entitlementPoolEntity
                .getVendorLicenseModelId(), entitlementPoolEntity.getVersion(),
            EntitlementPoolConvertor.convertEntitlementPoolToElement(entitlementPoolEntity)));
  }

  private static void convertFeatureGroup(SessionContext context,
                                          CassandraElementRepository cassandraElementRepository) {
    FeatureGroupCassandraLoader featureGroupCassandraLoader = new FeatureGroupCassandraLoader();
    Collection<FeatureGroupEntity> featureGroupEntities = featureGroupCassandraLoader.list();
    featureGroupEntities.stream()
        .filter(entity -> needMigration(entity.getVendorLicenseModelId(), entity.getVersion
            ()))
        .forEach(featureGroupEntity -> ElementHandler.save(context, cassandraElementRepository,
            featureGroupEntity
                .getVendorLicenseModelId(), featureGroupEntity.getVersion(),
            FeatureGroupConvertor.convertFeatureGroupToElement(featureGroupEntity)));
  }

  private static void convertLicenseAgreement(SessionContext context,
                                              CassandraElementRepository cassandraElementRepository) {
    LicenseAgreementCassandraLoader licenseAgreementCassandraLoader =
        new LicenseAgreementCassandraLoader();
    Collection<LicenseAgreementEntity> licenseAgreementEntities =
        licenseAgreementCassandraLoader.list();
    licenseAgreementEntities.stream()
        .filter(entity -> needMigration(entity.getVendorLicenseModelId(), entity.getVersion
            ()))
        .forEach(licenseAgreementEntity -> ElementHandler.save(context, cassandraElementRepository,
            licenseAgreementEntity
                .getVendorLicenseModelId(), licenseAgreementEntity.getVersion(),
            LicenseAgreementConvertor.convertLicenseAgreementToElement(licenseAgreementEntity)));
  }


  private static boolean needMigration(String itemId, Version versionId) {

    VersionInfoEntity versionInfo =
        MigrationMain.versionInfoMap.get(itemId);
    if (versionInfo == null) {
      printMessage(logger, "ItemId: " + itemId + " is not in version_info table.");
      return false;
    }
    return (versionInfo.getCandidate() != null && versionId.equals(versionInfo.getCandidate()
        .getVersion()))
        || (versionInfo
        .getViewableVersions() != null && versionInfo
        .getViewableVersions().contains(versionId));
  }


}
