/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.be.components.utils;

import org.mockito.Mockito;
import org.openecomp.sdc.be.components.impl.ArtifactsBusinessLogic;
import org.openecomp.sdc.be.components.impl.BaseBusinessLogicMock;
import org.openecomp.sdc.be.components.validation.component.ComponentContactIdValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentDescriptionValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentIconValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentNameValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentProjectCodeValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentTagsValidator;
import org.openecomp.sdc.be.components.validation.component.ComponentValidator;

public abstract class ComponentBusinessLogicMock extends BaseBusinessLogicMock {

    protected final ArtifactsBusinessLogic artifactsBusinessLogic = Mockito.mock(ArtifactsBusinessLogic.class);
    protected final ComponentTagsValidator componentTagsValidator = Mockito.mock(ComponentTagsValidator.class);
    protected final ComponentValidator componentValidator = Mockito.mock(ComponentValidator.class);
    protected final ComponentIconValidator componentIconValidator = Mockito.mock(ComponentIconValidator.class);
    protected final ComponentProjectCodeValidator componentProjectCodeValidator = Mockito.mock(ComponentProjectCodeValidator.class);
    protected final ComponentDescriptionValidator componentDescriptionValidator = Mockito.mock(ComponentDescriptionValidator.class);
    protected final ComponentContactIdValidator componentContactIdValidator = Mockito.mock(ComponentContactIdValidator.class);
    protected final ComponentNameValidator componentNameValidator = Mockito.mock(ComponentNameValidator.class);

}
