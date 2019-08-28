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

import java.util.Base64;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;
import org.openecomp.sdc.securityutil.CipherUtil;
import org.openecomp.sdc.securityutil.CipherUtilException;

import static org.junit.Assert.*;

public class CipherUtilTest {

    private static final String KEY = "AGLDdG4D04BKm2IxIWEr8o==";
    private static final String DATA = "data";

    @Test
    public void encryptDecryptPKC() throws CipherUtilException {
        String generatedKey = RandomStringUtils.randomAlphabetic(16);
        String base64Key = Base64.getEncoder().encodeToString(generatedKey.getBytes());
        String encrypted = CipherUtil.encryptPKC(DATA, base64Key);
        assertNotEquals(DATA, encrypted);
        String decrypted = CipherUtil.decryptPKC(encrypted, base64Key);
        assertEquals(decrypted, DATA);
    }

    @Test
    public void encryptInvalidKey() {
        try {
            CipherUtil.encryptPKC(DATA, "invalidKey");
            fail();
        } catch (CipherUtilException ex) {
            assertTrue(ex.getMessage().contains("Invalid AES key length"));
        }
    }

    @Test
    public void decryptInvalidKey() {
        try {
            CipherUtil.decryptPKC(DATA, "invalidKey");
            fail();
        } catch (CipherUtilException ex) {
            assertTrue(ex.getMessage().contains("length"));
        }
    }

    @Test
   public void decryptInvalidData() {
        try {
            CipherUtil.decryptPKC(DATA, KEY);
          fail();
       } catch (CipherUtilException ex) {
          assertTrue(ex.getMessage().contains("Wrong IV length"));
        }
    }
}
