package org.openecomp.core.impl;

import org.apache.commons.collections.MapUtils;
import org.openecomp.core.converter.ServiceTemplateReaderService;
import org.openecomp.core.converter.ToscaConverter;
import org.openecomp.core.converter.datatypes.Constants;
import org.openecomp.core.converter.datatypes.CsarFileTypes;
import org.openecomp.core.converter.errors.SubstitutionMappingsConverterErrorBuilder;
import org.openecomp.core.impl.services.ServiceTemplateReaderServiceImpl;
import org.openecomp.core.utilities.file.FileContentHandler;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.*;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import static org.openecomp.core.converter.datatypes.Constants.*;
import static org.openecomp.core.impl.GlobalSubstitutionServiceTemplate.GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME;

public class ToscaConverterImpl implements ToscaConverter {

    private static final String ORIGINAL = ".original";

    @Override
    public ToscaServiceModel convert(FileContentHandler fileContentHandler)
        throws IOException {
        Map<String, byte[]> csarFiles = new HashMap<>(fileContentHandler.getFiles());
        ToscaServiceModel toscaServiceModel = new ToscaServiceModel();
        Map<String, ServiceTemplate> serviceTemplates = new HashMap<>();
        FileContentHandler artifacts = new FileContentHandler();
        GlobalSubstitutionServiceTemplate gsst = new GlobalSubstitutionServiceTemplate();
        for (Map.Entry<String, byte[]> fileEntry : csarFiles.entrySet()) {
            CsarFileTypes fileType = getFileType(fileEntry.getKey());
            switch (fileType) {
                case mainServiceTemplate:
                    handleServiceTemplate(mainStName, fileEntry.getKey(), csarFiles, serviceTemplates);
                    break;

                case globalServiceTemplate:
                    handleServiceTemplate(globalStName, fileEntry.getKey(), csarFiles, serviceTemplates);
                    break;

                case externalFile:
                    artifacts.addFile(
                        getConcreteArtifactFileName(fileEntry.getKey()), fileEntry.getValue());
                    break;

                case definitionsFile:
                    handleDefintionTemplate(fileEntry.getKey(), csarFiles, gsst);
                    break;

                default:
                    break;
            }
        }
        handleMetadataFile(csarFiles);
        updateToscaServiceModel(toscaServiceModel, serviceTemplates, artifacts, gsst, csarFiles);
        return toscaServiceModel;
    }

    private void handleMetadataFile(Map<String, byte[]> csarFiles) {
        byte[] bytes = csarFiles.remove(metadataFile);
        if (bytes != null) {
            csarFiles.put(metadataFile + ORIGINAL, bytes);
        }
    }

