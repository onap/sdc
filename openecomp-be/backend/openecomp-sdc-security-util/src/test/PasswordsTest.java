package org.onap.sdc.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordsTest {

    @Test
    public void hashPassword() throws Exception {
        String hash = Passwords.hashPassword("hello1234");
        assertTrue(Passwords.isExpectedPassword("hello1234", hash));

        //test different salt-> result in different hash
        String hash2 = Passwords.hashPassword("hello1234");
        assertFalse(hash.equals(hash2));

        String hash3  = Passwords.hashPassword("");
        assertTrue(Passwords.isExpectedPassword("", hash3));

        String hash4  = Passwords.hashPassword(null);
        assertTrue(hash4 == null);
    }

    @Test
    public void isExpectedPassword() throws Exception {
        //region isExpectedPassword(String password, String salt, String hash)
        assertTrue(Passwords.isExpectedPassword(null, null, null));
        //valid hash
        assertTrue(Passwords.isExpectedPassword("hello1234", "e0277df331f4ff8f74752ac4a8fbe03b", "6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0"));
        //invalid salt
        assertFalse(Passwords.isExpectedPassword("hello1234", "c0000df331f4ff8f74752ac4a00be03c", "6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0"));
        assertFalse(Passwords.isExpectedPassword("hello1234", null, "6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0"));
        //exacly 1 param uninitialized
        assertFalse(Passwords.isExpectedPassword("hello1234", "", null));
        assertFalse(Passwords.isExpectedPassword(null, "", "hello1234"));
        //no salt & no hash
        assertFalse(Passwords.isExpectedPassword("hello1234", null, "hello1234"));
        //endregion

        //region isExpectedPassword(String password, String expectedHash)
        assertTrue(Passwords.isExpectedPassword(null, null));
        //valid hash
        assertTrue(Passwords.isExpectedPassword("hello1234", "e0277df331f4ff8f74752ac4a8fbe03b:6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0"));
        //invalid salt
        assertFalse(Passwords.isExpectedPassword("hello1234", "c0000df331f4ff8f74752ac4a00be03c:6dfbad308cdf53c9ff2ee2dca811ee92f1b359586b33027580e2ff92578edbd0"));
        //exacly 1 param uninitialized
        assertFalse(Passwords.isExpectedPassword("hello1234", null));
        assertFalse(Passwords.isExpectedPassword(null, "hello1234"));
        //no salt & no hash
        assertFalse(Passwords.isExpectedPassword("hello1234", "hello1234"));
        //endregion
    }

    @Test
    public void hashtest() {
        String password = "123456";
        String hash = Passwords.hashPassword(password);
        assertTrue(Passwords.isExpectedPassword(password, hash));
        password = "1sdfgsgd23456";
        hash = Passwords.hashPassword(password);
        assertTrue(Passwords.isExpectedPassword(password, hash));
        password = "1sdfgsgd2345((*&%$%6";
        hash = Passwords.hashPassword(password);
        assertTrue(Passwords.isExpectedPassword(password, hash));
        password = "";
        hash = Passwords.hashPassword(password);
        assertTrue(Passwords.isExpectedPassword(password, hash));
        password = " ";
        hash = Passwords.hashPassword(password);
        assertTrue(Passwords.isExpectedPassword(password, hash));
    }


}