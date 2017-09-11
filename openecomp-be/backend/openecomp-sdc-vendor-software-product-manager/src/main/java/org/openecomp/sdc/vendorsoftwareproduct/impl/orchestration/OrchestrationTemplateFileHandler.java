package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.InputStream;
public interface OrchestrationTemplateFileHandler {
    UploadFileResponse upload(String vspId, Version version, InputStream fileToUpload, String user,
                              CandidateService candidateService, VspDetails vspDetails);
}
