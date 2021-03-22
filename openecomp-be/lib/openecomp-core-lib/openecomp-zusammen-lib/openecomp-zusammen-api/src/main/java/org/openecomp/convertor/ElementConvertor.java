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
package org.openecomp.convertor;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ElementInfo;
import com.amdocs.zusammen.datatypes.item.Item;
import com.amdocs.zusammen.datatypes.item.ItemVersion;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.types.ElementPropertyName;

public abstract class ElementConvertor<T> {

    public static ElementType getElementType(Element element) {
        return ElementType.valueOf(element.getInfo().getProperty(ElementPropertyName.elementType.name()));
    }

    public static String getElementName(Element element) {
        return element.getInfo().getName();
    }

    abstract public T convert(Element element);

    public T convert(ElementInfo elementInfo) {
        throw new UnsupportedOperationException("convert elementInfo item is not supported ");
    }

    public T convert(Item item) {
        throw new UnsupportedOperationException("convert from item is not supported ");
    }

    public T convert(ItemVersion itemVersion) {
        throw new UnsupportedOperationException("convert from itemVersion is not supported ");
    }
}
