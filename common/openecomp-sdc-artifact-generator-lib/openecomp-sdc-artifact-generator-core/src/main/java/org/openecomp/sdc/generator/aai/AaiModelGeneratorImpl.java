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

import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.generator.aai.model.Resource;
import org.openecomp.sdc.generator.aai.model.Service;
import org.openecomp.sdc.generator.aai.model.Widget;
import org.openecomp.sdc.generator.aai.xml.Model;
import org.openecomp.sdc.generator.aai.xml.ModelElement;
import org.openecomp.sdc.generator.aai.xml.ModelElements;
import org.openecomp.sdc.generator.aai.xml.ModelVer;
import org.openecomp.sdc.generator.aai.xml.ModelVers;
import org.openecomp.sdc.generator.aai.xml.Relationship;
import org.openecomp.sdc.generator.aai.xml.RelationshipData;
import org.openecomp.sdc.generator.aai.xml.RelationshipList;
import org.openecomp.sdc.generator.logging.annotations.Audit;
import org.openecomp.sdc.generator.logging.annotations.Metrics;
import org.w3c.dom.DOMException;

import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Implementation of the {@link AaiModelGenerator} which generates the XML models from the
 * Service/Resource/Widget java models.
 */
public class AaiModelGeneratorImpl implements AaiModelGenerator {
  private static Logger log = LoggerFactory.getLogger(AaiModelGeneratorImpl.class.getName());

  /**
   * Method to generate the AAI model for a Service.
   *
   * @param service Java object model representing an AAI {@link Service} model
   * @return XML representation of the service model in String format
   */
  @Override
  @Metrics
  public String generateModelFor(Service service) {
    //Create a JAXB Model for AAI service model
    Model aaiServiceModel = new Model();
    log.debug("Generating Model for Service with ModelName: " + service.getModelName());
    // after new model
    aaiServiceModel.setModelInvariantId(service.getModelId());
    aaiServiceModel.setModelVers(new ModelVers());
    ModelVer modelVer = new ModelVer();
    modelVer.setModelDescription(service.getModelDescription());
    modelVer.setModelName(service.getModelName());
    modelVer.setModelVersion(service.getModelVersion());
    modelVer.setModelVersionId(service.getModelNameVersionId());
    modelVer.setModelElements(new ModelElements());
    ModelElements modelElements = modelVer.getModelElements();
    //Populate basic model details
    aaiServiceModel
       .setModelType(service.getModelType().name().toLowerCase()); //Using enum name as model type
    List<ModelElement> modelElementList = modelElements.getModelElement();

    //Add service base widget model element
    ModelElement serviceWidgetModelRelationshipElement =
        createRelationshipModelElement(getNewDataDelFlagValue(service.getDeleteFlag()),
            service.getWidgetId(),service.getWidgetInvariantId());
    modelElementList.add(serviceWidgetModelRelationshipElement);

    //Add the resource model elements
    ModelElements serviceModelElements = serviceWidgetModelRelationshipElement.getModelElements();
    List<ModelElement> serviceModelElementList = serviceModelElements.getModelElement();
    Set<Resource> serviceResources = service.getResources();
    if (serviceResources != null && !serviceResources.isEmpty()) {
      for (Resource resourceModel : serviceResources) {
        ModelElement aaiResourceModelElement =
            createRelationshipModelElement(getNewDataDelFlagValue(resourceModel.getDeleteFlag()),
                resourceModel.getModelNameVersionId(),resourceModel.getModelId());
        serviceModelElementList.add(aaiResourceModelElement);
      }
    }

    //Add the widget model elements
    Set<Widget> serviceWidgets = service.getWidgets();
    if (serviceWidgets != null && !serviceWidgets.isEmpty()) {
      for (Widget widgetModel : serviceWidgets) {
        ModelElement widgetModelElement =
            createRelationshipModelElement(getNewDataDelFlagValue(widgetModel.getDeleteFlag()),
                widgetModel.getId(),widgetModel.getWidgetId());
        serviceModelElementList.add(widgetModelElement);
      }
    }
    ModelVers modelVers = aaiServiceModel.getModelVers();
    List<ModelVer> modelVerList = modelVers.getModelVer();
    modelVerList.add(modelVer);
    return getModelAsString(aaiServiceModel);
  }

  /**
   * Method to generate the AAI model for a Resource.
   *
   * @param resource Java object model representing an AAI {@link Resource} model
   * @return XML representation of the resource model in String format
   */
  @Override
  @Metrics
  public String generateModelFor(Resource resource) {
    //Create a JAXB Model for AAI Resource model
    Model aaiResourceModel = new Model();
    log.debug("Generating Model for Resource with ModelName: " + resource.getModelName());
    aaiResourceModel.setModelInvariantId(resource.getModelId());
    aaiResourceModel.setModelVers(new ModelVers());
    ModelVer modelVer = new ModelVer();
    modelVer.setModelDescription(resource.getModelDescription());
    modelVer.setModelName(resource.getModelName());
    modelVer.setModelVersion(resource.getModelVersion());
    modelVer.setModelVersionId(resource.getModelNameVersionId());
    modelVer.setModelElements(new ModelElements());
    ModelElements modelElements = modelVer.getModelElements();
    aaiResourceModel
        .setModelType(resource.getModelType().name().toLowerCase()); //Using enum name as model type
    List<ModelElement> modelElementList = modelElements.getModelElement();

    //Add resource base widget model element
    ModelElement resourceWidgetModelRelationshipElement =
        createRelationshipModelElement(getNewDataDelFlagValue(resource.getDeleteFlag()),
            resource.getWidgetId(),resource.getWidgetInvariantId());
    modelElementList.add(resourceWidgetModelRelationshipElement);

    //Add the child resources to the base widget model element list
    ModelElements baseResourceWidgetModelElements =
        resourceWidgetModelRelationshipElement.getModelElements();
    List<ModelElement> baseResourceWidgetModelElementList =
        baseResourceWidgetModelElements.getModelElement();
    Set<Resource> childResources = resource.getResources();
    if (childResources != null && !childResources.isEmpty()) {
      for (Resource childResourceModel : childResources) {
        ModelElement aaiChildResourceModelElement = createRelationshipModelElement(
            getNewDataDelFlagValue(childResourceModel.getDeleteFlag()),
            childResourceModel.getModelNameVersionId(),childResourceModel.getModelId());
        baseResourceWidgetModelElementList.add(aaiChildResourceModelElement);
      }
    }
    //Add resource widgets/resources to the resource widget model relationship element
    Set<Widget> resourceWidgets = resource.getWidgets();
    if (resourceWidgets != null && !resourceWidgets.isEmpty()) {
      generateWidgetChildren(resourceWidgetModelRelationshipElement, resourceWidgets);
    }

    ModelVers modelVers = aaiResourceModel.getModelVers();
    List<ModelVer> modelVerList = modelVers.getModelVer();
    modelVerList.add(modelVer);
    return getModelAsString(aaiResourceModel);

  }

