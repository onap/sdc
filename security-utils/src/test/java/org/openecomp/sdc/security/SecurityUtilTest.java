package org.openecomp.sdc.security;

import org.junit.Test;
import java.util.Base64;
import static org.junit.Assert.*;

public class SecurityUtilTest {

    @Test
    public void encryptDecryptAES128() throws Exception {
        String data = "decrypt SUCCESS!!";
        String encrypted = SecurityUtil.INSTANCE.encrypt(data).left().value();
        assertNotEquals( data, encrypted );
        byte[] decryptMsg = Base64.getDecoder().decode(encrypted);
        assertEquals( SecurityUtil.INSTANCE.decrypt( decryptMsg , false ).left().value() ,data );
        assertEquals( SecurityUtil.INSTANCE.decrypt( encrypted.getBytes() , true ).left().value() ,data );
    }

    @Test
    public void obfuscateKey() throws Exception {
        String key = "abcdefghij123456";
        String expectedkey = "********ij123456";
        String obfuscated = SecurityUtil.INSTANCE.obfuscateKey( key );
        System.out.println( obfuscated );
        assertEquals( obfuscated , expectedkey );
    }
}