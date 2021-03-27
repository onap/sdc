/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.be.csar.security;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.openecomp.sdc.be.csar.security.api.CertificateManager;
import org.openecomp.sdc.be.csar.security.api.CertificateReader;
import org.openecomp.sdc.be.csar.security.api.PrivateKeyReader;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.openecomp.sdc.be.csar.security.exception.CertificateNotFoundException;
import org.openecomp.sdc.be.csar.security.model.CertificateInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CertificateManagerImpl implements CertificateManager {

    public static final String CERT_DIR_ENV_VARIABLE = "SDC_CERT_DIR";
    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateManagerImpl.class);
    private final PrivateKeyReader privateKeyReader;
    private final CertificateReader certificateReader;
    private final Environment environment;
    private final Map<String, CertificateInfo> certificateMap = new HashMap<>();
    private Path certificateDirectoryPath;
    private File certificateDirectory;

    public CertificateManagerImpl(final PrivateKeyReader privateKeyReader, final CertificateReader certificateReader, final Environment environment) {
        this.certificateReader = certificateReader;
        this.privateKeyReader = privateKeyReader;
        this.environment = environment;
        init();
    }

    private void init() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        final String certificateDir = environment.getProperty(CERT_DIR_ENV_VARIABLE);
        if (certificateDir == null) {
            LOGGER.warn("Environment variable '{}' was not provided. Could not load certificates.", CERT_DIR_ENV_VARIABLE);
            return;
        }
        try {
            this.certificateDirectoryPath = Paths.get(certificateDir);
        } catch (final Exception e) {
            LOGGER.error("Invalid path '{}' provided in the environment variable '{}'. Could not load certificates.", certificateDir,
                CERT_DIR_ENV_VARIABLE, e);
            return;
        }
        try {
            loadCertificateDirectory();
        } catch (final Exception e) {
            LOGGER.error("Could not load certificate directory", e);
            return;
        }
        try {
            loadCertificates();
        } catch (final Exception e) {
            LOGGER.error("Could not load certificates", e);
        }
    }

    private void loadCertificates() {
        final File[] files = certificateDirectory.listFiles();
        if (files == null || files.length == 0) {
            LOGGER.warn("Certificate directory is empty. No trusted certificate found.");
            return;
        }
        final List<File> certFileList = Arrays.stream(files).filter(file -> "cert".equals(FilenameUtils.getExtension(file.getName())))
            .collect(Collectors.toList());
        final List<File> keyFileList = Arrays.stream(files).filter(file -> "key".equals(FilenameUtils.getExtension(file.getName())))
            .collect(Collectors.toList());
        if (certFileList.isEmpty()) {
            LOGGER.error("Certificate directory is empty. No trusted certificate found.");
            return;
        }
        certFileList.forEach(certFile -> {
            final String baseFileName = FilenameUtils.getBaseName(certFile.getName());
            final Certificate certificate = loadCertificate(certFile);
            final Optional<File> keyFileOptional = keyFileList.stream()
                .filter(keyFile1 -> FilenameUtils.getBaseName(keyFile1.getName()).equals(baseFileName)).findFirst();
            keyFileOptional.ifPresentOrElse(keyFile -> {
                final CertificateInfoImpl certificateInfo = new CertificateInfoImpl(certFile, certificate, keyFile, loadPrivateKey(keyFile));
                if (certificateInfo.isValid()) {
                    certificateMap.put(baseFileName, certificateInfo);
                }
            }, () -> {
                final CertificateInfoImpl certificateInfo = new CertificateInfoImpl(certFile, certificate);
                if (certificateInfo.isValid()) {
                    certificateMap.put(baseFileName, new CertificateInfoImpl(certFile, certificate));
                }
            });
        });
    }

    private void loadCertificateDirectory() {
        final File file = certificateDirectoryPath.toFile();
        if (!file.exists() || !file.isDirectory()) {
            final String errorMsg = String.format("Provided certificate path '%s' is not a directory or does not exist", certificateDirectoryPath);
            throw new CertificateNotFoundException(errorMsg);
        }
        this.certificateDirectory = file;
    }

    private Certificate loadCertificate(final File certFile) {
        return certificateReader.loadCertificate(certFile);
    }

    private Key loadPrivateKey(final File privateKeyFile) {
        return privateKeyReader.loadPrivateKey(privateKeyFile);
    }

    @Override
    public Optional<CertificateInfo> getCertificate(final String certName) {
        return Optional.ofNullable(certificateMap.get(certName));
    }
}
