package org.openecomp.core.enrichment.types;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class ComponentProcessInfoTest {

    @Test
    public void verifiedContentValue() throws IOException {
        ComponentProcessInfo componentProcessInfo = new ComponentProcessInfo();
        String contentData = "my test content data";
        componentProcessInfo.setContent(contentData.getBytes());
        InputStream getterContent = componentProcessInfo.getContent();
        Assert.assertEquals(contentData, IOUtils.toString(getterContent, StandardCharsets.UTF_8.name()));

    }
}