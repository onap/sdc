/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.components.csar;

import static org.openecomp.sdc.be.components.impl.ImportUtils.Constants.DEFAULT_ICON;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;

import fj.data.Either;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils;
import org.openecomp.sdc.be.components.impl.ImportUtils.ResultStatusEnum;
import org.openecomp.sdc.be.components.impl.ImportUtils.ToscaElementTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.NodeTypeDefinition;
import org.openecomp.sdc.be.model.NodeTypeInfo;
import org.openecomp.sdc.be.model.NodeTypeMetadata;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Provides access to the contents of a Service CSAR
 */
public class ServiceCsarInfo extends CsarInfo {

    private static final Logger log = Logger.getLogger(ServiceCsarInfo.class);
    private final Map<String, Map<String, Object>> mainTemplateImports;
    private Set<NodeTypeDefinition> nodeTypeDefinitions;

    public ServiceCsarInfo(final User modifier, final String csarUUID, final Map<String, byte[]> csar, final String vfResourceName,
                           final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate) {
        super(modifier, csarUUID, csar, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);

        final Path mainTemplateDir = Paths.get(getMainTemplateName().substring(0, getMainTemplateName().lastIndexOf('/') + 1));
        this.mainTemplateImports = getTemplateImports(csar, new Yaml().load(mainTemplateContent), mainTemplateDir);
    }

