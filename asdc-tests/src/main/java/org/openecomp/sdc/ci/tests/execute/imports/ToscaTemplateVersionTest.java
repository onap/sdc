package org.openecomp.sdc.ci.tests.execute.imports;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Map;

import org.junit.rules.TestName;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.ci.tests.api.ComponentBaseTest;
import org.openecomp.sdc.ci.tests.datatypes.ImportReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.ServiceReqDetails;
import org.openecomp.sdc.ci.tests.datatypes.enums.ServiceCategoriesEnum;
import org.openecomp.sdc.ci.tests.datatypes.enums.UserRoleEnum;
import org.openecomp.sdc.ci.tests.datatypes.http.RestResponse;
import org.openecomp.sdc.ci.tests.utils.ToscaParserUtils;
import org.openecomp.sdc.ci.tests.utils.general.ElementFactory;
import org.openecomp.sdc.ci.tests.utils.general.ImportUtils;
import org.openecomp.sdc.ci.tests.utils.rest.BaseRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResourceRestUtils;
import org.openecomp.sdc.ci.tests.utils.rest.ResponseParser;
import org.openecomp.sdc.ci.tests.utils.rest.ServiceRestUtils;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ToscaTemplateVersionTest extends ComponentBaseTest {

    private static final String EXPECTED_EXPORT_TOSCA_VERSION = "tosca_simple_yaml_1_1";
    private static final String YML_110_VERSION = "loadBalancerTestVersion.yml";
    public static TestName name = new TestName();
    private String SOURCE_DIR;
    private static String WORK_DIR = "importToscaResourceByCreateUrl";

    public ToscaTemplateVersionTest() {
        super(name, ToscaTemplateVersionTest.class.getName());
    }

    @BeforeMethod
    public void setUp() throws Exception {
        SOURCE_DIR = config.getResourceConfigDir();
    }

    @Test
    public void verifyExportYamlVersion() throws Exception {
        User defaultUser = ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER);
        ServiceReqDetails serviceDetails = ElementFactory.getDefaultService("ciYamlVersionTest", ServiceCategoriesEnum.MOBILITY, defaultUser.getUserId());
        RestResponse serviceResponse = ServiceRestUtils.createService(serviceDetails, defaultUser);
        BaseRestUtils.checkCreateResponse(serviceResponse);
        Service service = ResponseParser.parseToObjectUsingMapper(serviceResponse.getResponse(), Service.class);
        Map<String, Object> load = ToscaParserUtils.downloadAndParseToscaTemplate(defaultUser, service);
        assertEquals(ToscaParserUtils.getToscaVersion(load), EXPECTED_EXPORT_TOSCA_VERSION);
    }

    @Test
    public void testVerifyImportYamlVersion_1_1_0() throws Exception {
        String testResourcesPath = Paths.get(SOURCE_DIR, WORK_DIR).toString();
        ImportReqDetails importReqDetails = ImportUtils.getImportResourceDetailsByPathAndName(ElementFactory.getDefaultImportResource(), testResourcesPath,
                YML_110_VERSION);
        RestResponse importResourceResponse = ResourceRestUtils.createImportResource(importReqDetails, ElementFactory.getDefaultUser(UserRoleEnum.DESIGNER),
                null);
        BaseRestUtils.checkCreateResponse(importResourceResponse);

    }
}
