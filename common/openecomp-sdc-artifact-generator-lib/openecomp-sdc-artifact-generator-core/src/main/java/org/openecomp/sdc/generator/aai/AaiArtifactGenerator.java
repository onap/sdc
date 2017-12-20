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

package org.openecomp.sdc.generator.aai;

import static org.openecomp.sdc.generator.data.GeneratorConstants.ARTIFACT_MODEL_INFO;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGFILE_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_PROVIDING_SERVICE_MISSING;
import static org.openecomp.sdc.generator.data.GeneratorConstants.ID_LENGTH;
import static org.openecomp.sdc.generator.util.ArtifactGeneratorUtil.logError;

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.aai.model.AllotedResource;
import org.openecomp.sdc.generator.aai.model.ProvidingService;
import org.openecomp.sdc.generator.aai.model.L3NetworkWidget;
import org.openecomp.sdc.generator.aai.model.Model;
import org.openecomp.sdc.generator.aai.model.Resource;
import org.openecomp.sdc.generator.aai.model.Service;
import org.openecomp.sdc.generator.aai.model.TunnelXconnectWidget;
import org.openecomp.sdc.generator.aai.model.VfModule;
import org.openecomp.sdc.generator.aai.model.Widget;
import org.openecomp.sdc.generator.aai.tosca.GroupDefinition;
import org.openecomp.sdc.generator.aai.tosca.NodeTemplate;
import org.openecomp.sdc.generator.aai.tosca.ToscaTemplate;
import org.openecomp.sdc.generator.aai.types.ModelType;
import org.openecomp.sdc.generator.data.AdditionalParams;
import org.openecomp.sdc.generator.data.Artifact;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GenerationData;
import org.openecomp.sdc.generator.data.GeneratorConstants;
import org.openecomp.sdc.generator.data.GeneratorUtil;
import org.openecomp.sdc.generator.data.GroupType;
import org.openecomp.sdc.generator.data.WidgetConfigurationUtil;
import org.openecomp.sdc.generator.intf.ArtifactGenerator;
import org.openecomp.sdc.generator.intf.Generator;
import org.openecomp.sdc.generator.logging.annotations.Audit;
import org.openecomp.sdc.generator.util.ArtifactGeneratorUtil;
import org.slf4j.MDC;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Generator(artifactType = ArtifactType.AAI)
public class AaiArtifactGenerator implements ArtifactGenerator {

  private static Logger log = LoggerFactory.getLogger(AaiArtifactGenerator.class.getName());

  /**
   * Implementation of the method to generate AAI artifacts.
   *
   * @param input List of input tosca files
   * @return Translated/Error data as a {@link GenerationData} object
   */
  @Override
  @Audit
  public GenerationData generateArtifact(List<Artifact> input,
                                         Map<String, String> additionalParams) {
    try {
      if (input != null && input.size() != 0 ) {
        ArtifactGeneratorUtil.initializeArtifactLoggingContext(input.get(0));
      }
      initWidgetConfiguration();
      return generateArtifactInternal(input, additionalParams);
    } catch (Exception exception) {
      logError(exception.getMessage(), exception);
      GenerationData generationData = new GenerationData();
      generationData.add(ArtifactType.AAI.name(), exception.getMessage());
      return generationData;
    }
  }

