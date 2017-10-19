package org.openecomp.sdc.healing.healers;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.structure.Artifact;
import org.openecomp.sdc.heat.datatypes.structure.HeatStructureTree;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.OrchestrationTemplateDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadData;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.UploadDataEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Talio on 7/30/2017.
 */
public class ValidationStructureHealer implements Healer {

  private static final VendorSoftwareProductInfoDao vspInfoDao =
      VendorSoftwareProductInfoDaoFactory.getInstance().createInterface();
  private static final OrchestrationTemplateDao orchestrationTemplateDao =
      OrchestrationTemplateDaoFactory.getInstance().createInterface();
  private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {

    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);

    VspDetails vspDetails = vspInfoDao.get(new VspDetails(vspId, version));
    UploadDataEntity orchestrationTemplate =
        orchestrationTemplateDao.getOrchestrationTemplate(vspId, version);

    if(Objects.isNull(orchestrationTemplate.getValidationData())
        || !JsonUtil.isValidJson(orchestrationTemplate.getValidationData())){
      return Optional.empty();
    }

    OldValidationStructureTree oldValidationStructureTree;
    try{
      oldValidationStructureTree =
          JsonUtil.json2Object(orchestrationTemplate.getValidationData(), OldValidationStructureTree
              .class);
    } catch (Exception e){
      logger.debug("",e);
      return Optional.empty();
    }

    Optional<HeatStructureTree> newHeatStructureTreeFromOldStructureTree =
        createNewHeatStructureTreeFromOldStructureTree(oldValidationStructureTree.getImportStructure());