  /**
   * Method to create the <model-element></model-element> holding the relationship value for a
   * resource/widget
   * model.
   *
   * @param newDataDelFlag    Value of the <new-data-del-flag></new-data-del-flag> attribute for
   *                          a widget/resource in
   *                          the model xml
   * @param relationshipValue Value of the <relationship-value></relationship-value> attribute
   *                          for the widget/resource in
   *                          the model xml
   * @return Java object representation for the <model-element></model-element> holding the
   relationship
   */
  private ModelElement createRelationshipModelElement(String newDataDelFlag,
                                                      String modelVersionId,String
                                                          modelInvariantId) {
    ModelElement relationshipModelElement = new ModelElement();
    relationshipModelElement.setNewDataDelFlag(newDataDelFlag); //Set new-data-del-flag value
    relationshipModelElement.setCardinality("unbounded");
    RelationshipList relationShipList = new RelationshipList();
    final List<Relationship> relationships = relationShipList.getRelationship();
    Relationship relationship = new Relationship();
    relationship.setRelatedTo("model-ver");
    List<RelationshipData> relationshipDataList = relationship.getRelationshipData();

    RelationshipData modelVersionRelationshipData = new RelationshipData();
    modelVersionRelationshipData.setRelationshipKey("model-ver.model-version-id");
    modelVersionRelationshipData.setRelationshipValue(
        modelVersionId);  //Set the widget/resource name-version-uuid as value
    relationshipDataList.add(modelVersionRelationshipData);
    RelationshipData modelInvariantRelationshipData = new RelationshipData();
    modelInvariantRelationshipData.setRelationshipKey("model.model-invariant-id");
    modelInvariantRelationshipData.setRelationshipValue(
        modelInvariantId);
    relationshipDataList.add(modelInvariantRelationshipData);
    relationships.add(relationship);
    relationshipModelElement.setRelationshipList(relationShipList);
    relationshipModelElement.setModelElements(new ModelElements());
    return relationshipModelElement;
  }

  /**
   * Method to create the child model elements of the widget. Handles the generation of recursive
   * child widget elements (if any)
   *
   * @param parent            Reference to the parent widget model element
   * @param widgetChildrenSet Set of children obtained from the tosca/widget definition
   */
  private void generateWidgetChildren(ModelElement parent, Set<Widget> widgetChildrenSet) {
    for (Widget widget : widgetChildrenSet) {
      Set<Widget> widgetSubChildren = widget.getWidgets();
      if (widgetSubChildren != null && !widgetSubChildren.isEmpty()) {
        ModelElement widgetChildRelationshipElement =
            createRelationshipModelElement(getNewDataDelFlagValue(widget.getDeleteFlag()),
                widget.getId(),widget.getWidgetId());
        //Recursive call for getting the children of widgets (if any)
        generateWidgetChildren(widgetChildRelationshipElement, widgetSubChildren);
        parent.getModelElements().getModelElement().add(widgetChildRelationshipElement);
      } else {
        ModelElement widgetChildRelationshipElement =
            createRelationshipModelElement(getNewDataDelFlagValue(widget.getDeleteFlag()),
                widget.getId(),widget.getWidgetId()
            );
        parent.getModelElements().getModelElement().add(widgetChildRelationshipElement);
      }
    }
  }

  /**
   * Converts the data delete flag value from boolean to String as per AAI model.
   *
   * @param delFlag Boolean value as true/false from the annotation
   * @return Converted value to a flag as per AAI model
   */
  private String getNewDataDelFlagValue(boolean delFlag) {
    if (delFlag) {
      return "T";
    } else {
      return "F";
    }
  }

  /**
   * JAXB marshalling helper method to convert the Java object model to XML String.
   *
   * @param model Java Object model of a service/widget/resource
   * @return XML representation of the Java model in String format
   */
  private String getModelAsString(Model model) {
    JAXBContext jaxbContext;
    StringWriter modelStringWriter = new StringWriter();
    try {
      jaxbContext = JAXBContext.newInstance(Model.class);
      Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
      jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "US-ASCII");
      jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
      jaxbMarshaller.marshal(model, modelStringWriter);
    } catch (JAXBException jaxbException) {
      //jaxbException.printStackTrace();
      log.error(jaxbException.getMessage());
      throw new DOMException(DOMException.SYNTAX_ERR, jaxbException.getMessage());
    }

    //System.out.println(modelStringWriter.toString());
    return modelStringWriter.toString();
  }
}
