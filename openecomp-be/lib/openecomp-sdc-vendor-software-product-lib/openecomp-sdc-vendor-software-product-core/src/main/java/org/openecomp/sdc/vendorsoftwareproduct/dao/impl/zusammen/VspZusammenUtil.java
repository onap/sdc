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
package org.openecomp.sdc.vendorsoftwareproduct.dao.impl.zusammen;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.io.IOUtils;

class VspZusammenUtil {

    private VspZusammenUtil() {
    }

    static ZusammenElement aggregateElements(ZusammenElement... elements) {
        ZusammenElement head = null;
        ZusammenElement father = null;
        for (ZusammenElement element : elements) {
            if (Objects.isNull(head)) {
                head = father = element;
            } else {
                if (father != null) {
                    father.getSubElements().add(element);
                    father = element;
                }
            }
        }
        return head;
    }

    static boolean hasEmptyData(InputStream elementData) {
        String EMPTY_DATA = "{}";
        byte[] byteElementData;
        try {
            byteElementData = IOUtils.toByteArray(elementData);
        } catch (IOException ex) {
            return false;
        }
        if (Arrays.equals(EMPTY_DATA.getBytes(), byteElementData)) {
            return true;
        }
        return false;
    }
}
