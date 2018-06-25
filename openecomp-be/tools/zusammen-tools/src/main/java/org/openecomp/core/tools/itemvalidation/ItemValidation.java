/*
* Copyright © 2016-2018 European Support Limited
*
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
*/

package org.openecomp.core.tools.itemvalidation;

import org.openecomp.core.tools.store.ElementCassandraLoader;
import org.openecomp.core.tools.store.ItemHandler;
import org.openecomp.core.tools.store.VersionCassandraLoader;
import org.openecomp.core.tools.store.VersionElementsCassandraLoader;
import org.openecomp.core.tools.store.zusammen.datatypes.ElementEntity;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionElementsEntity;
import org.openecomp.core.tools.store.zusammen.datatypes.VersionEntity;
import org.openecomp.core.zusammen.impl.CassandraConnectionInitializer;
import org.openecomp.sdc.common.session.SessionContextProviderFactory;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.openecomp.core.tools.util.Utils.printMessage;
import static org.openecomp.core.tools.util.Utils.logError;

public class ItemValidation {
    private static final Logger logger = LoggerFactory.getLogger(ItemValidation.class);
    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String PUBLIC_SPACE = "public";
    private static String itemId = null;
    private LinkedList<String> subElementsList;
    private LinkedList<String> validationMessage;

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {
                if (args[i+1] != null) {
                    itemId = args[i + 1];
                }
                break;
            }
        }

        if (itemId == null) {
            printMessage(logger, "-i 123456 ");
            System.exit(-1);
        }

        ItemValidation itemValidation = new ItemValidation();
        itemValidation.validate();
        System.exit(1);
    }

    private void validate() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        subElementsList = new LinkedList<>();
        validationMessage = new LinkedList<>();

        try {
            SessionContextProviderFactory.getInstance().createInterface().create("GLOBAL_USER", "dox");
            CassandraConnectionInitializer.setCassandraConnectionPropertiesToSystem();

            List<String> itemList = new ItemHandler().getItemList();
            if (itemList.stream().filter(item -> item.equals(itemId)).collect(Collectors.toList()).isEmpty()) {
                printMessage(logger,String.format("%s No data found for itemId: %s %s", NEW_LINE, itemId, NEW_LINE));
                return;
            }
            printMessage(logger,String.format("%s Validation started at %s %S", NEW_LINE,
                    sdf.format(new Date()), NEW_LINE));

            List<VersionEntity> versionEntityList = new VersionCassandraLoader().list().all().stream().
                    filter(entry -> entry.getItemId().equals(itemId)).collect(Collectors.toList());

            versionEntityList.sort(( VersionEntity e1, VersionEntity e2) -> {
                     if (e1.getSpace().equals(PUBLIC_SPACE)) {
                         return -1;
                     } else if (e2.getSpace().equals(PUBLIC_SPACE)) {
                         return 1;
                     } else {
                         return e1.getSpace().compareTo(e2.getSpace());
                     }
            });

            versionEntityList.forEach((VersionEntity versionEntity) -> {
                List<VersionElementsEntity> versionElementsEntityList =  new VersionElementsCassandraLoader().
                listVersionElementsByPK(versionEntity.getSpace(), versionEntity.getItemId(),
                        versionEntity.getVersionId()).all();
                versionElementsEntityList.forEach(this::accept);
            });
         }catch (Exception ex) {
            logError(logger,ex);
        }
        if (validationMessage.isEmpty()) {
            printMessage(logger,String.format("%s Item %s is successfully validated.", NEW_LINE, itemId));
        } else {
            printMessage(logger,"\n Errors:");
            for (String message : validationMessage) {
                printMessage(logger,message);
            }
        }
        printMessage(logger,String.format("%s Validation ended at %s %s", NEW_LINE, sdf.format(new Date()), NEW_LINE));
    }

    private void validateElement(String space, String itemId, String versionId,
                                 String elementId, String revisionId) {

        ElementEntity elementEntity =
                new ElementCassandraLoader().getByPK(space, itemId, versionId, elementId, revisionId).one();

        if (elementEntity == null) {
            validationMessage.add(String.format(
                    "Element is defined in VERSION_ELEMENTS.element_ids is not found in ELEMENT. " +
                            "Space:%s, ItemID:%s ,VersionID:%s, ElementID:%s, element revisionID:%s",
                    space, itemId, versionId, elementId, revisionId));
            return;
        }

        if (elementEntity.getSubElementIds() != null) {
            subElementsList.addAll(elementEntity.getSubElementIds());
        }
    }

    private void accept(VersionElementsEntity versionElementsEntity) {
        printMessage(logger, String.format(
                "Found VERSION_ELEMENTS entry. Space: %s, ItemID: %s, VersionId: %s, Revision: %s",
                versionElementsEntity.getSpace(),
                versionElementsEntity.getItemId(),
                versionElementsEntity.getVersionId(),
                versionElementsEntity.getRevisionId()));

        subElementsList.clear();
        if (versionElementsEntity.getElementIds().isEmpty()) {
            validationMessage.add(String.format
                    ("Empty field VERSION_ELEMENT.element_ids. Space: %s, ItemID: %s, VersionId: %s, Revision: %s",
                            versionElementsEntity.getSpace(),
                            versionElementsEntity.getItemId(),
                            versionElementsEntity.getVersionId(),
                            versionElementsEntity.getRevisionId()));
        } else {

            //loop over element_ids stored in version_elements
            versionElementsEntity.getElementIds().forEach((key, value) ->
                validateElement(versionElementsEntity.getSpace(),
                        versionElementsEntity.getItemId(),
                        versionElementsEntity.getVersionId(),
                        key,
                        value)
            );

            //loop over collected sub-elements to insure existence in version_elements.elements_ids
            subElementsList.forEach((String key) -> {
                if (!versionElementsEntity.getElementIds().containsKey(key)) {
                    validationMessage.add(String.format(
                            "Element is defined in table ELEMENT but not found in VERSION_ELEMENTS.element_ids."
                                    + "  Space:%s, ItemID:%s, VersionID:%s, Version RevisionID:%s, ElementID:%s",
                            versionElementsEntity.getSpace(),
                            versionElementsEntity.getItemId(),
                            versionElementsEntity.getVersionId(),
                            versionElementsEntity.getRevisionId(),
                            key));
                }
            });
        }
    }
}