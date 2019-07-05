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

    public static byte[] getFileResource(final String filePath) throws IOException {
        final InputStream inputStream = ClassLoader.class.getResourceAsStream(filePath);
        if (inputStream == null) {
            throw new IOException(String.format("Could not find the resource on path \"%s\"", filePath));
        }
        return IOUtils.toByteArray(inputStream);
    }
}
