package org.openecomp.sdc.healing.healers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComponentDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ComputeDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;

public class ComponentQuestionnaireHealerTest {
  private static final String HANDLE_NUM_OF_VMS_METHOD = "handleNumOfVm";
  private static final String GENERAL = "general";
  private static final String IMAGE = "image";
  private static final String FORMAT = "format";
  private static final String CPU_OVER_SUBSCRIPTION_RATIO = "CpuOverSubscriptionRatio";
  private static final String MEMORY_RAM = "MemoryRAM";
  private static final String VM_SIZING = "vmSizing";
  private static final String COMPUTE = "compute";
  private static final String NUM_OF_VMS = "numOfVMs";
  private static final String DISK = "disk";
  private static final String BOOT_DISK_SIZE_PER_VM = "bootDiskSizePerVM";
  private static final String EPHEMERAL_DISK_SIZE_PER_VM = "ephemeralDiskSizePerVM";
  private static final Version VERSION = new Version(0, 1);
  private static final String DUMMY_VSP_ID = "1495ef442f964cbfb00d82bd54292f89";
  private static final String DUMMY_COMPONENT_ID = "2495ef442f964cbfb00d82bd54292f89";
  private static final String DUMMY_COMPUTE_ID = "3495ef442f964cbfb00d82bd54292f89";
  private static final String DUMMY_IMAGE_ID = "4495ef442f964cbfb00d82bd54292f89";
  private static final String componentQuestionnaireData = "{\"compute\": {" +
      "\"guestOS\": {\"bitSize\": 64},\"vmSizing\": {\"IOOperationsPerSec\": \"0\"}," +
      "\"numOfVMs\": {\"CpuOverSubscriptionRatio\": \"1:1\",\"MemoryRAM\": \"2 GB\"}}," +
      "\"general\": {\"image\": {\"providedBy\": \"AIC\",\"format\":\"qcow2\"," +
      "\"bootDiskSizePerVM\": \"100\",\"ephemeralDiskSizePerVM\": \"200\"},\"hypervisor\": {" +
      "\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final String componentQuestionnaireMissingDiskAttrData = "{\"compute\": {" +
      "\"guestOS\": {\"bitSize\": 64},\"vmSizing\": {\"IOOperationsPerSec\": \"0\"},\"numOfVMs\"" +
      ": {\"CpuOverSubscriptionRatio\": \"1:1\",\"MemoryRAM\": \"2 GB\"}},\"general\": " +
      "{\"image\": {\"providedBy\": \"AIC\",\"format\":\"qcow2\"}," +
      "\"hypervisor\": {\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final String componentQuestionnaireWithoutVMSizingData = "{\"compute\": {" +
      "\"guestOS\": {\"bitSize\": 64},\"numOfVMs\": {\"CpuOverSubscriptionRatio\": \"1:1\"," +
      "\"maximum\": \"400\"," +
      "\"MemoryRAM\": \"2 GB\"}},\"general\": {\"image\": {\"providedBy\": \"AIC\",\"format\"" +
      ":\"qcow2\",\"bootDiskSizePerVM\": \"100\",\"ephemeralDiskSizePerVM\": \"200\"}," +
      "\"hypervisor\": {\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final String componentQuestionnaireWithoutNumOfVMData = "{\"compute\": " +
      "{\"guestOS\": {\"bitSize\": 64}," +
      "\"vmSizing\": {\"IOOperationsPerSec\": \"0\"}}," +
      "\"general\": {\"image\": {\"providedBy\": \"AIC\",\"format\":\"qcow2\"," +
      "\"bootDiskSizePerVM\": \"100\",\"ephemeralDiskSizePerVM\": \"200\"}," +
      "\"hypervisor\": {\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final String componentQuestionnaireWithMemoryRamData = "{\"compute\": " +
      "{\"guestOS\": {\"bitSize\": 64}," +
      "\"vmSizing\": {\"IOOperationsPerSec\": \"0\"},\"numOfVMs\": {\"MemoryRAM\": \"2 GB\"}}," +
      "\"general\": {\"image\": {\"providedBy\": \"AIC\",\"format\":\"qcow2\"," +
      "\"bootDiskSizePerVM\": \"100\",\"ephemeralDiskSizePerVM\": \"200\"}," +
      "\"hypervisor\": {\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final String componentQuestionnaireWithCPURatioData = "{\"compute\": " +
      "{\"guestOS\": {\"bitSize\": 64},\"vmSizing\": {\"IOOperationsPerSec\": " +
      "\"0\"},\"numOfVMs\": {\"CpuOverSubscriptionRatio\": " +
      "\"1:1\"}},\"general\": {\"image\": {\"providedBy\": \"AIC\",\"format\":\"qcow2\"," +
      "\"bootDiskSizePerVM\": \"100\",\"ephemeralDiskSizePerVM\": \"200\"}," +
      "\"hypervisor\": {\"hypervisor\": \"KVM\" } },\"highAvailabilityAndLoadBalancing\": {" +
      "\"isComponentMandatory\": \"\",\"highAvailabilityMode\": \"\"},\"storage\": {" +
      "\"backup\": {\"backupNIC\": \"\",\"backupType\": \"On Site\" }," +
      "\"snapshotBackup\": {\"snapshotFrequency\": \"24\"}},\"network\": {\"networkCapacity\": {" +
      "\"protocolWithHighestTrafficProfileAcrossAllNICs\": \"\"}}}";

  private static final JsonParser jsonParser = new JsonParser();
  private ComponentEntity componentEntity;

  @Mock
  private ImageDao imageDao;

  @Mock
  private ComputeDao computeDao;

  @Mock
  private ComponentDao componentDao;

  @InjectMocks
  private ComponentQuestionnaireHealer componentQuestionnaireHealer;

  @Before
  public void init() throws Exception {
    MockitoAnnotations.initMocks(ComponentQuestionnaireHealerTest.this);
  }

  @Test
  public void healQuestionnaireNullTest() throws Exception {
    prepareHealingData();
    componentEntity.setQuestionnaireData(null);
    Object returnObject = componentQuestionnaireHealer.heal(DUMMY_VSP_ID, VERSION);
    Assert.assertTrue(returnObject instanceof Collection);
    Collection<ComponentEntity> componentEntities = (Collection<ComponentEntity>) returnObject;
    componentEntities.forEach(componentEntity -> {
      Assert.assertNull(componentEntity.getQuestionnaireData());
    });
  }

  @Test
  public void healAllCasesTest() throws Exception {
    prepareHealingData();

    Object returnObject = componentQuestionnaireHealer.heal(DUMMY_VSP_ID, VERSION);
    Assert.assertTrue(returnObject instanceof Collection);
    Collection<ComponentEntity> componentEntities = (Collection<ComponentEntity>) returnObject;
    componentEntities.forEach(componentEntity -> {
      JsonObject json = (JsonObject) jsonParser.parse(componentEntity.getQuestionnaireData());
      Assert.assertNotNull(json.getAsJsonObject(GENERAL).getAsJsonObject(DISK));
      Assert.assertNotNull(json.getAsJsonObject(GENERAL).getAsJsonObject(DISK)
          .getAsJsonPrimitive(BOOT_DISK_SIZE_PER_VM));
      Assert.assertNotNull(json.getAsJsonObject(GENERAL).getAsJsonObject(DISK)
          .getAsJsonPrimitive(EPHEMERAL_DISK_SIZE_PER_VM));
      Assert.assertNotNull(json.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS));
      Assert.assertNull(json.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS)
          .getAsJsonPrimitive(MEMORY_RAM));
      Assert.assertNull(json.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS)
          .getAsJsonPrimitive(CPU_OVER_SUBSCRIPTION_RATIO));
      Assert.assertNull(json.getAsJsonObject(GENERAL).getAsJsonObject(IMAGE)
          .getAsJsonPrimitive(FORMAT));
      Assert.assertNull(json.getAsJsonObject(COMPUTE).getAsJsonObject(VM_SIZING));
    });
  }

  @Test
  public void healDiskAttrMissingTest() throws Exception {
    prepareHealingData();
    componentEntity.setQuestionnaireData(componentQuestionnaireMissingDiskAttrData);
    Object returnObject = componentQuestionnaireHealer.heal(DUMMY_VSP_ID, VERSION);
    Assert.assertTrue(returnObject instanceof Collection);
    Collection<ComponentEntity> componentEntities = (Collection<ComponentEntity>) returnObject;
    componentEntities.forEach(componentEntity -> {
      JsonObject json = (JsonObject) jsonParser.parse(componentEntity.getQuestionnaireData());
      Assert.assertNull(json.getAsJsonObject(COMPUTE).getAsJsonObject(VM_SIZING));
    });
  }

  @Test
  public void handleVMSizingWithVMSizingTest()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JsonObject jsonObject = (JsonObject) jsonParser.parse(componentQuestionnaireWithoutNumOfVMData);
    Method method = ComponentQuestionnaireHealer.class.getDeclaredMethod("handleVmSizing",
        JsonObject.class);
    method.setAccessible(true);
    method.invoke(componentQuestionnaireHealer, jsonObject.getAsJsonObject(COMPUTE));

    Assert.assertNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(VM_SIZING));
  }

