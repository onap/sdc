/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.dmaap;

import org.junit.Test;

import java.io.File;

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
