package org.openecomp.sdc.ci.tests.execute.sanity;

import com.aventstack.extentreports.Status;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.ci.tests.datatypes.DataTestIdEnum.ToscaArtifactsScreenEnum;
import org.openecomp.sdc.ci.tests.datatypes.ResourceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.NormativeTypesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.ResourceCategoryEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.execute.setup.SetupCDTest;
import org.openecomp.sdc.ci.tests.pages.ResourceGeneralPage;
import org.openecomp.sdc.ci.tests.tosca.datatypes.ToscaDefinition;
import org.openecomp.sdc.ci.tests.utilities.FileHandling;
import org.openecomp.sdc.ci.tests.utilities.GeneralUIUtils;
import org.openecomp.sdc.ci.tests.utilities.ResourceUIUtils;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class Annotation extends SetupCDTest {
	private String filePath;

	@BeforeMethod
	public void beforeTest() {
		filePath = FileHandling.getFilePath("SRIOV");
	}

	@Test
	public void importCsarWithAnnotationVerifyDownloadYmlContainsAnnotationSection() throws Exception {
		String fileName = "SIROV_annotations_VSP.csar";
		ResourceReqDetails vfMetaData = ElementFactory.getDefaultResourceByTypeNormTypeAndCatregory(ResourceTypeEnum.VF,
				NormativeTypesEnum.ROOT, ResourceCategoryEnum.NETWORK_L2_3_ROUTERS, getUser());
		ResourceUIUtils.importVfFromCsar(vfMetaData, filePath, fileName, getUser());
		getExtendTest().log(Status.INFO, "Csar with annotations imported successfully.");
		ResourceGeneralPage.getLeftMenu().moveToToscaArtifactsScreen();
		GeneralUIUtils.clickOnElementByTestId(ToscaArtifactsScreenEnum.TOSCA_MODEL.getValue());
		File latestFilefromDir = FileHandling.getLastModifiedFileNameFromDir();
		ToscaDefinition toscaMainVfDefinition = ToscaParserUtils.parseToscaMainYamlToJavaObjectByCsarLocation(latestFilefromDir);
		assertTrueAnnotationTestSuite(toscaMainVfDefinition);
		getExtendTest().log(Status.INFO, "Success to validate the ToscaMainYaml contains annotation type source with properties.");
	}

	
	public void assertTrueAnnotationTestSuite(ToscaDefinition toscaMainVfDefinition) {
		assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").annotations).containsKey("source");
		assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").getAnnotations().get("source").getType()).isEqualTo("org.openecomp.annotations.Source");
		assertThat(toscaMainVfDefinition.getTopology_template().getInputs().get("availabilityzone_name").getAnnotations().get("source").getProperties().get("source_type")).isEqualTo("HEAT");
	}
	
	@Override
	protected UserRoleEnum getRole() {
		return UserRoleEnum.DESIGNER;
	}

}
