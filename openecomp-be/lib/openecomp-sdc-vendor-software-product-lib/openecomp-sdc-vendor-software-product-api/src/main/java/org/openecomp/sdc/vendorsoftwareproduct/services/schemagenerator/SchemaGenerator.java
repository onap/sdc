/*
 * Copyright © 2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorsoftwareproduct.services.schemagenerator;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.openecomp.sdc.errors.CoreException;
import org.openecomp.sdc.errors.ErrorCategory;
import org.openecomp.sdc.errors.ErrorCode;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityType;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateContext;
import org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator.SchemaTemplateInput;

public class SchemaGenerator {

    public static final String SCHEMA_GENERATION_ERROR = "SCHEMA_GENERATION_ERROR";

    private SchemaGenerator() {
        // Utility classes, which are a collection of static members, are not meant to be instantiated
    }

    /**
     * Generate string.
     *
     * @param schemaTemplateContext the schema template context
     * @param entityType            the entity type
     * @param input                 the input
     * @return the string
     */
    public static String generate(SchemaTemplateContext schemaTemplateContext, CompositionEntityType entityType, SchemaTemplateInput input) {
        Template schemaTemplate = SchemaGeneratorConfig.getSchemaTemplate(schemaTemplateContext, entityType);
        return processTemplate(input, schemaTemplate);
    }

    private static String processTemplate(SchemaTemplateInput input, Template schemaTemplate) {
        try (Writer writer = new StringWriter(1024)) {
            schemaTemplate.process(input, writer);
            return writer.toString();
        } catch (IOException | TemplateException exception) {
            throw new CoreException(new ErrorCode.ErrorCodeBuilder().withCategory(ErrorCategory.APPLICATION).withId(SCHEMA_GENERATION_ERROR)
                .withMessage(exception.getMessage()).build(), exception);
        }
    }
}
