/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation
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

package org.openecomp.sdc.vendorsoftwareproduct.impl.onboarding;

import static org.openecomp.sdc.common.errors.Messages.PACKAGE_EMPTY_ERROR;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_INVALID_ERROR;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_INVALID_EXTENSION;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_MISSING_INTERNAL_PACKAGE;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_PROCESS_ERROR;
import static org.openecomp.sdc.common.errors.Messages.PACKAGE_PROCESS_INTERNAL_PACKAGE_ERROR;

import com.google.common.collect.ImmutableSet;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.core.utilities.orchestration.OnboardingTypesEnum;
import org.openecomp.sdc.common.exception.ZipException;
import org.openecomp.sdc.common.utils.CommonUtil;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.vendorsoftwareproduct.exception.OnboardPackageException;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackage;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardPackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.types.OnboardSignedPackage;

public class OnboardingPackageProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(OnboardingPackageProcessor.class);
    private static final Set<String> ALLOWED_SIGNATURE_EXTENSIONS = ImmutableSet.of("cms");
    private static final Set<String> ALLOWED_CERTIFICATE_EXTENSIONS = ImmutableSet.of("cert", "crt");
    private static final String CSAR_EXTENSION = "csar";
    private static final String ZIP_EXTENSION = "zip";

    private final String packageFileName;
    private final byte[] packageFileContent;
    private FileContentHandler onboardPackageContentHandler;
    private Set<ErrorMessage> errorMessageSet = new HashSet<>();
    private OnboardPackageInfo onboardPackageInfo;

    public OnboardingPackageProcessor(final String packageFileName, final byte[] packageFileContent) {
        this.packageFileName = packageFileName;
        this.packageFileContent = packageFileContent;
        onboardPackageInfo = processPackage();
    }

    private OnboardPackageInfo processPackage() {
        if(!hasValidExtension()) {
            final String message = PACKAGE_INVALID_EXTENSION.formatMessage(packageFileName, String.join(", ", CSAR_EXTENSION, ZIP_EXTENSION));
            reportError(ErrorLevel.ERROR, message);
            return null;
        }
        try {
            onboardPackageContentHandler = CommonUtil.getZipContent(packageFileContent);
        } catch (final ZipException e) {
            final String message = PACKAGE_PROCESS_ERROR.formatMessage(packageFileName);
            LOGGER.error(message, e);
            reportError(ErrorLevel.ERROR, message);
            return null;
        }
        if(isPackageEmpty()) {
            final String message = PACKAGE_EMPTY_ERROR.formatMessage(packageFileName);
            reportError(ErrorLevel.ERROR, message);
            return null;
        }

        final String packageName = FilenameUtils.getBaseName(packageFileName);
        final String packageExtension = FilenameUtils.getExtension(packageFileName);

        if (hasSignedPackageStructure()) {
            return processSignedPackage(packageName, packageExtension);
        } else {
            final OnboardPackage onboardPackage = new OnboardPackage(packageName, packageExtension,
                ByteBuffer.wrap(packageFileContent), onboardPackageContentHandler);
            if (packageExtension.equalsIgnoreCase(CSAR_EXTENSION)) {
                return new OnboardPackageInfo(onboardPackage, OnboardingTypesEnum.CSAR);
            } else if (packageExtension.equalsIgnoreCase(ZIP_EXTENSION)) {
                return new OnboardPackageInfo(onboardPackage, OnboardingTypesEnum.ZIP);
            }
        }

        reportError(ErrorLevel.ERROR, PACKAGE_INVALID_ERROR.formatMessage(packageFileName));
        return null;
    }

    private boolean hasValidExtension() {
        final String packageExtension = FilenameUtils.getExtension(packageFileName);
        return packageExtension.equalsIgnoreCase(CSAR_EXTENSION) || packageExtension.equalsIgnoreCase(ZIP_EXTENSION);
    }

    private OnboardPackageInfo processSignedPackage(final String packageName, final String packageExtension) {
        final String internalPackagePath = findInternalPackagePath().orElse(null);
        if (internalPackagePath == null) {
            reportError(ErrorLevel.ERROR, PACKAGE_MISSING_INTERNAL_PACKAGE.getErrorMessage());
            return null;
        }
        final String signatureFilePath = findSignatureFilePath().orElse(null);
        final String certificateFilePath = findCertificateFilePath().orElse(null);
        final OnboardSignedPackage onboardSignedPackage =
            new OnboardSignedPackage(packageName, packageExtension, ByteBuffer.wrap(packageFileContent),
                onboardPackageContentHandler, signatureFilePath, internalPackagePath, certificateFilePath);

        final String internalPackageName = FilenameUtils.getName(internalPackagePath);
        final String internalPackageBaseName = FilenameUtils.getBaseName(internalPackagePath);
        final String internalPackageExtension = FilenameUtils.getExtension(internalPackagePath);
        final byte[] internalPackageContent = onboardPackageContentHandler.getFileContent(internalPackagePath);

        final OnboardPackage onboardPackage;
        try {
            onboardPackage = new OnboardPackage(internalPackageBaseName, internalPackageExtension,
                internalPackageContent);
        } catch (final OnboardPackageException e) {
            final String message = PACKAGE_PROCESS_INTERNAL_PACKAGE_ERROR.formatMessage(internalPackageName);
            LOGGER.error(message, e);
            reportError(ErrorLevel.ERROR, message);
            return null;
        }

        return new OnboardPackageInfo(onboardSignedPackage, onboardPackage, OnboardingTypesEnum.SIGNED_CSAR);
    }

    private void reportError(final ErrorLevel errorLevel, final String message) {
        errorMessageSet.add(new ErrorMessage(errorLevel, message));
    }

    public boolean hasErrors() {
        return !errorMessageSet.isEmpty();
    }

    public Set<ErrorMessage> getErrorMessageSet() {
        return errorMessageSet;
    }

    private Optional<String> findInternalPackagePath() {
        return onboardPackageContentHandler.getFileList().stream()
            .filter(filePath -> {
                    final String extension = FilenameUtils.getExtension(filePath);
                    return CSAR_EXTENSION.equalsIgnoreCase(extension) || ZIP_EXTENSION.equalsIgnoreCase(extension);
                }
            )
            .findFirst();
    }

    private boolean isPackageEmpty() {
        return MapUtils.isEmpty(onboardPackageContentHandler.getFiles());
    }

    private boolean hasSignedPackageStructure() {
        if (MapUtils.isEmpty(onboardPackageContentHandler.getFiles()) || !CollectionUtils.isEmpty(
            onboardPackageContentHandler.getFolderList())) {
            return false;
        }
        final int numberOfFiles = onboardPackageContentHandler.getFileList().size();
        if (numberOfFiles == 2) {
            return hasOneInternalPackageFile(onboardPackageContentHandler) &&
                hasOneSignatureFile(onboardPackageContentHandler);
        }

        if (numberOfFiles == 3) {
            return hasOneInternalPackageFile(onboardPackageContentHandler) &&
                hasOneSignatureFile(onboardPackageContentHandler) &&
                hasOneCertificateFile(onboardPackageContentHandler);
        }

        return false;
    }

    private boolean hasOneInternalPackageFile(final FileContentHandler fileContentHandler) {
        return fileContentHandler.getFileList().parallelStream()
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(file -> file.endsWith(CSAR_EXTENSION)).count() == 1;
    }

    private boolean hasOneSignatureFile(final FileContentHandler fileContentHandler) {
        return fileContentHandler.getFileList().parallelStream()
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(ALLOWED_SIGNATURE_EXTENSIONS::contains).count() == 1;
    }

    private boolean hasOneCertificateFile(final FileContentHandler fileContentHandler) {
        return fileContentHandler.getFileList().parallelStream()
            .map(FilenameUtils::getExtension)
            .map(String::toLowerCase)
            .filter(ALLOWED_CERTIFICATE_EXTENSIONS::contains).count() == 1;
    }

    private Optional<String> findSignatureFilePath() {
        final Map<String, byte[]> files = onboardPackageContentHandler.getFiles();
        return files.keySet().stream()
            .filter(fileName -> ALLOWED_SIGNATURE_EXTENSIONS.contains(FilenameUtils.getExtension(fileName).toLowerCase()))
            .findFirst();
    }

    private Optional<String> findCertificateFilePath() {
        final Map<String, byte[]> files = onboardPackageContentHandler.getFiles();
        return files.keySet().stream()
            .filter(fileName -> ALLOWED_CERTIFICATE_EXTENSIONS.contains(FilenameUtils.getExtension(fileName).toLowerCase()))
            .findFirst();
    }

    public Optional<OnboardPackageInfo> getOnboardPackageInfo() {
        return Optional.ofNullable(onboardPackageInfo);
    }
}
