package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ImageManager {
  ImageEntity createImage(ImageEntity imageEntity);

  CompositionEntityResponse<Image> getImageSchema(String vspId);

  Collection<ImageEntity> listImages(String vspId, Version version, String componentId);

  CompositionEntityResponse<Image> getImage(String vspId, Version version, String componentId,
                                            String imageId);

  QuestionnaireResponse getImageQuestionnaire(String vspId, Version version, String componentId,
                                              String imageId);

  void deleteImage(String vspId, Version version, String componentId, String imageId);

  CompositionEntityValidationData updateImage(ImageEntity imageEntity);

  void updateImageQuestionnaire(String vspId, Version version, String componentId, String imageId,
                                String questionnaireData);
}
