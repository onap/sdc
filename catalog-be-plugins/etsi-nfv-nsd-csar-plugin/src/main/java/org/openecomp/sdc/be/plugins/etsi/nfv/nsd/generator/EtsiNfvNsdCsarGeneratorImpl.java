/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 Nordix Foundation
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
package org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator;

import static org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer.SIGNATURE_EXTENSION;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ETSI_PACKAGE;
import static org.openecomp.sdc.common.api.ArtifactTypeEnum.ONBOARDED_PACKAGE;

import fj.data.Either;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.openecomp.sdc.be.csar.security.api.model.CertificateInfo;
import org.openecomp.sdc.be.dao.cassandra.ArtifactCassandraDao;
import org.openecomp.sdc.be.dao.cassandra.CassandraOperationStatus;
import org.openecomp.sdc.be.datatypes.enums.JsonPresentationFields;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdCsarManifestBuilder;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.builder.NsdToscaMetadataBuilder;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.exception.NsdException;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.factory.NsDescriptorGeneratorFactory;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.EtsiVersion;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.NsDescriptorConfig;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.generator.config.NsDescriptorVersionComparator;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.Nsd;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.NsdCsar;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.model.VnfDescriptor;
import org.openecomp.sdc.be.plugins.etsi.nfv.nsd.security.NsdCsarEtsiOption2Signer;
import org.openecomp.sdc.be.resources.data.DAOArtifactData;
import org.openecomp.sdc.be.tosca.utils.OperationArtifactUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Implementation of a ETSI NFV NSD CSAR generator
 */
