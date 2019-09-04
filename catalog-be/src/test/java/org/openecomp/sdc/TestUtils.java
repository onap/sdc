package org.openecomp.sdc;

import org.apache.commons.io.IOUtils;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

public class TestUtils {
    public static boolean downloadedPayloadMatchesExpected(Response response, byte[] expected) {
        boolean bRet = false;
        try (InputStream is = response.readEntity(InputStream.class)) {
            byte[] body = IOUtils.toByteArray(is);
            assertArrayEquals(expected, body);
            bRet = true;
        } catch(IOException ex) {
            bRet = false;
        }
        return bRet;
    }
}
