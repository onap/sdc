package org.openecomp.core.converter.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.openecomp.core.converter.datatypes.Constants.ONAP_INDEX;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.DEFININTION_VERSION;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.HEAT_INDEX;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.TEMPLATE_NAME_PROPERTY;
import static org.openecomp.sdc.common.utils.CommonUtil.validateAndUploadFileContent;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.core.impl.GlobalSubstitutionServiceTemplate;
import org.openecomp.core.impl.ToscaConverterImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;

import java.net.URL;
import java.util.Map;
import java.util.Set;
public class ToscaConvertorDefinitionsTest {


    @Test
    public void loadCsar() throws Exception {
        URL resource = ToscaConvertorDefinitionsTest.class.getResource("/csar/vCSCF.csar");
        byte[] bytes = IOUtils.toByteArray(resource);
        assertNotNull(bytes);
        FileContentHandler contentMap = validateAndUploadFileContent(OnboardingTypesEnum.CSAR, bytes);
        ToscaConverterImpl toscaConverter = new ToscaConverterImpl();
        ToscaServiceModel convert = toscaConverter.convert(contentMap);
        Map<String, ServiceTemplate> serviceTemplates = convert.getServiceTemplates();
        assertTrue(serviceTemplates.containsKey(GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME));
        ServiceTemplate serviceTemplate = serviceTemplates.get(GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME);

        assertNotNull(serviceTemplate);
        assertTrue(serviceTemplate instanceof GlobalSubstitutionServiceTemplate);

        assertNotNull(serviceTemplate.getMetadata());
        assertFalse(serviceTemplate.getMetadata().isEmpty());
        assertTrue(serviceTemplate.getMetadata().containsKey(TEMPLATE_NAME_PROPERTY));

        assertNotNull(serviceTemplate.getImports());
        assertFalse(serviceTemplate.getImports().isEmpty());
        assertEquals(1 ,serviceTemplate.getImports().size());
        assertTrue(serviceTemplate.getImports().get(0).containsKey(HEAT_INDEX));
        assertTrue(serviceTemplate.getImports().get(0).containsKey(ONAP_INDEX));

        assertEquals(DEFININTION_VERSION, serviceTemplate.getTosca_definitions_version());


        assertNotNull(serviceTemplate.getNode_types());
        assertEquals(1, serviceTemplate.getNode_types().size());
        Set<String> keys = serviceTemplate.getNode_types().keySet();
        assertTrue(keys.contains("tosca.nodes.nfv.ext.zte.VNF.vCSCF"));
    }


}