@org.springframework.stereotype.Component("etsiNfvNsdCsarGenerator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class EtsiNfvNsdCsarGeneratorImpl implements EtsiNfvNsdCsarGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(EtsiNfvNsdCsarGeneratorImpl.class);
    private static final String MANIFEST_EXT = "mf";
    private static final String SLASH = "/";
    private static final String DOT = ".";
    private static final String CSAR_SIGNATURE_EXTENSION = ".cms";
    private static final String CSAR_EXTENSION = ".csar";
    private static final String DOT_YAML = DOT + "yaml";
    private static final String DEFINITION = "Definitions";
    private static final String TOSCA_META_PATH = "TOSCA-Metadata/TOSCA.meta";
    private final VnfDescriptorGenerator vnfDescriptorGenerator;
    private final NsDescriptorGeneratorFactory nsDescriptorGeneratorFactory;
    private final ArtifactCassandraDao artifactCassandraDao;
    private final NsDescriptorConfig nsDescriptorConfig;
    private final NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer;

    public EtsiNfvNsdCsarGeneratorImpl(final NsDescriptorConfig nsDescriptorConfig, final VnfDescriptorGenerator vnfDescriptorGenerator,
                                       final NsDescriptorGeneratorFactory nsDescriptorGeneratorFactory,
                                       final ArtifactCassandraDao artifactCassandraDao,
                                       final NsdCsarEtsiOption2Signer nsdCsarEtsiOption2Signer) {
        this.nsDescriptorConfig = nsDescriptorConfig;
        this.vnfDescriptorGenerator = vnfDescriptorGenerator;
        this.nsDescriptorGeneratorFactory = nsDescriptorGeneratorFactory;
        this.artifactCassandraDao = artifactCassandraDao;
        this.nsdCsarEtsiOption2Signer = nsdCsarEtsiOption2Signer;
    }

    @Override
    public NsdCsar generateNsdCsar(final Component component) throws NsdException {
        if (component == null) {
            throw new NsdException("Could not generate the NSD CSAR, invalid component argument");
        }
        loadComponentArtifacts(component);
        loadComponentInstancesArtifacts(component);
        final String componentName = component.getName();
        try {
            LOGGER.debug("Starting NSD CSAR generation for component '{}'", componentName);
            final String nsdFileName = getNsdFileName(component);
            final NsdCsar nsdCsar = new NsdCsar(nsdFileName);

            final List<VnfDescriptor> vnfDescriptorList = generateVnfPackages(component);
            vnfDescriptorList.forEach(vnfPackage -> nsdCsar.addAllFiles(vnfPackage.getDefinitionFiles()));

            final EtsiVersion etsiVersion = nsDescriptorConfig.getNsVersion();
            final Nsd nsd = generateNsd(component, vnfDescriptorList);

            nsdCsar.addFile(getNsdPath(nsdFileName), nsd.getContents());
            nsdCsar.addFile(TOSCA_META_PATH, buildToscaMetaContent(nsdFileName).getBytes());

            nsdCsar.addAllFiles(createEtsiSolNsdTypeEntries(etsiVersion));
            for (final String referencedFile : nsd.getArtifactReferences()) {
                getReferencedArtifact(component, referencedFile)
                    .ifPresent(artifactDefinition -> nsdCsar.addFile(referencedFile, artifactDefinition.getPayloadData())
                );
            }
            final boolean isCertificateConfigured = nsdCsarEtsiOption2Signer.isCertificateConfigured();
            final String manifestPath = getManifestPath(nsdFileName);
            final NsdCsarManifestBuilder manifestBuilder =
                createManifestBuilder(nsd, etsiVersion, manifestPath, nsdCsar.getFileMap().keySet(), isCertificateConfigured);

            nsdCsar.addManifest(manifestBuilder);

            if (isCertificateConfigured) {
                nsdCsarEtsiOption2Signer.signArtifacts(nsdCsar);
            }

            byte[] package1 = buildCsarPackage(nsdCsar.getFileMap());
            if (isCertificateConfigured) {
                package1 = buildZipWithCsarAndSignature(nsdCsar.getFileName(), package1);
                nsdCsar.setSigned(true);
            }
            nsdCsar.setCsarPackage(package1);
            LOGGER.debug("Successfully generated NSD CSAR package");
            return nsdCsar;
        } catch (final Exception exception) {
            throw new NsdException("Could not generate the NSD CSAR file", exception);
        }
    }

    private void loadComponentArtifacts(final Component component) {
        final Map<String, ArtifactDefinition> allArtifactsMap = component.getAllArtifacts();
        if (allArtifactsMap == null) {
            return;
        }
        allArtifactsMap.keySet().forEach(key -> {
            final ArtifactDefinition artifactDefinition = allArtifactsMap.get(key);
            if (StringUtils.isNotEmpty(artifactDefinition.getEsId())) {
                final Optional<byte[]> artifactPayload = loadArtifactPayload(artifactDefinition.getEsId());
                if (artifactPayload.isPresent()) {
                    artifactDefinition.setPayload(artifactPayload.get());
                } else {
                    LOGGER.warn("Could not load component '{}' artifact '{}'", component.getName(), artifactDefinition.getArtifactName());
                }
            }
        });
    }

    private void loadComponentInstancesArtifacts(final Component component) {
        final List<ComponentInstance> componentInstanceList = component.getComponentInstances();
        if (CollectionUtils.isEmpty(componentInstanceList)) {
            return;
        }
        for (final ComponentInstance componentInstance : componentInstanceList) {
            final Map<String, ArtifactDefinition> deploymentArtifacts = componentInstance.getDeploymentArtifacts();
            if (MapUtils.isEmpty(deploymentArtifacts)) {
                continue;
            }
            deploymentArtifacts.values().stream().filter(artifactDefinition -> StringUtils.isNotEmpty(artifactDefinition.getEsId()))
                .forEach(artifactDefinition -> {
                    final Optional<byte[]> artifactPayload = loadArtifactPayload(artifactDefinition.getEsId());
                    if (artifactPayload.isPresent()) {
                        artifactDefinition.setPayload(artifactPayload.get());
                    } else {
                        LOGGER.warn("Could not load component '{}' instance '{}' artifact '{}'", component.getName(), componentInstance.getName(),
                            artifactDefinition.getArtifactName());
                    }
                });
        }
    }

    private List<VnfDescriptor> generateVnfPackages(final Component component) throws NsdException {
        final List<ComponentInstance> componentInstanceList = component.getComponentInstances();
        if (CollectionUtils.isEmpty(componentInstanceList)) {
            LOGGER.warn("Could not find any instance in service '{}'", component.getName());
            return Collections.emptyList();
        }
        final List<VnfDescriptor> vnfDescriptorList = new ArrayList<>();
        for (final ComponentInstance componentInstance : componentInstanceList) {
            final String componentInstanceName = componentInstance.getName();
            final ArtifactDefinition onboardedCsarArtifact = findOnboardedCsar(componentInstance).orElse(null);
            if (onboardedCsarArtifact == null) {
                LOGGER.warn("Unable to generate VNF Package for component instance '{}', no onboarded package present", componentInstanceName);
                continue;
            }
            final Optional<VnfDescriptor> vnfPackage;
            try {
                vnfPackage = vnfDescriptorGenerator.generate(componentInstanceName, onboardedCsarArtifact);
            } catch (final Exception e) {
                final String errorMsg = String.format("Could not generate VNF package for component instance %s", componentInstanceName);
                throw new NsdException(errorMsg, e);
            }
            if (vnfPackage.isPresent()) {
                vnfDescriptorList.add(vnfPackage.get());
            } else {
                LOGGER.warn("Unable to generate VNF Package for component instance '{}', no onboarded package present", componentInstanceName);
            }
        }
        return vnfDescriptorList;
    }

    private Optional<ArtifactDefinition> findOnboardedCsar(final ComponentInstance componentInstance) {
        final Map<String, ArtifactDefinition> artifactDefinitionMap = componentInstance.getDeploymentArtifacts();
        if (artifactDefinitionMap == null || artifactDefinitionMap.isEmpty()) {
            return Optional.empty();
        }
        return artifactDefinitionMap.values().stream().filter(artifactDefinition -> {
            final String artifactType = (String) artifactDefinition.getToscaPresentationValue(JsonPresentationFields.ARTIFACT_TYPE);
            return ONBOARDED_PACKAGE.getType().equals(artifactType) || ETSI_PACKAGE.getType().equals(artifactType);
        }).findFirst();
    }

    private Map<String, byte[]> createEtsiSolNsdTypeEntries(final EtsiVersion etsiVersion) {
        final EtsiVersion currentVersion = etsiVersion == null ? EtsiVersion.getDefaultVersion() : etsiVersion;

        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            final Resource[] resources =
                resolver.getResources(String.format("classpath:etsi-nfv-types/%s/*.*", currentVersion.getVersion()));
            if (resources.length > 0) {
                final Map<String, byte[]> entryMap = new HashMap<>();
                for (final Resource resource : resources) {
                    readResource(resource).ifPresent(resourceBytes -> {
                        final String entryName = createDefinitionEntryName(resource.getFilename());
                        entryMap.put(entryName, resourceBytes);
                    });
                }
                return entryMap;
            }
        } catch (final IOException e) {
            LOGGER.error("Could not find types files for the version '{}'", currentVersion.getVersion(), e);
        }

        return Collections.emptyMap();
    }

    private String createDefinitionEntryName(final String fileName) {
        return DEFINITION + "/" + fileName;
    }

    private Optional<byte[]> readResource(final Resource resource) {
        try {
            return Optional.ofNullable(IOUtils.toByteArray(resource.getInputStream()));
        } catch (final IOException exception) {
            LOGGER.error("Error adding '{}' to NSD CSAR", resource.getFilename(), exception);
        }
        return Optional.empty();
    }

    private Nsd generateNsd(final Component component,
                            final List<VnfDescriptor> vnfDescriptorList) throws NsdException {
        final NsDescriptorGenerator nsDescriptorGenerator =
            nsDescriptorGeneratorFactory.create();
        return nsDescriptorGenerator.generate(component, vnfDescriptorList)
            .orElseThrow(() ->
                new NsdException(String
                    .format("Could not generate the Network Service Descriptor for component %s", component.getName()))
            );
    }

    private Optional<ArtifactDefinition> getReferencedArtifact(final Component component,
                                                               final String filePath) throws NsdException {
        final Map<String, ArtifactDefinition> interfaceOperationArtifactsByName =
            OperationArtifactUtil.getDistinctInterfaceOperationArtifactsByName(component);
        final String[] pathComponents = filePath.split(SLASH);
        final String artifactName = pathComponents[pathComponents.length - 1];
        final ArtifactDefinition artifactDefinition = interfaceOperationArtifactsByName.get(artifactName);
        if (artifactDefinition == null) {
            throw new NsdException(String.format("Could not find artifact '%s'", filePath));
        }
        LOGGER.debug("ArtifactName {}, unique ID {}", artifactDefinition.getArtifactName(),
            artifactDefinition.getUniqueId());
        if (artifactDefinition.getPayloadData() == null) {
            final Optional<byte[]> artifactPayload = loadArtifactPayload(artifactDefinition.getEsId());

            if (artifactPayload.isEmpty()) {
                throw new NsdException(String.format("Could not load artifact '%s' payload", filePath));
            }
            artifactDefinition.setPayload(artifactPayload.get());
        }

        return Optional.of(artifactDefinition);
    }

    private Optional<byte[]> loadArtifactPayload(final String artifactCassandraId) {
        final Either<DAOArtifactData, CassandraOperationStatus> artifactResponse = artifactCassandraDao
            .getArtifact(artifactCassandraId);

        if (artifactResponse.isRight()) {
            LOGGER.debug("Failed to fetch artifact from Cassandra by id {} error {} ", artifactCassandraId,
                artifactResponse.right().value());
            return Optional.empty();
        }
        final DAOArtifactData artifactData = artifactResponse.left().value();
        return Optional.of(artifactData.getDataAsArray());
    }

    private String buildToscaMetaContent(final String nsdFileName) {
        LOGGER.debug("Creating TOSCA.meta content");
        final NsdToscaMetadataBuilder builder = new NsdToscaMetadataBuilder();

        builder.withCsarVersion("1.1")
            .withCreatedBy("ONAP")
            .withToscaMetaVersion("1.0")
            .withEntryDefinitions(getNsdPath(nsdFileName))
            .withEntryManifest(getManifestPath(nsdFileName))
            .withEntryChangeLog("ChangeLog.txt");

        final String toscaMetadata = builder.build();
        LOGGER.debug("Successfully created NS CSAR TOSCA.meta content:\n {}", toscaMetadata);
        return toscaMetadata;
    }

    private NsdCsarManifestBuilder createManifestBuilder(final Nsd nsd, final EtsiVersion nsdVersion,
                                                         final String manifestFilePath, final Set<String> filePath,
                                                         final boolean addSignatureFiles) {
        LOGGER.debug("Creating NS manifest file content");

        final Set<String> filesToAdd = new HashSet<>(filePath);
        if (addSignatureFiles) {
            filePath.forEach(file -> filesToAdd.add(file + SIGNATURE_EXTENSION));
        }
        filesToAdd.add(manifestFilePath);

        final NsdCsarManifestBuilder nsdCsarManifestBuilder = new NsdCsarManifestBuilder();
        nsdCsarManifestBuilder.withDesigner(nsd.getDesigner())
            .withInvariantId(nsd.getInvariantId())
            .withName(nsd.getName())
            .withNowReleaseDateTime()
            .withFileStructureVersion(nsd.getVersion())
            .withSources(filesToAdd);

        final NsDescriptorVersionComparator nsdVersionComparator = new NsDescriptorVersionComparator();

        if (nsdVersion != null && nsdVersionComparator.compare(nsdVersion, EtsiVersion.VERSION_3_3_1) >= 0) {
            nsdCsarManifestBuilder.withCompatibleSpecificationVersion(nsdVersion.getVersion());
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Successfully created NS CSAR manifest file content:\n {}", nsdCsarManifestBuilder.build());
        }
        return nsdCsarManifestBuilder;
    }

    private String getManifestPath(final String nsdFileName) {
        return nsdFileName + DOT + MANIFEST_EXT;
    }

    private String getNsdPath(final String nsdFileName) {
        return DEFINITION + SLASH + nsdFileName + DOT_YAML;
    }

    private String getNsdFileName(final Component component) {
        return component.getNormalizedName();
    }

    private byte[] buildCsarPackage(final Map<String, byte[]> nsdCsarFileMap) throws NsdException {
        if (nsdCsarFileMap.isEmpty()) {
            throw new NsdException("No files were provided to build the NSD CSAR package");
        }
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ZipOutputStream zip = new ZipOutputStream(out)) {
            for (final Entry<String, byte[]> entry : nsdCsarFileMap.entrySet()) {
                final String filePath = entry.getKey();
                final byte[] fileContent = entry.getValue();
                if (fileContent == null) {
                    LOGGER.error("Could not add '{}' to NSD CSAR. File content is null", filePath);
                    continue;
                }
                LOGGER.debug("Adding '{}' to NSD CSAR with content size: '{}'", filePath, fileContent.length);
                zip.putNextEntry(new ZipEntry(filePath));
                zip.write(fileContent);
            }
            zip.flush();
            zip.finish();
            LOGGER.debug("NSD CSAR zip file was successfully built");

            return out.toByteArray();
        } catch (final IOException e) {
            throw new NsdException("Could not build the NSD CSAR zip file", e);
        }
    }

    private byte[] buildZipWithCsarAndSignature(final String csarFileName, final byte[] csarPackageBytes) throws NsdException {
        final byte[] signature;
        try {
            signature = nsdCsarEtsiOption2Signer.sign(csarPackageBytes);
        } catch (final Exception e) {
            throw new NsdException(String.format("Could not sign the CSAR '%s'", csarFileName), e);
        }
        final Optional<CertificateInfo> certificateInfoOpt = nsdCsarEtsiOption2Signer.getSigningCertificate();
        if (certificateInfoOpt.isEmpty()) {
            throw new NsdException(String.format("Could not sign the CSAR '%s'. No certificate configured.", csarFileName));
        }
        final CertificateInfo certificateInfo = certificateInfoOpt.get();
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final ZipOutputStream zip = new ZipOutputStream(out)) {
            zip.putNextEntry(new ZipEntry(csarFileName + CSAR_EXTENSION));
            zip.write(csarPackageBytes);
            zip.putNextEntry(new ZipEntry(csarFileName + CSAR_SIGNATURE_EXTENSION));
            zip.write(signature);
            final File certificateFile = certificateInfo.getCertificateFile();
            zip.putNextEntry(new ZipEntry(csarFileName + "." + FilenameUtils.getExtension(certificateFile.getName())));
            zip.write(Files.readAllBytes(certificateFile.toPath()));
            zip.flush();
            zip.finish();
            LOGGER.debug("NSD signed CSAR zip file was successfully built");

            return out.toByteArray();
        } catch (final IOException e) {
            throw new NsdException("Could not build the NSD signed CSAR zip file", e);
        }
    }

}


