package org.openecomp.sdc.vendorsoftwareproduct.services;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.generator.datatypes.tosca.ComputeFlavor;
import org.openecomp.sdc.generator.datatypes.tosca.DeploymentFlavorModel;
import org.openecomp.sdc.generator.datatypes.tosca.LicenseFlavor;
import org.openecomp.sdc.generator.datatypes.tosca.MultiFlavorVfcImage;
import org.openecomp.sdc.generator.datatypes.tosca.VendorInfo;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.DeploymentFlavorDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.NicDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentComputeAssociation;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComponentData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.DeploymentFlavor;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Nic;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.compute.Compute;
import org.openecomp.sdc.vendorsoftwareproduct.types.questionnaire.component.image.ImageDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class ManualVspDataCollectionService {

  private static final VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static final DeploymentFlavorDao deploymentFlavorDao =
      DeploymentFlavorDaoFactory.getInstance().createInterface();
  private static final ComputeDao computeDao =
      ComputeDaoFactory.getInstance().createInterface();
  private static final ImageDao imageDao =
      ImageDaoFactory.getInstance().createInterface();
  private static final ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();

  private static final NicDao nicDao =
      NicDaoFactory.getInstance().createInterface();
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();

  private final Logger log = (Logger) LoggerFactory.getLogger(this.getClass().getName());

  /**
   * Gets vendor name for the vsp.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the release vendor name
   */
  public Optional<String> getReleaseVendor(String vspId, Version version) {
    String vendorName = null;
    VspDetails vspDetails = vendorSoftwareProductInfoDao.get(new VspDetails(vspId, version));
    if (Objects.nonNull(vspDetails)) {
      vendorName = vspDetails.getVendorName();
    }
    return Optional.ofNullable(vendorName);
  }

  /**
   * Gets the deployment flavor data for manually onboarded VSPs.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the allowed flavors
   */
  public Map<String, DeploymentFlavorModel> getAllowedFlavors(String vspId, Version version) {
    Map<String, DeploymentFlavorModel> allowedFlavors = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    if (CollectionUtils.isNotEmpty(deploymentFlavorEntities)) {
      for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
        DeploymentFlavor deploymentFlavorCompositionData =
            deploymentFlavorEntity.getDeploymentFlavorCompositionData();

        VspDetails vspDetails = vendorSoftwareProductInfoDao.get(new VspDetails(vspId, version));
        String vspVlmId;
        Version vlmVersion;
        if (Objects.nonNull(vspDetails)) {
          vspVlmId = vspDetails.getVendorId();
          vlmVersion = vspDetails.getVlmVersion();
          if (StringUtils.isNotEmpty(vspVlmId)) {
            DeploymentFlavorModel deploymentFlavorModel = new DeploymentFlavorModel();
            String featureGroupId = deploymentFlavorCompositionData.getFeatureGroupId();
            if (Objects.isNull(featureGroupId)) {
              //No feature group associated with deployment flavor. So excluding this deployment
              // flavor for Tosca model
              continue;
            }
            //Gather and set License flavor info
            LicenseFlavor licenseFlavor = getLicenseFlavor(featureGroupId);
            deploymentFlavorModel.setLicense_flavor(licenseFlavor);
            //Get sp_part_number
            Optional<String> partNumber = getPartNumber(vspVlmId, vlmVersion, featureGroupId);
            partNumber.ifPresent(deploymentFlavorModel::setSp_part_number);
            //Gather and set Vendor Info
            String vendorModel = deploymentFlavorCompositionData.getModel();
            Optional<VendorInfo> vendorInfo = getVendorInfo(vspVlmId, vendorModel, vlmVersion,
                featureGroupId);
            vendorInfo.ifPresent(deploymentFlavorModel::setVendor_info);
            //Gather and set Compute info
            List<ComponentComputeAssociation> componentComputeAssociations =
                deploymentFlavorCompositionData.getComponentComputeAssociations();
            if (CollectionUtils.isNotEmpty(componentComputeAssociations)) {
              for (ComponentComputeAssociation componentComputeAssociation :
                  componentComputeAssociations) {
                String componentId = componentComputeAssociation.getComponentId();
                String computeFlavorId = componentComputeAssociation.getComputeFlavorId();
                Optional<ComputeFlavor> computeFlavor =
                    getComputeFlavor(vspId, version, componentId, computeFlavorId);
                computeFlavor.ifPresent(deploymentFlavorModel::setCompute_flavor);
              }
            }
            partNumber.ifPresent(spPartNumber -> allowedFlavors.put(spPartNumber,
                deploymentFlavorModel));
          }
        }
      }
    }
    return allowedFlavors;
  }

  /**
   * Gets the component image data for manually onboarded VSPs.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the vsp component images
   */
  public Map<String, List<MultiFlavorVfcImage>> getVspComponentImages(String vspId,
                                                                      Version version) {
    Map<String, List<MultiFlavorVfcImage>> vspComponentImages = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
      DeploymentFlavor deploymentFlavorCompositionData =
          deploymentFlavorEntity.getDeploymentFlavorCompositionData();

      List<ComponentComputeAssociation> componentComputeAssociations =
          deploymentFlavorCompositionData.getComponentComputeAssociations();
      if (CollectionUtils.isNotEmpty(componentComputeAssociations)) {
        for (ComponentComputeAssociation componentComputeAssociation :
            componentComputeAssociations) {
          String componentId = componentComputeAssociation.getComponentId();
          List<MultiFlavorVfcImage> componentImages =
              getComponentImages(vspId, version, componentId);
          if (CollectionUtils.isNotEmpty(componentImages)) {
            vspComponentImages.put(componentId, componentImages);
          }
        }
      }
    }
    return vspComponentImages;
  }

  /**
   * Gets the component data for manually onboarded VSPs.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the vsp components
   */
  public Map<String, String> getVspComponents(String vspId, Version version) {
    Map<String, String> componentIdNameMap = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
      DeploymentFlavor deploymentFlavorCompositionData =
          deploymentFlavorEntity.getDeploymentFlavorCompositionData();

      List<ComponentComputeAssociation> componentComputeAssociations =
          deploymentFlavorCompositionData.getComponentComputeAssociations();
      if (CollectionUtils.isNotEmpty(componentComputeAssociations)) {
        for (ComponentComputeAssociation componentComputeAssociation :
            componentComputeAssociations) {
          String componentId = componentComputeAssociation.getComponentId();
          Optional<String> componentName = getComponentName(vspId, version, componentId);
          componentName.ifPresent(name -> componentIdNameMap.put(componentId, name));
        }
      }
    }
    return componentIdNameMap;
  }

  /**
   * Gets the NIC data for manually onboarded VSPs.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @return the vsp component nics
   */
  public Map<String, List<Nic>> getVspComponentNics(String vspId, Version version) {
    Map<String, List<Nic>> vspComponentNics = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorDao.list(new DeploymentFlavorEntity(vspId, version, null));
    if (CollectionUtils.isNotEmpty(deploymentFlavorEntities)) {
      for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
        DeploymentFlavor deploymentFlavorCompositionData =
            deploymentFlavorEntity.getDeploymentFlavorCompositionData();
        if (Objects.nonNull(deploymentFlavorCompositionData)) {
          List<ComponentComputeAssociation> componentComputeAssociations =
              deploymentFlavorCompositionData.getComponentComputeAssociations();
          if (CollectionUtils.isNotEmpty(componentComputeAssociations)) {
            for (ComponentComputeAssociation componentComputeAssociation :
                componentComputeAssociations) {
              String componentId = componentComputeAssociation.getComponentId();
              List<Nic> componentNics = getComponentNics(vspId, version, componentId);
              if (CollectionUtils.isNotEmpty(componentNics)) {
                vspComponentNics.put(componentId, componentNics);
              }
            }
          }
        }
      }
    }
    return vspComponentNics;
  }

  private List<Nic> getComponentNics(String vspId, Version version, String componentId) {
    List<Nic> componentNics = new ArrayList<>();
    Collection<NicEntity> nics = nicDao.list(new NicEntity(vspId, version, componentId, null));
    if (Objects.nonNull(nics)) {
      for (NicEntity nic : nics) {
        String nicId = nic.getId();
        NicEntity nicEntity = nicDao.get(new NicEntity(vspId, version, componentId, nicId));
        if (Objects.nonNull(nicEntity)
            && Objects.nonNull(nicEntity.getCompositionData())) {
          componentNics.add(nicEntity.getNicCompositionData());
        }
      }
    }
    return componentNics;
  }

  private LicenseFlavor getLicenseFlavor(String featureGroupId) {
    LicenseFlavor licenseFlavor = new LicenseFlavor();
    licenseFlavor.setFeature_group_uuid(featureGroupId);
    return licenseFlavor;
  }

  private Optional<String> getPartNumber(String vlmId, Version version,
                                         String featureGroupId) {
    FeatureGroupModel featureGroup = getFeatureGroup(vlmId, version, featureGroupId);
    if (Objects.nonNull(featureGroup)) {
      return Optional.ofNullable(featureGroup.getFeatureGroup().getPartNumber());
    }
    return Optional.empty();
  }

  private Optional<VendorInfo> getVendorInfo(String vlmId, String vendorModel, Version version,
                                             String featureGroupId) {
    VendorInfo vendorInfo = null;
    FeatureGroupModel featureGroup = getFeatureGroup(vlmId, version, featureGroupId);
    if (Objects.nonNull(featureGroup)) {
      //Process Feature group to get Manufacturer ref no.
      String manufacturerReferenceNumber = featureGroup.getEntityManufacturerReferenceNumber();
      vendorInfo = new VendorInfo();
      vendorInfo.setVendor_model(vendorModel);
      if (Objects.nonNull(manufacturerReferenceNumber)) {
        vendorInfo.setManufacturer_reference_number(manufacturerReferenceNumber);
      }
    }
    return Optional.ofNullable(vendorInfo);
  }

  private Optional<ComputeFlavor> getComputeFlavor(String vspId, Version version,
                                                   String componentId, String computeFlavorId) {
    ComputeFlavor computeFlavor = null;
    ComputeEntity computeQuestionnaire = null;
    try {
      computeQuestionnaire = computeDao.getQuestionnaireData(vspId, version, componentId,
          computeFlavorId);
    } catch (Exception ex) {
      log.debug("", ex);
      computeQuestionnaire = null;
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
          LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(), "Failed to get compute questionnaire : "
              + ex.getMessage());
    }
    if (Objects.nonNull(computeQuestionnaire)) {
      String computeQuestionnaireData = computeQuestionnaire.getQuestionnaireData();
      if (Objects.nonNull(computeQuestionnaireData)) {
        Compute compute;
        try {
          compute = JsonUtil.json2Object(computeQuestionnaireData, Compute.class);
        } catch (Exception ex) {
          log.debug("", ex);
          compute = null;
          MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
              LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
              LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to parse compute questionnaire : "
                  + ex.getMessage());
        }
        if (compute != null && Objects.nonNull(compute.getVmSizing())) {
          computeFlavor = new ComputeFlavor();
          if (Objects.nonNull(compute.getVmSizing().getNumOfCPUs())) {
            computeFlavor.setNum_cpus(compute.getVmSizing().getNumOfCPUs());
          }
          if (Objects.nonNull(compute.getVmSizing().getFileSystemSizeGB())) {
            computeFlavor.setDisk_size(compute.getVmSizing().getFileSystemSizeGB() + " GB");
          }
          if (Objects.nonNull(compute.getVmSizing().getMemoryRAM())) {
            computeFlavor.setMem_size(compute.getVmSizing().getMemoryRAM());
          }
        }
      }
    }
    return Optional.ofNullable(computeFlavor);
  }

  private FeatureGroupModel getFeatureGroup(String vlmId, Version version, String featureGroupId) {
    FeatureGroupEntity fgInput = new FeatureGroupEntity();
    fgInput.setVendorLicenseModelId(vlmId);
    fgInput.setVersion(version);
    fgInput.setId(featureGroupId);
    return vendorLicenseFacade.getFeatureGroupModel(fgInput);
  }

  private Optional<String> getComponentName(String vspId, Version version, String componentId) {

    ComponentEntity componentEntity =
        componentDao.get(new ComponentEntity(vspId, version, componentId));
    if (Objects.nonNull(componentEntity)
        && Objects.nonNull(componentEntity.getComponentCompositionData())) {
      ComponentData componentCompositionData = componentEntity.getComponentCompositionData();
      return Optional.ofNullable(componentCompositionData.getDisplayName());
    }
    return Optional.empty();
  }

  private List<MultiFlavorVfcImage> getComponentImages(String vspId, Version version,
                                                       String componentId) {
    List<MultiFlavorVfcImage> multiFlavorVfcImages = new ArrayList<>();
    MultiFlavorVfcImage multiFlavorVfcImage;
    Collection<ImageEntity> componentImages =
        imageDao.list(new ImageEntity(vspId, version, componentId, null));
    if (Objects.nonNull(componentImages)) {
      for (ImageEntity componentImage : componentImages) {
        ImageEntity imageEntity = imageDao.get(componentImage);
        ImageEntity imageQuestionnaireDataEntity = imageDao.getQuestionnaireData(vspId, version,
            componentId, componentImage.getId());
        Image imageCompositionData = imageEntity.getImageCompositionData();
        if (Objects.nonNull(imageEntity)
            && Objects.nonNull(imageQuestionnaireDataEntity)
            && Objects.nonNull(imageCompositionData)) {
          ImageDetails imageDetails;
          try {
            imageDetails = JsonUtil.json2Object(imageQuestionnaireDataEntity
                .getQuestionnaireData(), ImageDetails.class);
          } catch (Exception ex) {
            log.debug("", ex);
            imageDetails = null;
            MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_API,
                LoggerTragetServiceName.COLLECT_MANUAL_VSP_TOSCA_DATA, ErrorLevel.INFO.name(),
                LoggerErrorCode.DATA_ERROR.getErrorCode(), "Unable to parse image questionnaire : "
                    + ex.getMessage());
          }
          if (Objects.nonNull(imageDetails)
              && Objects.nonNull(imageDetails.getVersion())) {
            //Image version is used as a key for the image block
            //So excluding the population if questionnaire data is absent or invalid
            multiFlavorVfcImage = new MultiFlavorVfcImage();
            multiFlavorVfcImage.setSoftware_version(imageDetails.getVersion());
            if (Objects.nonNull(imageCompositionData.getFileName())) {
              multiFlavorVfcImage.setFile_name(imageCompositionData.getFileName());
            }
            if (Objects.nonNull(imageDetails.getMd5())) {
              multiFlavorVfcImage.setFile_hash(imageDetails.getMd5());
            }
            multiFlavorVfcImage.setFile_hash_type("md5");
            multiFlavorVfcImages.add(multiFlavorVfcImage);
          }
        }
      }
    }
    return multiFlavorVfcImages;
  }
}
