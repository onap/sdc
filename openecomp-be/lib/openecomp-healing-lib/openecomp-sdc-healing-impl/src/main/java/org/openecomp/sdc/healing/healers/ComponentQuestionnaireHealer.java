package org.openecomp.sdc.healing.healers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openecomp.sdc.common.utils.SdcCommon;
import org.openecomp.sdc.healing.interfaces.Healer;
import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDaoFactory;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;


public class ComponentQuestionnaireHealer implements Healer {

  private static final ComponentDao componentDao =
      ComponentDaoFactory.getInstance().createInterface();
  private static final ComputeDao computeDao =
      ComputeDaoFactory.getInstance().createInterface();
  private static final ImageDao imageDao =
      ImageDaoFactory.getInstance().createInterface();

  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();

  public static final String GENERAL = "general";
  public static final String IMAGE = "image";
  public static final String FORMAT = "format";
  public static final String CPU_OVER_SUBSCRIPTION_RATIO = "CpuOverSubscriptionRatio";
  public static final String MEMORY_RAM = "MemoryRAM";
  public static final String VM_SIZING = "vmSizing";
  public static final String COMPUTE = "compute";
  public static final String NUM_OF_VMS = "numOfVMs";
  public static final String DISK = "disk";
  public static final String IO_OP_PER_SEC = "IOOperationsPerSec";

  public static final String COMPUTE_CPU_OVER_SUBSCRIPTION_RATIO = "cpuOverSubscriptionRatio";
  public static final String COMPUTE_MEMORY_RAM = "memoryRAM";
  public static final String COMPUTE_IO_OP_PER_SEC = "ioOperationsPerSec";

  public ComponentQuestionnaireHealer(){

  }
  @Override
  public Object heal(Map<String, Object> healingParams) throws Exception {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    String vspId = (String) healingParams.get(SdcCommon.VSP_ID);
    Version version = (Version) healingParams.get(SdcCommon.VERSION);
    String user = (String) healingParams.get(SdcCommon.USER);
    Collection<ComponentEntity> componentEntities =
        componentDao.list(new ComponentEntity(vspId, version, null));
    componentEntities.forEach(componentEntity -> {
      ComponentEntity componentQuestionnaireData =
          componentDao.getQuestionnaireData(vspId, version, componentEntity.getId());
      String questionnaire = Objects.isNull(componentQuestionnaireData) ? null
      : componentQuestionnaireData.getQuestionnaireData();

      if (questionnaire != null) {
        JsonParser jsonParser = new JsonParser();
        JsonObject  json = (JsonObject) jsonParser.parse(questionnaire);

        Collection<ComputeEntity> computeEntities = computeDao.list(new ComputeEntity(vspId,
            version, componentEntity.getId(), null));
        computeEntities.stream().forEach(
            computeEntity -> {
              populateComputeQuestionnaire(json, computeEntity);
            }
        );

        Collection<ImageEntity> imageEntities = imageDao.list(new ImageEntity(vspId,
            version, componentEntity.getId(), null));
        imageEntities.stream().forEach(
            imageEntity -> {
              populateImageQuestionnaire(json, imageEntity);
            }
        );

        processDiskAttribute(json, "bootDiskSizePerVM");
        processDiskAttribute(json, "ephemeralDiskSizePerVM");

        String questionnaireData = json.toString();
        componentDao.updateQuestionnaireData(vspId, version, componentEntity.getId(),
            questionnaireData);
      }
    });
    return componentEntities;
  }

