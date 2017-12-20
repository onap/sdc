package org.openecomp.sdc.vendorsoftwareproduct;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.impl.ImageManagerImpl;
import org.openecomp.sdc.vendorsoftwareproduct.services.composition.CompositionEntityDataManager;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class ImagesTest {

  private static String VSP_ID = "VSP_ID";
  private static String COMP_ID = "COMP_ID";
  private static String ID = "ID";
  public static final Version VERSION01 = new Version("version_id");

  @Mock
  private VendorSoftwareProductInfoDao vendorSoftwareProductInfoDao;

  @Mock
  private CompositionEntityDataManager compositionEntityDataManager;

  @InjectMocks
  @Spy
  private ImageManagerImpl imageManager;

  @BeforeMethod
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void createImage() {
    ImageEntity imageEntity = new ImageEntity(VSP_ID, VERSION01, COMP_ID, ID);
    doReturn(true).when(vendorSoftwareProductInfoDao).isManual(anyObject(), anyObject());

    imageManager.createImage(imageEntity);
    verify(compositionEntityDataManager).createImage(imageEntity);
  }

  @Test
  public void createImageHeat() {
    ImageEntity imageEntity = new ImageEntity(VSP_ID, VERSION01, COMP_ID, ID);
    doReturn(false).when(vendorSoftwareProductInfoDao).isManual(anyObject(), anyObject());

    try {
      imageManager.createImage(imageEntity);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(),
          VendorSoftwareProductErrorCodes.ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    }
  }
  /*private static final String USER1 = "imageTestUser1";
  private static final String USER2 = "imageTestUser2";
  private static final Version VERSION01 = new Version(0, 1);
  private static final VendorSoftwareProductManager vendorSoftwareProductManager =
      new VendorSoftwareProductManagerImpl();
  private static final VendorSoftwareProductDao vendorSoftwareProductDao =
      VendorSoftwareProductDaoFactory
          .getInstance().createInterface();

  private static String image1Id;


  private static String comp1 = "{\"displayName\": \"VFC_Manual\", " +
      "\"description\": \"desc manual\"}";

  private static String vsp1Id;
  private static String vsp2Id;
  private static String vsp3Id;
  private static String comp1Id;

  private static String image2Id;

  @BeforeClass
  private void init() {
    VspDetails
        vsp1 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp1", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null,
            VSPCommon.OnboardingMethod.Manual.name())1
        );
    vsp1Id = vsp1.getId();

    VspDetails vsp2 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp2", "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null, VSPCommon.OnboardingMethod.
                Manual.name())1);
    vsp2Id = vsp2.getId();

    VspDetails vsp3 = vendorSoftwareProductManager.createNewVsp(VSPCommon
        .createVspDetails(null, null, "VSP_" + CommonMethods.nextUuId(), "Test-vsp3",
            "vendorName",
            "vlm1Id", "icon", "category", "subCategory", "123", null, VSPCommon
                .OnboardingMethod.HEAT.name())1);
    vsp3Id = vsp3.getId();

    ComponentEntity comp = new ComponentEntity();
    comp.setVspId(vsp2Id);
    comp.setCompositionData(comp1);
    ComponentEntity createdComp = vendorSoftwareProductManager.createComponent(comp1);
    comp1Id = createdComp.getId();
  }

  @Test
  public void testListImagesNonExistingVspId_negative() {
    testList_negative("non existing vsp id", null, image1Id1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST,
        "Versionable entity VendorSoftwareProduct with id non existing vsp id does not exist." );
  }

  @Test
  public void testListImagesNonExistingVfcId_negative() {
    testList_negative(vsp1Id, VERSION01, "444"1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component with Id 444 does not exist for Vendor Software Product "
            + "with id "+vsp1Id+ " and version "+VERSION01);
  }

  @Test
  public void testListImages() {
    createImageEntity("media-vsrx-vmdisk-15.1X49-D40.6.aki", "aki");
    createImageEntity("riverbed-15.1X49-D40.6.vdi", "vdi");
    final Collection<ImageEntity> imageEntities =
        vendorSoftwareProductManager.listImages(vsp2Id, VERSION01, comp1Id1);
    System.out.println("size::"+imageEntities.size());
  }

  @Test
  public void testCreateNonExistingVspId_negative() {
    testCreate_negative(new ImageEntity("non existing vsp id", null, null, null)1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST,
        "Versionable entity VendorSoftwareProduct with id non existing vsp id does not exist.");
  }

  @Test
  public void testCreateNonExistingVfcId_negative() {
    testCreate_negative(new ImageEntity(vsp1Id, VERSION01, "222", null)1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component with Id 222 does not exist for Vendor Software Product "
            + "with id "+vsp1Id + " and version "+VERSION01);
  }

  @Test(dependsOnMethods = "testUpdateNonExistingImageId_negative")
  public void testCreateOnAvailableVsp_negative() {
    vendorSoftwareProductManager.checkin(vsp1Id1);
    testCreate_negative(new ImageEntity(vsp1Id, null, null, null)1,
        VersioningErrorCodes.EDIT_ON_UNLOCKED_ENTITY,
        "Can not edit versionable entity VendorSoftwareProduct with id "+vsp1Id+ " since it is not"
            + " checked out.");
  }

  @Test(dependsOnMethods = "testCreateOnAvailableVsp_negative")
  public void testCreateOnVspOtherUser_negative() {
    vendorSoftwareProductManager.checkout(vsp1Id1);
    testCreate_negative(new ImageEntity(vsp1Id, null, null, null)2,
        VersioningErrorCodes.EDIT_ON_ENTITY_LOCKED_BY_OTHER_USER,
        "Versionable entity VendorSoftwareProduct with id " +vsp1Id+
            " can not be edited since it is locked by other user "+ USER1+ ".");
  }

  @Test(dependsOnMethods = "testCreateOnVspOtherUser_negative")
  public void testOnUndoCheckoutImagesDeleted() {

    ComponentEntity comp = new ComponentEntity();
    comp.setVspId(vsp1Id);
    comp.setCompositionData(comp1);
    ComponentEntity createdComp = vendorSoftwareProductManager.createComponent(comp1);
    String compId = createdComp.getId();

    vendorSoftwareProductManager.checkin(vsp1Id1);
    vendorSoftwareProductManager.checkout(vsp1Id1);

    for(int i = 1; i<=3; i++) {
      ImageEntity imageEntity = new ImageEntity();
      imageEntity.setVspId(vsp1Id);
      imageEntity.setComponentId(compId);
      Image image = new Image();
      image.setFileName(i + "testimage.aki");
      //image.setVersion("9.2.0");
      image.setDescription("riverbed image");
      //image.setFormat("aki");
      //image.setMd5("233343DDDD");
      imageEntity.setImageCompositionData(image);
      ImageEntity createdImage = vendorSoftwareProductManager.createImage(imageEntity1);
    }

    Collection<ImageEntity> imageEntities =
        vendorSoftwareProductManager.listImages(vsp1Id, null, compId1);

    Assert.assertEquals(imageEntities.size(), 3);

    vendorSoftwareProductManager.undoCheckout(vsp1Id1);

    imageEntities = vendorSoftwareProductManager.listImages(vsp1Id, null, compId1);

    Assert.assertEquals(imageEntities.size(), 0);
  }

  @Test
  public void testCreateOnHeatVsp_negative() {
    final ErrorCode addImageNotSupportedHeatOnboardMethodErrorBuilder =
        NotSupportedHeatOnboardMethodErrorBuilder
            .getAddImageNotSupportedHeatOnboardMethodErrorBuilder();
    testCreate_negative(new ImageEntity(vsp3Id, null, null, null)1,
        addImageNotSupportedHeatOnboardMethodErrorBuilder.id(),
        addImageNotSupportedHeatOnboardMethodErrorBuilder.message()
        );
  }

  @Test(dependsOnMethods = "testListImages")
  public void testCreateImage() {
    ImageEntity createdImage = createImageEntity("riverbed-WX-IMG-9.2.0.qcow2", "qcow2");
    Assert.assertNotNull(image1Id);
    Assert.assertNotNull(createdImage.getCompositionData());
    Assert.assertNotNull(
        vendorSoftwareProductManager.getImage(vsp2Id, VERSION01, comp1Id,image1Id,
            USER1).getData());
  }

  @Test(dependsOnMethods = "testCreateImage")
  public void testCreateDupImageNameForSameComponent_negative() {
    ImageEntity createdImage = null;
    try {
      createdImage = createImageEntity("riverbed-WX-IMG-9.2.0.qcow2", "qcow2");
      Assert.fail();
    }
    catch(CoreException exception) {
      Assert.assertEquals(exception.code().id(), VendorSoftwareProductErrorCodes.
          DUPLICATE_IMAGE_NAME_NOT_ALLOWED);
      Assert.assertEquals(exception.getMessage(),
          String.format("Invalid request, Image with name riverbed-WX-IMG-9.2.0.qcow2 already " +
              "exists for component with ID "+comp1Id)+".");
    }
  }

  @Test
  public void testGet() {
    ImageEntity createdImage = createImageEntity("read-riverbed-WX-IMG-9.2.0.qcow2", "qcow2");
    testGet(vsp2Id, VERSION01, comp1Id, createdImage.getId()1, createdImage);
  }

  @Test
  public void testGetNonExistingVspId_negative() {
    testGet_negative("non existing vsp id", null, null, image1Id1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST,
        "Versionable entity VendorSoftwareProduct with id non existing vsp id does not exist." );
  }

  @Test
  public void testGetNonExistingVfcId_negative() {
    testGet_negative(vsp1Id, VERSION01, "111", null1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component with Id 111 does not exist for Vendor Software Product "
            + "with id "+vsp1Id + " and version "+VERSION01);
  }

  @Test
  public void testUpdateNonExistingVspId_negative() {
    ImageEntity imageEntity = new ImageEntity("non existing vsp id", null, null, image1Id);

    testUpdate_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST,
        "Versionable entity VendorSoftwareProduct with id non existing vsp id does not exist." );
  }

  @Test(dependsOnMethods =  "testUpdateNonExistingVspId_negative")
  public void testUpdateNonExistingVfcId_negative() {
    ImageEntity imageEntity = new ImageEntity(vsp1Id, VERSION01, "111", null);

    testUpdate_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component with Id 111 does not exist for Vendor Software Product "
            + "with id "+vsp1Id + " and version "+VERSION01);
  }

  @Test(dependsOnMethods =  "testUpdateNonExistingVfcId_negative")
  public void testUpdateNonExistingImageId_negative() {
    ImageEntity imageEntity = new ImageEntity(vsp2Id, VERSION01, comp1Id, "222");

    testUpdate_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component Image with Id 222 does not exist for Vendor " +
            "Software Product with id "+vsp2Id+ " and version "+VERSION01 );
  }

  @Test(dependsOnMethods = "testCreateImage")
  public void testUpdate() {
    ImageEntity imageEntity = createImageEntity("testimage1","qcow2");
    testGet(vsp2Id, VERSION01, comp1Id, imageEntity.getId(),USER1, imageEntity );

    final Image imageCompositionData = imageEntity.getImageCompositionData();
    //imageCompositionData.setVersion("10.0");
    imageCompositionData.setDescription("updated image");

    vendorSoftwareProductManager.updateImage(imageEntity1);

    testGet(vsp2Id, VERSION01, comp1Id, imageEntity.getId(),USER1, imageEntity );
    image2Id = imageEntity.getId();
  }

  @Test(dependsOnMethods = "testUpdate")
  public void testUpdateNegative_UniqueName() {
    final CompositionEntityResponse<Image> image =
        vendorSoftwareProductManager.getImage(vsp2Id, VERSION01, comp1Id,
            image2Id1);
    final Image data = image.getData();

    final Image imageCompositionData = data;
    imageCompositionData.setFileName("riverbed-WX-IMG-9.2.0.qcow2");

    ImageEntity entity = new ImageEntity(vsp2Id, VERSION01, comp1Id, image2Id );
    entity.setImageCompositionData(imageCompositionData);
    testUpdate_negative(entity1, ImageErrorBuilder.getDuplicateImageNameErrorBuilder(
        "riverbed-WX-IMG-9.2.0.qcow2", comp1Id).id()
        ,ImageErrorBuilder.getDuplicateImageNameErrorBuilder("riverbed-WX-IMG-9.2.0.qcow2", comp1Id)
            .message() );
  }

  @Test(dependsOnMethods = "testUpdateNegative_UniqueName")
  public void testDeleteImage() {
    CompositionEntityResponse<Image> image =
        vendorSoftwareProductManager.getImage(vsp2Id, VERSION01, comp1Id, image2Id1);

    Assert.assertNotNull(image.getData());

    vendorSoftwareProductManager.deleteImage(vsp2Id, comp1Id, image2Id1);

    testGet_negative(vsp2Id, VERSION01, comp1Id, image2Id1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component Image with Id "+image2Id+ " does not exist for " +
            "Vendor Software Product with id "+vsp2Id+ " and version "+VERSION01 );

  }

  @Test
  public void testDeleteNonExistingVspId_negative() {
    ImageEntity imageEntity = new ImageEntity("non existing vsp id", null, null, image1Id);

    testDelete_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_ENTITY_NOT_EXIST,
        "Versionable entity VendorSoftwareProduct with id non existing vsp id does not exist." );
  }

  @Test(dependsOnMethods =  "testDeleteNonExistingVspId_negative")
  public void testDeleteNonExistingVfcId_negative() {
    ImageEntity imageEntity = new ImageEntity(vsp1Id, VERSION01, "111", null);

    testDelete_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component with Id 111 does not exist for Vendor Software Product "
            + "with id "+vsp1Id + " and version "+VERSION01);
  }

  @Test(dependsOnMethods =  "testDeleteNonExistingVfcId_negative")
  public void testDeleteNonExistingImageId_negative() {
    ImageEntity imageEntity = new ImageEntity(vsp2Id, VERSION01, comp1Id, "222");

    testDelete_negative(imageEntity1,
        VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND,
        "Vendor Software Product Component Image with Id 222 does not exist for Vendor " +
            "Software Product with id "+vsp2Id+ " and version "+VERSION01 );
  }

  @Test
  public void testUpdateandGetQuestionnaire() {
    ImageEntity imageEntity = createImageEntity("newimage_testUpdateandGetQuestionnaire.vdi","vdi");
    vendorSoftwareProductManager.updateImageQuestionnaire(vsp2Id, comp1Id, imageEntity.getId(),
        "{\"format\":\"vdi\",\"version\":\"1.2\",\"md5\":\"ssd3344\"}",
        USER1);

    final QuestionnaireResponse imageQuestionnaire =
        vendorSoftwareProductManager.getImageQuestionnaire(vsp2Id, VERSION01, comp1Id,
            imageEntity.getId()1);

    String imageDetails = imageQuestionnaire.getData();
    Assert.assertEquals("vdi", JsonUtil.json2Object(imageDetails, ImageDetails.class).getFormat());
    Assert.assertEquals("1.2", JsonUtil.json2Object(imageDetails, ImageDetails.class).getVersion());
    Assert.assertEquals("ssd3344", JsonUtil.json2Object(imageDetails, ImageDetails.class).getMd5());
  }

  @Test
  public void testUpdateQuestionnaireInvalidFormat_negative() {
    ImageEntity imageEntity = createImageEntity("newimage.vdi","vdi");
    try {
      vendorSoftwareProductManager.updateImageQuestionnaire(vsp2Id, comp1Id, imageEntity.getId(),
          "{\"format\":\"invalidformat\",\"version\":\"1.2\",\"md5\":\"ssd3344\"}",
          USER1);
      Assert.fail();
    }
    catch(CoreException exception) {
      Assert.assertEquals(exception.code().id(), "VFC_IMAGE_INVALID_FORMAT");
      Assert.assertEquals(exception.getMessage(), "The format value doesn't meet the expected "
          + "attribute value.");
    }
  }

  private ImageEntity createImageEntity(String fileName, String format) {
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setVspId(vsp2Id);
    imageEntity.setComponentId(comp1Id);
    Image image = new Image();
    image.setFileName(fileName);
    //image.setVersion("9.2.0");
    image.setDescription("riverbed image");
    //image.setFormat(format);
    // image.setMd5("233343DDDD");
    imageEntity.setImageCompositionData(image);

    ImageEntity createdImage = vendorSoftwareProductManager.createImage(imageEntity1);
    image1Id = createdImage.getId();
    return createdImage;
  }

  private void testGet(String vspId, Version version, String componentId, String imageId, String
      user, ImageEntity expected) {
    CompositionEntityResponse<Image>
        response = vendorSoftwareProductManager.getImage(vspId, null, componentId, imageId);
    Assert.assertEquals(response.getId(), expected.getId());
    Assert.assertEquals(expected.getImageCompositionData().getFileName(), response.getData().
        getFileName());
    Assert.assertEquals(expected.getImageCompositionData().getDescription(), response.getData().
        getDescription());
    Assert.assertNotNull(response.getSchema());
  }

  private void testCreate_negative(ImageEntity image, String user,
                                   String expectedErrorCode, String expectedErrorMsg) {
    try {
      vendorSoftwareProductManager.createImage(image);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }

  private void testGet_negative(String vspId, Version version, String componentId, String imageId,
                                String user,
                                String expectedErrorCode, String expectedErrorMsg) {
    try {
      vendorSoftwareProductManager.getImage(vspId, version, componentId, imageId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }

  private void testList_negative(String vspId, Version version, String componentId, String user,
                                 String expectedErrorCode, String expectedErrorMsg) {
    try {
      vendorSoftwareProductManager.listImages(vspId, version, componentId);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }

  private void testUpdate_negative(ImageEntity imageEntity,
                                String user,
                                String expectedErrorCode, String expectedErrorMsg) {
    try {

      vendorSoftwareProductManager.updateImage(imageEntity);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }

  private void testDelete_negative(ImageEntity imageEntity,
                                   String user,
                                   String expectedErrorCode, String expectedErrorMsg) {
    try {

      vendorSoftwareProductManager.updateImage(imageEntity);
      Assert.fail();
    } catch (CoreException exception) {
      Assert.assertEquals(exception.code().id(), expectedErrorCode);
      Assert.assertEquals(exception.getMessage(), expectedErrorMsg);
    }
  }*/
}