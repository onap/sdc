package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;

public class ImageCompositionSchemaInput implements SchemaTemplateInput {

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }

  private Image image;


}
