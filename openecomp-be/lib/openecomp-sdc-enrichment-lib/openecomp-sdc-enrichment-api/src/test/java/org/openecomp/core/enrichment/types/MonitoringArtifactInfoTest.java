package org.openecomp.core.enrichment.types;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

public class MonitoringArtifactInfoTest {

    @Test
    public void verifiedContentValue() throws IOException {
        MonitoringArtifactInfo monitoringArtifactInfo = new MonitoringArtifactInfo();
        String contentData = "my test content data";
        monitoringArtifactInfo.setContent(contentData.getBytes());
        InputStream getterContent = monitoringArtifactInfo.getContent();
        Assert.assertEquals(contentData, IOUtils.toString(getterContent, StandardCharsets.UTF_8.name()));

    }
}