  /**
   * Move Disk Atributes from genral/image/  to genral/disk in component questionnaire itself
   * @param json
   * @param diskAttrName
   * @return
   */
  private void processDiskAttribute(JsonObject json, String diskAttrName) {
    boolean isBootDisksizePerVM = isDiskAttributePresent(json, diskAttrName);
    if (isBootDisksizePerVM) {
      JsonObject diskJsonObject = json.getAsJsonObject(GENERAL).getAsJsonObject(DISK);
      if (diskJsonObject == null) {
        diskJsonObject = new JsonObject();
      }

      diskJsonObject.addProperty(diskAttrName, json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE)
          .get(diskAttrName).getAsNumber());

      json.getAsJsonObject(GENERAL).add(DISK, diskJsonObject);
      json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE).remove(diskAttrName);
    }
  }

  private boolean isDiskAttributePresent(JsonObject json, String diskAttrName) {
    return json.getAsJsonObject(GENERAL) != null &&
        json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE) != null &&
        json.getAsJsonObject(GENERAL).getAsJsonObject (IMAGE).get(diskAttrName)
            != null;
  }

  /**
   * Move the required attributes from component to Image Questionnaire
   * @param json
   * @param imageEntity
   */
  private void populateImageQuestionnaire(JsonObject json, ImageEntity imageEntity) {
    JsonObject general = getJsonObject(json, GENERAL);
    boolean isImageFormat = general != null && json
        .getAsJsonObject(GENERAL)
        .getAsJsonObject(IMAGE) != null && json.getAsJsonObject(GENERAL).getAsJsonObject
        (IMAGE).get(FORMAT) != null;
    if (isImageFormat) {
      JsonObject image = getJsonObject(general, IMAGE);
      JsonElement jsonElement = image.get(FORMAT);
      JsonObject jsonObject = new JsonObject();
      jsonObject.add(FORMAT, jsonElement);
      imageDao.updateQuestionnaireData(imageEntity.getVspId(), imageEntity.getVersion(), imageEntity
          .getComponentId(),imageEntity.getId(), jsonObject.toString());
      image.remove(FORMAT);
    }
  }

  /**
   * Move the required attributes from component to Compute Questionnaire
   * @param json
   * @param computeEntity
   */
  private void populateComputeQuestionnaire(JsonObject json, ComputeEntity computeEntity) {
    JsonObject compute = getJsonObject(json, COMPUTE);
    JsonObject vmSizing = getJsonObject(compute, VM_SIZING);
    if (compute != null && vmSizing != null) {
      JsonElement ioOperationsPerSec = vmSizing.get(IO_OP_PER_SEC);
      if (ioOperationsPerSec != null) {
        vmSizing.addProperty(COMPUTE_IO_OP_PER_SEC, ioOperationsPerSec.getAsNumber());
        vmSizing.remove(IO_OP_PER_SEC);
      }

      JsonObject numberOfVms = getJsonObject(compute, NUM_OF_VMS);
      if (numberOfVms != null ) {
        JsonElement cpuRatio =  numberOfVms.get(CPU_OVER_SUBSCRIPTION_RATIO);
        if (cpuRatio != null ) {
          vmSizing.addProperty(COMPUTE_CPU_OVER_SUBSCRIPTION_RATIO, cpuRatio.getAsString());
          numberOfVms.remove(CPU_OVER_SUBSCRIPTION_RATIO);
        }
        JsonElement memoryRam =  numberOfVms.get(MEMORY_RAM);
        if (memoryRam != null ) {
          vmSizing.addProperty(COMPUTE_MEMORY_RAM, memoryRam.getAsString());
          numberOfVms.remove(MEMORY_RAM);
        }
      }

      JsonObject computeQuestionnaireJsonObject = new JsonObject();
      computeQuestionnaireJsonObject.add(VM_SIZING, vmSizing);
      String computeQuestionnaire = computeQuestionnaireJsonObject.toString();
      computeDao.updateQuestionnaireData(computeEntity.getVspId(), computeEntity.getVersion(),
          computeEntity.getComponentId(), computeEntity.getId(), computeQuestionnaire);
      compute.remove(VM_SIZING);

    }
  }

  private JsonObject getJsonObject(JsonObject json, String name) {
    return json.getAsJsonObject(name);
  }
}