    private Map<String, Map<String, Object>> getTemplateImports(final Map<String, byte[]> csar, Map<String, Object> mappedToscaMainTemplate,
                                                                final Path fileParentDir) {
        final Map<String, Map<String, Object>> templateImports = new HashMap<>();

        final List<Path> importFilePaths = getTempateImportFilePaths(mappedToscaMainTemplate, fileParentDir);

        importFilePaths.stream().forEach(
                importFilePath -> {
                    byte[] importFile = csar.get(importFilePath.toString());
                    if (importFile != null) {
                        Map<String, Object> mappedImportFile = new Yaml().load(new String(csar.get(importFilePath.toString())));
                        templateImports.put(importFilePath.toString(), mappedImportFile);

                        templateImports.putAll(getTemplateImports(csar, mappedImportFile, importFilePath.getParent()));

                    } else {
                        log.info("Import {} cannot be found in CSAR", importFilePath.toString());
                    }
                });

        return templateImports;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Path> getTempateImportFilePaths(final Map<String, Object> mappedToscaTemplate, final Path fileParentDir) {
        final Either<Object, ResultStatusEnum> importsEither =
            findToscaElement(mappedToscaTemplate, ToscaTagNamesEnum.IMPORTS, ToscaElementTypeEnum.ALL);

        if (importsEither.isLeft()) {
            final List importsList = (List) importsEither.left().value();
            if (CollectionUtils.isNotEmpty(importsList)) {
                if (importsList.get(0) instanceof String) {
                    List<Path> importPaths = new ArrayList<>();
                    importsList.stream().forEach(importPath -> importPaths.add(Paths.get((String) importPath)));
                    return importPaths;
                } else if (importsList.get(0) instanceof Map) {
                    return getTemplateImportFilePathsMultiLineGrammar(importsList, fileParentDir);
                }
            }

        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Path> getTemplateImportFilePathsMultiLineGrammar(final List<Map<String, Object>> importsList, final Path fileParentDir) {
        final List<Path> importFiles = new ArrayList<>();

        for (Map<String, Object> importFileMultiLineGrammar : importsList) {
            if (MapUtils.isNotEmpty(importFileMultiLineGrammar)) {
                if (importFileMultiLineGrammar.values().iterator().next() instanceof String) {
                    Path relativePath = Paths.get((String) importFileMultiLineGrammar.get("file"));
                    Path absolutePath = fileParentDir.resolve(relativePath).normalize();
                    importFiles.add(absolutePath);
                } else if (importFileMultiLineGrammar.values().iterator().next() instanceof Map) {
                    importFileMultiLineGrammar.values().forEach(value -> {
                        Path relativePath = Paths.get((String) ((Map<String, Object>) value).get("file"));
                        Path absolutePath = fileParentDir.resolve(relativePath).normalize();
                        importFiles.add(absolutePath);
                    });
                }
            }
        }
        return importFiles;
    }

    @Override
    public Map<String, NodeTypeInfo> extractTypesInfo() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> getDataTypes() {
        final Map<String, Object> definitions = new HashMap<>();
        mainTemplateImports.entrySet().stream()
            .forEach(entry -> definitions.putAll(getTypesFromTemplate(entry.getValue(), TypeUtils.ToscaTagNamesEnum.DATA_TYPES)));
        definitions.putAll(getTypesFromTemplate(getMappedToscaMainTemplate(), TypeUtils.ToscaTagNamesEnum.DATA_TYPES));
        return definitions;
    }

    public NodeTypeDefinition getNodeTypeDefinition(String nodeType) {
        Set<String> nodeTypesUsed = Collections.singleton(nodeType);
        for (Map.Entry<String, Map<String, Object>> entry : mainTemplateImports.entrySet()) {
            final Map<String, Object> types = getTypesFromTemplate(entry.getValue(),
                TypeUtils.ToscaTagNamesEnum.NODE_TYPES, nodeTypesUsed);
            if (MapUtils.isNotEmpty(types)) {
                Map.Entry<String, Object> type = types.entrySet().iterator().next();
                final NodeTypeMetadata metadata =
                    getMetaDataFromTemplate(entry.getValue(), getModifier().getUserId(), type.getKey());
                return new NodeTypeDefinition(type, metadata);
            }
        }
        return null;
    }

    public Set<NodeTypeDefinition> getNodeTypesUsed() {
        if (nodeTypeDefinitions == null) {
            nodeTypeDefinitions = new HashSet<>();
            final Set<String> nodeTypesUsed = getNodeTypesUsedInToscaTemplate(getMappedToscaMainTemplate());
            mainTemplateImports.entrySet().forEach(entry -> {
                final Map<String, Object> types = getTypesFromTemplate(entry.getValue(), TypeUtils.ToscaTagNamesEnum.NODE_TYPES, nodeTypesUsed);
                if (MapUtils.isNotEmpty(types)) {
                    Map.Entry<String, Object> type = types.entrySet().iterator().next();
                    final NodeTypeMetadata metadata =
                        getMetaDataFromTemplate(entry.getValue(), getModifier().getUserId(), type.getKey());
                    nodeTypeDefinitions.add(new NodeTypeDefinition(type, metadata));
                }
            });
        }
        return nodeTypeDefinitions;
    }

    @SuppressWarnings("unchecked")
    private Set<String> getNodeTypesUsedInToscaTemplate(Map<String, Object> mappedToscaTemplate) {
        final Either<Object, ResultStatusEnum> nodeTemplatesEither = findToscaElement(mappedToscaTemplate,
            TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES, ToscaElementTypeEnum.MAP);
        final Set<String> nodeTypesUsedInNodeTemplates = new HashSet<>();
        if (nodeTemplatesEither.isLeft()) {
            final Map<String, Map<String, Object>> nodeTemplates =
                (Map<String, Map<String, Object>>) nodeTemplatesEither.left().value();
            nodeTypesUsedInNodeTemplates.addAll(findNodeTypesUsedInNodeTemplates(nodeTemplates));
        }
        return nodeTypesUsedInNodeTemplates;
    }

    private NodeTypeMetadata getMetaDataFromTemplate(Map<String, Object> mappedResourceTemplate, String userId, String nodeTemplateType) {
        NodeTypeMetadata nodeTypeMetadata = new NodeTypeMetadata();
        final Either<Map<String, Object>, ImportUtils.ResultStatusEnum> metadataEither = ImportUtils.findFirstToscaMapElement(mappedResourceTemplate, TypeUtils.ToscaTagNamesEnum.METADATA);
        if (metadataEither.isLeft()) {
            Map<String, Object> metadata = metadataEither.left().value();
            nodeTypeMetadata.setToscaName(nodeTemplateType);
            nodeTypeMetadata.setContactId(getModifier().getUserId());
            nodeTypeMetadata.setDescription((String) metadata.get("description"));
            List<String> tags = new ArrayList<>();
            tags.add((String) metadata.get("name"));
            nodeTypeMetadata.setTags(tags);
            SubCategoryDefinition subCategory = new SubCategoryDefinition();
            subCategory.setName((String) metadata.get("subcategory"));
            CategoryDefinition category = new CategoryDefinition();
            category.setName((String) metadata.get("category"));
            category.setNormalizedName(((String) metadata.get("category")).toLowerCase());
            category.setIcons(List.of(DEFAULT_ICON));
            category.setNormalizedName(((String) metadata.get("category")).toLowerCase());
            category.addSubCategory(subCategory);
            List<CategoryDefinition> categories = new ArrayList<>();
            categories.add(category);
            nodeTypeMetadata.setCategories(categories);
            nodeTypeMetadata.setName((String) metadata.get("name"));
            nodeTypeMetadata.setIcon("defaulticon");
            nodeTypeMetadata.setResourceVendorModelNumber((String) metadata.get("resourceVendorModelNumber"));
            nodeTypeMetadata.setResourceType((String) metadata.get("type"));
            nodeTypeMetadata.setVendorName((String) metadata.get("resourceVendor"));
            nodeTypeMetadata.setVendorRelease((String) metadata.get("resourceVendorRelease"));
            nodeTypeMetadata.setModel((String) metadata.get("model"));
            nodeTypeMetadata.setNormative(false);
        } else {
            nodeTypeMetadata.setToscaName(nodeTemplateType);
            nodeTypeMetadata.setContactId(getModifier().getUserId());
            nodeTypeMetadata.setDescription("A vfc of type " + nodeTemplateType);
            Either<Map<String, Object>, ResultStatusEnum> mainMetadataEither = ImportUtils.findFirstToscaMapElement(getMappedToscaMainTemplate(),
                    ToscaTagNamesEnum.METADATA);
            Map<String, Object> mainMetadata = mainMetadataEither.left().value();
            nodeTypeMetadata.setModel((String) mainMetadata.get("model"));
            SubCategoryDefinition subCategory = new SubCategoryDefinition();
            subCategory.setName("Network Elements");
            CategoryDefinition category = new CategoryDefinition();
            category.setName("Generic");
            category.setNormalizedName("generic");
            category.setIcons(List.of(DEFAULT_ICON));
            category.setNormalizedName("generic");
            category.addSubCategory(subCategory);
            List<CategoryDefinition> categories = new ArrayList<>();
            categories.add(category);
            nodeTypeMetadata.setCategories(categories);
            String[] nodeTemplateName = nodeTemplateType.split("\\.");
            String name =  nodeTemplateName[nodeTemplateName.length - 1];
            nodeTypeMetadata.setName(name);
            List<String> tags = new ArrayList<>();
            tags.add(name);
            nodeTypeMetadata.setTags(tags);
            nodeTypeMetadata.setIcon("defaulticon");
            nodeTypeMetadata.setNormative(false);
        }
        return nodeTypeMetadata;
    }

}
