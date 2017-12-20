/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.notification.workers;

import com.datastax.driver.core.utils.UUIDs;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openecomp.core.utilities.json.JsonUtil;
import org.openecomp.sdc.notification.types.NotificationEntityDto;
import org.openecomp.sdc.notification.types.NotificationsStatusDto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

//import org.junit.Before;


public class NotificationWorkerTest {

    private static final String OWNER = "owner_1";

    private String fileName = null;
    private NewNotificationsReader news = this.new SimNewNotificationsReader();
    private NotificationWorker worker = new NotificationWorker(news);

    @BeforeClass
    public static void beforeClass() throws Exception {
    }

    @Test
    public void testBasicResourceCreation() throws IOException, InterruptedException {

        Consumer<NotificationsStatusDto> notesProcessor = this::notifyReceiver;

        fileName = "notification_1.csv";

        worker.register(OWNER, null, notesProcessor);
        worker.register("owner_2", null, notesProcessor);
        worker.register("owner_3", null, notesProcessor);

        int pollInterval = 2000;
        Thread.sleep(pollInterval);

        worker.unregister("owner_2");

        fileName = "notification_2.csv";

        Thread.sleep(pollInterval);

        worker.stopPolling();

    }

    private void notifyReceiver(NotificationsStatusDto notes) {
        if (Objects.nonNull(notes)) {
            System.out.println("Received notes:");
            System.out.println(notes);
        }
    }


    private class SimNewNotificationsReader implements NewNotificationsReader {


        private String resourcesDir = "src/test/resources/";

        public NotificationsStatusDto getNewNotifications(String ownerId, UUID eventId, int limit) {
            if (fileName == null) {
                return null;
            }
            String fn = fileName;
            fileName = null;

            return getNotifications(fn);
        }

        private NotificationsStatusDto getNotifications(String fn) {
            NotificationsStatusDto notificationsStatusDto = new NotificationsStatusDto();
            List<NotificationEntityDto> inputList = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(resourcesDir + fn))))) {
                int limit = 10;
                inputList = br.lines().skip(1).limit(limit).map(mapToEntity).collect(Collectors.toCollection(ArrayList::new));
            } catch (IOException e) {
                System.err.println("getNotifications(): file " + resourcesDir + fn + " open exception: " + e.getMessage());
            }
            notificationsStatusDto.setNotifications(inputList);
            notificationsStatusDto.setLastScanned(inputList.get(0).getEventId());
            return notificationsStatusDto;
        }

        private Function<String, NotificationEntityDto> mapToEntity = (line) -> {
            String[] p = line.split("\\|");
            NotificationEntityDto entity = new NotificationEntityDto();
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
            entity.setRead(Boolean.parseBoolean(p[1]));
            entity.setEventId(UUID.fromString(p[2]));
            entity.setEventType(p[4]);
            entity.setDateTime(formatter.format(UUIDs.unixTimestamp(entity.getEventId())));
            entity.setEventAttributes(JsonUtil.json2Object(p[5], Map.class));
            return entity;
        };
    }

}
