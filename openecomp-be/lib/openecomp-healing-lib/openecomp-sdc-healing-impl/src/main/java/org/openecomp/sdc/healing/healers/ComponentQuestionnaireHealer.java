package org.openecomp.sdc.healing.healers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
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
import java.util.Objects;


public class ComponentQuestionnaireHealer implements Healer {

  private static final String GENERAL = "general";
  private static final String IMAGE = "image";
  private static final String FORMAT = "format";
  private static final String CPU_OVER_SUBSCRIPTION_RATIO = "CpuOverSubscriptionRatio";
  private static final String MEMORY_RAM = "MemoryRAM";
  private static final String VM_SIZING = "vmSizing";
  private static final String COMPUTE = "compute";
  private static final String NUM_OF_VMS = "numOfVMs";
  private static final String DISK = "disk";
  private static final String IO_OP_PER_SEC = "IOOperationsPerSec";
  private static final String COMPUTE_CPU_OVER_SUBSCRIPTION_RATIO = "cpuOverSubscriptionRatio";
  private static final String COMPUTE_MEMORY_RAM = "memoryRAM";
  private static final String COMPUTE_IO_OP_PER_SEC = "ioOperationsPerSec";
  private static MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private final ComponentDao componentDao;
  private final ComputeDao computeDao;
  private final ImageDao imageDao;

  public ComponentQuestionnaireHealer() {
    this.componentDao = ComponentDaoFactory.getInstance().createInterface();
    this.computeDao = ComputeDaoFactory.getInstance().createInterface();
    this.imageDao = ImageDaoFactory.getInstance().createInterface();
  }

  public ComponentQuestionnaireHealer(ComponentDao componentDao,
                                      ComputeDao computeDao, ImageDao imageDao) {
    this.componentDao = componentDao;
    this.computeDao = computeDao;
    this.imageDao = imageDao;
  }

  @Override
  public Object heal(String vspId, Version version) throws Exception {
    mdcDataDebugMessage.debugEntryMessage("VSP ID", vspId);

    Collection<ComponentEntity> componentEntities =
        componentDao.list(new ComponentEntity(vspId, version, null));
    componentEntities.forEach(componentEntity -> {
      ComponentEntity componentQuestionnaireData =
          componentDao.getQuestionnaireData(vspId, version, componentEntity.getId());
      String questionnaire = Objects.isNull(componentQuestionnaireData) ? null
          : componentQuestionnaireData.getQuestionnaireData();

      if (StringUtils.isNotBlank(questionnaire)) {
        JsonParser jsonParser = new JsonParser();
        JsonObject json = (JsonObject) jsonParser.parse(questionnaire);

        Collection<ComputeEntity> computeEntities = computeDao.list(new ComputeEntity(vspId,
            version, componentEntity.getId(), null));
        populateComputeQuestionnaire(json, computeEntities);

        Collection<ImageEntity> imageEntities = imageDao.list(new ImageEntity(vspId,
            version, componentEntity.getId(), null));
        populateImageQuestionnaire(json, imageEntities);

        processDiskAttribute(json, "bootDiskSizePerVM");
        processDiskAttribute(json, "ephemeralDiskSizePerVM");

        String questionnaireData = json.toString();
        componentEntity.setQuestionnaireData(questionnaireData); //Added to validate data in Junit

        componentDao.updateQuestionnaireData(vspId, version, componentEntity.getId(),
            questionnaireData);
      }
    });
    return componentEntities;
  }

  /**
   * Move Disk Atributes from genral/image/  to genral/disk in component questionnaire itself
   *
   * @param json Component Json
   * @param diskAttrName Name of disk attribute
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
    return json.getAsJsonObject(GENERAL) != null
        && json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE) != null
        && json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE).get(diskAttrName)
            != null;
  }

  /**
   * Move the required attributes from component to Image Questionnaire
   *
   * @param json Component Json
   * @param imageEntities All images present in component
   */
  private void populateImageQuestionnaire(JsonObject json, Collection<ImageEntity> imageEntities) {
    JsonObject general = getJsonObject(json, GENERAL);
    boolean isImageFormat = general != null && json
        .getAsJsonObject(GENERAL)
        .getAsJsonObject(IMAGE) != null && json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE)
        .get(FORMAT) != null;
    if (isImageFormat) {
      JsonObject image = getJsonObject(general, IMAGE);
      JsonElement jsonElement = image.get(FORMAT);
      JsonObject jsonObject = new JsonObject();
      jsonObject.add(FORMAT, jsonElement);
      imageEntities.forEach(imageEntity -> imageDao.updateQuestionnaireData(imageEntity.getVspId(),
          imageEntity.getVersion(), imageEntity.getComponentId(),
          imageEntity.getId(), jsonObject.toString()));
      image.remove(FORMAT);
    }
  }

  private void populateComputeQuestionnaire(JsonObject json, Collection<ComputeEntity>
      computeEntities) {
    JsonObject compute = getJsonObject(json, COMPUTE);
    if (compute != null) {
      JsonObject vmSizing = handleVmSizing(compute);
      vmSizing = handleNumOfVm(compute, vmSizing);

      if (vmSizing != null) {
        JsonObject computeQuestionnaireJsonObject = new JsonObject();
        computeQuestionnaireJsonObject.add(VM_SIZING, vmSizing);
        String computeQuestionnaire = computeQuestionnaireJsonObject.toString();
        computeEntities.forEach(
            computeEntity -> computeDao.updateQuestionnaireData(computeEntity.getVspId(),
                computeEntity.getVersion(), computeEntity.getComponentId(),
                computeEntity.getId(), computeQuestionnaire));
      }
    }
  }

  private JsonObject handleVmSizing(JsonObject compute) {
    JsonObject vmSizing = getJsonObject(compute, VM_SIZING);
    if (vmSizing != null) {
      JsonElement ioOperationsPerSec = vmSizing.get(IO_OP_PER_SEC);
      if (ioOperationsPerSec != null) {
        vmSizing.addProperty(COMPUTE_IO_OP_PER_SEC, ioOperationsPerSec.getAsNumber());
        vmSizing.remove(IO_OP_PER_SEC);
      }
      compute.remove(VM_SIZING);
    }
    return vmSizing;
  }

  private JsonObject handleNumOfVm(JsonObject compute, JsonObject vmSizing) {
    JsonObject numberOfVms = getJsonObject(compute, NUM_OF_VMS);
    if (numberOfVms != null) {
      JsonElement cpuRatio = numberOfVms.get(CPU_OVER_SUBSCRIPTION_RATIO);
      JsonElement memoryRam = numberOfVms.get(MEMORY_RAM);
      if (vmSizing == null && (cpuRatio != null || memoryRam != null)) {
        vmSizing = new JsonObject();
      }
      if (cpuRatio != null) {
        vmSizing.addProperty(COMPUTE_CPU_OVER_SUBSCRIPTION_RATIO, cpuRatio.getAsString());
        numberOfVms.remove(CPU_OVER_SUBSCRIPTION_RATIO);
      }
      if (memoryRam != null) {
        vmSizing.addProperty(COMPUTE_MEMORY_RAM, memoryRam.getAsString());
        numberOfVms.remove(MEMORY_RAM);
      }
    }
    return vmSizing;
  }

  private JsonObject getJsonObject(JsonObject json, String name) {
    return json.getAsJsonObject(name);
  }
}
