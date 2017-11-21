package org.openecomp.sdc.generator.aai.model;

import org.openecomp.sdc.generator.aai.types.Cardinality;

@org.openecomp.sdc.generator.aai.types.Model(widget = Widget.Type.PNF, cardinality = Cardinality
    .UNBOUNDED,
    dataDeleteFlag = true)
public class PnfResource extends Resource {
}
