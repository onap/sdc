/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.sdc.activityspec.be.dao.impl;

import com.amdocs.zusammen.adaptor.inbound.api.types.item.Element;
import com.amdocs.zusammen.adaptor.inbound.api.types.item.ZusammenElement;
import com.amdocs.zusammen.datatypes.SessionContext;
import com.amdocs.zusammen.datatypes.item.Action;
import com.amdocs.zusammen.datatypes.item.ElementContext;
import com.amdocs.zusammen.datatypes.item.Info;
import org.onap.sdc.activityspec.be.dao.ActivitySpecDao;
import org.onap.sdc.activityspec.be.dao.types.ActivitySpecEntity;
import org.onap.sdc.activityspec.be.datatypes.ActivitySpecData;
import org.onap.sdc.activityspec.be.datatypes.ElementType;
import org.onap.sdc.activityspec.utils.ActivitySpecConstant;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.core.zusammen.api.ZusammenAdaptor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.core.zusammen.api.ZusammenUtil.buildStructuralElement;
import static org.openecomp.core.zusammen.api.ZusammenUtil.createSessionContext;

public class ActivitySpecDaoZusammenImpl implements ActivitySpecDao {

    private final ZusammenAdaptor zusammenAdaptor;

    public ActivitySpecDaoZusammenImpl(ZusammenAdaptor zusammenAdaptor) {
        this.zusammenAdaptor = zusammenAdaptor;
    }

    @Override
    public void create(ActivitySpecEntity entity) {
        SessionContext context = createSessionContext();
        ZusammenElement generalElement = mapActivityDetailsToZusammenElement(entity, Action.CREATE);

        ElementContext elementContext = new ElementContext(entity.getId(), entity.getVersion().getId());
        zusammenAdaptor
                .saveElement(context, elementContext, generalElement, "Create Activity Spec General Info Element");
    }

    @Override
    public ActivitySpecEntity get(ActivitySpecEntity entity) {
        SessionContext context = createSessionContext();

        ElementContext elementContext = new ElementContext(entity.getId(), entity.getVersion().getId());
        Optional<Element> element =
                zusammenAdaptor.getElementByName(context, elementContext, null, ElementType.ACTIVITYSPEC.name());
        if (element.isPresent()) {
            return mapZusammenElementToActivityDetails(element.get());
        } else {
            return null;
        }
    }

    @Override
    public void update(ActivitySpecEntity entity) {
        SessionContext context = createSessionContext();
        ZusammenElement generalElement = mapActivityDetailsToZusammenElement(entity, Action.UPDATE);

        ElementContext elementContext = new ElementContext(entity.getId(), entity.getVersion().getId());
        zusammenAdaptor
                .saveElement(context, elementContext, generalElement, "Update Activity Spec General Info Element");
    }


    private ActivitySpecEntity mapZusammenElementToActivityDetails(Element element) {
        ActivitySpecEntity entity = new ActivitySpecEntity();
        entity.setId(element.getElementId().getValue());
        enrichEntityFromElementData(entity, element.getData());
        enrichEntityFromElementInfo(entity, element.getInfo());
        return entity;
    }

    private ZusammenElement mapActivityDetailsToZusammenElement(ActivitySpecEntity entity, Action action) {
        ZusammenElement generalElement = buildStructuralElement(ElementType.ACTIVITYSPEC.name(), action);

        enrichElementInfoFromEntity(generalElement, entity);
        enrichElementDataFromEntity(generalElement, entity);
        return generalElement;
    }


    private void enrichEntityFromElementInfo(ActivitySpecEntity entity, Info info) {
        entity.setName(info.getProperty(InfoPropertyName.NAME.getValue()));
        entity.setDescription(info.getProperty(InfoPropertyName.DESCRIPTION.getValue()));
        entity.setCategoryList(info.getProperty(InfoPropertyName.CATEGORY.getValue()));
    }

    private void enrichEntityFromElementData(ActivitySpecEntity entity, InputStream data) {
        ActivitySpecData activitySpecData = JsonUtil.json2Object(data, ActivitySpecData.class);
        if (Objects.nonNull(activitySpecData)) {
            entity.setInputs(activitySpecData.getInputs());
            entity.setOutputs(activitySpecData.getOutputs());
            entity.setType(activitySpecData.getType());
            entity.setContent(activitySpecData.getContent());
        }
    }

    private void enrichElementInfoFromEntity(ZusammenElement element, ActivitySpecEntity entity) {
        element.getInfo().addProperty(InfoPropertyName.DESCRIPTION.getValue(), entity.getDescription());
        element.getInfo().addProperty(InfoPropertyName.NAME.getValue(), entity.getName());
        element.getInfo().addProperty(InfoPropertyName.CATEGORY.getValue(), entity.getCategoryList());
    }


    private void enrichElementDataFromEntity(ZusammenElement element, ActivitySpecEntity entity) {
        ActivitySpecData activitySpecData = new ActivitySpecData();
        activitySpecData.setInputs(entity.getInputs());
        activitySpecData.setOutputs(entity.getOutputs());
        activitySpecData.setType(entity.getType());
        activitySpecData.setContent(entity.getContent());
        element.setData(new ByteArrayInputStream(JsonUtil.object2Json(activitySpecData).getBytes()));
    }

    public enum InfoPropertyName {
        DESCRIPTION("description"), NAME("name"), CATEGORY(ActivitySpecConstant.CATEGORY_ATTRIBUTE_NAME);

        private final String value;

        InfoPropertyName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

}
