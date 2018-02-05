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

    package org.openecomp.sdc.tosca.datatypes.model.heatextend;

    import org.openecomp.sdc.tosca.datatypes.model.PropertyDefinition;

    import java.util.Map;

    public class AnnotationType {

      private String version;
      private String description;
      private Map<String, PropertyDefinition> properties;

      /**
       * Gets version.
       *
       * @return the version
       */
      public String getVersion() {
        return version;
      }

      /**
       * Sets version.
       *
       * @param version the version
       */
      public void setVersion(String version) {
        this.version = version;
      }

      /**
       * Gets description.
       *
       * @return the description
       */
      public String getDescription() {
        return description;
      }

      /**
       * Sets description.
       *
       * @param description the description
       */
      public void setDescription(String description) {
        this.description = description;
      }

      /**
       * Gets properties.
       *
       * @return the properties
       */
      public Map<String, PropertyDefinition> getProperties() {
        return properties;
      }

      /**
       * Sets properties.
       *
       * @param properties the properties
       */
      public void setProperties(Map<String, PropertyDefinition> properties) {
        this.properties = properties;
      }

    }