package org.openecomp.sdc.vendorsoftwareproduct;

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ImageEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ImageManager {
  ImageEntity createImage(ImageEntity imageEntity, String user);

  CompositionEntityResponse<Image> getImageSchema(String vspId, String user);

  Collection<ImageEntity> listImages(String vspId, Version version, String componentId,
                                     String user);

  CompositionEntityResponse<Image> getImage(String vspId, Version version, String componentId,
                                            String imageId, String user);

  QuestionnaireResponse getImageQuestionnaire(String vspId, Version version, String
      componentId, String imageId, String user);

  void deleteImage(String vspId, Version version, String componentId, String imageId, String user);

  CompositionEntityValidationData updateImage(ImageEntity imageEntity, String user);

  void updateImageQuestionnaire(String vspId, Version version, String componentId, String imageId,
                                String
      questionnaireData, String user);
}
