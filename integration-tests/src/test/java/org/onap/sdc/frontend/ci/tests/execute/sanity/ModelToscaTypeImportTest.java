
package org.onap.sdc.frontend.ci.tests.execute.sanity;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ComponentType;
import org.onap.sdc.backend.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.onap.sdc.backend.ci.tests.datatypes.http.HttpHeaderEnum;
import org.onap.sdc.backend.ci.tests.utils.general.ElementFactory;
import org.onap.sdc.frontend.ci.tests.datatypes.ComponentData;
import org.onap.sdc.frontend.ci.tests.datatypes.ModelName;
import org.onap.sdc.frontend.ci.tests.datatypes.ResourceCreateData;
import org.onap.sdc.frontend.ci.tests.datatypes.ToscaTypeUploadEnum;
import org.onap.sdc.frontend.ci.tests.execute.setup.DriverFactory;
import org.onap.sdc.frontend.ci.tests.execute.setup.SetupCDTest;
import org.onap.sdc.frontend.ci.tests.flow.AddNodeToCompositionFlow;
import org.onap.sdc.frontend.ci.tests.flow.CreateVfFlow;
import org.onap.sdc.frontend.ci.tests.flow.DownloadCsarArtifactFlow;
import org.onap.sdc.frontend.ci.tests.flow.exception.UiTestFlowRuntimeException;
import org.onap.sdc.frontend.ci.tests.pages.ResourceCreatePage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.CompositionPage;
import org.onap.sdc.frontend.ci.tests.pages.component.workspace.ToscaArtifactsPage;
import org.onap.sdc.frontend.ci.tests.pages.home.HomePage;
import org.onap.sdc.frontend.ci.tests.utilities.FileHandling;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.DependsOn;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ModelToscaTypeImportTest extends SetupCDTest {

    private final static String NODE_TYPE_TO_ADD = "Network";
    private final static String MODEL_VNFD_TYPES = "vnfd_types";
    private final static String MODEL_NSD_TYPES = "nsd_types";
    private final static String TYPE_URL = "http://localhost:8080/sdc2/rest/v1/catalog/uploadType/%s";
    private final static String NODE_TYPE_URL = "http://localhost:8080/sdc2/rest/v1/catalog/upload/multipart";
    private final static String IMPORT_PATH =
            System.getProperty("user.dir").concat("/src/test/resources/Files/imports/tosca");
    private final static String NODE_TYPE_DIRECTORY = IMPORT_PATH.concat(File.separator).concat("node-types");

    private WebDriver webDriver;

    @BeforeMethod
    public void init() {
        webDriver = DriverFactory.getDriver();
    }

    @Test
    public void importTypesForModel() throws Exception {
        for (ToscaTypeUploadEnum type : ToscaTypeUploadEnum.values()) {
            postType(type.getUrlSuffix(), type.getModelParam(), type.getDirectory(), type.getZipParam(),
                    type.isMetadata());
        }
    }

    @Test
    public void addNodeType() throws Exception {
        postNodeType(NODE_TYPE_TO_ADD);
    }

    @Test
    @DependsOn("addNodeType")
    public void verifyToscaTypesIncludedInCsar() throws Exception {
        final HomePage home = new HomePage(webDriver);

        ResourceCreateData vf = new ResourceCreateData();
        vf.setRandomName(ElementFactory.getResourcePrefix() + "-VF");
        vf.setCategory(ResourceCategoryEnum.GENERIC_ABSTRACT.getSubCategory());
        vf.setTagList(Arrays.asList(vf.getName()));
        vf.setDescription("Test");
        vf.setVendorName("EST");
        vf.setVendorRelease("2.5.1");
        vf.setVendorModelNumber("0001");
        vf.setModel(ModelName.ETSI_SOL001_v2_5_1.getName());

        final CreateVfFlow createVfFlow = new CreateVfFlow(webDriver, vf);
        createVfFlow.run(home);
        final ResourceCreatePage resourceCreatePage = createVfFlow.getLandedPage()
                .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ResourceCreatePage"));
        resourceCreatePage.isLoaded();

        final ComponentData parentComponent = new ComponentData();
        parentComponent.setName(vf.getName());
        parentComponent.setVersion("0.1");
        parentComponent.setComponentType(ComponentType.RESOURCE);
        final ComponentData addComponent = new ComponentData();
        addComponent.setName("Network");
        addComponent.setVersion("1.0");
        addComponent.setComponentType(ComponentType.RESOURCE);

        final CompositionPage compositionPage = resourceCreatePage.goToComposition();
        compositionPage.isLoaded();
        final AddNodeToCompositionFlow addNodeToCompositionFlow =
                new AddNodeToCompositionFlow(webDriver, parentComponent, addComponent);
        addNodeToCompositionFlow.run(compositionPage);
        compositionPage.isLoaded();
        compositionPage.goToServiceGeneral();

        final DownloadCsarArtifactFlow downloadCsarArtifactFlow = new DownloadCsarArtifactFlow(webDriver);
        downloadCsarArtifactFlow.setWaitBeforeGetTheFile(5L);
        downloadCsarArtifactFlow.run(resourceCreatePage);
        final ToscaArtifactsPage toscaArtifactsPage = downloadCsarArtifactFlow.getLandedPage()
                .orElseThrow(() -> new UiTestFlowRuntimeException("Missing expected ToscaArtifactsPage"));
        assertTrue(toscaArtifactsPage.getDownloadedArtifactList().size() > 0, "No artifact download was found");
        toscaArtifactsPage.getDownloadedArtifactList().get(0);

        final String downloadFolderPath = getConfig().getDownloadAutomationFolder();
        final Map<String, byte[]> csarFiles =
                FileHandling.getFilesFromZip(downloadFolderPath, toscaArtifactsPage.getDownloadedArtifactList().get(0));
        assertNotNull(csarFiles.keySet().parallelStream()
                .filter(filename -> filename.contains(NODE_TYPE_TO_ADD.concat("-template.yml"))).findAny());
        assertNotNull(csarFiles.keySet().parallelStream()
                .filter(filename -> filename.contains(MODEL_VNFD_TYPES.concat(".yml"))).findAny());
        assertNotNull(csarFiles.keySet().parallelStream()
                .filter(filename -> filename.contains(MODEL_NSD_TYPES.concat(".yml"))).findAny());
    }

    private void postNodeType(final String directory) throws Exception {
        final File yamlDirectory = new File(NODE_TYPE_DIRECTORY.concat(File.separator).concat(directory));
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("resourceMetadata", getJsonString(yamlDirectory));
        final File zipFile = getImportZipFile(yamlDirectory);
        builder.addBinaryBody("resourceZip", zipFile, ContentType.APPLICATION_OCTET_STREAM, zipFile.getName());
        postEntity(NODE_TYPE_URL, "jh0003", builder.build());
        zipFile.delete();
    }

    private void postType(final String urlSuffix, final String modelParam, final String directory, String zipParam,
            boolean metadata) throws Exception {
        final File yamlDirectory = new File(IMPORT_PATH.concat(File.separator).concat(directory));
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (metadata) {
            builder.addTextBody("toscaTypeMetadata", getJsonString(yamlDirectory));
        }
        builder.addTextBody("model", ModelName.ETSI_SOL001_v2_5_1.getName());
        final File zipFile = getImportZipFile(yamlDirectory);
        builder.addBinaryBody(zipParam, zipFile, ContentType.APPLICATION_OCTET_STREAM, zipFile.getName());

        postEntity(String.format(TYPE_URL, urlSuffix), "cs0008", builder.build());
        zipFile.delete();
    }

    private void postEntity(final String url, final String user, final HttpEntity entity) throws Exception {
        final CloseableHttpClient client = HttpClients.createDefault();
        final HttpPost httpPost = new HttpPost(String.format(url));

        httpPost.setHeader(HttpHeaderEnum.USER_ID.getValue(), user);
        httpPost.setEntity(entity);

        final CloseableHttpResponse response = client.execute(httpPost);
        assertEquals(201, response.getStatusLine().getStatusCode());
        client.close();

    }

    private File getImportZipFile(final File yamlDirectory) throws Exception {
        final File yml = yamlDirectory.listFiles((dir, filename) -> filename.toLowerCase().endsWith(".yml"))[0];
        final String zipName = yamlDirectory.getAbsolutePath().concat(File.separator)
                .concat(yml.getName().replace(".yml", "")).concat(".zip");
        final FileOutputStream fos = new FileOutputStream(zipName);
        final ZipOutputStream zipOut = new ZipOutputStream(fos);
        final File fileToZip = yml;
        final FileInputStream fis = new FileInputStream(fileToZip);
        final ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);
        final byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }
        zipOut.close();
        fis.close();
        fos.close();
        return new File(zipName);
    }

    private String getJsonString(final File yamlDirectory) throws Exception {
        return new String(Files.readAllBytes(
                Paths.get(yamlDirectory.listFiles((dir, filename) -> filename.toLowerCase().endsWith(".json"))[0]
                        .getAbsolutePath())));
    }
}
