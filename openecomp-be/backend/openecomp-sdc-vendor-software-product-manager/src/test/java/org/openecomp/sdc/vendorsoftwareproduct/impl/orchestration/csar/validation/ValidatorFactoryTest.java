package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.core.utilities.file.FileContentHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.ENTRY_CHANGE_LOG;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.ENTRY_DEFINITIONS;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.ENTRY_MANIFEST;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.ENTRY_SEPARATOR;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_CHANGELOG_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_DEFINITION_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_MANIFEST_FILEPATH;
import static org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration.csar.validation.TestConstants.TOSCA_METADATA_FILEPATH;

public class ValidatorFactoryTest {

    private String metaFile;
    private FileContentHandler handler;

    @Before
    public void setUp(){
        handler = new FileContentHandler();
        metaFile =
                "TOSCA-Meta-File-Version: 1.0\n" +
                "CSAR-Version: 1.1\n" +
                "Created-by: Bilal Iqbal\n";
    }

    @Test(expected = IOException.class)
    public void testGivenEmptyMetaFile_thenIOExceptionIsThrown() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        ValidatorFactory.getValidator(handler);
    }

    @Test
    public void testGivenEmptyBlock0_thenONAPCsarValidatorIsReturned() throws IOException{
        handler.addFile(TOSCA_METADATA_FILEPATH, " ".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }


    @Test
    public void testGivenNonSOL004MetaDirectoryCompliantMetaFile_thenONAPCSARValidatorIsReturned() throws IOException{
        metaFile = metaFile +
                ENTRY_DEFINITIONS + ENTRY_SEPARATOR + TOSCA_DEFINITION_FILEPATH;
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));

        assertEquals(ONAPCsarValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }

    @Test
    public void testGivenSOL004MetaDirectoryCompliantMetafile_thenONAPCsarValidatorIsReturned() throws IOException{

        metaFile = metaFile +
                ENTRY_DEFINITIONS + ENTRY_SEPARATOR + TOSCA_DEFINITION_FILEPATH + "\n"
                + ENTRY_MANIFEST + ENTRY_SEPARATOR + TOSCA_MANIFEST_FILEPATH + "\n"
                + ENTRY_CHANGE_LOG + ENTRY_SEPARATOR + TOSCA_CHANGELOG_FILEPATH + "\n";
        handler.addFile(TOSCA_METADATA_FILEPATH, metaFile.getBytes(StandardCharsets.UTF_8));

       assertEquals(SOL004MetaDirectoryValidator.class, ValidatorFactory.getValidator(handler).getClass());
    }

    @Test
    public void testGivenMultiBlockMetadataWithSOL00CompliantMetaFile_thenSOL004MetaDirectoryValidatorReturned() throws IOException {

        handler.addFile(TOSCA_METADATA_FILEPATH, ValidatorUtil.getFileResource("/validation.files/metafile/metaFileWithMultipleBlocks.meta"));
        handler.addFile(TOSCA_DEFINITION_FILEPATH, "".getBytes());
        handler.addFile(TOSCA_CHANGELOG_FILEPATH, "".getBytes(StandardCharsets.UTF_8));
        handler.addFile(TOSCA_MANIFEST_FILEPATH, "".getBytes(StandardCharsets.UTF_8));

        assertEquals(SOL004MetaDirectoryValidator.class, ValidatorFactory.getValidator(handler).getClass());

    }

}