  @Test
  public void handleNumOfVMWithoutVMSizingTest()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JsonObject jsonObject = (JsonObject) jsonParser
        .parse(componentQuestionnaireWithoutVMSizingData);
    provideAccessToPrivateMethod(HANDLE_NUM_OF_VMS_METHOD, jsonObject);

    Assert.assertNotNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS));
    Assert.assertNotNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS)
        .getAsJsonPrimitive("maximum"));
  }

  @Test
  public void handleVMSizingWithOnlyMemoryRAMTest()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JsonObject jsonObject = (JsonObject) jsonParser.parse(componentQuestionnaireWithMemoryRamData);
    provideAccessToPrivateMethod(HANDLE_NUM_OF_VMS_METHOD, jsonObject);

    Assert.assertNotNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS));
    Assert.assertNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS)
        .getAsJsonPrimitive(MEMORY_RAM));
  }

  @Test
  public void handleVMSizingWithOnlyCpuRatioTest()
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    JsonObject jsonObject = (JsonObject) jsonParser.parse(componentQuestionnaireWithCPURatioData);
    provideAccessToPrivateMethod(HANDLE_NUM_OF_VMS_METHOD, jsonObject);

    Assert.assertNotNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS));
    Assert.assertNull(jsonObject.getAsJsonObject(COMPUTE).getAsJsonObject(NUM_OF_VMS)
        .getAsJsonPrimitive(CPU_OVER_SUBSCRIPTION_RATIO));
  }

  private void provideAccessToPrivateMethod(String methodName, JsonObject jsonObject)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method method = ComponentQuestionnaireHealer.class
        .getDeclaredMethod(methodName, JsonObject.class, JsonObject.class);
    method.setAccessible(true);

    method.invoke(componentQuestionnaireHealer, jsonObject.getAsJsonObject(COMPUTE), null);
  }

  private void prepareHealingData() {
    componentEntity = new ComponentEntity(DUMMY_VSP_ID, VERSION, DUMMY_COMPONENT_ID);
    componentEntity.setQuestionnaireData(componentQuestionnaireData);

    Collection<ComponentEntity> componentEntities = new ArrayList<>();
    componentEntities.add(componentEntity);
    doReturn(componentEntities).when(componentDao).list(anyObject());
    doReturn(componentEntity).when(componentDao).getQuestionnaireData(DUMMY_VSP_ID,
        VERSION, DUMMY_COMPONENT_ID);

    ComputeEntity computeEntity = new ComputeEntity(DUMMY_VSP_ID, VERSION,
        DUMMY_COMPONENT_ID, DUMMY_COMPUTE_ID);
    Collection<ComputeEntity> computeEntities = new ArrayList<>();
    computeEntities.add(computeEntity);
    doReturn(computeEntities).when(computeDao).list(anyObject());

    ImageEntity imageEntity = new ImageEntity(DUMMY_VSP_ID, VERSION,
        DUMMY_COMPONENT_ID, DUMMY_IMAGE_ID);
    Collection<ImageEntity> imageEntities = new ArrayList<>();
    imageEntities.add(imageEntity);
    doReturn(imageEntities).when(imageDao).list(anyObject());

    doNothing().when(componentDao).updateQuestionnaireData(anyObject(),
        anyObject(), anyObject(), anyObject());
  }
}
