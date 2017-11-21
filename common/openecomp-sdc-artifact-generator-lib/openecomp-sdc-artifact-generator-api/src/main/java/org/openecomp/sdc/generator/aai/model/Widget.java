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

import static org.openecomp.sdc.generator.data.GeneratorConstants.GENERATOR_AAI_CONFIGLPROP_NOT_FOUND;

import org.openecomp.sdc.generator.aai.types.ModelType;
import org.openecomp.sdc.generator.aai.types.ModelWidget;
import org.openecomp.sdc.generator.data.ArtifactType;
import org.openecomp.sdc.generator.data.GeneratorConstants;
import org.openecomp.sdc.generator.data.WidgetConfigurationUtil;
import org.openecomp.sdc.generator.error.IllegalAccessException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public abstract class Widget extends Model {

  private Set<String> keys = new HashSet<>();

  /**
   * Gets widget.
   *
   * @param type the type
   * @return the widget
   */
  public static Widget getWidget(Type type) {

    switch (type) {
      case SERVICE:
        return new ServiceWidget();
      case VF:
        return new VfWidget();
      case VFC:
        return new VfcWidget();
      case VSERVER:
        return new VServerWidget();
      case VOLUME:
        return new VolumeWidget();
      case FLAVOR:
        return new FlavorWidget();
      case TENANT:
        return new TenantWidget();
      case VOLUME_GROUP:
        return new VolumeGroupWidget();
      case LINT:
        return new LIntfWidget();
      case L3_NET:
        return new L3NetworkWidget();
      case VFMODULE:
        return new VfModuleWidget();
      case IMAGE:
        return new ImageWidget();
      case OAM_NETWORK:
        return new OamNetwork();
      case ALLOTTED_RESOURCE:
        return new AllotedResourceWidget();
      case TUNNEL_XCONNECT:
        return new TunnelXconnectWidget();
      case PNF:
        return new PnfWidget();
      default:
        return null;
    }

  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    Properties properties = WidgetConfigurationUtil.getConfig();
    String id = properties.getProperty(ArtifactType.AAI.name() + ".model-version-id." + getName());
    if (id == null) {
      throw new IllegalArgumentException(String.format(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND,
          ArtifactType.AAI.name() + ".model-version-id." + getName()));
    }
    return id;
  }

  public ModelType getType() {
    ModelWidget widgetModel = this.getClass().getAnnotation(ModelWidget.class);
    return widgetModel.type();
  }

  public String getName() {
    ModelWidget widgetModel = this.getClass().getAnnotation(ModelWidget.class);
    return widgetModel.name();
  }

  /**
   * Get Widget Id from properties file.
   * @return - Widget Id
   */
  public String getWidgetId() {
    Properties properties = WidgetConfigurationUtil.getConfig();
    String id = properties.getProperty(ArtifactType.AAI.name() + ".model-invariant-id."
        + getName());
    if (id == null) {
      throw new IllegalArgumentException(String.format(GENERATOR_AAI_CONFIGLPROP_NOT_FOUND,
          ArtifactType.AAI.name() + ".model-invariant-id." + getName()));
    }
    return id;
  }

  public int hashCode() {
    return getId().hashCode();
  }

  @Override
  public Type getWidgetType() {
    return null;
  }

  /**
   * Equals.
   *
   * @param obj Object
   * @return the boolean
   */
  public boolean equals(Object obj) {
    if (obj instanceof Widget) {
      if (getId().equals(((Widget) obj).getId())) {
        ((Widget) obj).keys.addAll(this.keys);
        return true;
      }
      return false;
    } else {
      return false;
    }
  }

  public void addKey(String key) {
    this.keys.add(key);
  }

  /**
   * Member of boolean.
   *
   * @param keys the keys
   * @return the boolean
   */
  public boolean memberOf(List<String> keys) {
    if (keys == null) {
      return false;
    }
    return !Collections.disjoint(this.keys, keys);
  }

  /**
   * All instances used boolean.
   *
   * @param collection the collection
   * @return the boolean
   */
  public boolean allInstancesUsed(Set<String> collection) {
    Set<String> keyCopy = new HashSet<>(keys);
    keyCopy.removeAll(collection);
    return keyCopy.isEmpty();
  }

  public boolean addResource(Resource resource) {
    throw new IllegalAccessException(GeneratorConstants
        .GENERATOR_AAI_ERROR_UNSUPPORTED_WIDGET_OPERATION);
  }

  public boolean addWidget(Widget widget) {
    return true;
  }

  public enum Type {
    SERVICE, VF, VFC, VSERVER, VOLUME, FLAVOR, TENANT, VOLUME_GROUP, LINT, L3_NET, VFMODULE, IMAGE,
    OAM_NETWORK,ALLOTTED_RESOURCE,TUNNEL_XCONNECT, PNF
  }
}
