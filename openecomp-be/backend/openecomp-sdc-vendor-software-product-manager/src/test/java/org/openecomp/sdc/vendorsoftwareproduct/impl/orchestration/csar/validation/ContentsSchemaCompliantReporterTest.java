package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.sdc.tosca.csar.ManifestTokenType;
import org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding.OnboardingPackageContentHandler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.openecomp.sdc.be.config.NonManoArtifactType.ONAP_PM_DICTIONARY;
import static org.openecomp.sdc.be.test.util.TestResourcesHandler.getResourceBytesOrFail;
import static org.openecomp.sdc.tosca.csar.ManifestTokenType.*;


public class ContentsSchemaCompliantReporterTest {

    private ContentsSchemaCompliantReporter contentsSchemaCompliantReporter;
    private OnboardingPackageContentHandler contentHandler;
    private ManifestBuilder manifestBuilder;
    private String nonManoSource = "Files/Measurements/PM_Dictionary.yaml";
    private String failReasonMessage = "The actual error message and expected error message lists should be the same";

    @Before
    public void setUp() throws IOException {
        manifestBuilder = getPnfManifestSampleBuilder();
        contentHandler = new OnboardingPackageContentHandler();
        contentsSchemaCompliantReporter = new ContentsSchemaCompliantReporter();
    }

    @Test
    public void shouldReportEmptyYamlMessage_whenPmDictionaryIsEmpty() {
        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/empty.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> expectedErrorMessageList = Arrays.asList("PM_Dictionary YAML file is empty");
        List<String> paths = Arrays.asList(nonManoSource);

        //when
        List<String> errorMessageList = contentsSchemaCompliantReporter.report(paths, contentHandler);

        //then
        assertThat(failReasonMessage
                , errorMessageList, containsInAnyOrder(expectedErrorMessageList.toArray())
        );
    }

    @Test
    public void shouldNotReportAnyMessage_whenPmDictionaryIsCorrect() {
        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-valid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> paths = Arrays.asList(nonManoSource);

        //when
        List<String> errorMessageList = contentsSchemaCompliantReporter.report(paths, contentHandler);

        //then
        assertThat("The actual error message should be empty"
                ,  errorMessageList, is(empty())
        );
    }

    @Test
    public void shouldReportKeyNotFoundMessage_whenPmDictionaryHeaderIsMissing() {
        //given
        contentHandler.addFile(nonManoSource, getResourceBytesOrFail("validation.files/measurements/pmEvents-invalid.yaml"));
        manifestBuilder.withNonManoArtifact(ONAP_PM_DICTIONARY.getType(), nonManoSource);
        List<String> expectedErrorMessageList = Arrays.asList("Key not found: pmDictionaryHeader");
        List<String> paths = Arrays.asList(nonManoSource);

        //when
        List<String> errorMessageList = contentsSchemaCompliantReporter.report(paths, contentHandler);

        //then
        assertThat(failReasonMessage
                , errorMessageList, containsInAnyOrder(expectedErrorMessageList.toArray())
        );
    }

    private ManifestBuilder getPnfManifestSampleBuilder() {
        return new ManifestBuilder()
                .withMetaData(PNFD_NAME.getToken(), "myPnf")
                .withMetaData(ManifestTokenType.PNFD_PROVIDER.getToken(), "ACME")
                .withMetaData(PNFD_ARCHIVE_VERSION.getToken(), "1.0")
                .withMetaData(PNFD_RELEASE_DATE_TIME.getToken(), "2019-03-11T11:25:00+00:00");
    }

}
