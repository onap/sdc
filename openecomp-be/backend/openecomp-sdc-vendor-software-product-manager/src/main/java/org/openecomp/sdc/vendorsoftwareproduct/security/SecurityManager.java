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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

/**
 * This is temporary solution. When AAF provides functionality for verifying trustedCertificates, this class should be reviewed Class is responsible
 * for providing root trustedCertificates from configured location in onboarding container.
 */
public class SecurityManager {

    private static final String CERTIFICATE_DEFAULT_LOCATION = "cert";
    public static final Set<String> ALLOWED_SIGNATURE_EXTENSIONS = ImmutableSet.of("cms");
    public static final Set<String> ALLOWED_CERTIFICATE_EXTENSIONS = ImmutableSet.of("cert", "crt");
    private Logger logger = LoggerFactory.getLogger(SecurityManager.class);
    private Set<X509Certificate> trustedCertificates = new HashSet<>();
    private Set<X509Certificate> trustedCertificatesFromPackage = new HashSet<>();
    private File certificateDirectory;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private SecurityManager() {
        certificateDirectory = this.getcertDirectory(System.getenv("SDC_CERT_DIR"));
    }

    // Package level constructor use in tests to avoid power mock
    SecurityManager(String sdcCertDir) {
        certificateDirectory = this.getcertDirectory(sdcCertDir);
    }

    public static SecurityManager getInstance() {
        return SecurityManagerInstanceHolder.instance;
    }

    /**
     * Initialization on demand class / synchronized singleton pattern.
     */
    private static class SecurityManagerInstanceHolder {

        private static final SecurityManager instance = new SecurityManager();
    }

    /**
     * Checks the configured location for available trustedCertificates
     *
     * @return set of trustedCertificates
     * @throws SecurityManagerException
     */
    public Set<X509Certificate> getTrustedCertificates() throws SecurityManagerException, FileNotFoundException {
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
        if (!trustedCertificatesFromPackage.isEmpty()) {
            return Stream.concat(trustedCertificatesFromPackage.stream(), trustedCertificates.stream()).collect(Collectors.toUnmodifiableSet());
        }
        return ImmutableSet.copyOf(trustedCertificates);
    }

    /**
     * Cleans certificate collection
     */
    public void cleanTrustedCertificates() {
        trustedCertificates.clear();
    }

    /**
     * Verifies if packaged signed with trusted certificate
     *
     * @param messageSyntaxSignature - signature data in cms format
     * @param packageCert            - package certificate if not part of cms signature, can be null
     * @param innerPackageFile       data package signed with cms signature
     * @return true if signature verified
     * @throws SecurityManagerException
     */
    public boolean verifySignedData(final byte[] messageSyntaxSignature, final byte[] packageCert, final byte[] innerPackageFile)
        throws SecurityManagerException {
        try (ByteArrayInputStream signatureStream = new ByteArrayInputStream(messageSyntaxSignature); final PEMParser pemParser = new PEMParser(
            new InputStreamReader(signatureStream))) {
            final Object parsedObject = pemParser.readObject();
            if (!(parsedObject instanceof ContentInfo)) {
                throw new SecurityManagerException("Signature is not recognized");
            }
            final ContentInfo signature = ContentInfo.getInstance(parsedObject);
            final CMSTypedData signedContent = new CMSProcessableByteArray(innerPackageFile);
            final CMSSignedData signedData = new CMSSignedData(signedContent, signature);
            final Collection<SignerInformation> signers = signedData.getSignerInfos().getSigners();
            final SignerInformation firstSigner = signers.iterator().next();
            final X509Certificate cert;
            Collection<X509CertificateHolder> certs;
            if (packageCert == null) {
                certs = signedData.getCertificates().getMatches(null);
                cert = readSignCert(certs, firstSigner)
                    .orElseThrow(() -> new SecurityManagerException("No certificate found in cms signature that should contain one!"));
            } else {
                certs = parseCertsFromPem(packageCert);
                cert = readSignCert(certs, firstSigner)
                    .orElseThrow(() -> new SecurityManagerException("No matching certificate found in certificate file that should contain one!"));
            }
            trustedCertificatesFromPackage = readTrustedCerts(certs, firstSigner);
            if (verifyCertificate(cert, getTrustedCertificates()) == null) {
                return false;
            }
            return firstSigner.verify(new JcaSimpleSignerInfoVerifierBuilder().build(cert));
        } catch (OperatorCreationException | IOException | CMSException e) {
            logger.error(e.getMessage(), e);
            throw new SecurityManagerException("Unexpected error occurred during signature validation!", e);
        } catch (GeneralSecurityException e) {
            throw new SecurityManagerException("Could not verify signature!", e);
        }
    }

    private Optional<X509Certificate> readSignCert(final Collection<X509CertificateHolder> certs, final SignerInformation firstSigner) {
        return certs.stream().filter(crt -> firstSigner.getSID().match(crt)).findAny().map(this::loadCertificate);
    }

    private Set<X509Certificate> readTrustedCerts(final Collection<X509CertificateHolder> certs, final SignerInformation firstSigner) {
        return certs.stream().filter(crt -> !firstSigner.getSID().match(crt)).map(this::loadCertificate).filter(Predicate.not(this::isSelfSigned))
            .collect(Collectors.toSet());
    }

    private Set<X509CertificateHolder> parseCertsFromPem(final byte[] packageCert) throws IOException {
        final ByteArrayInputStream packageCertStream = new ByteArrayInputStream(packageCert);
        final PEMParser pemParser = new PEMParser(new InputStreamReader(packageCertStream));
        Object readObject = pemParser.readObject();
        Set<X509CertificateHolder> allCerts = new HashSet<>();
        while (readObject != null) {
            if (readObject instanceof X509CertificateHolder) {
                allCerts.add((X509CertificateHolder) readObject);
            }
            readObject = pemParser.readObject();
        }
        return allCerts;
    }

    private void processCertificateDir() throws SecurityManagerException, FileNotFoundException {
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

    private File getcertDirectory(String sdcCertDir) {
        String certDirLocation = sdcCertDir;
        if (certDirLocation == null) {
            certDirLocation = CERTIFICATE_DEFAULT_LOCATION;
        }
        return new File(certDirLocation);
    }

    private X509Certificate loadCertificate(File certFile) throws SecurityManagerException {
        try (FileInputStream fi = new FileInputStream(certFile)) {
            return loadCertificateFactory(fi);
        } catch (IOException e) {
            throw new SecurityManagerException("Error during loading Certificate from file!", e);
        }
    }

    private X509Certificate loadCertificate(X509CertificateHolder cert) {
        try {
            return loadCertificateFactory(new ByteArrayInputStream(cert.getEncoded()));
        } catch (IOException | SecurityManagerException e) {
            throw new RuntimeException("Error during loading Certificate from bytes!", e);
        }
    }

    private X509Certificate loadCertificateFactory(InputStream in) throws SecurityManagerException {
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) factory.generateCertificate(in);
        } catch (CertificateException e) {
            throw new SecurityManagerException("Error during loading Certificate from bytes!", e);
        }
    }

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert, Set<X509Certificate> additionalCerts)
        throws GeneralSecurityException, SecurityManagerException {
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

    private PKIXCertPathBuilderResult verifyCertificate(X509Certificate cert, Set<X509Certificate> allTrustedRootCerts,
                                                        Set<X509Certificate> allIntermediateCerts) throws GeneralSecurityException {
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

    private CertStore createCertStore(Set<X509Certificate> certificateSet)
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
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

    private boolean isSelfSigned(X509Certificate cert) {
        return cert.getIssuerDN().equals(cert.getSubjectDN());
    }
}
