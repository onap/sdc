package org.openecomp.sdc.fe.config;

import org.junit.Test;

public class ConnectionTest {

    private Connection createTestSubject() {
        return new Connection();
    }

    @Test
    public void testGetUrl() {
        Connection testSubject;
        String result;

        testSubject = createTestSubject();
        result = testSubject.getUrl();
    }

    @Test
    public void testSetUrl() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.setUrl("http://test");
    }


    @Test
    public void testGetPoolSize() {
        Connection testSubject;
        Integer result;

        testSubject = createTestSubject();
        result = testSubject.getPoolSize();
    }

    @Test
    public void testSetPoolSize() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.setPoolSize(10);
    }

    @Test
    public void testToString() {
        Connection testSubject;

        testSubject = createTestSubject();
        testSubject.toString();
    }


}
