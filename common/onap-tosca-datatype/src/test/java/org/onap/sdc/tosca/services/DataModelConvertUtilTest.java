package org.onap.sdc.tosca.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.onap.sdc.tosca.datatypes.model.Import;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;


public class DataModelConvertUtilTest {

    @Test
    public void testConvertToscaImport() throws Exception {
        ServiceTemplate serviceTemplate;
        String inputResourceName = "/mock/serviceTemplate/importConvertTest.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            serviceTemplate = toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
        }
        assertNotNull(((Map)serviceTemplate.getImports().get(0)).get("data"));
        assertNotNull(((Map)serviceTemplate.getImports().get(1)).get("artifacts"));
        assertNotNull(((Map)serviceTemplate.getImports().get(2)).get("capabilities"));
        assertNotNull(((Map)serviceTemplate.getImports().get(3)).get("api_interfaces"));
        assertNotNull(((Map)serviceTemplate.getImports().get(4)).get("api_util_relationships"));
        assertNotNull(((Map)serviceTemplate.getImports().get(5)).get("common"));
        assertNotNull(((Map)serviceTemplate.getImports().get(6)).get("api_util"));
        assertNotNull(((Map)serviceTemplate.getImports().get(7)).get("relationshipsExt"));
        assertNotNull(((Map)serviceTemplate.getImports().get(8)).get("some_definition_file"));
        assertNotNull(((Map)serviceTemplate.getImports().get(9)).get("site_index"));
    }

    @Test
    public void testConvertToscaImportForEmptyImport() throws Exception {
        ServiceTemplate serviceTemplate;
        String inputResourceName = "/mock/serviceTemplate/importConvertTestNoImport.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            serviceTemplate = toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
        }

        Assert.assertNull(serviceTemplate.getImports());
    }

    @Test
    public void testInvalidToscaImportSection() {
        String inputResourceName = "/mock/serviceTemplate/invalidToscaImport.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot create property=imports"));
        }
    }

    @Test
    public void testEmptyImportList() throws Exception {
        String inputResourceName = "/mock/serviceTemplate/emptyImportList.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            ServiceTemplate serviceTemplate = toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
            assertNull(serviceTemplate.getImports());
        }
    }

    @Test
    public void testConvertToscaImportMultiLineGrammar() throws Exception {
        ServiceTemplate serviceTemplate;
        String inputResourceName = "/mock/serviceTemplate/importMultiLineGrammar.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            serviceTemplate = toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
        }
        Map<String, Import> parsedMultiLineImport = (Map<String, Import>) serviceTemplate.getImports().get(0);
        assertNotNull(parsedMultiLineImport);
        Import multiLineGrammarImport = parsedMultiLineImport.get("multi_line_grammar_import");
        assertNotNull(multiLineGrammarImport);
        assertEquals(multiLineGrammarImport.getFile(), "path1/path2/importFile.yaml");
        assertEquals(multiLineGrammarImport.getRepository(), "service_repo");
        assertEquals(multiLineGrammarImport.getNamespace_uri(), "http://test.xyz/tosca/1.0/platform");
        assertEquals(multiLineGrammarImport.getNamespace_prefix(), "ns_prefix");
    }


    @Test
    public void testConvertToscaImportInvalidMultiLineGrammar() {
        String inputResourceName = "/mock/serviceTemplate/invalidImportMultiLineGrammar.yml";
        URL resource = this.getClass().getResource(inputResourceName);
        File inputPayload = new File(resource.getFile());
        ToscaExtensionYamlUtil toscaExtensionYamlUtil = new ToscaExtensionYamlUtil();
        try (FileInputStream fis = new FileInputStream(inputPayload)) {
            toscaExtensionYamlUtil.yamlToObject(fis, ServiceTemplate.class);
            Assert.fail();
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("Cannot create property=imports"));
        }
    }
}
