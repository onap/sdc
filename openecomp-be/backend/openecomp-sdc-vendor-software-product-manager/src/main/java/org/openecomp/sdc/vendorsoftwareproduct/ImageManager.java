/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
package org.openecomp.sdc.vendorsoftwareproduct;

import java.util.Collection;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ImageManager {

    ImageEntity createImage(ImageEntity imageEntity);

    CompositionEntityResponse<Image> getImageSchema(String vspId);

    Collection<ImageEntity> listImages(String vspId, Version version, String componentId);

    CompositionEntityResponse<Image> getImage(String vspId, Version version, String componentId, String imageId);

    QuestionnaireResponse getImageQuestionnaire(String vspId, Version version, String componentId, String imageId);

    void deleteImage(String vspId, Version version, String componentId, String imageId);

    CompositionEntityValidationData updateImage(ImageEntity imageEntity);

    void updateImageQuestionnaire(String vspId, Version version, String componentId, String imageId, String questionnaireData);
}
