package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen.convertor;

import static org.junit.Assert.assertEquals;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Info;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComponentMonitoringUploadEntity;

public class ElementToComponentMonitoringUploadConvertorTest {

    private static final String ENTITY_ID = "entityId1";
    private static final String ARTIFACT_NAME ="testArtifact.zip";

    private ElementToComponentMonitoringUploadConvertor converter = new ElementToComponentMonitoringUploadConvertor();


    @Test
    public void shouldConvertElementToComponentMonitoringUploadEntity() throws IOException {
        ZusammenElement elementToConvert = new ZusammenElement();
        elementToConvert.setElementId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        InputStream inputStreamMock = IOUtils.toInputStream("some test data for my input stream", "UTF-8");
        elementToConvert.setData(inputStreamMock);
        ComponentMonitoringUploadEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID,result.getId());
        assertEquals(ARTIFACT_NAME,result.getArtifactName());
        assertEquals("SNMP_TRAP",result.getType().name());
    }

    @Test
    public void shouldConvertElementInfoToComponentMonitoringUploadEntity() {
        ElementInfo elementToConvert = new ElementInfo();
        elementToConvert.setId(new Id(ENTITY_ID));
        elementToConvert.setInfo(createInfo());
        ComponentMonitoringUploadEntity result = converter.convert(elementToConvert);
        assertEquals(ENTITY_ID,result.getId());
        assertEquals(ARTIFACT_NAME,result.getArtifactName());
        assertEquals("SNMP_TRAP",result.getType().name());
    }


    private Info createInfo() {
        Info info = new Info();
        info.setName("SNMP_TRAP");
        info.addProperty( "artifactName", ARTIFACT_NAME);
        return info;
    }

}
