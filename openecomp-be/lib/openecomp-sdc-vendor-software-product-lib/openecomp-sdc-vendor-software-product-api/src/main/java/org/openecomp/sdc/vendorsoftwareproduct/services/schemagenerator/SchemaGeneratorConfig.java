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

package org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfig;
import org.openecomp.core.utilities.applicationconfig.ApplicationConfigFactory;
import org.openecomp.core.utilities.applicationconfig.type.ConfigurationData;
import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.common.errors.ErrorCategory;
import org.openecomp.sdc.common.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SchemaGeneratorConfig {
  public static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR =
      "SCHEMA_GENERATOR_INITIALIZATION_ERROR";
  public static final String SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG =
      "Error occurred while loading questionnaire schema schemaTemplates";
  private static final String CONFIGURATION_NAMESPACE = "vsp.schemaTemplates";
  private static Map<SchemaTemplateId, SchemaTemplate> schemaTemplates = new HashMap<>();
  private static ApplicationConfig applicationConfig =
      ApplicationConfigFactory.getInstance().createInterface();

  private static Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
  private static StringTemplateLoader stringLoader = new StringTemplateLoader();

  static {
    configuration.setClassLoaderForTemplateLoading(SchemaGenerator.class.getClassLoader(),
        File.pathSeparator);
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setLogTemplateExceptions(true);
    configuration.setTemplateLoader(stringLoader);
  }

  public static void insertSchemaTemplate(SchemaTemplateContext schemaTemplateContext,
                                          CompositionEntityType entityType,
                                          String schemaTemplateString) {
    applicationConfig.insertValue(CONFIGURATION_NAMESPACE,
        new SchemaTemplateId(schemaTemplateContext, entityType).toString(), schemaTemplateString);
  }

  /**
   * Gets schema template.
   *
   * @param schemaTemplateContext the schema template context
   * @param entityType            the entity type
   * @return the schema template
   */
  public static Template getSchemaTemplate(SchemaTemplateContext schemaTemplateContext,
                                           CompositionEntityType entityType) {
    SchemaTemplateId id = new SchemaTemplateId(schemaTemplateContext, entityType);
    ConfigurationData configurationData =
        applicationConfig.getConfigurationData(CONFIGURATION_NAMESPACE, id.toString());

    SchemaTemplate schemaTemplate = schemaTemplates.get(id);
    if (schemaTemplate == null || schemaTemplate.timestamp != configurationData.getTimeStamp()) {
      stringLoader.putTemplate(id.toString(), configurationData.getValue());
      Template template;
      try {
        template = configuration.getTemplate(id.toString());
      } catch (IOException exception) {
        throw new CoreException(
            new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION)
                .withId(SCHEMA_GENERATOR_INITIALIZATION_ERROR)
                .withMessage(SCHEMA_GENERATOR_INITIALIZATION_ERROR_MSG).build(), exception);
      }
      schemaTemplate = new SchemaTemplate(template, configurationData.getTimeStamp());
      schemaTemplates.put(id, schemaTemplate);
    }
    return schemaTemplate.template;
  }

  private static class SchemaTemplateId {
    private SchemaTemplateContext context;
    private CompositionEntityType entityType;

    public SchemaTemplateId(SchemaTemplateContext context, CompositionEntityType entityType) {
      this.context = context;
      this.entityType = entityType;
    }

    @Override
    public String toString() {
      return context + "." + entityType;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }

      SchemaTemplateId that = (SchemaTemplateId) obj;

      if (entityType != that.entityType) {
        return false;
      }
      if (context != that.context) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      int result = entityType != null ? entityType.hashCode() : 0;
      result = 31 * result + (context != null ? context.hashCode() : 0);
      return result;
    }
  }

  private static class SchemaTemplate {
    private Template template;
    private long timestamp;

    public SchemaTemplate(Template template, long timestamp) {
      this.template = template;
      this.timestamp = timestamp;
    }
  }

}
