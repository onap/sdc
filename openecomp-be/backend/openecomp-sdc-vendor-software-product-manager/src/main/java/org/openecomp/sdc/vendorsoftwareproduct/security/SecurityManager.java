/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019, Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.vendorsoftwareproduct.security;

import com.google.common.collect.ImmutableSet;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashSet;
import java.util.Set;

/**
 * This is temporary solution. When AAF provides functionality for verifying certificates, this class should be reviewed
 * Class is responsible for providing root certificates from configured location in onboarding container.
 */
public class SecurityManager {
    private static final String CERTIFICATE_DEFAULT_LOCATION = "/root/cert";

    private Logger logger = LoggerFactory.getLogger(SecurityManager.class);
    private Set<Certificate> certificates = new HashSet<>();
    private File certificateDirectory;


    public SecurityManager(){
        certificateDirectory = this.getcertDirectory();
    }

    private void processCertificateDir() {
        if(!certificateDirectory.exists() || !certificateDirectory.isDirectory()){
            logger.error("Issue with certificate directory, check if exists!");
            return;
        }

        File [] files = certificateDirectory.listFiles();
        if(files == null){
            logger.error("Certificate directory is empty!");
            return;
        }
        for(File f : files) {
            certificates.add(loadCertificate(f));
        }
    }

    private File getcertDirectory() {
        String certDirLocation = System.getenv("SDC_CERT_DIR");
        if(certDirLocation == null){
            certDirLocation = CERTIFICATE_DEFAULT_LOCATION;
        }
        return new File(certDirLocation);
    }

    private Certificate loadCertificate(File certFile){
        try (InputStream fileInputStream = new FileInputStream(certFile)){
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return factory.generateCertificate(fileInputStream);
        } catch (CertificateException|IOException e) {
            throw new SecurityManagerException("Error during loading Certificate file!", e);
        }
    }

    /**
     * Checks the configured location for available certificates
     * @return set of certificates
     */
    public Set<Certificate> getCertificates() {
        //if file number in certificate directory changed reload certs
        String[] certFiles = certificateDirectory.list();
        if(certFiles == null){
            logger.error("Certificate directory is empty!");
            return ImmutableSet.copyOf(new HashSet<>());
        }
        if(certificates.size() != certFiles.length){
            certificates = new HashSet<>();
            processCertificateDir();
        }
        return ImmutableSet.copyOf(certificates);
    }
}
