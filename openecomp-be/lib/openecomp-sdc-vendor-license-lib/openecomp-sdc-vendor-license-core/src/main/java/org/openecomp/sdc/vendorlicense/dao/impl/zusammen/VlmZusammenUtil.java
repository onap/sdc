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
package org.openecomp.sdc.vendorlicense.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.Id;
import com.amdocs.zusammen.datatypes.item.Relation;
import com.amdocs.zusammen.datatypes.item.RelationEdge;
import java.util.stream.Collectors;

public class VlmZusammenUtil {

    static ZusammenElement getZusammenElement(ElementInfo elementInfo) {
        ZusammenElement zusammenElement = new ZusammenElement();
        zusammenElement.setElementId(elementInfo.getId());
        zusammenElement.setInfo(elementInfo.getInfo());
        zusammenElement.setRelations(elementInfo.getRelations());
        zusammenElement.setSubElements(elementInfo.getSubElements().stream().map(VlmZusammenUtil::getZusammenElement).collect(Collectors.toList()));
        return zusammenElement;
    }

    public static Relation createRelation(RelationType type, String to) {
        Relation relation = new Relation();
        relation.setType(type.name());
        RelationEdge edge2 = new RelationEdge();
        edge2.setElementId(new Id(to));
        relation.setEdge2(edge2);
        return relation;
    }

    public static Integer toInteger(Object val) {
        if (val == null) {
            return null;
        }
        if (val instanceof Double) {
            return ((Double) val).intValue();
        } else if (val instanceof String) {
            return new Integer((String) val);
        } else if (val instanceof Integer) {
            return (Integer) val;
        }
        throw new RuntimeException("invalid value for integer:" + val.getClass());
    }
}
