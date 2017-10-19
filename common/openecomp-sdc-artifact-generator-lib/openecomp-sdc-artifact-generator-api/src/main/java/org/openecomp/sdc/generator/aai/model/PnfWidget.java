package org.openecomp.sdc.generator.aai.model;

import org.openecomp.sdc.generator.aai.types.Cardinality;
import org.openecomp.sdc.generator.aai.types.ModelType;
import org.openecomp.sdc.generator.aai.types.ModelWidget;

@org.openecomp.sdc.generator.aai.types.Model(widget = Widget.Type.PNF, cardinality
    = Cardinality.UNBOUNDED, dataDeleteFlag = true)
@ModelWidget(type = ModelType.WIDGET, name = "pnf")
public class PnfWidget extends ResourceWidget  {
}