    private void handleDefintionTemplate(String key, Map<String, byte[]> csarFiles,
                                         GlobalSubstitutionServiceTemplate gsst) {
        try {
            ServiceTemplateReaderService readerService = new ServiceTemplateReaderServiceImpl(csarFiles.get(key));
            Object nodeTypes = readerService.getNodeTypes();
            if (nodeTypes instanceof Map) {
                Map<String, NodeType> nodeTypeMap = (Map<String, NodeType>) nodeTypes;
                gsst.appendNodes(nodeTypeMap);
            }
        } catch (YAMLException ye) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder()
                .withMessage("Invalid YAML content in file " + key + ". reason - "
                    + ye.getMessage())
                .withCategory(ErrorCategory.APPLICATION).build());
        }
    }

    private String getConcreteArtifactFileName(String fileName){
        int artifactIndex = fileName.indexOf(CsarFileTypes.Artifacts.name());
        if(artifactIndex < 0){
            return fileName;
        }

        int artifactDirectoryIndex =
            artifactIndex + CsarFileTypes.Artifacts.name().length() + 1;
        return fileName.substring(artifactDirectoryIndex);
    }

    private void updateToscaServiceModel(ToscaServiceModel toscaServiceModel,
                                         Map<String, ServiceTemplate> serviceTemplates,
                                         FileContentHandler externalFilesHandler,
                                         GlobalSubstitutionServiceTemplate globalSubstitutionServiceTemplate,
                                         Map<String, byte[]> csarFiles) {
        Collection<ServiceTemplate> globalServiceTemplates =
            GlobalTypesGenerator.getGlobalTypesServiceTemplate().values();
        addGlobalServiceTemplates(globalServiceTemplates, serviceTemplates);
        toscaServiceModel.setEntryDefinitionServiceTemplate(mainStName);
        toscaServiceModel.setServiceTemplates(serviceTemplates);
        externalFilesHandler.addFile(metadataFile + ORIGINAL,
            csarFiles.get(metadataFile + ORIGINAL));
        toscaServiceModel.setArtifactFiles(externalFilesHandler);

        if(MapUtils.isNotEmpty(globalSubstitutionServiceTemplate.getNode_types())) {
            serviceTemplates
                .put(GLOBAL_SUBSTITUTION_SERVICE_FILE_NAME, globalSubstitutionServiceTemplate);
        }
    }

    private void addGlobalServiceTemplates(Collection<ServiceTemplate> globalServiceTemplates,
                                           Map<String, ServiceTemplate> serviceTemplates) {
        for (ServiceTemplate serviceTemplate : globalServiceTemplates) {
            serviceTemplates.put(ToscaUtil.getServiceTemplateFileName(serviceTemplate), serviceTemplate);
        }
    }

    private void handleServiceTemplate(String serviceTemplateName,
                                       String fileName, Map<String, byte[]> csarFiles,
                                       Map<String, ServiceTemplate> serviceTemplates) {
        Optional<ServiceTemplate> serviceTemplate =
            getServiceTemplateFromCsar(fileName, csarFiles);
        serviceTemplate.ifPresent(
            serviceTemplateValue -> addServiceTemplate(serviceTemplateName, serviceTemplateValue,
                serviceTemplates));
    }

    private void addServiceTemplate(String serviceTemplateName,
                                    ServiceTemplate serviceTemplate,
                                    Map<String, ServiceTemplate> serviceTemplates) {
        serviceTemplates.put(serviceTemplateName, serviceTemplate);
    }

    private Optional<byte[]> getManifestContent(Map<String, byte[]> csarFiles) {
        for (Map.Entry<String, byte[]> csarFileEntry : csarFiles.entrySet()) {
            if (csarFileEntry.getKey().contains(manifestFileName)) {
                return Optional.of(csarFileEntry.getValue());
            }
        }

        return Optional.empty();
    }

    private Optional<ServiceTemplate> getServiceTemplateFromCsar(String fileName,
                                                                 Map<String, byte[]> csarFiles) {
        byte[] fileContent = csarFiles.get(fileName);
        ServiceTemplate serviceTemplate = convertServiceTemplate(fileName, fileContent);

        return Optional.of(serviceTemplate);
    }

    private ServiceTemplate convertServiceTemplate(String serviceTemplateName,
                                                   byte[] fileContent) {
        ServiceTemplate serviceTemplate = new ServiceTemplate();
        try {
            ServiceTemplateReaderService readerService =
                new ServiceTemplateReaderServiceImpl(fileContent);
            convertMetadata(serviceTemplateName, serviceTemplate, readerService);
            convertToscaVersion(serviceTemplate, readerService);
            convertImports(serviceTemplate);
            convertNodeTypes(serviceTemplate, readerService);
            convertTopologyTemplate(serviceTemplate, readerService);

        } catch (YAMLException ye) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder()
                .withMessage("Invalid YAML content in file" + serviceTemplateName + ". reason - "
                    + ye.getMessage())
                .withCategory(ErrorCategory.APPLICATION).build());
        }


        return serviceTemplate;
    }

    private void convertToscaVersion(ServiceTemplate serviceTemplate,
                                     ServiceTemplateReaderService readerService) {
        Object toscaVersion = readerService.getToscaVersion();
        serviceTemplate.setTosca_definitions_version((String) toscaVersion);
    }

    private void convertImports(ServiceTemplate serviceTemplate) {
        serviceTemplate.setImports(new ArrayList<>());
        serviceTemplate.getImports()
            .add(createImportMap(openecompHeatIndex, "openecomp-heat/_index.yml"));
        serviceTemplate.getImports().add(createImportMap(globalSubstitution, globalStName));

    }

    private Map<String, Import> createImportMap(String key, String fileName) {
        Map<String, Import> importMap = new HashMap<>();
        Import anImport = new Import();
        anImport.setFile(fileName);
        importMap.put(key, anImport);

        return importMap;
    }

    private void convertMetadata(String serviceTemplateName,
                                 ServiceTemplate serviceTemplate,
                                 ServiceTemplateReaderService readerService) {
        Map<String, Object> metadataToConvert = (Map<String, Object>) readerService.getMetadata();
        Map<String, String> finalMetadata = new HashMap<>();

        if (MapUtils.isNotEmpty(metadataToConvert)) {
            for (Map.Entry<String, Object> metadataEntry : metadataToConvert.entrySet()) {
                if (Objects.isNull(metadataEntry.getValue()) ||
                    !(metadataEntry.getValue() instanceof String)) {
                    continue;
                }
                finalMetadata.put(metadataEntry.getKey(), (String) metadataEntry.getValue());
            }
        }

        finalMetadata.put("template_name", getTemplateNameFromStName(serviceTemplateName));
        serviceTemplate.setMetadata(finalMetadata);
    }

    private void convertNodeTypes(ServiceTemplate serviceTemplate, ServiceTemplateReaderService readerService) {
        Map<String, Object> nodeTypes = readerService.getNodeTypes();
        if (MapUtils.isEmpty(nodeTypes)) {
            return;
        }

        for (Map.Entry<String, Object> nodeTypeEntry : nodeTypes.entrySet()) {
            Optional<NodeType> nodeType = ToscaConverterUtil
                .createObjectFromClass(nodeTypeEntry.getKey(), nodeTypeEntry.getValue(),
                    NodeType.class);

            nodeType.ifPresent(nodeTypeValue -> DataModelUtil
                .addNodeType(serviceTemplate, nodeTypeEntry.getKey(), nodeTypeValue));
        }
    }

    private void convertTopologyTemplate(ServiceTemplate serviceTemplate,
                                         ServiceTemplateReaderService readerService) {

        convertInputs(serviceTemplate, readerService);
        convertNodeTemplates(serviceTemplate, readerService);
        convertOutputs(serviceTemplate, readerService);
        convertSubstitutionMappings(serviceTemplate, readerService);
    }

    private void convertInputs(ServiceTemplate serviceTemplate,
                               ServiceTemplateReaderService readerService) {
        Map<String, Object> inputs = readerService.getInputs();
        addInputsOrOutputsToServiceTemplate(serviceTemplate, inputs, Constants.inputs);
    }

    private void convertOutputs(ServiceTemplate serviceTemplate,
                                ServiceTemplateReaderService readerService) {
        Map<String, Object> outputs = readerService.getOutputs();
        addInputsOrOutputsToServiceTemplate(serviceTemplate, outputs, Constants.outputs);
    }

    private void addInputsOrOutputsToServiceTemplate(ServiceTemplate serviceTemplate,
                                                     Map<String, Object> mapToConvert,
                                                     String inputsOrOutputs) {
        if (MapUtils.isEmpty(mapToConvert)) {
            return;
        }

        for (Map.Entry<String, Object> entry : mapToConvert.entrySet()) {
            Optional<ParameterDefinition> parameterDefinition =
                ToscaConverterUtil.createObjectFromClass(
                    entry.getKey(), entry.getValue(), ParameterDefinition.class);

            parameterDefinition.ifPresent(parameterDefinitionValue -> {
                Optional<Object> defaultValue =
                    ToscaConverterUtil.getDefaultValue(entry.getValue(), parameterDefinition.get());
                defaultValue.ifPresent(parameterDefinitionValue::set_default);
                addToServiceTemplateAccordingToSection(
                    serviceTemplate, inputsOrOutputs, entry.getKey(), parameterDefinition.get());
            } );
        }
    }

    private void addToServiceTemplateAccordingToSection(ServiceTemplate serviceTemplate,
                                                        String inputsOrOutputs,
                                                        String parameterId,
                                                        ParameterDefinition parameterDefinition) {
        if (inputsOrOutputs.equals(inputs)) {
            DataModelUtil
                .addInputParameterToTopologyTemplate(serviceTemplate, parameterId, parameterDefinition);
        } else if (inputsOrOutputs.equals(outputs)) {
            DataModelUtil
                .addOutputParameterToTopologyTemplate(serviceTemplate, parameterId, parameterDefinition);
        }
    }

    private void convertNodeTemplates(ServiceTemplate serviceTemplate,
                                      ServiceTemplateReaderService readerService) {
        Map<String, Object> nodeTemplates = readerService.getNodeTemplates();
        if (MapUtils.isEmpty(nodeTemplates)) {
            return;
        }

        for (Map.Entry<String, Object> nodeTemplateEntry : nodeTemplates.entrySet()) {
            NodeTemplate nodeTemplate = convertNodeTemplate(nodeTemplateEntry.getValue());
            DataModelUtil.addNodeTemplate(serviceTemplate, nodeTemplateEntry.getKey(), nodeTemplate);
        }
    }

    private void convertSubstitutionMappings(ServiceTemplate serviceTemplate,
                                             ServiceTemplateReaderService readerService) {
        Map<String, Object> substitutionMappings = readerService.getSubstitutionMappings();
        if (MapUtils.isEmpty(substitutionMappings)) {
            return;
        }
        SubstitutionMapping substitutionMapping = convertSubstitutionMappings(substitutionMappings);
        DataModelUtil.addSubstitutionMapping(serviceTemplate, substitutionMapping);
    }

    private SubstitutionMapping convertSubstitutionMappings(Map<String, Object> substitutionMappings) {
        SubstitutionMapping substitutionMapping = new SubstitutionMapping();

        substitutionMapping.setNode_type((String) substitutionMappings.get(nodeType));
        substitutionMapping.setCapabilities(
            convertSubstitutionMappingsSections(capabilities, substitutionMappings.get(capabilities)));
        substitutionMapping.setRequirements(
            convertSubstitutionMappingsSections(requirements, substitutionMappings.get(requirements)));

        return substitutionMapping;
    }

    private Map<String, List<String>> convertSubstitutionMappingsSections(String sectionName,
                                                                          Object sectionToConvert) {

        if(Objects.isNull(sectionToConvert)){
            return null;
        }

        if(!(sectionToConvert instanceof Map)) {
            throw new CoreException(
                new SubstitutionMappingsConverterErrorBuilder(
                    sectionName, sectionToConvert.getClass().getSimpleName()).build());
        }

        return convertSection(sectionToConvert);
    }

    private Map<String, List<String>> convertSection(Object sectionToConvert) {

        Map<String, Object> sectionAsMap = (Map<String, Object>)sectionToConvert;
        Map<String, List<String>> convertedSection = new HashMap<>();

        if (MapUtils.isEmpty(sectionAsMap)) {
            return null;
        }

        for (Map.Entry<String, Object> entry : sectionAsMap.entrySet()) {
            if (entry.getValue() instanceof List) {
                convertedSection.put(entry.getKey(), (List<String>) entry.getValue());
            }
        }

        return convertedSection;
    }

    private CsarFileTypes getFileType(String fileName) {
        if (isMainServiceTemplate(fileName)) {
            return CsarFileTypes.mainServiceTemplate;
        } else if (isGlobalServiceTemplate(fileName)) {
            return CsarFileTypes.globalServiceTemplate;
        } else if (isDefinitions(fileName)) {
            return CsarFileTypes.definitionsFile;
        } else if (isMetadataFile(fileName)) {
            return CsarFileTypes.toscaMetadata;
        }
        return CsarFileTypes.externalFile;
    }

    private Optional<Manifest> getCsarManifest(Map<String, byte[]> csarFiles) throws IOException {
        Optional<byte[]> manifestContent = getManifestContent(csarFiles);

        if (manifestContent.isPresent()) {
            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(manifestContent.get());

            return Optional.of(new Manifest(byteInputStream));
        }

        return Optional.empty();
    }

    private NodeTemplate convertNodeTemplate(Object candidateNodeTemplate) {
        NodeTemplate nodeTemplate = new NodeTemplate();

        Map<String, Object> nodeTemplateAsMap = (Map<String, Object>) candidateNodeTemplate;
        nodeTemplate.setArtifacts((Map<String, ArtifactDefinition>) nodeTemplateAsMap.get("artifacts"));
        nodeTemplate.setAttributes((Map<String, Object>) nodeTemplateAsMap.get("attributes"));
        nodeTemplate.setCopy((String) nodeTemplateAsMap.get("copy"));
        nodeTemplate.setDescription((String) nodeTemplateAsMap.get("description"));
        nodeTemplate.setDirectives((List<String>) nodeTemplateAsMap.get("directives"));
        nodeTemplate.setInterfaces(
            (Map<String, InterfaceDefinition>) nodeTemplateAsMap.get("interfaces"));
        nodeTemplate.setNode_filter((NodeFilter) nodeTemplateAsMap.get("node_filter"));
        nodeTemplate.setProperties((Map<String, Object>) nodeTemplateAsMap.get("properties"));
        nodeTemplate.setRequirements(
            (List<Map<String, RequirementAssignment>>) nodeTemplateAsMap.get("requirements"));
        nodeTemplate.setType((String) nodeTemplateAsMap.get("type"));
        nodeTemplate.setCapabilities(
            convertCapabilities((Map<String, Object>) nodeTemplateAsMap.get("capabilities")));

        return nodeTemplate;
    }

    private List<Map<String, CapabilityAssignment>> convertCapabilities(Map<String, Object> capabilities) {
        List<Map<String, CapabilityAssignment>> convertedCapabilities = new ArrayList<>();
        if (MapUtils.isEmpty(capabilities)) {
            return Collections.emptyList();
        }
        for (Map.Entry<String, Object> capabilityAssignmentEntry : capabilities.entrySet()) {
            Map<String, CapabilityAssignment> tempMap = new HashMap<>();
            Optional<CapabilityAssignment> capabilityAssignment = ToscaConverterUtil.createObjectFromClass
                (capabilityAssignmentEntry.getKey(), capabilityAssignmentEntry.getValue(),
                    CapabilityAssignment.class);

            capabilityAssignment.ifPresent(capabilityAssignmentValue -> {
                tempMap.put(capabilityAssignmentEntry.getKey(), capabilityAssignmentValue);
                convertedCapabilities.add(tempMap);
                }
            );

        }
        return convertedCapabilities;
    }


    private boolean isMainServiceTemplate(String fileName) {
        return fileName.endsWith(mainStName);
    }

    private boolean isMetadataFile(String fileName) {
        return fileName.equals(metadataFile);
    }

    private boolean isGlobalServiceTemplate(String fileName) {
        return fileName.endsWith(globalStName);
    }

    private boolean isDefinitions(String fileName) {
        return fileName.startsWith(definitionsDir);
    }

    private String getTemplateNameFromStName(String serviceTemplateName) {
        String fileNameWithoutDirectories;
        fileNameWithoutDirectories = getFileNameWithoutDirectories(serviceTemplateName);
        return fileNameWithoutDirectories.split("ServiceTemplate")[0];
    }

    private String getFileNameWithoutDirectories(String serviceTemplateName) {
        String fileNameWithoutDirectories;
        if (serviceTemplateName.contains("/")) {
            String[] split = serviceTemplateName.split("/");
            fileNameWithoutDirectories = split[split.length - 1];
        } else if (serviceTemplateName.contains(File.separator)) {
            String[] split = serviceTemplateName.split(Pattern.quote(File.separator));
            fileNameWithoutDirectories = split[split.length - 1];
        } else {
            fileNameWithoutDirectories = serviceTemplateName;
        }
        return fileNameWithoutDirectories;
    }
}
