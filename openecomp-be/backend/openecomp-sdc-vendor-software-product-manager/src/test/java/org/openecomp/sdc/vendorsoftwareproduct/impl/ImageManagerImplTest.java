package org.openecomp.sdc.vendorsoftwareproduct.impl;


import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.vendorsoftwareproduct.CompositionEntityDataManager;
import org.openecomp.sdc.vendorsoftwareproduct.dao.ImageDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.VendorSoftwareProductInfoDao;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.errors.VendorSoftwareProductErrorCodes;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.errors.VersioningErrorCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ImageManagerImplTest {

    private static final String IMAGE_NOT_EXIST_MSG =
            "Vendor Software Product Image with Id image1 does not exist for Vendor Software Product with"
                    + " id VSP_ID and version 0.1";

    private static final String VSP_ID = "VSP_ID";
    private static final Version VERSION = new Version(0, 1);
    private static final String COMPONENT_ID = "COMPONENT_ID";
    private static final String IMAGE1_ID = "image1";
    private static final String IMAGE2_ID = "image2";

    private static final String IMAGE_QUEST_SCHEMA =
            "{\n" + "\"$schema\": \"http://json-schema.org/draft-04/schema#\",\n" + "\"type\": \"object\",\n"
                    + "\"properties\": {\n" + "    \"format\": {\n" + "     \"type\": \"string\",\n"
                    + "        \"enum\": [\n"
                    + "       \"aki\",\"ami\",\"ari\",\"iso\",\"qcow2\",\"raw\", \"vdi\",\"vhd\",\"vmdk\"\n"
                    + "        ]\n" + "    },\n" + "    \"version\": {\n" + "        \"type\": \"string\",\n"
                    + "        \"minLength\": 1\n" + "    },\n" + "    \"md5\": {\n" + "        \"type\": \"string\",\n"
                    + "        \"maxLength\": 32\n" + "    }\n" + "},\n" + "    \"additionalProperties\": false,\n"
                    + "    \"required\": [\n" + "     \"version\"\n" + "    ]\n" + "}";

    @Mock
    private ImageDao imageDao;
    @Mock
    private CompositionEntityDataManager compositionEntityDataManagerMock;
    @Mock
    private VendorSoftwareProductInfoDao vspInfoDao;
    @InjectMocks
    @Spy
    private ImageManagerImpl imageManager;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
        imageManager = null;
    }


    @Test
    public void testListWhenNone() {
        final Collection<ImageEntity> imageEntities = imageManager.listImages(VSP_ID, VERSION, COMPONENT_ID);
        Assert.assertEquals(imageEntities.size(), 0);
    }

    @Test
    public void testList() {

        doReturn(Arrays.asList(createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID),
                createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE2_ID))).when(imageDao).list(any());


        final Collection<ImageEntity> images = imageManager.listImages(VSP_ID, VERSION, COMPONENT_ID);
        Assert.assertEquals(images.size(), 2);
        for (ImageEntity image : images) {
            Assert.assertEquals(image.getImageCompositionData().getFileName(),
                    IMAGE1_ID.equals(image.getId()) ? IMAGE1_ID + "_name" : IMAGE2_ID + "_name");
        }
    }

    @Test
    public void testCreateOnNotManualImage_negative() {

        testCreate_negative(new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, null),
                VendorSoftwareProductErrorCodes.ADD_IMAGE_NOT_ALLOWED_IN_HEAT_ONBOARDING);
    }

    @Test
    public void testCreateManualImage() {
        ImageEntity expected = createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        doReturn(true).when(vspInfoDao).isManual(any(), any());
        imageManager.createImage(expected);
        verify(compositionEntityDataManagerMock).createImage(expected);
        verify(compositionEntityDataManagerMock).createImage(expected);
    }

    @Test
    public void testUpdateNonExistingImageId_negative() {
        testUpdate_negative(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID,
                VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test
    public void testUpdateImage() {
        doReturn(createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID)).when(imageDao).get(any());

        doReturn(new CompositionEntityValidationData(CompositionEntityType.image, IMAGE1_ID))
                .when(compositionEntityDataManagerMock).validateEntity(any(), any(), any());

        ImageEntity imageEntity = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        Image imageData = new Image();
        imageData.setFileName(IMAGE1_ID + "_name");
        imageData.setDescription(IMAGE1_ID + " desc updated");
        imageEntity.setImageCompositionData(imageData);

        CompositionEntityValidationData validationData = imageManager.updateImage(imageEntity);
        Assert.assertTrue(validationData == null || validationData.getErrors() == null);
        verify(imageDao).update(imageEntity);
    }

    @Test
    public void testIllegalImageUpdate() {
        doReturn(createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID)).when(imageDao).get(any());

        doReturn(true).when(vspInfoDao).isManual(any(), any());

        CompositionEntityValidationData toBeReturned =
                new CompositionEntityValidationData(CompositionEntityType.image, IMAGE1_ID);
        toBeReturned.setErrors(Arrays.asList("error1", "error2"));
        doReturn(toBeReturned).when(compositionEntityDataManagerMock)
                              .validateEntity(any(), any(), any());

        ImageEntity imageEntity = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        Image imageData = new Image();
        imageData.setFileName(IMAGE1_ID + "_name_updated");
        imageData.setDescription(IMAGE1_ID + " desc updated");
        imageEntity.setImageCompositionData(imageData);

        CompositionEntityValidationData validationData = imageManager.updateImage(imageEntity);
        Assert.assertNotNull(validationData);
        Assert.assertEquals(validationData.getErrors().size(), 2);

        verify(imageDao, never()).update(imageEntity);
    }

    @Test
    public void testUpdateHEATImageFileName() throws Exception {
        doReturn(createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID)).when(imageDao).get(any());

        String updatedName = IMAGE1_ID + " name updated";
        CompositionEntityValidationData toBeReturned =
                new CompositionEntityValidationData(CompositionEntityType.image, IMAGE1_ID);

        toBeReturned.setErrors(Arrays.asList("#/name: " + updatedName + " is not a valid value." + IMAGE1_ID
                                                     + "is the only possible value for this field"));
        doReturn(toBeReturned).when(compositionEntityDataManagerMock)
                              .validateEntity(any(), any(), any());

        ImageEntity imageEntity = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        Image imageData = new Image();
        imageData.setFileName(updatedName);
        imageData.setDescription(IMAGE1_ID + " desc updated");
        imageEntity.setImageCompositionData(imageData);

        CompositionEntityValidationData output = imageManager.updateImage(imageEntity);
        Assert.assertEquals(output.getErrors(), toBeReturned.getErrors());

    }

    @Test
    public void testGetNonExistingImageId_negative() {
        testGet_negative(VSP_ID, VERSION, COMPONENT_ID, "non existing image id",
                VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test
    public void testGet() {
        ImageEntity expected = createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        doReturn(expected).when(imageDao).get(any());
        String compositionSchema = "schema string";
        doReturn(compositionSchema).when(imageManager).getImageCompositionSchema(any());

        CompositionEntityResponse<Image> response = imageManager.getImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        Assert.assertEquals(response.getId(), expected.getId());
        Assert.assertEquals(response.getData().getFileName(), expected.getImageCompositionData()
                                                                      .getFileName());
        Assert.assertEquals(response.getData().getDescription(), expected.getImageCompositionData().getDescription());
        Assert.assertEquals(response.getSchema(), compositionSchema);
    }

    @Test
    public void testDeleteOnNotManualImage() {
        ImageEntity expected = createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        doReturn(expected).when(imageDao).get(any());
        testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID,
                VendorSoftwareProductErrorCodes.DELETE_IMAGE_NOT_ALLOWED);
    }

    @Test
    public void testDeleteOnNotExistImage() {
        testDelete_negative(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID,
                VersioningErrorCodes.VERSIONABLE_SUB_ENTITY_NOT_FOUND);
    }

    @Test
    public void testDeleteOnManualImage() {
        ImageEntity expected = createImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        doReturn(expected).when(imageDao).get(any());
        doReturn(true).when(vspInfoDao).isManual(any(), any());
        imageManager.deleteImage(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        verify(imageDao).delete(any());
    }

    @Test
    public void testGetQuestionnaire() throws Exception {
        ImageEntity image = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        image.setQuestionnaireData("{}");
        doReturn(image).when(imageDao).getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);

        String schema = "schema string";
        doReturn(schema).when(imageManager).getImageQuestionnaireSchema(any());

        QuestionnaireResponse questionnaire =
                imageManager.getImageQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        Assert.assertNotNull(questionnaire);
        Assert.assertEquals(questionnaire.getData(), image.getQuestionnaireData());
        Assert.assertEquals(questionnaire.getSchema(), schema);
        Assert.assertNull(questionnaire.getErrorMessage());
    }

    @Test
    public void testUpdateManualImageQuestionnaire() throws Exception {
        String json = "{\"md5\" :\"FFDSD33SS\"}";
        doReturn(true).when(vspInfoDao).isManual(any(), any());
        doReturn(new ImageEntity()).when(imageDao).get(any());

        imageManager.updateImageQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID, json);
        verify(imageDao).updateQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID, json);
    }


    @Test
    public void testUpdateManDupImageVerQuestionnaire() throws Exception {
        try {
            String json = "{\"md5\" :\"FFDSD33SS\", \"version\" :\"1.0\"}";
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setId(IMAGE2_ID);
            imageEntity.setQuestionnaireData(json);
            List<ImageEntity> imageEntities = new ArrayList<ImageEntity>() {{
                add(imageEntity);
            }};

            doReturn(true).when(vspInfoDao).isManual(any(), any());
            doReturn(imageEntity).when(imageDao).get(any());
            doReturn(imageEntities).when(imageDao).list(any());
            doReturn(imageEntities.get(0)).when(imageDao)
                                          .getQuestionnaireData(any(), any(), any(), any());

            doReturn(IMAGE_QUEST_SCHEMA).when(imageManager).getImageQuestionnaireSchema(any());

            imageManager.updateImageQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID, json);
            Assert.fail();
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(),
                    VendorSoftwareProductErrorCodes.DUPLICATE_IMAGE_VERSION_NOT_ALLOWED);

        }
    }

    @Test
    public void testUpdateHEATImageQuestionnaireWithFormat() throws Exception {
        String json = "{\"format\" :\"qcow2\"}";
        ImageEntity image = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        image.setQuestionnaireData(json);
        doReturn(image).when(imageDao).get(any());

        doReturn(false).when(vspInfoDao).isManual(any(), any());
        doReturn(IMAGE_QUEST_SCHEMA).when(imageManager).getImageQuestionnaireSchema(any());
        doReturn(image).when(imageDao).getQuestionnaireData(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        String updJson = "{\"format\" :\"aki\"}";
        try {
            imageManager.updateImageQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID, updJson);
            Assert.fail();
        } catch (CoreException ex) {
            Assert.assertEquals(ex.code().id(), VendorSoftwareProductErrorCodes.UPDATE_IMAGE_NOT_ALLOWED);
        }
    }

    @Test
    public void testUpdateImageQuestionnaireWithInvalidFormat() throws Exception {
        String json = "{\"format\" :\"qcow2\"}";
        ImageEntity image = new ImageEntity(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID);
        image.setQuestionnaireData(json);
        doReturn(image).when(imageDao).get(any());

        String updJson = "{\"format\" :\"a22\"}";
        try {
            imageManager.updateImageQuestionnaire(VSP_ID, VERSION, COMPONENT_ID, IMAGE1_ID, updJson);
            Assert.fail();
        } catch (CoreException ex) {
            Assert.assertEquals(ex.code().id(), VendorSoftwareProductErrorCodes.VFC_IMAGE_INVALID_FORMAT);
        }
    }

    private void testCreate_negative(ImageEntity image, String expectedErrorCode) {
        try {
            imageManager.createImage(image);
            Assert.fail();
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

    private void testDelete_negative(String vspId, Version version, String componentId, String nicId,
                                            String expectedErrorCode) {
        try {
            imageManager.deleteImage(vspId, version, componentId, nicId);
            Assert.fail();
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

    private static ImageEntity createImage(String vspId, Version version, String compId, String imageId) {
        ImageEntity imageEntity = new ImageEntity(vspId, version, compId, imageId);
        Image imageData = new Image();
        imageData.setFileName(imageId + "_name");
        imageData.setDescription(imageId + " desc");
        imageEntity.setImageCompositionData(imageData);
        return imageEntity;
    }

    private void testUpdate_negative(String vspId, Version version, String componentId, String imageId,
                                            String expectedErrorCode) {
        try {
            imageManager.updateImage(new ImageEntity(vspId, version, componentId, imageId));
            Assert.fail();
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

    private void testGet_negative(String vspId, Version version, String componentId, String imageId,
                                         String expectedErrorCode) {
        try {
            imageManager.getImage(vspId, version, componentId, imageId);
            Assert.fail();
        } catch (CoreException exception) {
            Assert.assertEquals(exception.code().id(), expectedErrorCode);
        }
    }

}