    if(newHeatStructureTreeFromOldStructureTree.isPresent()){
      ValidationStructureList validationData = new ValidationStructureList
          (newHeatStructureTreeFromOldStructureTree.get());
      vspDetails.setValidationDataStructure(validationData);

      updateValuesInDb(vspId, vspDetails, orchestrationTemplate, validationData);
    }
    return newHeatStructureTreeFromOldStructureTree;

  }

  private void updateValuesInDb(String vspId, VspDetails vspDetails,
                                UploadDataEntity orchestrationTemplate,
                                ValidationStructureList validationData) {
    vspInfoDao.update(vspDetails);
    UploadData uploadData = getUpdatedUploadData(orchestrationTemplate, validationData);
    orchestrationTemplateDao.updateOrchestrationTemplateData(vspId, uploadData);
  }

  private UploadData getUpdatedUploadData(UploadDataEntity orchestrationTemplate,
                                          ValidationStructureList validationData) {
    UploadData uploadData = new UploadData();
    uploadData.setValidationDataStructure(validationData);
    uploadData.setValidationData(JsonUtil.object2Json(validationData));
    uploadData.setContentData(orchestrationTemplate.getContentData());
    uploadData.setId(orchestrationTemplate.getId());
    uploadData.setPackageName(orchestrationTemplate.getPackageName());
    uploadData.setPackageVersion(orchestrationTemplate.getPackageVersion());
    return uploadData;
  }


  private Optional<HeatStructureTree> createNewHeatStructureTreeFromOldStructureTree(OldHeatStructureTree
                                                               oldHeatStructureTree){

    HeatStructureTree heatStructureTree = new HeatStructureTree();

    if(Objects.isNull(oldHeatStructureTree)){
      return Optional.empty();
    }

    mapOldHeatStructureTreeValues(oldHeatStructureTree, heatStructureTree);

    Set<OldHeatStructureTree> heat =
        oldHeatStructureTree.getHeat() == null ? new HashSet<>() : oldHeatStructureTree.getHeat();
    Set<OldHeatStructureTree> volume =
        oldHeatStructureTree.getVolume() == null ? new HashSet<>() : oldHeatStructureTree.getVolume();
    Set<OldHeatStructureTree> nested =
        oldHeatStructureTree.getNested() == null ? new HashSet<>() : oldHeatStructureTree.getNested();
    Set<OldHeatStructureTree> network =
        oldHeatStructureTree.getNetwork() == null ? new HashSet<>() : oldHeatStructureTree.getNetwork();


    heatStructureTree.setHeat(createHeatStructureTreeSetFromOld(heat));
    heatStructureTree.setVolume(createHeatStructureTreeSetFromOld(volume));
    heatStructureTree.setNested(createHeatStructureTreeSetFromOld(nested));
    heatStructureTree.setNetwork(createHeatStructureTreeSetFromOld(network));


    return Optional.of(heatStructureTree);

  }

  private void mapOldHeatStructureTreeValues(
      OldHeatStructureTree oldHeatStructureTree,
      HeatStructureTree heatStructureTree) {
    heatStructureTree.setFileName(oldHeatStructureTree.getFileName());
    heatStructureTree.setBase(oldHeatStructureTree.getBase());
    heatStructureTree.setType(oldHeatStructureTree.getType());
    heatStructureTree.setArtifacts(oldHeatStructureTree.getArtifacts());
    heatStructureTree.setErrors(oldHeatStructureTree.getErrors());

    if(Objects.nonNull(oldHeatStructureTree.getEnv())) {
      heatStructureTree.setEnv(new HeatStructureTree(oldHeatStructureTree.getEnv(), false));
    }
  }

  private Set<HeatStructureTree> createHeatStructureTreeSetFromOld(Set<OldHeatStructureTree>
                                                                        oldHeatStructureTreeSet){
    if(CollectionUtils.isEmpty(oldHeatStructureTreeSet)){
      return null;
    }
    Set<HeatStructureTree> newHeatStructureSet = new HashSet<>();

    for(OldHeatStructureTree old : oldHeatStructureTreeSet){
      Optional<HeatStructureTree> newHeatStructureTree =
          createNewHeatStructureTreeFromOldStructureTree(old);
      if(newHeatStructureTree.isPresent()){
        newHeatStructureSet.add(newHeatStructureTree.get());
      }
    }

    return newHeatStructureSet;
  }

  private class OldValidationStructureTree{
    private OldHeatStructureTree importStructure;

    public OldHeatStructureTree getImportStructure() {
      return importStructure;
    }

    public void setImportStructure(
        OldHeatStructureTree importStructure) {
      this.importStructure = importStructure;
    }
  }

  private class OldHeatStructureTree{
    private String fileName;
    private FileData.Type type;
    private Boolean isBase;
    private String env;
    private List<ErrorMessage> errors;
    private Set<OldHeatStructureTree> heat;
    private Set<OldHeatStructureTree> volume;
    private Set<OldHeatStructureTree> network;
    private Set<OldHeatStructureTree> nested;
    private Set<OldHeatStructureTree> other;
    private Set<Artifact> artifacts;

    public OldHeatStructureTree() {
    }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public FileData.Type getType() {
    return type;
  }

  public void setType(FileData.Type type) {
    this.type = type;
  }

  public Boolean getBase() {
    return isBase;
  }

  public void setBase(Boolean base) {
    isBase = base;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public List<ErrorMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  public Set<OldHeatStructureTree> getHeat() {
    return heat;
  }

  public void setHeat(Set<OldHeatStructureTree> heat) {
    this.heat = heat;
  }

  public Set<OldHeatStructureTree> getVolume() {
    return volume;
  }

  public void setVolume(Set<OldHeatStructureTree> volume) {
    this.volume = volume;
  }

  public Set<OldHeatStructureTree> getNetwork() {
    return network;
  }

  public void setNetwork(
      Set<OldHeatStructureTree> network) {
    this.network = network;
  }

  public Set<OldHeatStructureTree> getNested() {
    return nested;
  }

  public void setNested(Set<OldHeatStructureTree> nested) {
    this.nested = nested;
  }

  public Set<OldHeatStructureTree> getOther() {
    return other;
  }

  public void setOther(Set<OldHeatStructureTree> other) {
    this.other = other;
  }

  public Set<Artifact> getArtifacts() {
    return artifacts;
  }

  public void setArtifacts(Set<Artifact> artifacts) {
    this.artifacts = artifacts;
  }
}
}
