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

package org.openecomp.sdc.generator.aai.model;

import org.openecomp.sdc.generator.aai.types.Cardinality;
import org.openecomp.sdc.generator.aai.types.ModelType;
import org.openecomp.sdc.generator.data.GeneratorConstants;
import org.openecomp.sdc.generator.error.IllegalAccessException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public abstract class Model {

  protected Set<Resource> resources = new HashSet<>();
  protected Set<Widget> widgets = new HashSet<>();
  private String modelId;
  private String modelName;
  private ModelType modelType;
  private String modelVersion;
  private String modelNameVersionId;
  private String modelDescription;

  /**
   * Gets model for.
   *
   * @param toscaType the tosca type
   * @return the model for
   */
  public static Model getModelFor(String toscaType) {

    Model modelToBeReturned = null;
    while (toscaType != null && toscaType.lastIndexOf(".") != -1 && modelToBeReturned == null) {

      switch (toscaType) {

        case "org.openecomp.resource.vf.allottedResource":
          modelToBeReturned = new AllotedResource();
          break;
        case "org.openecomp.resource.vfc.AllottedResource":
          modelToBeReturned = new ProvidingService();
          break;
        case "org.openecomp.resource.vfc":
          modelToBeReturned = new VServerWidget();
          break;
        case "org.openecomp.resource.cp":
        case "org.openecomp.cp":
          modelToBeReturned = new LIntfWidget();
          break;
        case "org.openecomp.resource.vl":
          modelToBeReturned = new L3Network();
          break;
        case "org.openecomp.resource.vf":
          modelToBeReturned = new VirtualFunction();
          break;
        case "org.openecomp.groups.vfmodule":
        case "org.openecomp.groups.VfModule":
          modelToBeReturned = new VfModule();
          break;
        case "org.openecomp.resource.vfc.nodes.heat.cinder":
          modelToBeReturned = new VolumeWidget();
          break;
        case "org.openecomp.resource.pnf":
          modelToBeReturned = new PnfResource();
          break;
        default:
          modelToBeReturned = null;
          break;
      }

      toscaType = toscaType.substring(0, toscaType.lastIndexOf("."));
    }

    return modelToBeReturned;
  }

  public abstract boolean addResource(Resource resource);

  public abstract boolean addWidget(Widget resource);

  /**
   * Gets widget version id.
   *
   * @return the widget version id
   */
  public String getWidgetId() {
    org.openecomp.sdc.generator.aai.types.Model model =
        this.getClass().getAnnotation(org.openecomp.sdc.generator.aai.types.Model.class);
    return Widget.getWidget(model.widget()).getId();
  }

  /**
   * Gets invariant id.
   *
   * @return the invariant id
   */
  public String getWidgetInvariantId() {
    org.openecomp.sdc.generator.aai.types.Model model =
        this.getClass().getAnnotation(org.openecomp.sdc.generator.aai.types.Model.class);
    return Widget.getWidget(model.widget()).getWidgetId();
  }

  /**
   * Gets delete flag.
   *
   * @return the delete flag
   */
  public boolean getDeleteFlag() {
    org.openecomp.sdc.generator.aai.types.Model model =
        this.getClass().getAnnotation(org.openecomp.sdc.generator.aai.types.Model.class);
    return model.dataDeleteFlag();
  }

  /**
   * Gets cardinality.
   *
   * @return the cardinality
   */
  public Cardinality getCardinality() {
    org.openecomp.sdc.generator.aai.types.Model model =
        this.getClass().getAnnotation(org.openecomp.sdc.generator.aai.types.Model.class);
    return model.cardinality();
  }

  public abstract Widget.Type getWidgetType();

  public String getModelId() {
    checkSupported();
    return modelId;
  }

  /**
   * Gets model type.
   *
   * @return the model type
   */
  public ModelType getModelType() {
    //checkSupported();
    if (this instanceof Service) {
      return ModelType.SERVICE;
    } else if (this instanceof Resource) {
      return ModelType.RESOURCE;
    } else if (this instanceof Widget) {
      return ModelType.WIDGET;
    } else {
      return null;
    }
  }

  public String getModelName() {
    //checkSupported();
    return modelName;
  }

  public String getModelVersion() {
    //checkSupported();
    return modelVersion;
  }

  public String getModelNameVersionId() {
    checkSupported();
    return modelNameVersionId;
  }

  public String getModelDescription() {
    //checkSupported();
    return modelDescription;
  }

  /**
   * Populate model identification information.
   *
   * @param modelIdentInfo the model ident info
   */
  public void populateModelIdentificationInformation(Map<String, String> modelIdentInfo) {
    Iterator<String> iter = modelIdentInfo.keySet().iterator();
    String property;
    while (iter.hasNext()) {
      switch (property = iter.next()) {

        case "vfModuleModelInvariantUUID":
        case "serviceInvariantUUID":
        case "resourceInvariantUUID":
        case "invariantUUID":
        case "providing_service_invariant_uuid":
          modelId = modelIdentInfo.get(property);
          break;
        case "vfModuleModelUUID":
        case "resourceUUID":
        case "serviceUUID":
        case "UUID":
        case "providing_service_uuid":
          modelNameVersionId = modelIdentInfo.get(property);
          break;
        case "vfModuleModelVersion":
        case "serviceVersion":
        case "resourceversion":
        case "version":
          modelVersion = modelIdentInfo.get(property);
          break;
        case "vfModuleModelName":
        case "serviceName":
        case "resourceName":
        case "name":
          modelName = modelIdentInfo.get(property);
          break;
        case "serviceDescription":
        case "resourceDescription":
        case "vf_module_description":
        case "description":
          modelDescription = modelIdentInfo.get(property);
          break;
        case "providing_service_name":
          modelName = modelIdentInfo.get(property);
          modelDescription = modelIdentInfo.get(property);
          break;
        default:
          break;
      }
      property = null;
    }



  }

  public Set<Resource> getResources() {
    return resources;
  }

  public Set<Widget> getWidgets() {
    return widgets;
  }

  private void checkSupported() throws IllegalAccessException {
    if (this instanceof Widget) {
      throw new IllegalAccessException(GeneratorConstants
          .GENERATOR_AAI_ERROR_UNSUPPORTED_WIDGET_OPERATION);
    }
  }

}
