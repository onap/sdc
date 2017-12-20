package org.openecomp.sdc.vendorsoftwareproduct.impl.orchestration;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.services.filedatastructuremodule.CandidateService;
import org.openecomp.sdc.vendorsoftwareproduct.types.UploadFileResponse;

import java.io.InputStream;

public interface OrchestrationTemplateFileHandler {

  UploadFileResponse upload(VspDetails vspDetails, InputStream fileToUpload,
                            String fileSuffix, String networkPackageName,
                            CandidateService candidateService);
}
