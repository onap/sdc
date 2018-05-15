package org.openecomp.sdc.vendorsoftwareproduct.types.schemagenerator;

import org.openecomp.sdc.vendorsoftwareproduct.types.composition.Image;

public class ImageCompositionSchemaInput implements SchemaTemplateInput {

  private boolean manual;
  private Image image;

  public boolean isManual() {
    return manual;
  }

  public void setManual(boolean manual) {
    this.manual = manual;
  }

  public Image getImage() {
    return image;
  }

  public void setImage(Image image) {
    this.image = image;
  }




}
