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
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is temporary solution. When AAF provides functionality for verifying trustedCertificates, this class should be reviewed
 * Class is responsible for providing root trustedCertificates from configured location in onboarding container.
 */
public class SecurityManager {
    private static final String CERTIFICATE_DEFAULT_LOCATION = "cert";
    private static final SecurityManager INSTANCE = new SecurityManager();

    private Logger logger = LoggerFactory.getLogger(SecurityManager.class);
    private Set<X509Certificate> trustedCertificates = new HashSet<>();
    private File certificateDirectory;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SecurityManager() {
        certificateDirectory = this.getcertDirectory();
    }

    public static SecurityManager getInstance(){
        return INSTANCE;
    }

    /**
     *
     * Checks the configured location for available trustedCertificates
     *
     * @return set of trustedCertificates
     * @throws SecurityManagerException
     */
    public Set<X509Certificate> getTrustedCertificates() throws SecurityManagerException {
        //if file number in certificate directory changed reload certs
        String[] certFiles = certificateDirectory.list();
        if (certFiles == null) {
            logger.error("Certificate directory is empty!");
            return ImmutableSet.copyOf(new HashSet<>());
        }
        if (trustedCertificates.size() != certFiles.length) {
            trustedCertificates = new HashSet<>();
            processCertificateDir();
        }
        return ImmutableSet.copyOf(trustedCertificates);
    }

    /**
     * Cleans certificate collection
     */
    public void cleanTrustedCertificates(){
        trustedCertificates.clear();
    }

    /**
     *
     * Verifies if packaged signed with trusted certificate
     *
     * @param messageSyntaxSignature - signature data in cms format
     * @param packageCert            - package certificate if not part of cms signature, can be null
     * @param innerPackageFile       data package signed with cms signature
     * @return true if signature verified
     * @throws SecurityManagerException
     */
    public boolean verifySignedData(final byte[] messageSyntaxSignature, final byte[] packageCert,
                                    final byte[] innerPackageFile) throws SecurityManagerException{
        try (ByteArrayInputStream signatureStream = new ByteArrayInputStream(messageSyntaxSignature)) {
            Object parsedObject = new PEMParser(new InputStreamReader(signatureStream)).readObject();
            if (!(parsedObject instanceof ContentInfo)) {
                throw new SecurityManagerException("Signature is not recognized");
            }
            ContentInfo signature = ContentInfo.getInstance(parsedObject);
            CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFile);
            CMSSignedData signedData = new CMSSignedData(signedContent, signature);

            Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            SignerInformation firstSigner = signers.iterator().next();
            Store certificates = signedData.getCertificates();
            X509Certificate cert;
            if (packageCert == null) {
                Collection<X509CertificateHolder> firstSignerCertificates = certificates.getMatches(firstSigner.getSID());
                if(!firstSignerCertificates.iterator().hasNext()){
                    throw new SecurityManagerException("No certificate found in cms signature that should contain one!");
                }
                X509CertificateHolder firstSignerFirstCertificate = firstSignerCertificates.iterator().next();
                cert = loadCertificate(firstSignerFirstCertificate.getEncoded());
            } else {
                cert = loadCertificate(packageCert);
            }

            PKIXCertPathBuilderResult result = verifyCertificate(cert, getTrustedCertificates());

            if (result == null) {
                return false;
            }

            return firstSigner.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (OperatorCreationException | IOException | CMSException e) {
            logger.error(e.getMessage(), e);
            throw new SecurityManagerException("Unexpected error occurred during signature validation!", e);
        } catch (GeneralSecurityException e){
            throw new SecurityManagerException("Could not verify signature!", e);
        }
    }

    private void processCertificateDir() throws SecurityManagerException {
        if (!certificateDirectory.exists() || !certificateDirectory.isDirectory()) {
            logger.error("Issue with certificate directory, check if exists!");
            return;
        }

        File[] files = certificateDirectory.listFiles();
        if (files == null) {
            logger.error("Certificate directory is empty!");
            return;
        }
        for (File f : files) {
            trustedCertificates.add(loadCertificate(f));
        }
    }

