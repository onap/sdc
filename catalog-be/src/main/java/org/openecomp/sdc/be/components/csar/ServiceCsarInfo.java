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
import static org.openecomp.sdc.be.components.impl.ImportUtils.findFirstToscaStringElement;
import static org.openecomp.sdc.be.components.impl.ImportUtils.findToscaElement;

import fj.data.Either;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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
import org.openecomp.sdc.be.model.NullNodeTypeMetadata;
import org.openecomp.sdc.be.model.User;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.category.SubCategoryDefinition;
import org.openecomp.sdc.be.model.operations.impl.ModelOperation;
import org.openecomp.sdc.be.utils.TypeUtils;
import org.openecomp.sdc.be.utils.TypeUtils.ToscaTagNamesEnum;
import org.openecomp.sdc.common.api.Constants;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.yaml.snakeyaml.Yaml;

/**
 * Provides access to the contents of a Service CSAR
 */
public class ServiceCsarInfo extends CsarInfo {

    private static final Logger log = Logger.getLogger(ServiceCsarInfo.class);
    private final Map<String, Map<String, Object>> mainTemplateImports;
    private List<NodeTypeDefinition> nodeTypeDefinitions;
    private final String model;
    private final ModelOperation modelOperation;

    public ServiceCsarInfo(final User modifier, final String csarUUID, final Map<String, byte[]> csar,
                           final String vfResourceName, final String model,
                           final String mainTemplateName, final String mainTemplateContent, final boolean isUpdate, final ModelOperation modelOperation) {
        super(modifier, csarUUID, csar, vfResourceName, mainTemplateName, mainTemplateContent, isUpdate);
        this.model = model;
        this.modelOperation = modelOperation;
        final Path mainTemplateDir = Paths.get(getMainTemplateName().substring(0, getMainTemplateName().lastIndexOf('/') + 1));
        final Collection<Path> filesHandled = new HashSet<>();
        filesHandled.add(Paths.get(mainTemplateName));
        this.mainTemplateImports = getTemplateImports(csar, new Yaml().load(mainTemplateContent), mainTemplateDir, filesHandled);
    }

