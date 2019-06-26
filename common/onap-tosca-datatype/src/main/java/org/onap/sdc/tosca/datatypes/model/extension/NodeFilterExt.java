package org.onap.sdc.tosca.datatypes.model.extension;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.onap.sdc.tosca.datatypes.model.NodeFilter;

@Getter
@Setter
@EqualsAndHashCode
public class NodeFilterExt extends NodeFilter {

    private Object tosca_id;
}
