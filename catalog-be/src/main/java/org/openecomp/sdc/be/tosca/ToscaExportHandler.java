/*-
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
* Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.be.tosca;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections.MapUtils.isNotEmpty;
import static org.openecomp.sdc.be.components.utils.PropertiesUtils.resolvePropertyValueFromInput;
import static org.openecomp.sdc.be.tosca.InterfacesOperationsConverter.addInterfaceTypeElement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import fj.data.Either;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.onap.sdc.tosca.services.YamlUtil;
import org.openecomp.sdc.be.components.impl.exceptions.SdcResourceNotFoundException;
import org.openecomp.sdc.be.config.ConfigurationManager;
import org.openecomp.sdc.be.dao.janusgraph.JanusGraphOperationStatus;
import org.openecomp.sdc.be.datatypes.components.ResourceMetadataDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.CINodeFilterDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.InterfaceDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ListDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.PropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterCapabilityDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.RequirementNodeFilterPropertyDataDefinition;
import org.openecomp.sdc.be.datatypes.elements.ToscaArtifactDataDefinition;
import org.openecomp.sdc.be.datatypes.enums.ComponentTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.OriginTypeEnum;
import org.openecomp.sdc.be.datatypes.enums.ResourceTypeEnum;
import org.openecomp.sdc.be.model.ArtifactDefinition;
import org.openecomp.sdc.be.model.CapabilityDefinition;
import org.openecomp.sdc.be.model.Component;
import org.openecomp.sdc.be.model.ComponentInstance;
import org.openecomp.sdc.be.model.ComponentInstanceInput;
import org.openecomp.sdc.be.model.ComponentInstanceInterface;
import org.openecomp.sdc.be.model.ComponentInstanceProperty;
import org.openecomp.sdc.be.model.ComponentParametersView;
import org.openecomp.sdc.be.model.DataTypeDefinition;
import org.openecomp.sdc.be.model.GroupInstance;
import org.openecomp.sdc.be.model.InputDefinition;
import org.openecomp.sdc.be.model.InterfaceDefinition;
import org.openecomp.sdc.be.model.PropertyDefinition;
import org.openecomp.sdc.be.model.RelationshipInfo;
import org.openecomp.sdc.be.model.RequirementCapabilityRelDef;
import org.openecomp.sdc.be.model.RequirementDefinition;
import org.openecomp.sdc.be.model.Resource;
import org.openecomp.sdc.be.model.Service;
import org.openecomp.sdc.be.model.cache.ApplicationDataTypeCache;
import org.openecomp.sdc.be.model.category.CategoryDefinition;
import org.openecomp.sdc.be.model.jsonjanusgraph.operations.ToscaOperationFacade;
import org.openecomp.sdc.be.model.jsonjanusgraph.utils.ModelConverter;
import org.openecomp.sdc.be.model.operations.api.StorageOperationStatus;
import org.openecomp.sdc.be.model.operations.impl.InterfaceLifecycleOperation;
import org.openecomp.sdc.be.model.tosca.converters.ToscaValueBaseConverter;
import org.openecomp.sdc.be.tosca.model.CapabilityFilter;
import org.openecomp.sdc.be.tosca.model.NodeFilter;
import org.openecomp.sdc.be.tosca.model.SubstitutionMapping;
import org.openecomp.sdc.be.tosca.model.ToscaCapability;
import org.openecomp.sdc.be.tosca.model.ToscaDataType;
import org.openecomp.sdc.be.tosca.model.ToscaGroupTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaMetadata;
import org.openecomp.sdc.be.tosca.model.ToscaNodeTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaNodeType;
import org.openecomp.sdc.be.tosca.model.ToscaPolicyTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaProperty;
import org.openecomp.sdc.be.tosca.model.ToscaRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTemplate;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateArtifact;
import org.openecomp.sdc.be.tosca.model.ToscaTemplateRequirement;
import org.openecomp.sdc.be.tosca.model.ToscaTopolgyTemplate;
import org.openecomp.sdc.be.tosca.utils.ForwardingPathToscaUtil;
import org.openecomp.sdc.be.tosca.utils.InputConverter;
import org.openecomp.sdc.common.log.wrappers.Logger;
import org.openecomp.sdc.externalupload.utils.ServiceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

@org.springframework.stereotype.Component("tosca-export-handler")
public class ToscaExportHandler {

    private ApplicationDataTypeCache dataTypeCache;
    private ToscaOperationFacade toscaOperationFacade;
    private CapabilityRequirementConverter capabilityRequirementConverter;
    private PolicyExportParser policyExportParser;
    private GroupExportParser groupExportParser;
    private PropertyConvertor propertyConvertor;
    private InputConverter inputConverter;
    private InterfaceLifecycleOperation interfaceLifecycleOperation;
    private InterfacesOperationsConverter interfacesOperationsConverter;

    @Autowired
    public ToscaExportHandler(ApplicationDataTypeCache dataTypeCache, ToscaOperationFacade toscaOperationFacade,
            CapabilityRequirementConverter capabilityRequirementConverter, PolicyExportParser policyExportParser,
            GroupExportParser groupExportParser, PropertyConvertor propertyConvertor, InputConverter inputConverter,
            InterfaceLifecycleOperation interfaceLifecycleOperation,
            InterfacesOperationsConverter interfacesOperationsConverter) {
            this.dataTypeCache = dataTypeCache;
            this.toscaOperationFacade = toscaOperationFacade;
            this.capabilityRequirementConverter = capabilityRequirementConverter;
            this.policyExportParser = policyExportParser;
            this.groupExportParser = groupExportParser;
            this.propertyConvertor = propertyConvertor;
            this.inputConverter =  inputConverter;
            this.interfaceLifecycleOperation = interfaceLifecycleOperation;
            this.interfacesOperationsConverter = interfacesOperationsConverter;
      }


    private static final Logger log = Logger.getLogger(ToscaExportHandler.class);

    private static final String TOSCA_VERSION = "tosca_simple_yaml_1_1";
    private static final String SERVICE_NODE_TYPE_PREFIX = "org.openecomp.service.";
    private static final String IMPORTS_FILE_KEY = "file";
    private static final String TOSCA_INTERFACE_NAME = "-interface.yml";
    public static final String ASSET_TOSCA_TEMPLATE = "assettoscatemplate";
    private static final String FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION = "convertToToscaTemplate - failed to get Default Imports section from configuration";
    private static final String NOT_SUPPORTED_COMPONENT_TYPE = "Not supported component type {}";
    private static final List<Map<String, Map<String, String>>> DEFAULT_IMPORTS = ConfigurationManager
        .getConfigurationManager().getConfiguration().getDefaultImports();
    private static final String NATIVE_ROOT = "tosca.nodes.Root";
    private static YamlUtil yamlUtil = new YamlUtil();

    public ToscaExportHandler(){}

    public Either<ToscaRepresentation, ToscaError> exportComponent(Component component) {

        Either<ToscaTemplate, ToscaError> toscaTemplateRes = convertToToscaTemplate(component);
        if (toscaTemplateRes.isRight()) {
            return Either.right(toscaTemplateRes.right().value());
        }

        ToscaTemplate toscaTemplate = toscaTemplateRes.left().value();
        ToscaRepresentation toscaRepresentation = this.createToscaRepresentation(toscaTemplate);
        return Either.left(toscaRepresentation);
    }

    public Either<ToscaRepresentation, ToscaError> exportComponentInterface(final Component component,
                                                                            final boolean isAssociatedComponent) {
        if (null == DEFAULT_IMPORTS) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }

        String toscaVersion = null;
        if (component instanceof Resource) {
            toscaVersion = ((Resource) component).getToscaVersion();
        }
        ToscaTemplate toscaTemplate = new ToscaTemplate(toscaVersion != null ? toscaVersion : TOSCA_VERSION);
        toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
        final Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        final Either<ToscaTemplate, ToscaError> toscaTemplateRes =
            convertInterfaceNodeType(new HashMap<>(), component, toscaTemplate, nodeTypes, isAssociatedComponent);
        if (toscaTemplateRes.isRight()) {
            return Either.right(toscaTemplateRes.right().value());
        }

        toscaTemplate = toscaTemplateRes.left().value();
        ToscaRepresentation toscaRepresentation = this.createToscaRepresentation(toscaTemplate);
        return Either.left(toscaRepresentation);
    }

    public ToscaRepresentation createToscaRepresentation(ToscaTemplate toscaTemplate) {
        CustomRepresenter representer = new CustomRepresenter();
        DumperOptions options = new DumperOptions();
        options.setAllowReadOnlyProperties(false);
        options.setPrettyFlow(true);

        options.setDefaultFlowStyle(FlowStyle.FLOW);
        options.setCanonical(false);

        representer.addClassTag(toscaTemplate.getClass(), Tag.MAP);

        representer.setPropertyUtils(new UnsortedPropertyUtils());
        Yaml yaml = new Yaml(representer, options);

        String yamlAsString = yaml.dumpAsMap(toscaTemplate);

        StringBuilder sb = new StringBuilder();
        sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactHeader());
        sb.append(yamlAsString);
        sb.append(ConfigurationManager.getConfigurationManager().getConfiguration().getHeatEnvArtifactFooter());

        ToscaRepresentation toscaRepresentation = new ToscaRepresentation();
        toscaRepresentation.setMainYaml(sb.toString());
        toscaRepresentation.setDependencies(toscaTemplate.getDependencies());

        return toscaRepresentation;
    }

    public Either<ToscaTemplate, ToscaError> getDependencies(Component component) {
        ToscaTemplate toscaTemplate = new ToscaTemplate(null);
        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports = fillImports(component,
                toscaTemplate);
        if (fillImports.isRight()) {
            return Either.right(fillImports.right().value());
        }
        return Either.left(fillImports.left().value().left);
    }

    private Either<ToscaTemplate, ToscaError> convertToToscaTemplate(final Component component) {
        if (null == DEFAULT_IMPORTS) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        log.trace("start tosca export for {}", component.getUniqueId());
        String toscaVersion = null;
        if (component instanceof Resource) {
            toscaVersion = ((Resource) component).getToscaVersion();
        }
        final ToscaTemplate toscaTemplate = new ToscaTemplate(toscaVersion != null ? toscaVersion : TOSCA_VERSION);
        toscaTemplate.setMetadata(convertMetadata(component));
        toscaTemplate.setImports(new ArrayList<>(DEFAULT_IMPORTS));
        final Map<String, ToscaNodeType> nodeTypes = new HashMap<>();
        if (ModelConverter.isAtomicComponent(component)) {
            log.trace("convert component as node type");
            return convertNodeType(new HashMap<>(), component, toscaTemplate, nodeTypes);
        } else {
            log.trace("convert component as topology template");
            return convertToscaTemplate(component, toscaTemplate);
        }

    }

    private Either<ToscaTemplate, ToscaError> convertToscaTemplate(Component component, ToscaTemplate toscaNode) {

        Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> importsRes = fillImports(component,
                toscaNode);
        if (importsRes.isRight()) {
            return Either.right(importsRes.right().value());
        }
        toscaNode = importsRes.left().value().left;
        Map<String, Component> componentCache = importsRes.left().value().right;
        Either<Map<String, ToscaNodeType>, ToscaError> nodeTypesMapEither = createProxyNodeTypes(componentCache,
                component);
        if (nodeTypesMapEither.isRight()) {
            log.debug("Failed to fetch normative service proxy resource by tosca name, error {}",
                    nodeTypesMapEither.right().value());
            return Either.right(nodeTypesMapEither.right().value());
        }
        Map<String, ToscaNodeType> nodeTypesMap = nodeTypesMapEither.left().value();
        if (nodeTypesMap != null && !nodeTypesMap.isEmpty()) {
            toscaNode.setNode_types(nodeTypesMap);
        }

        Either<Map<String, Object>, ToscaError> proxyInterfaceTypesEither = createProxyInterfaceTypes(component);
        if (proxyInterfaceTypesEither.isRight()) {
            log.debug("Failed to populate service proxy local interface types in tosca, error {}",
                    nodeTypesMapEither.right().value());
            return Either.right(proxyInterfaceTypesEither.right().value());
        }
        Map<String, Object> proxyInterfaceTypes = proxyInterfaceTypesEither.left().value();
        if (MapUtils.isNotEmpty(proxyInterfaceTypes)) {
            toscaNode.setInterface_types(proxyInterfaceTypes);
        }

        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
        if (dataTypesEither.isRight()) {
            log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        ToscaTopolgyTemplate topologyTemplate = new ToscaTopolgyTemplate();
        List<InputDefinition> inputDef = component.getInputs();
        Map<String, ToscaProperty> inputs = inputConverter.convertInputs(inputDef, dataTypes);

        if (!inputs.isEmpty()) {
            topologyTemplate.setInputs(inputs);
        }

        List<ComponentInstance> componentInstances = component.getComponentInstances();
        Map<String, List<ComponentInstanceProperty>> componentInstancesProperties =
                component.getComponentInstancesProperties();
        Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces =
            component.getComponentInstancesInterfaces();
        if (componentInstances != null && !componentInstances.isEmpty()) {

            Either<Map<String, ToscaNodeTemplate>, ToscaError> nodeTemplates =
                    convertNodeTemplates(component, componentInstances,
                        componentInstancesProperties, componentInstanceInterfaces,
                        componentCache, dataTypes, topologyTemplate);
            if (nodeTemplates.isRight()) {
                return Either.right(nodeTemplates.right().value());
            }
            log.debug("node templates converted");

            topologyTemplate.setNode_templates(nodeTemplates.left().value());
        }


        addGroupsToTopologyTemplate(component, topologyTemplate);

        try {
            addPoliciesToTopologyTemplate(component, topologyTemplate);
        } catch (SdcResourceNotFoundException e) {
            log.debug("Fail to add policies to topology template:",e);
            return Either.right(ToscaError.GENERAL_ERROR);
        }


        SubstitutionMapping substitutionMapping = new SubstitutionMapping();
        String toscaResourceName;
        switch (component.getComponentType()) {
        case RESOURCE:
            toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition()
                    .getMetadataDataDefinition()).getToscaResourceName();
            break;
        case SERVICE:
            toscaResourceName = SERVICE_NODE_TYPE_PREFIX
                    + component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
            break;
        default:
            log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
            return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
        }
        substitutionMapping.setNode_type(toscaResourceName);

        Either<SubstitutionMapping, ToscaError> capabilities = convertCapabilities(component, substitutionMapping, componentCache);
        if (capabilities.isRight()) {
            return Either.right(capabilities.right().value());
        }
        substitutionMapping = capabilities.left().value();

        Either<SubstitutionMapping, ToscaError> requirements = capabilityRequirementConverter
                .convertSubstitutionMappingRequirements(componentCache, component, substitutionMapping);
        if (requirements.isRight()) {
            return Either.right(requirements.right().value());
        }
        substitutionMapping = requirements.left().value();

        topologyTemplate.setSubstitution_mappings(substitutionMapping);

        toscaNode.setTopology_template(topologyTemplate);

        return Either.left(toscaNode);
    }

  private void addGroupsToTopologyTemplate(Component component, ToscaTopolgyTemplate topologyTemplate) {
        Map<String, ToscaGroupTemplate> groups = groupExportParser.getGroups(component);
        if(groups!= null) {
            topologyTemplate.addGroups(groups);
        }
    }

      private void addPoliciesToTopologyTemplate(Component component, ToscaTopolgyTemplate topologyTemplate)
                  throws SdcResourceNotFoundException {
            Map<String, ToscaPolicyTemplate> policies = policyExportParser.getPolicies(component);
            if(policies!= null) {
                  topologyTemplate.addPolicies(policies);
            }
      }

    private ToscaMetadata convertMetadata(Component component) {
        return convertMetadata(component, false, null);
    }

    private ToscaMetadata convertMetadata(Component component, boolean isInstance,
            ComponentInstance componentInstance) {
        ToscaMetadata toscaMetadata = new ToscaMetadata();
        toscaMetadata.setInvariantUUID(component.getInvariantUUID());
        toscaMetadata.setUUID(component.getUUID());
        toscaMetadata.setDescription(component.getDescription());
        toscaMetadata.setName(component.getComponentMetadataDefinition().getMetadataDataDefinition().getName());

        List<CategoryDefinition> categories = component.getCategories();
        CategoryDefinition categoryDefinition = categories.get(0);
        toscaMetadata.setCategory(categoryDefinition.getName());

        if (isInstance) {
            toscaMetadata.setVersion(component.getVersion());
            toscaMetadata.setCustomizationUUID(componentInstance.getCustomizationUUID());
            if (componentInstance.getSourceModelInvariant() != null
                    && !componentInstance.getSourceModelInvariant().isEmpty()) {
                toscaMetadata.setVersion(componentInstance.getComponentVersion());
                toscaMetadata.setSourceModelInvariant(componentInstance.getSourceModelInvariant());
                toscaMetadata.setSourceModelUuid(componentInstance.getSourceModelUuid());
                toscaMetadata.setSourceModelName(componentInstance.getSourceModelName());
                toscaMetadata.setName(
                        componentInstance.getSourceModelName() + " " + OriginTypeEnum.ServiceProxy.getDisplayValue());
                toscaMetadata.setDescription(componentInstance.getDescription());
            }

        }
        switch (component.getComponentType()) {
        case RESOURCE:
            Resource resource = (Resource) component;

            if (isInstance && componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
                toscaMetadata.setType(componentInstance.getOriginType().getDisplayValue());
            } else {
                toscaMetadata.setType(resource.getResourceType().name());
            }
            toscaMetadata.setSubcategory(categoryDefinition.getSubcategories().get(0).getName());
            toscaMetadata.setResourceVendor(resource.getVendorName());
            toscaMetadata.setResourceVendorRelease(resource.getVendorRelease());
            toscaMetadata.setResourceVendorModelNumber(resource.getResourceVendorModelNumber());
            break;
        case SERVICE:
            Service service = (Service) component;
            toscaMetadata.setType(component.getComponentType().getValue());
            toscaMetadata.setServiceType(service.getServiceType());
            toscaMetadata.setServiceRole(service.getServiceRole());
            toscaMetadata.setServiceFunction(service.getServiceFunction());
            toscaMetadata.setEnvironmentContext(service.getEnvironmentContext());
            resolveInstantiationTypeAndSetItToToscaMetaData(toscaMetadata, service);
            if (!isInstance) {
                // DE268546
                toscaMetadata.setServiceEcompNaming(((Service) component).isEcompGeneratedNaming());
                toscaMetadata.setEcompGeneratedNaming(((Service) component).isEcompGeneratedNaming());
                toscaMetadata.setNamingPolicy(((Service) component).getNamingPolicy());
            }
            break;
        default:
            log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
        }
        return toscaMetadata;
    }

    private void resolveInstantiationTypeAndSetItToToscaMetaData(ToscaMetadata toscaMetadata, Service service) {
        if (service.getInstantiationType() != null) {
            toscaMetadata.setInstantiationType(service.getInstantiationType());
        }
        else {
            toscaMetadata.setInstantiationType(StringUtils.EMPTY);
        }
    }

    private Either<ImmutablePair<ToscaTemplate, Map<String, Component>>, ToscaError> fillImports(Component component,
            ToscaTemplate toscaTemplate) {

        if (null == DEFAULT_IMPORTS) {
            log.debug(FAILED_TO_GET_DEFAULT_IMPORTS_CONFIGURATION);
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        Map<String, Component> componentCache = new HashMap<>();

        if (!ModelConverter.isAtomicComponent(component)) {
            List<Map<String, Map<String, String>>> additionalImports = toscaTemplate.getImports() == null
                                                                               ? new ArrayList<>(DEFAULT_IMPORTS) : new ArrayList<>(toscaTemplate.getImports());

            List<Triple<String, String, Component>> dependecies = new ArrayList<>();

            Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
            if (isNotEmpty(toscaArtifacts)) {
                ArtifactDefinition artifactDefinition = toscaArtifacts.get(ToscaExportHandler.ASSET_TOSCA_TEMPLATE);
                if(artifactDefinition != null) {
                    Map<String, Map<String, String>> importsListMember = new HashMap<>();
                    Map<String, String> interfaceFiles = new HashMap<>();
                    interfaceFiles.put(IMPORTS_FILE_KEY, getInterfaceFilename(artifactDefinition.getArtifactName()));
                    StringBuilder keyNameBuilder = new StringBuilder();
                    keyNameBuilder.append(component.getComponentType().toString().toLowerCase()).append("-")
                                  .append(component.getName()).append("-interface");
                    importsListMember.put(keyNameBuilder.toString(), interfaceFiles);
                    additionalImports.add(importsListMember);
                }
            }
            List<ComponentInstance> componentInstances = component.getComponentInstances();
            if (componentInstances != null && !componentInstances.isEmpty()) {
                componentInstances.forEach(ci -> createDependency(componentCache, additionalImports, dependecies, ci));
            }
            toscaTemplate.setDependencies(dependecies);
            toscaTemplate.setImports(additionalImports);
        } else {
            log.debug("currently imports supported for VF and service only");
        }
        return Either.left(new ImmutablePair<>(toscaTemplate, componentCache));
    }

    private void createDependency(final Map<String, Component> componentCache, 
                                  final List<Map<String, Map<String, String>>> imports,
                                  final List<Triple<String, String, Component>> dependencies,
                                  final ComponentInstance componentInstance) {
        log.debug("createDependency componentCache {}",componentCache);
        final Component componentRI = componentCache.get(componentInstance.getComponentUid());
        if (componentRI == null) {
            // all resource must be only once!
            final Either<Component, StorageOperationStatus> resource = toscaOperationFacade
                .getToscaFullElement(componentInstance.getComponentUid());
            if ((resource.isRight()) && (log.isDebugEnabled())) {
                log.debug("Failed to fetch resource with id {} for instance {}", componentInstance.getComponentUid(),
                    componentInstance.getUniqueId());
                return ;
            }
            final Component fetchedComponent = resource.left().value();
            setComponentCache(componentCache, componentInstance, fetchedComponent);
            addDependencies(imports, dependencies, fetchedComponent);
        }
    }

    /**
     * Sets a componentCache from the given component/resource.
     */
    private void setComponentCache(final Map<String, Component> componentCache,
                                   final ComponentInstance componentInstance,
                                   final Component fetchedComponent) {
        componentCache.put(fetchedComponent.getUniqueId(), fetchedComponent);
        if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy) {
            final Either<Component, StorageOperationStatus> sourceService = toscaOperationFacade
                .getToscaFullElement(componentInstance.getSourceModelUid());
            if (sourceService.isRight() && (log.isDebugEnabled())) {
                log.debug("Failed to fetch source service with id {} for proxy {}",
                    componentInstance.getSourceModelUid(), componentInstance.getUniqueId());
            }
            final Component fetchedSource = sourceService.left().value();
            componentCache.put(fetchedSource.getUniqueId(), fetchedSource);
        }
    }

    /**
     * Retrieves all derived_from nodes and stores it in a predictable order.
     */
    private void addDependencies(final List<Map<String, Map<String, String>>> imports,
                                 final List<Triple<String, String, Component>> dependencies,
                                 final Component fetchedComponent) {
        final Set<Component> componentsList = new LinkedHashSet<>();
        if (fetchedComponent instanceof Resource) {
            log.debug("fetchedComponent is a resource {}",fetchedComponent);

            final Optional<Map<String, String>> derivedFromMapOfIdToName = getDerivedFromMapOfIdToName(fetchedComponent, componentsList);
            if (derivedFromMapOfIdToName.isPresent()) {
                derivedFromMapOfIdToName.get().entrySet().forEach(entry -> {
                    log.debug("Started entry.getValue() : {}",entry.getValue());
                    if (!NATIVE_ROOT.equals(entry.getValue())) {
                        Either<Resource, StorageOperationStatus> resourcefetched = toscaOperationFacade
                            .getToscaElement(entry.getKey());
                        if (resourcefetched != null && resourcefetched.isLeft()) {
                            componentsList.add(resourcefetched.left().value());
                        }
                    }
                });
            }
            setImports(imports, dependencies, componentsList);
        }
    }

    /**
     * Returns all derived_from nodes found.
     */
    private Optional<Map<String, String>> getDerivedFromMapOfIdToName(final Component fetchedComponent,
                                                                      final Set<Component> componentsList) {
        final Resource parentResource = (Resource) fetchedComponent;
        Map<String, String> derivedFromMapOfIdToName = new HashMap<>();
        if(CollectionUtils.isNotEmpty(parentResource.getComponentInstances())) {
            componentsList.add(fetchedComponent);
            for (final ComponentInstance componentInstance : parentResource.getComponentInstances()) {
                final Either<Resource, StorageOperationStatus> resourcefetched = toscaOperationFacade
                    .getToscaElement(componentInstance.getComponentUid());
                if (resourcefetched != null && resourcefetched.isLeft()) {
                    final Map<String, String> derivedWithId = resourcefetched.left().value().getDerivedFromMapOfIdToName();
                    if (MapUtils.isNotEmpty(derivedWithId)) {
                        derivedFromMapOfIdToName.putAll(derivedWithId);
                    }
                }
            }
        } else {
            derivedFromMapOfIdToName = parentResource.getDerivedFromMapOfIdToName();
        }
        log.debug("Started derivedFromMapOfIdToName: {}", derivedFromMapOfIdToName);
        return Optional.ofNullable(derivedFromMapOfIdToName);
    }

    /**
     * Creates a resource map and adds it to the import list.
     */
    private void setImports(final List<Map<String, Map<String, String>>> imports,
                            final List<Triple<String, String, Component>> dependencies,
                            final Set<Component> componentsList) {
        componentsList.forEach(component -> {
            final Map<String, ArtifactDefinition> toscaArtifacts = component.getToscaArtifacts();
            final ArtifactDefinition artifactDefinition = toscaArtifacts.get(ASSET_TOSCA_TEMPLATE);
            if (artifactDefinition != null) {
                final Map<String, String> files = new HashMap<>();
                final String artifactName = artifactDefinition.getArtifactName();
                files.put(IMPORTS_FILE_KEY, artifactName);
                final StringBuilder keyNameBuilder = new StringBuilder();
                keyNameBuilder.append(component.getComponentType().toString().toLowerCase());
                keyNameBuilder.append("-");
                keyNameBuilder.append(component.getName());
                addImports(imports, keyNameBuilder, files);
                dependencies
                    .add(new ImmutableTriple<String, String, Component>(artifactName, artifactDefinition.getEsId(),
                        component));

                if (!ModelConverter.isAtomicComponent(component)) {
                    final Map<String, String> interfaceFiles = new HashMap<>();
                    interfaceFiles.put(IMPORTS_FILE_KEY, getInterfaceFilename(artifactName));
                    keyNameBuilder.append("-interface");
                    addImports(imports, keyNameBuilder, interfaceFiles);
                }
            }
        });
    }

    /**
     * Adds the found resource to the import definition list.
     */
    private void addImports(final List<Map<String, Map<String, String>>> imports,
                            final StringBuilder keyNameBuilder,
                            final Map<String, String> files) {
        final String mapKey = keyNameBuilder.toString();
        if (imports.stream().allMatch(stringMapMap -> stringMapMap.get(mapKey) == null)) {
            final Map<String, Map<String, String>> importsListMember = new HashMap<>();
            importsListMember.put(keyNameBuilder.toString(), files);
            imports.add(importsListMember);
        }
    }

    public static String getInterfaceFilename(String artifactName) {
        return artifactName.substring(0, artifactName.lastIndexOf('.')) + ToscaExportHandler.TOSCA_INTERFACE_NAME;
    }

    private Either<ToscaTemplate, ToscaError> convertNodeType(Map<String, Component> componentsCache, Component component, ToscaTemplate toscaNode,
                                                              Map<String, ToscaNodeType> nodeTypes) {
        return convertInterfaceNodeType(componentsCache, component, toscaNode, nodeTypes, false);
    }

    private Either<ToscaTemplate, ToscaError> convertInterfaceNodeType(Map<String, Component> componentsCache,
                                                                       Component component, ToscaTemplate toscaNode,
                                                                       Map<String, ToscaNodeType> nodeTypes,
            boolean isAssociatedComponent) {
        log.debug("start convert node type for {}", component.getUniqueId());
        ToscaNodeType toscaNodeType = createNodeType(component);

        Either<Map<String, InterfaceDefinition>, StorageOperationStatus> lifecycleTypeEither =
                interfaceLifecycleOperation.getAllInterfaceLifecycleTypes();
        if(lifecycleTypeEither.isRight()){
            log.debug("Failed to fetch all interface types :", lifecycleTypeEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }
        List<String> allGlobalInterfaceTypes = lifecycleTypeEither.left().value()
                                                       .values()
                                                       .stream()
                .map(InterfaceDataDefinition::getType)
                                                       .collect(Collectors.toList());
        toscaNode.setInterface_types(addInterfaceTypeElement(component, allGlobalInterfaceTypes));

        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
        if (dataTypesEither.isRight()) {
            log.debug("Failed to fetch all data types :", dataTypesEither.right().value());
            return Either.right(ToscaError.GENERAL_ERROR);
        }

        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();

        List<InputDefinition> inputDef = component.getInputs();
        Map<String, ToscaProperty> mergedProperties = new HashMap<>();
        interfacesOperationsConverter.addInterfaceDefinitionElement(component, toscaNodeType, dataTypes, isAssociatedComponent);
        addInputsToProperties(dataTypes, inputDef, mergedProperties);

        if(CollectionUtils.isNotEmpty(component.getProperties())) {
            List<PropertyDefinition> properties = component.getProperties();
            Map<String, ToscaProperty> convertedProperties = properties.stream()
                    .map(propertyDefinition -> resolvePropertyValueFromInput(propertyDefinition, component.getInputs()))
                    .collect(Collectors.toMap(PropertyDataDefinition::getName,
                            property -> propertyConvertor.convertProperty(dataTypes, property,
                                    PropertyConvertor.PropertyType.PROPERTY)));
            // merge component properties and inputs properties
            mergedProperties.putAll(convertedProperties);
        }
        if (MapUtils.isNotEmpty(mergedProperties)) {
            toscaNodeType.setProperties(mergedProperties);
        }

        /* convert private data_types */
        List<DataTypeDefinition> privateDataTypes = component.getDataTypes();
        if (CollectionUtils.isNotEmpty(privateDataTypes) ) {
            Map<String, ToscaDataType> toscaDataTypeMap = new HashMap<>();
            for (DataTypeDefinition dataType: privateDataTypes) {
                log.debug("Emitting private data type: component.name={} dataType.name={}",
                        component.getNormalizedName(), dataType.getName());
                ToscaDataType toscaDataType = new ToscaDataType();
                toscaDataType.setDerived_from(dataType.getDerivedFromName());
                toscaDataType.setDescription(dataType.getDescription());
                toscaDataType.setVersion(dataType.getVersion());
                if (CollectionUtils.isNotEmpty(dataType.getProperties())) {
                    toscaDataType.setProperties(dataType.getProperties().stream()
                            .collect(Collectors.toMap(
                                    s -> s.getName(),
                                    s -> propertyConvertor.convertProperty(dataTypes, s, PropertyConvertor.PropertyType.PROPERTY)
                            )));
                }
                toscaDataTypeMap.put(dataType.getName(), toscaDataType);
            }
            toscaNode.setData_types(toscaDataTypeMap);
        }

        // Extracted to method for code reuse
        return convertReqCapAndTypeName(componentsCache, component, toscaNode, nodeTypes, toscaNodeType, dataTypes);
    }

    private Either<ToscaTemplate, ToscaError> convertReqCapAndTypeName(Map<String, Component> componentsCache, Component component, ToscaTemplate toscaNode,
            Map<String, ToscaNodeType> nodeTypes, ToscaNodeType toscaNodeType,
            Map<String, DataTypeDefinition> dataTypes) {
        Either<ToscaNodeType, ToscaError> capabilities = convertCapabilities(componentsCache, component, toscaNodeType, dataTypes);
        if (capabilities.isRight()) {
            return Either.right(capabilities.right().value());
        }
        toscaNodeType = capabilities.left().value();
        log.debug("Capabilities converted for {}", component.getUniqueId());

        Either<ToscaNodeType, ToscaError> requirements = capabilityRequirementConverter.convertRequirements(componentsCache, component,
                toscaNodeType);
        if (requirements.isRight()) {
            return Either.right(requirements.right().value());
        }
        toscaNodeType = requirements.left().value();
        log.debug("Requirements converted for {}", component.getUniqueId());

        String toscaResourceName;
        switch (component.getComponentType()) {
        case RESOURCE:
            toscaResourceName = ((ResourceMetadataDataDefinition) component.getComponentMetadataDefinition()
                    .getMetadataDataDefinition()).getToscaResourceName();
            break;
        case SERVICE:
            toscaResourceName = SERVICE_NODE_TYPE_PREFIX
                    + component.getComponentMetadataDefinition().getMetadataDataDefinition().getSystemName();
            break;
        default:
            log.debug(NOT_SUPPORTED_COMPONENT_TYPE, component.getComponentType());
            return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
        }

        nodeTypes.put(toscaResourceName, toscaNodeType);
        toscaNode.setNode_types(nodeTypes);
        log.debug("finish convert node type for {}", component.getUniqueId());
        return Either.left(toscaNode);
    }

    protected Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplates(
            Component component,
            List<ComponentInstance> componentInstances,
            Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
            Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces,
            Map<String, Component> componentCache, Map<String, DataTypeDefinition> dataTypes,
            ToscaTopolgyTemplate topologyTemplate) {

        Either<Map<String, ToscaNodeTemplate>, ToscaError> convertNodeTemplatesRes = null;
        log.debug("start convert topology template for {} for type {}", component.getUniqueId(),
                component.getComponentType());
        Map<String, ToscaNodeTemplate> nodeTemplates = new HashMap<>();
        Map<String, List<ComponentInstanceInput>> componentInstancesInputs = component.getComponentInstancesInputs();

        Map<String, ToscaGroupTemplate> groupsMap = null;
        for (ComponentInstance componentInstance : componentInstances) {
            ToscaNodeTemplate nodeTemplate = new ToscaNodeTemplate();
            if (MapUtils.isNotEmpty(componentInstance.getToscaArtifacts())) {
                nodeTemplate.setArtifacts(convertToNodeTemplateArtifacts(componentInstance.getToscaArtifacts()));
            }
            nodeTemplate.setType(componentInstance.getToscaComponentName());
            nodeTemplate.setDirectives(componentInstance.getDirectives());
            nodeTemplate.setNode_filter(convertToNodeTemplateNodeFilterComponent(componentInstance.getNodeFilter()));

            Either<Component, Boolean> originComponentRes = capabilityRequirementConverter
                    .getOriginComponent(componentCache, componentInstance);
            if (originComponentRes.isRight()) {
                convertNodeTemplatesRes = Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
                break;
            }
            Either<ToscaNodeTemplate, ToscaError> requirements = convertComponentInstanceRequirements(component,
                    componentInstance, component.getComponentInstancesRelations(), nodeTemplate,
                    originComponentRes.left().value(), componentCache);
            if (requirements.isRight()) {
                convertNodeTemplatesRes = Either.right(requirements.right().value());
                break;
            }
            String instanceUniqueId = componentInstance.getUniqueId();
            log.debug("Component instance Requirements converted for instance {}", instanceUniqueId);

            nodeTemplate = requirements.left().value();

            Component originalComponent = componentCache.get(componentInstance.getActualComponentUid());

            if (componentInstance.getOriginType() == OriginTypeEnum.ServiceProxy){
                Component componentOfProxy = componentCache.get(componentInstance.getComponentUid());
                nodeTemplate.setMetadata(convertMetadata(componentOfProxy, true, componentInstance));
            } else {
                nodeTemplate.setMetadata(convertMetadata(originalComponent, true, componentInstance));
            }

            Either<ToscaNodeTemplate, ToscaError> capabilities = capabilityRequirementConverter
                    .convertComponentInstanceCapabilities(componentInstance, dataTypes, nodeTemplate);
            if (capabilities.isRight()) {
                convertNodeTemplatesRes = Either.right(capabilities.right().value());
                break;
            }
            log.debug("Component instance Capabilities converted for instance {}", instanceUniqueId);

            nodeTemplate = capabilities.left().value();
            Map<String, Object> props = new HashMap<>();

            if (originalComponent.getComponentType() == ComponentTypeEnum.RESOURCE) {
                // Adds the properties of parent component to map
                addPropertiesOfParentComponent(dataTypes, originalComponent, props);
            }

            if (null != componentInstancesProperties && componentInstancesProperties.containsKey(instanceUniqueId)) {
                addPropertiesOfComponentInstance(componentInstancesProperties, dataTypes, instanceUniqueId,
                        props);
            }

            if (componentInstancesInputs != null && componentInstancesInputs.containsKey(instanceUniqueId)
                    && !isComponentOfTypeServiceProxy(componentInstance)) {
                //For service proxy the inputs are already handled under instance properties above
                addComponentInstanceInputs(dataTypes, componentInstancesInputs, instanceUniqueId,
                        props);
            }
            //M3[00001] - NODE TEMPLATE INTERFACES  - START
            handleInstanceInterfaces(componentInstanceInterfaces, componentInstance, dataTypes, nodeTemplate,
                    instanceUniqueId, component);
            //M3[00001] - NODE TEMPLATE INTERFACES  - END
            if (props != null && !props.isEmpty()) {
                nodeTemplate.setProperties(props);
            }

            List<GroupInstance> groupInstances = componentInstance.getGroupInstances();
            if (groupInstances != null) {
                if (groupsMap == null) {
                    groupsMap = new HashMap<>();
                }
                for (GroupInstance groupInst : groupInstances) {
                    boolean addToTosca = true;

                    List<String> artifacts = groupInst.getArtifacts();
                    if (artifacts == null || artifacts.isEmpty()) {
                        addToTosca = false;
                    }

                    if (addToTosca) {
                        ToscaGroupTemplate toscaGroup = groupExportParser.getToscaGroupTemplate(groupInst, componentInstance.getInvariantName());
                        groupsMap.put(groupInst.getName(), toscaGroup);
                    }
                }
            }

            nodeTemplates.put(componentInstance.getName(), nodeTemplate);
        }
        if (groupsMap != null) {
            log.debug("instance groups added");
            topologyTemplate.addGroups(groupsMap);
        }
        if (component.getComponentType() == ComponentTypeEnum.SERVICE && isNotEmpty(((Service) component).getForwardingPaths())) {
            log.debug("Starting converting paths for component {}, name {}", component.getUniqueId(),
                    component.getName());
            ForwardingPathToscaUtil.addForwardingPaths((Service) component, nodeTemplates, capabilityRequirementConverter, componentCache, toscaOperationFacade);
            log.debug("Finished converting paths for component {}, name {}", component.getUniqueId(),
                    component.getName());
        }
        if (convertNodeTemplatesRes == null) {
            convertNodeTemplatesRes = Either.left(nodeTemplates);
        }
        log.debug("finish convert topology template for {} for type {}", component.getUniqueId(),
                component.getComponentType());
        return convertNodeTemplatesRes;
    }

    private void handleInstanceInterfaces(
            Map<String, List<ComponentInstanceInterface>> componentInstanceInterfaces,
            ComponentInstance componentInstance, Map<String, DataTypeDefinition> dataTypes, ToscaNodeTemplate nodeTemplate,
            String instanceUniqueId,
            Component parentComponent) {

        Map<String, Object> interfaces;

        // we need to handle service proxy interfaces
        if(isComponentOfTypeServiceProxy(componentInstance)) {
            if(MapUtils.isEmpty(componentInstanceInterfaces)
                || !componentInstanceInterfaces.containsKey(instanceUniqueId)) {
                interfaces = null;
            } else {
                List<ComponentInstanceInterface> currServiceInterfaces =
                    componentInstanceInterfaces.get(instanceUniqueId);

                Map<String, InterfaceDefinition> tmpInterfaces = new HashMap<>();
                currServiceInterfaces.forEach(instInterface -> tmpInterfaces.put(instInterface
                    .getUniqueId(), instInterface));

                interfaces = interfacesOperationsConverter
                                     .getInterfacesMap(parentComponent, componentInstance, tmpInterfaces, dataTypes, true, true);
            }
        } else {
            interfaces =
                getComponentInstanceInterfaceInstances(componentInstanceInterfaces,
                    componentInstance, instanceUniqueId);
        }
        nodeTemplate.setInterfaces(interfaces);
    }

    private boolean isComponentOfTypeServiceProxy(ComponentInstance componentInstance) {
        return Objects.nonNull(componentInstance.getOriginType())
            && componentInstance.getOriginType().getValue().equals("Service Proxy");
    }

    //M3[00001] - NODE TEMPLATE INTERFACES  - START
    private Map<String, Object> getComponentInstanceInterfaceInstances(Map<String, List<ComponentInstanceInterface>> componentInstancesInterfaces,
                                                                        ComponentInstance componentInstance,
                                                                       String instanceUniqueId) {
        if(MapUtils.isEmpty(componentInstancesInterfaces)) {
            return null;
        }

        List<ComponentInstanceInterface> componentInstanceInterfaces =
            componentInstancesInterfaces.get(instanceUniqueId);

        if(CollectionUtils.isEmpty(componentInstanceInterfaces)) {
          return null;
        }

        Map<String, Object> interfaces = new HashMap<>();
        for(ComponentInstanceInterface componentInstanceInterface : componentInstanceInterfaces) {
            interfaces.put(componentInstanceInterface.getInterfaceId(),
                removeOperationsKeyFromInterface(componentInstanceInterface.getInterfaceInstanceDataDefinition()));
        }

        componentInstance.setInterfaces(interfaces);

        return interfaces;
    }

    private void addComponentInstanceInputs(Map<String, DataTypeDefinition> dataTypes,
                                            Map<String, List<ComponentInstanceInput>> componentInstancesInputs,
                                            String instanceUniqueId, Map<String, Object> props) {

        List<ComponentInstanceInput> instanceInputsList = componentInstancesInputs.get(instanceUniqueId);
        if (instanceInputsList != null) {
            instanceInputsList.forEach(input -> {

                Supplier<String> supplier = () -> input.getValue() != null && !Objects.isNull(input.getValue())
                        ? input.getValue() : input.getDefaultValue();
                        propertyConvertor.convertAndAddValue(dataTypes, props, input, supplier);
            });
        }
    }

    private void addPropertiesOfComponentInstance(
            Map<String, List<ComponentInstanceProperty>> componentInstancesProperties,
            Map<String, DataTypeDefinition> dataTypes, String instanceUniqueId,
            Map<String, Object> props) {

        if (isNotEmpty(componentInstancesProperties)) {
            componentInstancesProperties.get(instanceUniqueId)
                    // Converts and adds each value to property map
                    .forEach(prop -> propertyConvertor.convertAndAddValue(dataTypes, props, prop,
                            prop::getValue));
        }
    }

    private void addPropertiesOfParentComponent(Map<String, DataTypeDefinition> dataTypes,
                                                Component componentOfInstance, Map<String, Object> props) {

        List<PropertyDefinition> componentProperties = componentOfInstance.getProperties();
        if (isNotEmpty(componentProperties)) {
            componentProperties.stream()
                    // Filters out properties with empty default values
                               .filter(prop -> StringUtils.isNotEmpty(prop.getDefaultValue()))
                    // Converts and adds each value to property map
                    .forEach(prop -> propertyConvertor.convertAndAddValue(dataTypes, props, prop,
                            prop::getDefaultValue));
        }
    }

    /**
     * @param dataTypes
     * @param componentInstance
     * @param props
     * @param prop
     * @param supplier
     */
    private void convertAndAddValue(Map<String, DataTypeDefinition> dataTypes, ComponentInstance componentInstance,
            Map<String, Object> props, PropertyDefinition prop, Supplier<String> supplier) {
        Object convertedValue = convertValue(dataTypes, componentInstance, prop, supplier);
        if (!ToscaValueBaseConverter.isEmptyObjectValue(convertedValue)) {
            props.put(prop.getName(), convertedValue);
        }
    }

    private <T extends PropertyDefinition> Object convertValue(Map<String, DataTypeDefinition> dataTypes,
            ComponentInstance componentInstance, T input, Supplier<String> supplier) {
        log.debug("Convert property or input value {} for instance {}", input.getName(),
                componentInstance.getUniqueId());
        String propertyType = input.getType();
        String innerType = null;
        if (input.getSchema() != null && input.getSchema().getProperty() != null) {
            innerType = input.getSchema().getProperty().getType();
        }
        return propertyConvertor.convertToToscaObject(input, supplier.get(), dataTypes, true);
    }

    private ToscaNodeType createNodeType(Component component) {
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        if (ModelConverter.isAtomicComponent(component)) {
            if (((Resource) component).getDerivedFrom() != null) {
                toscaNodeType.setDerived_from(((Resource) component).getDerivedFrom().get(0));
            }
            toscaNodeType.setDescription(component.getDescription());
        } else {
            String derivedFrom = null != component.getDerivedFromGenericType() ? component.getDerivedFromGenericType()
                    : NATIVE_ROOT;
            toscaNodeType.setDerived_from(derivedFrom);
        }
        return toscaNodeType;
    }

    private Either<Map<String, Object>, ToscaError> createProxyInterfaceTypes(Component container) {

        Map<String, Object> proxyInterfaceTypes = new HashMap<>();
        Either<Map<String, Object>, ToscaError> res = Either.left(proxyInterfaceTypes);
        List<ComponentInstance> componentInstances = container.getComponentInstances();
        if (CollectionUtils.isEmpty(componentInstances)) {
            return res;
        }
        Map<String, ComponentInstance> serviceProxyInstanceList = new HashMap<>();
        componentInstances.stream()
                .filter(this::isComponentOfTypeServiceProxy)
                .forEach(inst -> serviceProxyInstanceList.put(inst.getToscaComponentName(), inst));
        if (MapUtils.isEmpty(serviceProxyInstanceList)) {
            return res;
        }
        for (Entry<String, ComponentInstance> entryProxy : serviceProxyInstanceList.entrySet()) {
            Component serviceComponent;
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreInterfaces(false);
            Either<Component, StorageOperationStatus> service = toscaOperationFacade
                    .getToscaElement(entryProxy.getValue().getSourceModelUid(), componentParametersView);
            if (service.isRight()) {
                log.debug("Failed to fetch original service component with id {} for instance {}",
                        entryProxy.getValue().getSourceModelUid(), entryProxy.getValue().getName());
                return Either.right(ToscaError.GENERAL_ERROR);
            } else {
                serviceComponent = service.left().value();
            }

            Either<Map<String, InterfaceDefinition>, StorageOperationStatus> lifecycleTypeEither =
                    interfaceLifecycleOperation.getAllInterfaceLifecycleTypes();
            if(lifecycleTypeEither.isRight()){
                log.debug("Failed to retrieve global interface types :", lifecycleTypeEither.right().value());
                return Either.right(ToscaError.GENERAL_ERROR);
            }

            List<String> allGlobalInterfaceTypes = lifecycleTypeEither.left().value().values().stream()
                    .map(InterfaceDataDefinition::getType)
                    .collect(Collectors.toList());
            //Add interface types for local interfaces in the original service component for proxy
            Map<String, Object> localInterfaceTypes = addInterfaceTypeElement(serviceComponent,
                    allGlobalInterfaceTypes);
            if (MapUtils.isNotEmpty(localInterfaceTypes)) {
                proxyInterfaceTypes.putAll(localInterfaceTypes);
            }

        }
        return Either.left(proxyInterfaceTypes);
    }

    private Either<Map<String, ToscaNodeType>, ToscaError> createProxyNodeTypes(Map<String, Component> componentCache,
                                                                                Component container) {

        Map<String, ToscaNodeType> nodeTypesMap = new HashMap<>();
        Either<Map<String, ToscaNodeType>, ToscaError> res = Either.left(nodeTypesMap);

        List<ComponentInstance> componentInstances = container.getComponentInstances();

        if (componentInstances == null || componentInstances.isEmpty()) {
            return res;
        }
        Map<String, ComponentInstance> serviceProxyInstanceList = new HashMap<>();
        List<ComponentInstance> proxyInst = componentInstances.stream()
                .filter(p -> p.getOriginType().name().equals(OriginTypeEnum.ServiceProxy.name()))
                .collect(Collectors.toList());
        if (proxyInst != null && !proxyInst.isEmpty()) {
            for (ComponentInstance inst : proxyInst) {
                serviceProxyInstanceList.put(inst.getToscaComponentName(), inst);
            }
        }

        if (serviceProxyInstanceList.isEmpty()) {
            return res;
        }
        Either<Resource, StorageOperationStatus> serviceProxyOrigin = toscaOperationFacade
                .getLatestByName("serviceProxy");
        if (serviceProxyOrigin.isRight()) {
            log.debug("Failed to fetch normative service proxy resource by tosca name, error {}",
                    serviceProxyOrigin.right().value());
            return Either.right(ToscaError.NOT_SUPPORTED_TOSCA_TYPE);
        }
        Component origComponent = serviceProxyOrigin.left().value();

        for (Entry<String, ComponentInstance> entryProxy : serviceProxyInstanceList.entrySet()) {
            Component serviceComponent = null;
            ComponentParametersView componentParametersView = new ComponentParametersView();
            componentParametersView.disableAll();
            componentParametersView.setIgnoreCategories(false);
            componentParametersView.setIgnoreProperties(false);
            componentParametersView.setIgnoreInputs(false);
            componentParametersView.setIgnoreInterfaces(false);
            componentParametersView.setIgnoreRequirements(false);
            Either<Component, StorageOperationStatus> service = toscaOperationFacade
                    .getToscaElement(entryProxy.getValue().getSourceModelUid(), componentParametersView);
            if (service.isRight()) {
                log.debug("Failed to fetch resource with id {} for instance {}", entryProxy.getValue().getSourceModelUid(),  entryProxy.getValue().getName());
            } else {
                serviceComponent = service.left().value();
            }

            ToscaNodeType toscaNodeType = createProxyNodeType(componentCache, origComponent, serviceComponent,
                    entryProxy.getValue());
            nodeTypesMap.put(entryProxy.getKey(), toscaNodeType);
        }

        return Either.left(nodeTypesMap);
    }

    private ToscaNodeType createProxyNodeType(Map<String, Component> componentCache , Component origComponent,
                                              Component proxyComponent, ComponentInstance instance) {
        ToscaNodeType toscaNodeType = new ToscaNodeType();
        String derivedFrom = ((Resource) origComponent).getToscaResourceName();

        toscaNodeType.setDerived_from(derivedFrom);
        Either<Map<String, DataTypeDefinition>, JanusGraphOperationStatus> dataTypesEither = dataTypeCache.getAll();
        if (dataTypesEither.isRight()) {
            log.debug("Failed to retrieve all data types {}", dataTypesEither.right().value());
        }
        Map<String, DataTypeDefinition> dataTypes = dataTypesEither.left().value();
        Map<String, ToscaCapability> capabilities = this.capabilityRequirementConverter
                .convertProxyCapabilities(componentCache, instance, dataTypes);

        if (MapUtils.isNotEmpty(capabilities)) {
            toscaNodeType.setCapabilities(capabilities);
        }
        List<Map<String, ToscaRequirement>> proxyNodeTypeRequirements = this.capabilityRequirementConverter
                .convertProxyRequirements(componentCache, instance);
        if (CollectionUtils.isNotEmpty(proxyNodeTypeRequirements)) {
            toscaNodeType.setRequirements(proxyNodeTypeRequirements);
        }
        Optional<Map<String, ToscaProperty>> proxyProperties = getProxyNodeTypeProperties(proxyComponent, dataTypes);
        proxyProperties.ifPresent(toscaNodeType::setProperties);

        Optional<Map<String, Object>> proxyInterfaces = getProxyNodeTypeInterfaces(proxyComponent, dataTypes);
        proxyInterfaces.ifPresent(toscaNodeType::setInterfaces);

        return toscaNodeType;
    }

    private Either<ToscaNodeTemplate, ToscaError> convertComponentInstanceRequirements(Component component,
            ComponentInstance componentInstance, List<RequirementCapabilityRelDef> relations,
            ToscaNodeTemplate nodeTypeTemplate, Component originComponent, Map<String, Component> componentCache) {

        List<Map<String, ToscaTemplateRequirement>> toscaRequirements = new ArrayList<>();
        if (!addRequirements(component, componentInstance, relations, originComponent, toscaRequirements, componentCache)) {
            log.debug("Failed to convert component instance requirements for the component instance {}. ",
                    componentInstance.getName());
            return Either.right(ToscaError.NODE_TYPE_REQUIREMENT_ERROR);
        }
        if (!toscaRequirements.isEmpty()) {
            nodeTypeTemplate.setRequirements(toscaRequirements);
        }
        log.debug("Finished to convert requirements for the node type {} ", componentInstance.getName());
        return Either.left(nodeTypeTemplate);
    }

    private boolean addRequirements(Component component, ComponentInstance componentInstance,
            List<RequirementCapabilityRelDef> relations, Component originComponent,
            List<Map<String, ToscaTemplateRequirement>> toscaRequirements, Map<String, Component> componentCache) {
        List<RequirementCapabilityRelDef> filteredRelations = relations.stream()
                .filter(p -> componentInstance.getUniqueId().equals(p.getFromNode())).collect(Collectors.toList());
        return isEmpty(filteredRelations) ||
                filteredRelations.stream()
                        .allMatch(rel -> addRequirement(componentInstance, originComponent, component.getComponentInstances(), rel, toscaRequirements, componentCache));
    }

    private boolean addRequirement(ComponentInstance fromInstance, Component fromOriginComponent,
            List<ComponentInstance> instancesList, RequirementCapabilityRelDef rel,
            List<Map<String, ToscaTemplateRequirement>> toscaRequirements, Map<String, Component> componentCache) {

        boolean result = true;
        Map<String, List<RequirementDefinition>> reqMap = fromOriginComponent.getRequirements();
        RelationshipInfo reqAndRelationshipPair = rel.getRelationships().get(0).getRelation();
        Either<Component, StorageOperationStatus> getOriginRes = null;
        Optional<RequirementDefinition> reqOpt = Optional.empty();
        Component toOriginComponent = null;
        Optional<CapabilityDefinition> capOpt = Optional.empty();

        ComponentInstance toInstance = instancesList.stream().filter(i -> rel.getToNode().equals(i.getUniqueId()))
                .findFirst().orElse(null);
        if (toInstance == null) {
            log.debug("Failed to find a relation from the node {} to the node {}", fromInstance.getName(),
                    rel.getToNode());
            result = false;
        }
        if (result) {
            reqOpt = findRequirement(fromOriginComponent, reqMap, reqAndRelationshipPair, fromInstance.getUniqueId());
            if (!reqOpt.isPresent()) {
                log.debug("Failed to find a requirement with uniqueId {} on a component with uniqueId {}",
                        reqAndRelationshipPair.getRequirementUid(), fromOriginComponent.getUniqueId());
                result = false;
            }
        }
        if (result) {
            ComponentParametersView filter = new ComponentParametersView(true);
            filter.setIgnoreComponentInstances(false);
            filter.setIgnoreCapabilities(false);
            filter.setIgnoreGroups(false);
            getOriginRes = toscaOperationFacade.getToscaElement(toInstance.getActualComponentUid(), filter);
            if (getOriginRes.isRight()) {
                log.debug("Failed to build substituted name for the requirement {}. Failed to get an origin component with uniqueId {}",
                        reqOpt.get().getName(), toInstance.getActualComponentUid());
                result = false;
            }
        }
        if (result) {
            toOriginComponent = getOriginRes.left().value();
            capOpt = toOriginComponent.getCapabilities().get(reqOpt.get().getCapability()).stream()
                    .filter(c -> isCapabilityBelongToRelation(reqAndRelationshipPair, c)).findFirst();
            if (!capOpt.isPresent()) {
                capOpt = findCapability(reqAndRelationshipPair, toOriginComponent, fromOriginComponent, reqOpt.get());
                if(!capOpt.isPresent()){
                result = false;
                log.debug("Failed to find a capability with name {} on a component with uniqueId {}",
                        reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
                }
            }
        }
        if (result) {
            result = buildAndAddRequirement(toscaRequirements, fromOriginComponent, toOriginComponent, capOpt.get(),
                    reqOpt.get(), reqAndRelationshipPair, toInstance, componentCache);
        }
        return result;
    }

    private boolean isCapabilityBelongToRelation(RelationshipInfo reqAndRelationshipPair, CapabilityDefinition capability) {
        return capability.getName().equals(reqAndRelationshipPair.getCapability()) && (capability.getOwnerId() !=null && capability.getOwnerId().equals(reqAndRelationshipPair.getCapabilityOwnerId()));
    }

    private Optional<CapabilityDefinition> findCapability(RelationshipInfo reqAndRelationshipPair, Component toOriginComponent, Component fromOriginComponent, RequirementDefinition requirement) {
        Optional<CapabilityDefinition> cap = toOriginComponent.getCapabilities().get(requirement.getCapability()).stream().filter(c -> c.getType().equals(requirement.getCapability())).findFirst();
        if (!cap.isPresent()) {
            log.debug("Failed to find a capability with name {} on a component with uniqueId {}", reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
        }
        return cap;
    }

    private boolean buildAndAddRequirement(List<Map<String, ToscaTemplateRequirement>> toscaRequirements, Component fromOriginComponent, Component toOriginComponent, CapabilityDefinition capability, RequirementDefinition requirement, RelationshipInfo reqAndRelationshipPair, ComponentInstance toInstance, Map<String, Component> componentCache) {
        List<String> reducedPath = capability.getPath();
        if(capability.getOwnerId() !=null){
            reducedPath =   capabilityRequirementConverter.getReducedPathByOwner(capability.getPath() , capability.getOwnerId() );
        }
        Either<String, Boolean> buildCapNameRes = capabilityRequirementConverter.buildSubstitutedName(componentCache,
                toOriginComponent, reducedPath, reqAndRelationshipPair.getCapability(), capability.getPreviousName());
        if (buildCapNameRes.isRight()) {
            log.debug(
                    "Failed to build a substituted capability name for the capability with name {} on a component with uniqueId {}",
                    reqAndRelationshipPair.getCapability(), fromOriginComponent.getUniqueId());
            return false;
        }
        Either<String, Boolean> buildReqNameRes  = capabilityRequirementConverter.buildSubstitutedName(componentCache, fromOriginComponent,
                requirement.getPath(), reqAndRelationshipPair.getRequirement(), requirement.getPreviousName());
        if (buildReqNameRes.isRight()) {
            log.debug(
                    "Failed to build a substituted requirement name for the requirement with name {} on a component with uniqueId {}",
                    reqAndRelationshipPair.getRequirement(), fromOriginComponent.getUniqueId());
            return false;
        }
        ToscaTemplateRequirement toscaRequirement = new ToscaTemplateRequirement();
        Map<String, ToscaTemplateRequirement> toscaReqMap = new HashMap<>();
        toscaRequirement.setNode(toInstance.getName());
        toscaRequirement.setCapability(buildCapNameRes.left().value());
        toscaReqMap.put(buildReqNameRes.left().value(), toscaRequirement);
        toscaRequirements.add(toscaReqMap);
        return true;
    }

    private Optional<RequirementDefinition> findRequirement(Component fromOriginComponent, Map<String, List<RequirementDefinition>> reqMap, RelationshipInfo reqAndRelationshipPair,  String fromInstanceId) {
        for(List<RequirementDefinition> reqList: reqMap.values()){
            Optional<RequirementDefinition> reqOpt = reqList.stream().filter(r -> isRequirementBelongToRelation(fromOriginComponent, reqAndRelationshipPair, r, fromInstanceId)).findFirst();
            if(reqOpt.isPresent()){
                return reqOpt;
            }
        }
        return Optional.empty();
    }

    /**
     * Allows detecting the requirement belonging to the received relationship
     * The detection logic is: A requirement belongs to a relationship IF 1.The
     * name of the requirement equals to the "requirement" field of the
     * relation; AND 2. In case of a non-atomic resource, OwnerId of the
     * requirement equals to requirementOwnerId of the relation OR uniqueId of
     * toInstance equals to capabilityOwnerId of the relation
     */
    private boolean isRequirementBelongToRelation(Component originComponent, RelationshipInfo reqAndRelationshipPair, RequirementDefinition requirement, String fromInstanceId) {
        if (!StringUtils.equals(requirement.getName(), reqAndRelationshipPair.getRequirement())) {
            log.debug("Failed to find a requirement with name {} and  reqAndRelationshipPair {}",
                    requirement.getName(), reqAndRelationshipPair.getRequirement());
            return false;
        }
        return ModelConverter.isAtomicComponent(originComponent) ||
                isRequirementBelongToOwner(reqAndRelationshipPair, requirement, fromInstanceId, originComponent);
    }

    private boolean isRequirementBelongToOwner(RelationshipInfo reqAndRelationshipPair, RequirementDefinition requirement, String fromInstanceId, Component originComponent) {
        return StringUtils.equals(requirement.getOwnerId(), reqAndRelationshipPair.getRequirementOwnerId())
                || (isCvfc(originComponent) && StringUtils.equals(fromInstanceId, reqAndRelationshipPair.getRequirementOwnerId())
                || StringUtils.equals(requirement.getOwnerId(), originComponent.getUniqueId()));
    }

    private boolean isCvfc(Component component) {
        return component.getComponentType() == ComponentTypeEnum.RESOURCE &&
                ((Resource) component).getResourceType() == ResourceTypeEnum.CVFC;
    }

    private Either<SubstitutionMapping, ToscaError> convertCapabilities(Component component,
            SubstitutionMapping substitutionMappings, Map<String, Component> componentCache) {

        Either<SubstitutionMapping, ToscaError> result = Either.left(substitutionMappings);
        Either<Map<String, String[]>, ToscaError> toscaCapabilitiesRes = capabilityRequirementConverter
                .convertSubstitutionMappingCapabilities(componentCache, component);
        if (toscaCapabilitiesRes.isRight()) {
            result = Either.right(toscaCapabilitiesRes.right().value());
            log.debug("Failed convert capabilities for the component {}. ", component.getName());
        } else if (isNotEmpty(toscaCapabilitiesRes.left().value())) {
            substitutionMappings.setCapabilities(toscaCapabilitiesRes.left().value());
            log.debug("Finish convert capabilities for the component {}. ", component.getName());
        }
        log.debug("Finished to convert capabilities for the component {}. ", component.getName());
        return result;
    }

    private Either<ToscaNodeType, ToscaError> convertCapabilities(Map<String, Component> componentsCache, Component component, ToscaNodeType nodeType,
            Map<String, DataTypeDefinition> dataTypes) {
        Map<String, ToscaCapability> toscaCapabilities = capabilityRequirementConverter.convertCapabilities(componentsCache, component,
                dataTypes);
        if (!toscaCapabilities.isEmpty()) {
            nodeType.setCapabilities(toscaCapabilities);
        }
        log.debug("Finish convert Capabilities for node type");

        return Either.left(nodeType);
    }

    private Map<String, ToscaTemplateArtifact> convertToNodeTemplateArtifacts(Map<String, ToscaArtifactDataDefinition> artifacts) {
        if (artifacts == null) {
            return null;
        }
        Map<String, ToscaTemplateArtifact> arts = new HashMap<>();
        for (Map.Entry<String, ToscaArtifactDataDefinition> entry : artifacts.entrySet()) {
           ToscaTemplateArtifact artifact = new ToscaTemplateArtifact();
           artifact.setFile(entry.getValue().getFile());
           artifact.setType(entry.getValue().getType());
           arts.put(entry.getKey(), artifact);
        }
        return arts;
    }

    protected NodeFilter convertToNodeTemplateNodeFilterComponent(CINodeFilterDataDefinition inNodeFilter) {
        if (inNodeFilter == null){
            return null;
        }
        NodeFilter nodeFilter = new NodeFilter();

        ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> origCapabilities =
                inNodeFilter.getCapabilities();

        ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> origProperties = inNodeFilter.getProperties();

        List<Map<String, CapabilityFilter>> capabilitiesCopy = new ArrayList<>();
        List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();

        copyNodeFilterCapabilitiesTemplate(origCapabilities, capabilitiesCopy);
        copyNodeFilterProperties(origProperties, propertiesCopy);

        if(CollectionUtils.isNotEmpty(capabilitiesCopy)) {
            nodeFilter.setCapabilities(capabilitiesCopy);
        }

        if(CollectionUtils.isNotEmpty(propertiesCopy)) {
            nodeFilter.setProperties(propertiesCopy);
        }

        nodeFilter.setTosca_id(cloneToscaId(inNodeFilter.getTosca_id()));


        nodeFilter = (NodeFilter) cloneObjectFromYml(nodeFilter, NodeFilter.class);

        return nodeFilter;
    }

    private Object cloneToscaId(Object toscaId) {
        return Objects.isNull(toscaId) ? null
                       : cloneObjectFromYml(toscaId, toscaId.getClass());
    }


    private Object cloneObjectFromYml(Object objToClone, Class classOfObj) {
        String objectAsYml = yamlUtil.objectToYaml(objToClone);
        return yamlUtil.yamlToObject(objectAsYml, classOfObj);
    }
    private void copyNodeFilterCapabilitiesTemplate(
            ListDataDefinition<RequirementNodeFilterCapabilityDataDefinition> origCapabilities,
            List<Map<String, CapabilityFilter>> capabilitiesCopy) {
        if(origCapabilities == null || origCapabilities.getListToscaDataDefinition() == null ||
                   origCapabilities.getListToscaDataDefinition().isEmpty() ) {
            return;
        }
        for(RequirementNodeFilterCapabilityDataDefinition capability : origCapabilities.getListToscaDataDefinition()) {
            Map<String, CapabilityFilter> capabilityFilterCopyMap = new HashMap<>();
            CapabilityFilter capabilityFilter = new CapabilityFilter();
            List<Map<String, List<Object>>> propertiesCopy = new ArrayList<>();
            copyNodeFilterProperties(capability.getProperties(), propertiesCopy);
            capabilityFilter.setProperties(propertiesCopy);
            capabilityFilterCopyMap.put(capability.getName(), capabilityFilter);
            capabilitiesCopy.add(capabilityFilterCopyMap);
        }
    }

    private List<Object> copyNodeFilterProperty(List<Object> propertyList) {
        String listAsString = yamlUtil.objectToYaml(propertyList);
        return yamlUtil.yamlToObject(listAsString, List.class);
    }


    private void copyNodeFilterProperties(
            ListDataDefinition<RequirementNodeFilterPropertyDataDefinition> origProperties,
            List<Map<String, List<Object>>> propertiesCopy) {
        if(origProperties == null || origProperties.getListToscaDataDefinition() == null ||
                   origProperties.isEmpty()) {
            return;
        }
        Map<String, List<Object>> propertyMapCopy = new HashMap<>();
        for(RequirementNodeFilterPropertyDataDefinition propertyDataDefinition : origProperties.getListToscaDataDefinition()) {
            for(String propertyInfoEntry : propertyDataDefinition.getConstraints()) {
                Map propertyValObj =  new YamlUtil().yamlToObject(propertyInfoEntry, Map.class);
                String propertyName = propertyDataDefinition.getName();
                if (propertyMapCopy.containsKey(propertyName)){
                    addPropertyConstraintValueToList(propertyName, propertyValObj, propertyMapCopy.get(propertyName));
                } else {
                    if (propertyName != null) {
                        List propsList = new ArrayList();
                        addPropertyConstraintValueToList(propertyName, propertyValObj, propsList);
                        propertyMapCopy.put(propertyName, propsList);
                    } else {
                        propertyMapCopy.putAll(propertyValObj);
                    }
                }
            }
        }
        propertyMapCopy.entrySet().stream().forEach(entry ->
            addCalculatedConstraintsIntoPropertiesList(propertiesCopy, entry));
    }

    private void addPropertyConstraintValueToList(String propertyName, Map propertyValObj, List propsList) {
        if(propertyValObj.containsKey(propertyName)) {
            propsList.add(propertyValObj.get(propertyName));
        } else {
            propsList.add(propertyValObj);
        }
    }



    private void addCalculatedConstraintsIntoPropertiesList(List<Map<String, List<Object>>> propertiesCopy,
            Entry<String, List<Object>> entry) {
        Map<String, List<Object>> tempMap = new HashMap<>();
        tempMap.put(entry.getKey(), entry.getValue());
        propertiesCopy.add(tempMap);
    }

    private static class CustomRepresenter extends Representer {
        CustomRepresenter() {
            super();
            // null representer is exceptional and it is stored as an instance
            // variable.
            this.nullRepresenter = new RepresentNull();

        }

        @Override
        protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue,
                Tag customTag) {
            if (propertyValue == null) {
                return null;
            }
            // skip not relevant for Tosca property
            if ("dependencies".equals(property.getName())) {
                return null;
            }
            NodeTuple defaultNode = super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);

            return "_defaultp_".equals(property.getName())
                    ? new NodeTuple(representData("default"), defaultNode.getValueNode()) : defaultNode;
        }

        @Override
        protected MappingNode representJavaBean(Set<Property> properties, Object javaBean) {
            // remove the bean type from the output yaml (!! ...)
            if (!classTags.containsKey(javaBean.getClass())) {
                addClassTag(javaBean.getClass(), Tag.MAP);
            }

            return super.representJavaBean(properties, javaBean);
        }

        private class RepresentNull implements Represent {
            @Override
            public Node representData(Object data) {
                // possible values are here http://yaml.org/type/null.html
                return representScalar(Tag.NULL, "");
            }
        }
    }

    private static class UnsortedPropertyUtils extends PropertyUtils {
        @Override
        protected Set<Property> createPropertySet(Class type, BeanAccess bAccess)
                throws IntrospectionException {
            Collection<Property> fields = getPropertiesMap(type, BeanAccess.FIELD).values();
            return new LinkedHashSet<>(fields);
        }
    }

    private Object removeOperationsKeyFromInterface(Object interfaceInstanceDataDefinition) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);

            Map<String, Object> interfaceAsMap = ServiceUtils.getObjectAsMap(interfaceInstanceDataDefinition);
            Map<String, Object> operations = (Map<String, Object>) interfaceAsMap.remove("operations");
            interfaceAsMap.remove("empty");

            if(MapUtils.isNotEmpty(operations)) {
                interfaceAsMap.putAll(operations);
            }

            Object interfaceObject = objectMapper.convertValue(interfaceAsMap, Object.class);

            return interfaceObject;

    }

    Optional<Map<String, ToscaProperty>> getProxyNodeTypeProperties(Component proxyComponent,
            Map<String, DataTypeDefinition>
                    dataTypes) {
        if (Objects.isNull(proxyComponent)) {
            return Optional.empty();
        }
        Map<String, ToscaProperty> proxyProperties = new HashMap<>();
        addInputsToProperties(dataTypes, proxyComponent.getInputs(), proxyProperties);
        if (CollectionUtils.isNotEmpty(proxyComponent.getProperties())) {
            proxyProperties.putAll(proxyComponent.getProperties().stream()
                                           .map(propertyDefinition -> resolvePropertyValueFromInput(propertyDefinition,
                                                   proxyComponent.getInputs()))
                                           .collect(Collectors.toMap(PropertyDataDefinition::getName,
                                                   property -> propertyConvertor.convertProperty(dataTypes, property,
                                                           PropertyConvertor.PropertyType.PROPERTY))));
        }
        return MapUtils.isNotEmpty(proxyProperties) ? Optional.of(proxyProperties) : Optional.empty();
    }

    void addInputsToProperties(Map<String, DataTypeDefinition> dataTypes,
            List<InputDefinition> componentInputs,
            Map<String, ToscaProperty> mergedProperties) {
        if (CollectionUtils.isEmpty(componentInputs)) {
            return;
        }
        for(InputDefinition input : componentInputs) {
            ToscaProperty property = propertyConvertor.convertProperty(dataTypes, input,
                    PropertyConvertor.PropertyType.INPUT);
            mergedProperties.put(input.getName(), property);
        }
    }

    Optional<Map<String, Object>> getProxyNodeTypeInterfaces(Component proxyComponent,
            Map<String, DataTypeDefinition> dataTypes) {
        if (Objects.isNull(proxyComponent) || MapUtils.isEmpty(proxyComponent.getInterfaces())) {
            return Optional.empty();
        }
        Map<String, InterfaceDefinition> proxyComponentInterfaces = proxyComponent.getInterfaces();
        //Unset artifact path for operation implementation for proxy node types as for operations with artifacts it is
        // always available in the proxy node template
        removeOperationImplementationForProxyNodeType(proxyComponentInterfaces);
        return Optional.ofNullable(interfacesOperationsConverter
                                           .getInterfacesMap(proxyComponent, null, proxyComponentInterfaces, dataTypes,
                                                   false, false));
    }

    private static void removeOperationImplementationForProxyNodeType(
            Map<String, InterfaceDefinition> proxyComponentInterfaces) {
        if (MapUtils.isEmpty(proxyComponentInterfaces)) {
            return;
        }
        proxyComponentInterfaces.values().stream().map(InterfaceDataDefinition::getOperations)
                .filter(MapUtils::isNotEmpty)
                .forEach(operations -> operations.values().forEach(operation -> operation.setImplementation(null)));
    }
}