  /**
   * Helper method to generate AAI artifacts.
   *
   * @param input List of input tosca files
   * @return Translated/Error data as a {@link GenerationData} object
   */
  private GenerationData generateArtifactInternal(List<Artifact> input,
                                                  Map<String, String> additionalParams) {
    final GenerationData generationData = new GenerationData();

    List<Resource> resources = new LinkedList<>();
    Map<String, String> idTypeStore = new HashMap<>();
    Map<String, String> resourcesVersion = new HashMap<>();

    List<ToscaTemplate> toscas = new LinkedList<>();

    String serviceVersion = additionalParams.get(AdditionalParams.ServiceVersion.getName());
    if (serviceVersion == null) {
      throw new IllegalArgumentException(GeneratorConstants
          .GENERATOR_AAI_ERROR_MISSING_SERVICE_VERSION);
    } else {
      String versionRegex = "^[1-9]\\d*(\\.0)$";
      if (! (serviceVersion.matches(versionRegex))) {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants
                .GENERATOR_AAI_INVALID_SERVICE_VERSION));
      }
    }

    for (Artifact inputArtifact : input) {
      ToscaTemplate tosca = getToscaModel(inputArtifact, serviceVersion);
      validateTosca(tosca, inputArtifact);
      ToscaTemplate processedTosca = preProcessingTosca(tosca);
      toscas.add(processedTosca);
    }

    //Get the service tosca from the list of artifacts
    ToscaTemplate serviceTosca = getServiceTosca(toscas);
    if (serviceTosca == null) {
      throw new IllegalArgumentException(GeneratorConstants
          .GENERATOR_AAI_ERROR_MISSING_SERVICE_TOSCA);
    }

    Service service = new Service();
    //Populate basic service model metadata
    service.populateModelIdentificationInformation(serviceTosca.getMetadata());

    if (serviceTosca.getTopology_template() != null
        && serviceTosca.getTopology_template().getNode_templates() != null) {
      processServiceTosca(service, idTypeStore,resourcesVersion, serviceTosca,resources);
    }
    validateResourceToscaAgainstService(idTypeStore, toscas);

    //Process the resource tosca files
    int counter = 0;
    List<Resource> currentToscaResources = new LinkedList<>();
    while (toscas.size() > 0) {
      ToscaTemplate resourceTemplate = toscas.remove(0);
      String resourceUuId = resourceTemplate.getMetadata().get("UUID");
      String mapValue = idTypeStore.get(resourceUuId);
      if (mapValue == null) {
        log.warn(
            "Additional tosca file found with resource version id : "
                + resourceUuId);
        continue;
      }
      //update resource version with version from service tosca
      String resourceVersion = resourcesVersion.get(resourceUuId);
      resourceTemplate.getMetadata().put("version", resourceVersion);
      Model model = Model.getModelFor(idTypeStore.get(resourceTemplate.getModelVersionId()));

      log.debug("Inside Resource artifact generation for resource");
      model.populateModelIdentificationInformation(
          resourceTemplate.getMetadata());  //Get base resource metadata information
      //Found model from the type store so removing the same
      idTypeStore.remove(model.getModelNameVersionId());
      if (resourceTemplate.getTopology_template() != null
          && resourceTemplate.getTopology_template().getNode_templates() != null) {
        processVfTosca(idTypeStore, resourceTemplate, model);
      }

      //Process group information from tosca for vfModules
      if (resourceTemplate.getTopology_template() != null
          && resourceTemplate.getTopology_template().getGroups() != null) {
        processVfModule(resources, currentToscaResources, resourceTemplate, model);
      } else {
        model.getWidgets().clear();
      }

      if ("Tunnel XConnect".equals(resourceTemplate.getMetadata().get("subcategory"))
          && "Allotted Resource".equals(resourceTemplate.getMetadata().get("category"))) {
        model.addWidget(new TunnelXconnectWidget());
      }

      resources.add((Resource) model);
      currentToscaResources
          .clear();    //Clearing the current tosca resource list for the next iteration
      counter = 0;
    }

    AaiModelGenerator modelGenerator = AaiModelGenerator.getInstance();
    //Generate AAI XML service model
    MDC.put(ARTIFACT_MODEL_INFO , service.getModelName() + "," + getArtifactLabel(service));
    String aaiServiceModel = modelGenerator.generateModelFor(service);
    generationData.add(getServiceArtifact(service, aaiServiceModel));

    //Generate AAI XML resource model
    for (Resource res : resources) {
      MDC.put(ARTIFACT_MODEL_INFO , res.getModelName() + "," + getArtifactLabel(res));
      String aaiResourceModel = modelGenerator.generateModelFor(res);
      generationData.add(getResourceArtifact(res, aaiResourceModel));
    }

    //Resetting logging parameters since they get overridden while writing metrics logs
    // recursively for service, resource and widgets.
    if (input != null && input.size() != 0 ) {
      ArtifactGeneratorUtil.initializeArtifactLoggingContext(input.get(0));
    }

    return generationData;
  }

  private void validateResourceToscaAgainstService(Map<String, String> idTypeStore,
                                                   List<ToscaTemplate> toscas) {
    Iterator entries = idTypeStore.entrySet().iterator();
    while (entries.hasNext()) {
      Map.Entry entry = (Map.Entry) entries.next();
      String resourceUuidFromService = (String)entry.getKey();
      Iterator<ToscaTemplate> itr = toscas.iterator();
      boolean toscaFound = false;
      while (itr.hasNext()) {
        ToscaTemplate toscaTemplate = itr.next();
        String resourceUuId = toscaTemplate.getMetadata().get("UUID");
        if (resourceUuidFromService.equals(resourceUuId)) {
          toscaFound = true;
          break;
        }
      }
      if (toscaFound == false) {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants.GENERATOR_AAI_ERROR_MISSING_RESOURCE_TOSCA,
                resourceUuidFromService));
      }
    }

  }

  private ToscaTemplate preProcessingTosca(ToscaTemplate tosca) {
    ToscaTemplate processedTosca = tosca;
    if (tosca.getTopology_template() != null
        && tosca.getTopology_template().getNode_templates() != null) {
      Collection<NodeTemplate> coll =
          processedTosca.getTopology_template().getNode_templates().values();
      for (NodeTemplate node : coll) {

        if (node.getType().contains("org.openecomp.resource.vf.") && node.getMetadata().get("category")
            .equals("Allotted Resource")) {
          node.setType("org.openecomp.resource.vf.allottedResource");
        }
        if (node.getType().contains("org.openecomp.resource.vfc.") && node.getMetadata().get
            ("category")
            .equals("Allotted Resource")) {
          node.setType("org.openecomp.resource.vfc.AllottedResource");
        }
      }
    }
    return processedTosca;
  }

  private void processVfTosca(Map<String, String> idTypeStore, ToscaTemplate resourceTemplate,
                              Model model) {
    Set<String> keys = resourceTemplate.getTopology_template().getNode_templates().keySet();
    boolean flag = false;
    for (String key : keys) {
      NodeTemplate node = resourceTemplate.getTopology_template().getNode_templates().get(key);
      Model resourceNode = Model.getModelFor(node.getType());
      if (resourceNode != null) {
        if (resourceNode instanceof ProvidingService) {
          flag = true;
          Map<String, String> properties = new HashMap<>();
          Map<String, Object> nodeProperties = node.getProperties();
          if (nodeProperties.get("providing_service_uuid") == null || nodeProperties.get(
              "providing_service_invariant_uuid") == null) {
            throw new IllegalArgumentException(String.format(
                GENERATOR_AAI_PROVIDING_SERVICE_METADATA_MISSING
                , model.getModelId()));
          }
          for (String key1 : nodeProperties.keySet()) {
            if (nodeProperties.get(key1) instanceof String) {
              properties.put(key1, nodeProperties.get(key1).toString());
            }
          }
          properties.put("version","1.0");
          resourceNode.populateModelIdentificationInformation(properties);
          model.addResource((Resource) resourceNode);
        } else if (resourceNode instanceof Resource && !(resourceNode.getWidgetType().equals(
            Widget.Type
            .L3_NET))) {
          //resourceNode.populateModelIdentificationInformation(node.getMetadata());
          idTypeStore.put(resourceNode.getModelNameVersionId(), node.getType());
          model.addResource((Resource) resourceNode);
        }
      }
    }
    if(model instanceof AllotedResource){
      if(!flag) {
        throw new IllegalArgumentException(String.format(GENERATOR_AAI_PROVIDING_SERVICE_MISSING,
            model.getModelId()));
      }
    }
  }

  /*  private void vfWarnScenario(Map<String, String> idTypeStore, ToscaTemplate resourceTemplate) {
    if (idTypeStore.size() == 0) {
      //Log message for extra model file
      log.warn(
          "Additional tosca file found with resource version id : "
              + resourceTemplate.getModelVersionId());
    } else {
      //Log message for missing model files.. Replace with logger statement
      log.warn("Service-Resource Tosca mapping not found for  : "
          + idTypeStore.keySet().toString());
    }
    return;
  }*/

  private void processVfModule(List<Resource> resources, List<Resource> currentToscaResources,
                               ToscaTemplate resourceTemplate, Model model) {
    log.debug("Inside Resource artifact generation for group/vfModule");
    Collection<GroupDefinition> groups =
        resourceTemplate.getTopology_template().getGroups().values();
    Set<String> nodeNameListForGroups = new HashSet<>();
    for (GroupDefinition gd : groups) {
      Model group = Model.getModelFor(gd.getType());
      if (group != null) {
        group.populateModelIdentificationInformation(gd.getMetadata());
        Map<String, String> properties = new HashMap<>();
        Map<String, Object> groupProperties = gd.getProperties();
        for (String key : groupProperties.keySet()) {
          if (groupProperties.get(key) instanceof String) {
            properties.put(key, groupProperties.get(key).toString());
          }
        }
        group.populateModelIdentificationInformation(properties);
        if (group instanceof VfModule && !currentToscaResources.contains(group)) {
          if (gd.getMembers() != null && !gd.getMembers().isEmpty()) {
            Set<String> groupMembers = new HashSet<>();
            ((VfModule) group).setMembers(gd.getMembers());
            nodeNameListForGroups.addAll(gd.getMembers());
            groupMembers.addAll(gd.getMembers());

            for (String member : groupMembers) {
              NodeTemplate node =
                  resourceTemplate.getTopology_template().getNode_templates().get(member);
              if (node != null) {
                Model resourceNode = null;
                //L3-network inside vf-module to be generated as Widget a special handling.
                if (node.getType().contains("org.openecomp.resource.vl")) {
                  resourceNode = new L3NetworkWidget();
                } else {
                  resourceNode = Model.getModelFor(node.getType());
                }
                if (resourceNode != null) {
                  if (!(resourceNode instanceof Resource)) {
                    Widget widget = (Widget) resourceNode;
                    widget.addKey(member);
                    //Add the widget element encountered
                    // in the resource tosca in the resource model
                    boolean isAdded = group.addWidget(widget);

                    //Add only widgets which are members of vf module and remove others
                    if (isAdded) {
                      model.addWidget(widget);
                    }
                  }
                }
              }
            }
          }

          model.addResource((Resource) group); //Added group (VfModule) to the (VF) model
          currentToscaResources
              .add((Resource) group); //Adding the VfModule group to file specific resources
          //Check if we have already encountered the same VfModule across all the artifacts
          if (!resources.contains(group)) {
            resources.add((Resource) group);
          }
        }
      }
    }

    Iterator<Widget> iter = model.getWidgets().iterator();
    while (iter.hasNext()) {
      if (iter.next().allInstancesUsed(nodeNameListForGroups) || true) {
        iter.remove();
      }
    }
  }

  private void processServiceTosca(Service service, Map<String, String> idTypeStore,Map<String,
      String> resourcesVersion,ToscaTemplate serviceTosca, List<Resource> resources) {
    Collection<NodeTemplate> coll =
        serviceTosca.getTopology_template().getNode_templates().values();
    log.debug("Inside Service Tosca ");
    //Get the resource/widgets in the service according to the node-template types
    for (NodeTemplate node : coll) {
      Model model = Model.getModelFor(node.getType());
      if (model != null) {
        model.populateModelIdentificationInformation(node.getMetadata());
        if (model instanceof Resource) {
          String resourceVersion = node.getMetadata().get("version");
          validateVersion(resourceVersion,model.getModelNameVersionId());
          //Keeping track of resource types and
          // their uuid for identification during resource tosca processing
          idTypeStore.put(model.getModelNameVersionId(), node.getType());
          resourcesVersion.put(model.getModelNameVersionId(),resourceVersion);
          service.addResource((Resource) model);
        } else {
          service.addWidget((Widget) model);
        }
      }
    }
  }

  /**
   * Create Service artifact model from the AAI xml model string.
   *
   * @param serviceModel    Model of the service artifact
   * @param aaiServiceModel AAI model as string
   * @return Generated {@link Artifact} model for the service
   */
  private Artifact getServiceArtifact(Model serviceModel, String aaiServiceModel) {
    Artifact artifact =
        new Artifact(ArtifactType.MODEL_INVENTORY_PROFILE.name(), GroupType.DEPLOYMENT.name(),
            GeneratorUtil.checkSum(aaiServiceModel.getBytes()),
            GeneratorUtil.encode(aaiServiceModel.getBytes()));
    String serviceArtifactName = getArtifactName(serviceModel);
    String serviceArtifactLabel = getArtifactLabel(serviceModel);
    artifact.setName(serviceArtifactName);
    artifact.setLabel(serviceArtifactLabel);
    String description = getArtifactDescription(serviceModel);
    artifact.setDescription(description);
    return artifact;
  }

  /**
   * Create Resource artifact model from the AAI xml model string.
   *
   * @param resourceModel    Model of the resource artifact
   * @param aaiResourceModel AAI model as string
   * @return Generated {@link Artifact} model for the resource
   */
  private Artifact getResourceArtifact(Model resourceModel, String aaiResourceModel) {
    Artifact artifact =
        new Artifact(ArtifactType.MODEL_INVENTORY_PROFILE.name(), GroupType.DEPLOYMENT.name(),
            GeneratorUtil.checkSum(aaiResourceModel.getBytes()),
            GeneratorUtil.encode(aaiResourceModel.getBytes()));
    String resourceArtifactName = getArtifactName(resourceModel);
    String resourceArtifactLabel = getArtifactLabel(resourceModel);
    artifact.setName(resourceArtifactName);
    artifact.setLabel(resourceArtifactLabel);
    String description = getArtifactDescription(resourceModel);
    artifact.setDescription(description);
    return artifact;
  }

  /**
   * Method to generate the artifact name for an AAI model.
   *
   * @param model AAI artifact model
   * @return Model artifact name
   */
  private String getArtifactName(Model model) {
    StringBuilder artifactName = new StringBuilder(ArtifactType.AAI.name());
    artifactName.append("-");

    String truncatedArtifactName = "";
    truncatedArtifactName = truncateName(model.getModelName());
    artifactName.append(truncatedArtifactName);

    artifactName.append("-");
    artifactName.append(model.getModelType().name().toLowerCase());
    artifactName.append("-");
    artifactName.append(model.getModelVersion());

    //artifactName.append(model.getModelVersion());
    artifactName.append(".");
    artifactName.append(GeneratorConstants.GENERATOR_AAI_GENERATED_ARTIFACT_EXTENSION);
    return artifactName.toString();
  }

  private String getArtifactLabel(Model model) {
    // String label = "";
    StringBuilder artifactName = new StringBuilder(ArtifactType.AAI.name());
    artifactName.append("-");
    artifactName.append(model.getModelType().name().toLowerCase());
    artifactName.append("-");
    artifactName.append(hashCodeUuId(model.getModelNameVersionId()));
    String label = (artifactName.toString()).replaceAll("[^a-zA-Z0-9 +]+", "-");
    return label;
  }

  private int hashCodeUuId(String uuId) {
    int hashcode = 0;
    for (int i = 0; i < uuId.length(); i++) {
      hashcode = 31 * hashcode + uuId.charAt(i);
    }
    return hashcode;
  }


  private String truncateName(String name) {
    String truncatedName = name;
    if (name.length() >= 200) {
      truncatedName = name.substring(0, 199);
    }
    return truncatedName;
  }

  private String getArtifactDescription(Model model) {
    String artifactDesc = model.getModelDescription();
    if (model.getModelType().equals(ModelType.SERVICE)) {
      artifactDesc = "AAI Service Model";
    } else if (model.getModelType().equals(ModelType.RESOURCE)) {
      artifactDesc = "AAI Resource Model";
    }
    return artifactDesc;
  }

  private void validateVersion(String version, String uuId) {
    String versionRegex = "^[0-9]\\d*(\\.\\d+)$";
    if (null == version  || version == "") {
      throw new IllegalArgumentException(String
          .format(GeneratorConstants.GENERATOR_AAI_ERROR_NULL_RESOURCE_VERSION_IN_SERVICE_TOSCA,
               uuId));
    } else if ( version.equals("0.0") || !(version.matches(versionRegex))) {
      throw new IllegalArgumentException(String
          .format(GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_RESOURCE_VERSION_IN_SERVICE_TOSCA,
               uuId));
    }
  }
  /**
   * Get the tosca java model from the tosca input artifact.
   *
   * @param input Input tosca file and its metadata information as {@link Artifact} object
   * @return Translated {@link ToscaTemplate tosca} object
   */

  private ToscaTemplate getToscaModel(Artifact input, String serviceVersion)
      throws SecurityException {
    byte[] decodedInput = GeneratorUtil.decoder(input.getPayload());
    String checksum = GeneratorUtil.checkSum(decodedInput);
    ToscaTemplate tosca = null;
    if (checksum.equalsIgnoreCase(input.getChecksum())) {
      try {
        log.debug("Input yaml name " + input.getName() + "payload " + new String(decodedInput));
        tosca = GeneratorUtil.translateTosca(new String(decodedInput), ToscaTemplate.class);
        tosca.getMetadata().put("version", serviceVersion);
        return tosca;
      } catch (Exception exception) {
        throw new IllegalArgumentException(
            String.format(GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_TOSCA, input.getName()), exception);
      }
    } else {
      throw new SecurityException(GeneratorConstants.GENERATOR_AAI_ERROR_CHECKSUM_MISMATCH);
    }
  }

  private void validateTosca(ToscaTemplate tosca, Artifact input) {
    log.debug("Validating tosca for Artifact: " + input.getName());
    if (tosca.getMetadata().containsKey("invariantUUID")) {
      if (tosca.getMetadata().get("invariantUUID") == null
          || tosca.getMetadata().get("invariantUUID") == "") {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION,
                "invariantUUID",
                input.getName()));
      } else if (tosca.getMetadata().get("invariantUUID").length() != ID_LENGTH) {
        throw new IllegalArgumentException(String.format(
            GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID, "invariantUUID", input.getName()));
      }
    }

    if (tosca.getMetadata().containsKey("UUID")) {
      if (tosca.getMetadata().get("UUID") == null || tosca.getMetadata().get("UUID") == "") {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION, "UUID",
                input.getName()));
      } else if (tosca.getMetadata().get("UUID").length() != ID_LENGTH) {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID, "UUID", input.getName()));
      }

    }
    if (tosca.getMetadata().containsKey("name")) {
      if (tosca.getMetadata().get("name") == null || tosca.getMetadata().get("name") == "") {
        throw new IllegalArgumentException(String
            .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION, "name",
                input.getName()));
      }
    }

    //Validate VFmodule
    if (tosca.getTopology_template() != null && tosca.getTopology_template().getGroups() != null) {
      Collection<GroupDefinition> groups = tosca.getTopology_template().getGroups().values();
      for (GroupDefinition gd : groups) {
        Model group = Model.getModelFor(gd.getType());
        if (group != null && group instanceof VfModule) {
          if (gd.getMetadata().containsKey("vfModuleModelName")
              && gd.getMetadata().get("vfModuleModelName") == null) {
            throw new IllegalArgumentException(String
                .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION,
                    "vfModuleModelName",
                    input.getName()));
          }
          if (gd.getMetadata().containsKey("vfModuleModelInvariantUUID")
              && gd.getMetadata().get("vfModuleModelInvariantUUID") == null) {
            throw new IllegalArgumentException(String
                .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION,
                    "vfModuleModelInvariantUUID", input.getName()));
          } else if (gd.getMetadata().get("vfModuleModelInvariantUUID").length() != ID_LENGTH) {
            throw new IllegalArgumentException(String.format(
                 GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID, "vfModuleModelInvariantUUID",
                 input.getName()));
          }

          if (gd.getMetadata().containsKey("vfModuleModelUUID")
              && gd.getMetadata().get("vfModuleModelUUID") == null) {
            throw new IllegalArgumentException(String
                .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION,
                    "vfModuleModelUUID",
                    input.getName()));
          } else if (gd.getMetadata().get("vfModuleModelUUID").length() != ID_LENGTH) {
            throw new IllegalArgumentException(String.format(
                GeneratorConstants.GENERATOR_AAI_ERROR_INVALID_ID, "vfModuleModelUUID",
                input.getName()));
          }
          if (gd.getMetadata().containsKey("vfModuleModelVersion")
              && gd.getMetadata().get("vfModuleModelVersion") == null) {
            throw new IllegalArgumentException(String
                .format(GeneratorConstants.GENERATOR_AAI_ERROR_MANDATORY_METADATA_DEFINITION,
                    "vfModuleModelVersion",
                    input.getName()));
          }
        }
      }
    }
  }

  /**
   * Identify the service tosca artifact from the list of translated tosca inputs.
   *
   * @param input List of translated {@link ToscaTemplate tosca} object models
   * @return Identified service {@link ToscaTemplate tosca}
   */
  private ToscaTemplate getServiceTosca(List<ToscaTemplate> input) {
    Iterator<ToscaTemplate> iter = input.iterator();
    while (iter.hasNext()) {
      ToscaTemplate tosca = iter.next();
      if (tosca.isService()) {
        iter.remove();
        return tosca;
      }
    }
    return null;
  }

  private void initWidgetConfiguration() throws IOException {
    log.debug("Getting Widget Configuration");
    String configLocation = System.getProperty("artifactgenerator.config");
    Properties properties = null;
    if (configLocation != null) {
      File file = new File(configLocation);
      if (file.exists()) {
        properties = new Properties();
        properties.load(new FileInputStream(file));
        WidgetConfigurationUtil.setConfig(properties);
      } else {
        throw new IllegalArgumentException(String.format(GENERATOR_AAI_CONFIGFILE_NOT_FOUND,
            configLocation));
      }
    } else {
      throw new IllegalArgumentException(GENERATOR_AAI_CONFIGLOCATION_NOT_FOUND);
    }
  }

}
