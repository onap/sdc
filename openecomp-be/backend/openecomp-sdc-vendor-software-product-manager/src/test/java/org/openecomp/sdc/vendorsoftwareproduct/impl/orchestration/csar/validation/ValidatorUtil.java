package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides util methods for Validation Test classes.
 */

class ValidatorUtil {

    private ValidatorUtil(){

    }

    public static byte[] getFileResource(String filePath) throws IOException{
        InputStream inputStream = ClassLoader.class.getClass().getResourceAsStream(filePath);
        return IOUtils.toByteArray(inputStream);
    }
}
