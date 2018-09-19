package org.openecomp.sdc.be.components.impl.model;

import org.junit.Test;
import org.openecomp.sdc.be.model.normatives.ToscaTypeMetadata;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ToscaTypeImportDataImplTest {

    @Test
    public void testToscaTypeImportData() {
        Map<String, ToscaTypeMetadata> toscaTypeMetadata = new HashMap<>();
        ToscaTypeImportData test = new ToscaTypeImportData("toscaTypeYaml",toscaTypeMetadata);
        assertEquals(test.getToscaTypesYml(), "toscaTypeYaml") ;
        assertEquals(test.getToscaTypeMetadata(),toscaTypeMetadata);
    }
}
