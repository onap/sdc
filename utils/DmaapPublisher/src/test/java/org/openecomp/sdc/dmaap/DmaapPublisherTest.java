package org.openecomp.sdc.dmaap;

import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DmaapPublisherTest {
    @Test
    public void main() throws Exception {
        File resource = new File("src/test/resources");
        String absPath = resource.getAbsolutePath();

        String msg = "{\"operationalEnvironmentId\":\"12345\",\"operationalEnvironmentName\":\"Op_Env_Name\",\"operationalEnvironmentType\":\"ECOMP\",\"tenantContext\":\"Test\",\"workloadContext\":\"VNF_E2E-IST\",\"action\":\"CREATE\"}";
        String cmd = "-cr 5 "+ "-notification=" + msg+ " -path "+absPath+" -yaml catalogMgmtTest.yaml" ;
        DmaapPublisher.main( cmd.split(" ") );
        Thread.sleep(10000);
    }
}