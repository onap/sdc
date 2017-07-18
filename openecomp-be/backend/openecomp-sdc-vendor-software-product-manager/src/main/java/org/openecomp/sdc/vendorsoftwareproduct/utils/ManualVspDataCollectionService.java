package org.openecomp.sdc.vendorsoftwareproduct.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.generator.datatypes.tosca.ComputeFlavor;
import org.openecomp.sdc.generator.datatypes.tosca.DeploymentFlavorModel;
import org.openecomp.sdc.generator.datatypes.tosca.LicenseFlavor;
import org.openecomp.sdc.generator.datatypes.tosca.MultiFlavorVfcImage;
import org.openecomp.sdc.generator.datatypes.tosca.VendorInfo;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupEntity;
import org.openecomp.sdc.vendorlicense.dao.types.FeatureGroupModel;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacade;
import org.openecomp.sdc.vendorlicense.facade.VendorLicenseFacadeFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComponentManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManager;
import org.openecomp.sdc.vendorsoftwareproduct.ComputeManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManager;
import org.openecomp.sdc.vendorsoftwareproduct.DeploymentFlavorManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManager;
import org.openecomp.sdc.vendorsoftwareproduct.ImageManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.NicManager;
import org.openecomp.sdc.vendorsoftwareproduct.NicManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.VendorSoftwareProductManager;
import org.openecomp.sdc.vendorsoftwareproduct.VspManagerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.DeploymentFlavorEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.NicEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
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

  private static final DeploymentFlavorManager deploymentFlavorManager =
      DeploymentFlavorManagerFactory.getInstance().createInterface();
  private static final ComputeManager computeManager =
      ComputeManagerFactory.getInstance().createInterface();
  private static final ImageManager imageManager =
      ImageManagerFactory.getInstance().createInterface();
  private static final ComponentManager componentManager =
      ComponentManagerFactory.getInstance().createInterface();
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      VspManagerFactory.getInstance().createInterface();
  private static final NicManager nicManager =
      NicManagerFactory.getInstance().createInterface();
  private static final VendorLicenseFacade vendorLicenseFacade =
      VendorLicenseFacadeFactory.getInstance().createInterface();


  /**
   * Gets vendor name for the vsp.
   *
   * @param vspId   the vsp id
   * @param version the version
   * @param user    the user
   * @return the release vendor name
   */
  public Optional<String> getReleaseVendor(String vspId, Version version, String user) {
    String vendorName = null;
    VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version, user);
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
   * @param user    the user
   * @return the allowed flavors
   */
  public Map<String, DeploymentFlavorModel> getAllowedFlavors(String vspId, Version version,
                                                              String user) {
    Map<String, DeploymentFlavorModel> allowedFlavors = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(vspId, version, user);
    if (CollectionUtils.isNotEmpty(deploymentFlavorEntities)) {
      for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
        DeploymentFlavor deploymentFlavorCompositionData =
            deploymentFlavorEntity.getDeploymentFlavorCompositionData();

        VspDetails vspDetails = vendorSoftwareProductManager.getVsp(vspId, version, user);
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
            Optional<String> partNumber = getPartNumber(vspVlmId, vlmVersion, featureGroupId,
                user);
            partNumber.ifPresent(deploymentFlavorModel::setSp_part_number);
            //Gather and set Vendor Info
            Optional<VendorInfo> vendorInfo = getVendorInfo(vspVlmId, vlmVersion, featureGroupId,
                user);
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
                    getComputeFlavor(vspId, version, componentId, computeFlavorId, user);
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
   * @param user    the user
   * @return the vsp component images
   */
  public Map<String, List<MultiFlavorVfcImage>> getVspComponentImages(String vspId,
                                                                      Version version,
                                                                      String user) {
    Map<String, List<MultiFlavorVfcImage>> vspComponentImages = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(vspId, version, user);
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
              getComponentImages(vspId, version, componentId, user);
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
   * @param user    the user
   * @return the vsp components
   */
  public Map<String, String> getVspComponents(String vspId, Version version, String user) {
    Map<String, String> componentIdNameMap = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(vspId, version, user);
    for (DeploymentFlavorEntity deploymentFlavorEntity : deploymentFlavorEntities) {
      DeploymentFlavor deploymentFlavorCompositionData =
          deploymentFlavorEntity.getDeploymentFlavorCompositionData();

      List<ComponentComputeAssociation> componentComputeAssociations =
          deploymentFlavorCompositionData.getComponentComputeAssociations();
      if (CollectionUtils.isNotEmpty(componentComputeAssociations)) {
        for (ComponentComputeAssociation componentComputeAssociation :
            componentComputeAssociations) {
          String componentId = componentComputeAssociation.getComponentId();
          Optional<String> componentName = getComponentName(vspId, version, componentId, user);
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
   * @param user    the user
   * @return the vsp component nics
   */
  public Map<String, List<Nic>> getVspComponentNics(String vspId, Version version, String user) {
    Map<String, List<Nic>> vspComponentNics = new HashMap<>();
    Collection<DeploymentFlavorEntity> deploymentFlavorEntities =
        deploymentFlavorManager.listDeploymentFlavors(vspId, version, user);
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
              List<Nic> componentNics = getComponentNics(vspId, version, componentId, user);
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

  private List<Nic> getComponentNics(String vspId, Version version, String componentId,
                                     String user) {
    List<Nic> componentNics = new ArrayList<>();
    Collection<NicEntity> nics = nicManager.listNics(vspId, version, componentId,  user);
    if (Objects.nonNull(nics)) {
      for (NicEntity nicEntity : nics) {
        String nicId = nicEntity.getId();
        CompositionEntityResponse<Nic> nicCompositionEntityResponse =
            nicManager.getNic(vspId, version, componentId, nicId, user);
        if (Objects.nonNull(nicCompositionEntityResponse)
            && Objects.nonNull(nicCompositionEntityResponse.getData())) {
          componentNics.add(nicCompositionEntityResponse.getData());
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
                                         String featureGroupId, String user) {
    FeatureGroupModel featureGroup = getFeatureGroup(vlmId, version, featureGroupId, user);
    if (Objects.nonNull(featureGroup)) {
      return Optional.ofNullable(featureGroup.getFeatureGroup().getPartNumber());
    }
    return Optional.empty();
  }

  private Optional<VendorInfo> getVendorInfo(String vlmId, Version version,
                                             String featureGroupId, String user) {
    VendorInfo vendorInfo = null;
    FeatureGroupModel featureGroup = getFeatureGroup(vlmId, version, featureGroupId, user);
    if (Objects.nonNull(featureGroup)) {
      //Process Feature group to get Manufacturer ref no.
      String manufacturerReferenceNumber = featureGroup.getEntityManufacturerReferenceNumber();
      vendorInfo = new VendorInfo();
      vendorInfo.setVendor_model(vlmId);
      if (Objects.nonNull(manufacturerReferenceNumber)) {
        vendorInfo.setManufacturer_reference_number(manufacturerReferenceNumber);
      }
    }
    return Optional.ofNullable(vendorInfo);
  }

  private Optional<ComputeFlavor> getComputeFlavor(String vspId, Version version,
                                                   String componentId, String computeFlavorId,
                                                   String user) {
    ComputeFlavor computeFlavor = null;
    QuestionnaireResponse computeQuestionnaire;
    try {
      computeQuestionnaire = computeManager.getComputeQuestionnaire(vspId, version, componentId,
          computeFlavorId, user);
    } catch (Exception ex) {
      computeQuestionnaire = null;
    }
    if (Objects.nonNull(computeQuestionnaire)) {
      String computeQuestionnaireData = computeQuestionnaire.getData();
      if (Objects.nonNull(computeQuestionnaireData)) {
        Compute compute;
        try {
          compute = JsonUtil.json2Object(computeQuestionnaireData, Compute.class);
        } catch (Exception ex) {
          compute = null;
        }
        if (Objects.nonNull(compute.getVmSizing())) {
          computeFlavor = new ComputeFlavor();
          if (Objects.nonNull(compute.getVmSizing().getNumOfCPUs())) {
            computeFlavor.setNum_cpus(compute.getVmSizing().getNumOfCPUs());
          }
          if (Objects.nonNull(compute.getVmSizing().getFileSystemSizeGB())) {
            computeFlavor.setDisk_size(compute.getVmSizing().getFileSystemSizeGB() + "GB");
          }
          if (Objects.nonNull(compute.getVmSizing().getMemoryRAM())) {
            computeFlavor.setMem_size(compute.getVmSizing().getMemoryRAM() + "GB");
          }
        }
      }
    }
    return Optional.ofNullable(computeFlavor);
  }

  private FeatureGroupModel getFeatureGroup(String vlmId, Version version, String featureGroupId,
                                            String user) {
    FeatureGroupEntity fgInput = new FeatureGroupEntity();
    fgInput.setVendorLicenseModelId(vlmId);
    fgInput.setVersion(version);
    fgInput.setId(featureGroupId);
    return vendorLicenseFacade.getFeatureGroupModel(fgInput, user);
  }

  private Optional<String> getComponentName(String vspId, Version version, String componentId,
                                            String user) {
    CompositionEntityResponse<ComponentData> component =
        componentManager.getComponent(vspId, version, componentId, user);
    if (Objects.nonNull(component.getData())) {
      return Optional.ofNullable(component.getData().getDisplayName());
    }
    return Optional.empty();
  }

  private List<MultiFlavorVfcImage> getComponentImages(String vspId, Version version,
                                                       String componentId, String user) {
    List<MultiFlavorVfcImage> componentImages = new ArrayList<>();
    MultiFlavorVfcImage multiFlavorVfcImage = null;
    Collection<ImageEntity> imageEntities =
        imageManager.listImages(vspId, version, componentId, user);
    if (Objects.nonNull(imageEntities)) {
      for (ImageEntity imageEntity : imageEntities) {
        String imageId = imageEntity.getId();
        QuestionnaireResponse imageQuestionnaire =
            imageManager.getImageQuestionnaire(vspId, version, componentId, imageId, user);
        CompositionEntityResponse<Image> imageCompositionData =
            imageManager.getImage(vspId, version, componentId, imageId, user);
        if (Objects.nonNull(imageQuestionnaire)
            && Objects.nonNull(imageQuestionnaire.getData())
            && Objects.nonNull(imageCompositionData)) {
          ImageDetails imageDetails;
          try {
            imageDetails = JsonUtil.json2Object(imageQuestionnaire.getData(),
                ImageDetails.class);
          } catch (Exception ex) {
            imageDetails = null;
          }
          if (Objects.nonNull(imageDetails)
              && Objects.nonNull(imageDetails.getVersion())) {
            //Image version is used as a key for the image block
            //So excluding the population if questionnaire data is absent or invalid
            multiFlavorVfcImage = new MultiFlavorVfcImage();
            Image image = imageCompositionData.getData();
            Optional<String> toscaImageFileName = getToscaImageFileName(image, imageDetails);
            toscaImageFileName.ifPresent(multiFlavorVfcImage::setFile_name);
            multiFlavorVfcImage.setSoftware_version(imageDetails.getVersion());
            if (Objects.nonNull(imageDetails.getMd5())) {
              multiFlavorVfcImage.setFile_hash(imageDetails.getMd5());
            }
            multiFlavorVfcImage.setFile_hash_type("md5");
            componentImages.add(multiFlavorVfcImage);
          }
        }
      }
    }
    return componentImages;
  }

  private Optional<String> getToscaImageFileName(Image image, ImageDetails imageDetails) {
    String toscaImageFileName = null;
    StringBuilder builder = new StringBuilder();
    if (Objects.nonNull(image.getFileName())) {
      builder.append(image.getFileName());
      builder.append("-");
      builder.append(imageDetails.getVersion());
      if (Objects.nonNull(imageDetails.getFormat())) {
        builder.append(".");
        builder.append(imageDetails.getFormat());
      }
    }
    toscaImageFileName = builder.toString();
    if (toscaImageFileName.isEmpty()) {
      return Optional.empty();
    }
    return Optional.ofNullable(toscaImageFileName);
  }
}