    private Map<String, Map<String, Object>> getTemplateImports(final Map<String, byte[]> csar, Map<String, Object> mappedToscaMainTemplate,
                                                                final Path fileParentDir, final Collection<Path> filesHandled) {
        final Map<String, Map<String, Object>> templateImports = new HashMap<>();

        final List<Path> importFilePaths = getTemplateImportFilePaths(mappedToscaMainTemplate, fileParentDir);

        importFilePaths.stream().filter(path -> !filesHandled.contains(path)).forEach(
            importFilePath -> {
                final String importFilePathString = importFilePath.toString();
                final byte[] importFile = csar.get(importFilePathString);
                if (importFile != null) {
                    filesHandled.add(importFilePath);
                    final Map<String, Object> mappedImportFile = new Yaml().load(new String(importFile));
                    templateImports.put(importFilePathString, mappedImportFile);
                    templateImports.putAll(getTemplateImports(csar, mappedImportFile, importFilePath.getParent(), filesHandled));
                } else {
                    log.warn("Import {} cannot be found in CSAR", importFilePathString);
                }
            });

        return templateImports;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<Path> getTemplateImportFilePaths(final Map<String, Object> mappedToscaTemplate, final Path fileParentDir) {
        final Either<Object, ResultStatusEnum> importsEither =
            findToscaElement(mappedToscaTemplate, ToscaTagNamesEnum.IMPORTS, ToscaElementTypeEnum.ALL);

        if (importsEither.isLeft()) {
            final List importsList = (List) importsEither.left().value();
            if (CollectionUtils.isNotEmpty(importsList)) {
                if (importsList.get(0) instanceof String) {
                    List<Path> importPaths = new ArrayList<>();
                    importsList.forEach(
                        importPath -> {
                            final Path path = fileParentDir == null ?
                                Paths.get((String) importPath) : fileParentDir.resolve(Paths.get((String) importPath)).normalize();
                            importPaths.add(path);
                        });
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
                    Path absolutePath = fileParentDir == null ? relativePath : fileParentDir.resolve(relativePath).normalize();
                    importFiles.add(absolutePath);
                } else if (importFileMultiLineGrammar.values().iterator().next() instanceof Map) {
                    importFileMultiLineGrammar.values().forEach(value -> {
                        Path relativePath = Paths.get((String) ((Map<String, Object>) value).get("file"));
                        Path absolutePath = fileParentDir == null ? relativePath : fileParentDir.resolve(relativePath).normalize();
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
        return getTypes(ToscaTagNamesEnum.DATA_TYPES);
    }

    @Override
    public Map<String, Object> getGroupTypes() {
        return getTypes(ToscaTagNamesEnum.GROUP_TYPES);
    }

    @Override
    public Map<String, Object> getCapabilityTypes() {
        return getTypes(ToscaTagNamesEnum.CAPABILITY_TYPES);
    }

    @Override
    public Map<String, Object> getArtifactTypes() {
        return getTypes(ToscaTagNamesEnum.ARTIFACT_TYPES);
    }

    @Override
    public Map<String, Object> getInterfaceTypes() {
        return getTypes(ToscaTagNamesEnum.INTERFACE_TYPES);
    }

    private Map<String, Object> getTypes(ToscaTagNamesEnum toscaTag) {
        final Map<String, Object> types = new HashMap<>();
        mainTemplateImports.entrySet().forEach(entry -> types.putAll(getTypesFromTemplate(entry.getValue(), toscaTag)));
        types.putAll(getTypesFromTemplate(getMappedToscaMainTemplate(), toscaTag));
        return types;
    }

    public List<NodeTypeDefinition> getNodeTypesUsed() {
        if (nodeTypeDefinitions == null) {
            nodeTypeDefinitions = new ArrayList<>();
            final Set<String> nodeTypesUsed = getNodeTypesUsedInToscaTemplate(getMappedToscaMainTemplate());
            nodeTypeDefinitions.addAll(getNodeTypeDefinitions(nodeTypesUsed).values());
        }
        nodeTypeDefinitions = sortNodeTypesByDependencyOrder(nodeTypeDefinitions);
        return nodeTypeDefinitions;
    }

    private List<NodeTypeDefinition> sortNodeTypesByDependencyOrder(final List<NodeTypeDefinition> nodeTypes) {
        final List<NodeTypeDefinition> sortedNodeTypeDefinitions = new ArrayList<>();
        final Map<String, NodeTypeDefinition> nodeTypeDefinitionsMap = new HashMap<>();

        nodeTypes.forEach(nodeType -> {
            int highestDependencyIndex = -1;
            for (final String dependencyName : getDependencyTypes(nodeType, nodeTypes)) {
                final NodeTypeDefinition dependency = nodeTypeDefinitionsMap.get(dependencyName);
                final int indexOfDependency = sortedNodeTypeDefinitions.lastIndexOf(dependency);
                highestDependencyIndex = Math.max(indexOfDependency, highestDependencyIndex);
            }
            sortedNodeTypeDefinitions.add(highestDependencyIndex + 1, nodeType);
            nodeTypeDefinitionsMap.put(nodeType.getMappedNodeType().getKey(), nodeType);
        });
        return sortedNodeTypeDefinitions;
    }

    private Collection<String> getDependencyTypes(final NodeTypeDefinition nodeType, final List<NodeTypeDefinition> nodeTypes) {
        final Set<String> dependencies = new HashSet<>();
        Either<Object, ResultStatusEnum> derivedFromTypeEither = findToscaElement((Map<String, Object>) nodeType.getMappedNodeType().getValue(),
            TypeUtils.ToscaTagNamesEnum.DERIVED_FROM, ToscaElementTypeEnum.STRING);
        if (derivedFromTypeEither.isLeft() && derivedFromTypeEither.left().value() != null) {
            final String derivedFrom = (String) derivedFromTypeEither.left().value();
            dependencies.add(derivedFrom);
            nodeTypes.stream().filter(derivedFromCandidate -> derivedFrom.contentEquals(derivedFromCandidate.getMappedNodeType().getKey()))
                .forEach(derivedFromNodeType -> dependencies.addAll(getDependencyTypes(derivedFromNodeType, nodeTypes)));
        }
        return dependencies;
    }

    private Map<String, NodeTypeDefinition> getNodeTypeDefinitions(final Set<String> nodeTypesToGet) {
        final Map<String, NodeTypeDefinition> foundNodeTypes = getTypes(nodeTypesToGet);
        final Map<String, NodeTypeDefinition> nodeTypesToReturn = new HashMap<>(foundNodeTypes);
        final Set<String> recursiveNodeTypesToGet = new HashSet<>();
        foundNodeTypes.values().forEach(nodeTypeDef -> {
            Either<Object, ResultStatusEnum> derivedFromTypeEither =
                findToscaElement((Map<String, Object>) nodeTypeDef.getMappedNodeType().getValue(), TypeUtils.ToscaTagNamesEnum.DERIVED_FROM,
                    ToscaElementTypeEnum.STRING);
            if (derivedFromTypeEither.isLeft()) {
                recursiveNodeTypesToGet.add((String) derivedFromTypeEither.left().value());
            }
        });
        recursiveNodeTypesToGet.removeAll(nodeTypesToGet);
        if (CollectionUtils.isNotEmpty(recursiveNodeTypesToGet)) {
            nodeTypesToReturn.putAll(getNodeTypeDefinitions(recursiveNodeTypesToGet));
        }
        return nodeTypesToReturn;
    }

    private Map<String, NodeTypeDefinition> getTypes(final Set<String> nodeTypes) {
        final Map<String, NodeTypeDefinition> nodeTypeDefinitionsMap = new HashMap<>();
        final Set<String> lowerPrecedenceImports = new HashSet<>();

        if (model != null && !model.equals(Constants.DEFAULT_MODEL_NAME)) {
            final Set<String> modelImports = new HashSet<>();
            modelOperation.findAllModelImports(model, true).forEach(modelImport -> modelImports.add("Definitions/" + modelImport.getFullPath()));

            lowerPrecedenceImports.add("Definitions/" + ModelOperation.ADDITIONAL_TYPE_DEFINITIONS_PATH);
            lowerPrecedenceImports.addAll(modelImports);

            mainTemplateImports.entrySet().stream().filter(entry -> modelImports.contains(entry.getKey()))
                    .forEach(template -> addTypesFromTemplate(nodeTypeDefinitionsMap, template.getValue(), nodeTypes));

            mainTemplateImports.entrySet().stream().filter(entry -> entry.getKey().equals(ModelOperation.ADDITIONAL_TYPE_DEFINITIONS_PATH.toString()))
                    .forEach(template -> addTypesFromTemplate(nodeTypeDefinitionsMap, template.getValue(), nodeTypes));
        }

        mainTemplateImports.entrySet().stream().filter(entry -> !lowerPrecedenceImports.contains(entry.getKey()))
                .forEach(template -> addTypesFromTemplate(nodeTypeDefinitionsMap, template.getValue(), nodeTypes));

        return nodeTypeDefinitionsMap;
    }

    private void addTypesFromTemplate(final Map<String, NodeTypeDefinition> nodeTypeDefinitionsMap, final Map<String, Object> mappedTemplate,
            final Set<String> nodeTypes) {
        final Map<String, Object> types = getTypesFromTemplate(mappedTemplate, ToscaTagNamesEnum.NODE_TYPES, nodeTypes);
        if (MapUtils.isNotEmpty(types)) {
            types.entrySet().forEach(typesEntry -> {
                final NodeTypeMetadata metadata = getMetaDataFromTemplate(mappedTemplate, typesEntry.getKey());
                nodeTypeDefinitionsMap.put(typesEntry.getKey(), new NodeTypeDefinition(typesEntry, metadata));
            });
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> getNodeTypesUsedInToscaTemplate(final Map<String, Object> mappedToscaTemplate) {
        final var nodeTemplatesEither = findToscaElement(mappedToscaTemplate, TypeUtils.ToscaTagNamesEnum.NODE_TEMPLATES, ToscaElementTypeEnum.MAP);
        final Set<String> nodeTypesUsedInToscaTemplate = new HashSet<>();
        if (nodeTemplatesEither.isLeft()) {
            final var nodeTemplates = (Map<String, Map<String, Object>>) nodeTemplatesEither.left().value();
            nodeTypesUsedInToscaTemplate.addAll(findNodeTypesUsedInNodeTemplates(nodeTemplates));
        }
        final var substitutionMappingsNodeType = findFirstToscaStringElement(mappedToscaTemplate, ToscaTagNamesEnum.NODE_TYPE);
        if (substitutionMappingsNodeType.isLeft()){
            nodeTypesUsedInToscaTemplate.add(substitutionMappingsNodeType.left().value());
        }
        return nodeTypesUsedInToscaTemplate;
    }

    private NodeTypeMetadata getMetaDataFromTemplate(Map<String, Object> mappedResourceTemplate, String nodeTemplateType) {
        NodeTypeMetadata nodeTypeMetadata = new NodeTypeMetadata();
        Either<Map<String, Object>, ImportUtils.ResultStatusEnum> metadataEither = ImportUtils.findFirstToscaMapElement(mappedResourceTemplate,
            TypeUtils.ToscaTagNamesEnum.METADATA);
        if (metadataEither.isLeft() && metadataEither.left().value().get("type").equals(ResourceTypeEnum.VFC.getValue())) {
            Map<String, Object> metadata = metadataEither.left().value();
            createMetadataFromTemplate(nodeTypeMetadata, metadata, nodeTemplateType);
        } else {
            nodeTypeMetadata = createDefaultMetadata(nodeTemplateType);
        }
        return nodeTypeMetadata;
    }

    private void createMetadataFromTemplate(NodeTypeMetadata nodeTypeMetadata, Map<String, Object> metadata, String nodeTemplateType) {
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
        category.addSubCategory(subCategory);
        List<CategoryDefinition> categories = new ArrayList<>();
        categories.add(category);
        nodeTypeMetadata.setCategories(categories);
        nodeTypeMetadata.setName((String) metadata.get("name"));
        nodeTypeMetadata.setIcon("defaulticon");
        nodeTypeMetadata.setResourceVendorModelNumber((String) metadata.get("resourceVendorModelNumber"));
        nodeTypeMetadata.setResourceType((String) metadata.get("type"));
        nodeTypeMetadata.setVendorName((String) metadata.get("resourceVendor"));
        nodeTypeMetadata.setVendorRelease(String.valueOf(metadata.get("resourceVendorRelease")));
        nodeTypeMetadata.setModel(model);
        nodeTypeMetadata.setNormative(false);
    }

    private NullNodeTypeMetadata createDefaultMetadata(String nodeTemplateType) {
        NullNodeTypeMetadata nodeTypeMetadata = new NullNodeTypeMetadata();
        nodeTypeMetadata.setToscaName(nodeTemplateType);
        nodeTypeMetadata.setModel(model);
        return nodeTypeMetadata;
    }
}