    private File getcertDirectory() {
        String certDirLocation = System.getenv("SDC_CERT_DIR");
        if (certDirLocation == null) {
            certDirLocation = CERTIFICATE_DEFAULT_LOCATION;
        }
        return new File(certDirLocation);
    }

    private X509Certificate loadCertificate(File certFile) throws SecurityManagerException {
        try (InputStream fileInputStream = new FileInputStream(certFile)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(fileInputStream);
        } catch (CertificateException | IOException e) {
            throw new SecurityManagerException("Error during loading Certificate file!", e);
        }
    }

    private X509Certificate loadCertificate(byte[] certFile) throws SecurityManagerException {
        try (InputStream in = new ByteArrayInputStream(certFile)) {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        } catch (CertificateException | IOException e) {
            throw new SecurityManagerException("Error during loading Certificate from bytes!", e);
        }
    }

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert,
                                                        Set<X509Certificate> additionalCerts) throws GeneralSecurityException, SecurityManagerException {
            if (null == cert) {
                throw new SecurityManagerException("The certificate is empty!");
            }

            if (isExpired(cert)) {
                throw new SecurityManagerException("The certificate expired on: " + cert.getNotAfter());
            }

            if (isSelfSigned(cert)) {
                throw new SecurityManagerException("The certificate is self-signed.");
            }

            Set<X509Certificate> trustedRootCerts = new HashSet<>();
            Set<X509Certificate> intermediateCerts = new HashSet<>();
            for (X509Certificate additionalCert : additionalCerts) {
                if (isSelfSigned(additionalCert)) {
                    trustedRootCerts.add(additionalCert);
                } else {
                    intermediateCerts.add(additionalCert);
                }
            }

            return verifyCertificate(cert, trustedRootCerts, intermediateCerts);
    }

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert,
                                                        Set<X509Certificate> allTrustedRootCerts,
                                                        Set<X509Certificate> allIntermediateCerts)
            throws GeneralSecurityException {

        // Create the selector that specifies the starting certificate
        X509CertSelector selector = new X509CertSelector();
        selector.setCertificate(cert);

        // Create the trust anchors (set of root CA certificates)
        Set<TrustAnchor> trustAnchors = new HashSet<>();
        for (X509Certificate trustedRootCert : allTrustedRootCerts) {
            trustAnchors.add(new TrustAnchor(trustedRootCert, null));
        }

        // Configure the PKIX certificate builder algorithm parameters
        PKIXBuilderParameters pkixParams;
        try {
            pkixParams = new PKIXBuilderParameters(trustAnchors, selector);
        } catch (InvalidAlgorithmParameterException ex) {
            throw new InvalidAlgorithmParameterException("No root CA has been found for this certificate", ex);
        }

        // Not supporting CRL checks for now
        pkixParams.setRevocationEnabled(false);

        Set<X509Certificate> certSet = new HashSet<>();
        certSet.add(cert);
        pkixParams.addCertStore(createCertStore(certSet));
        pkixParams.addCertStore(createCertStore(allIntermediateCerts));
        pkixParams.addCertStore(createCertStore(allTrustedRootCerts));

        CertPathBuilder builder = CertPathBuilder.getInstance(CertPathBuilder.getDefaultType(), BouncyCastleProvider.PROVIDER_NAME);
        return (PKIXCertPathBuilderResult) builder.build(pkixParams);
    }

    private CertStore createCertStore(Set<X509Certificate> certificateSet) throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        return CertStore.getInstance("Collection", new CollectionCertStoreParameters(certificateSet), BouncyCastleProvider.PROVIDER_NAME);
    }

    private boolean isExpired(X509Certificate cert) {
        try {
            cert.checkValidity();
        } catch (CertificateExpiredException e) {
            logger.error(e.getMessage(), e);
            return true;
        } catch (CertificateNotYetValidException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return false;
    }

    private boolean isSelfSigned(Certificate cert)
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException {
        try {
            // Try to verify certificate signature with its own public key
            PublicKey key = cert.getPublicKey();
            cert.verify(key);
            return true;
        } catch (SignatureException | InvalidKeyException e) {
            logger.error(e.getMessage(), e);
            //not self-signed
            return false;
        }
    }
}
