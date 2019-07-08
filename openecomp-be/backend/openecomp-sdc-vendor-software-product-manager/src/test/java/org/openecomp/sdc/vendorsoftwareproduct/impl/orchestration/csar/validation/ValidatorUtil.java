package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Provides util methods for Validation Test classes.
 */

class ValidatorUtil {

    private ValidatorUtil() {

    }

    /**
     * Reads a file and coverts it to a byte array.
     *
     * @param filePath      The file path
     * @return
     *  The file byte array
     * @throws IOException
     *  When the file was not found or the input stream could not be opened
     */
    public static byte[] getFileResource(final String filePath) throws IOException {
        try(final InputStream inputStream = ClassLoader.class.getResourceAsStream(filePath)) {
            if (inputStream == null) {
                throw new IOException(String.format("Could not find the resource on path \"%s\"", filePath));
            }
            return IOUtils.toByteArray(inputStream);
        } catch (final IOException ex) {
            throw new IOException(String.format("Could not open the input stream for resource on path \"%s\"", filePath), ex);
        }
    }
}
