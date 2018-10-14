/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import axios from 'axios';

let prevLastScanned;
let updateNotificationsTimeout;
let config;
let notificationsWorkerUpdateMillisecond;

function updateNotifications() {
    axios(config)
        .then(response => {
            const notifications = response.data;

            let lastScanned = notifications.lastScanned;
            if (prevLastScanned !== lastScanned) {
                postMessage(notifications);
            }
            prevLastScanned = lastScanned;
        })
        .catch(() => {});

    updateNotificationsTimeout = setTimeout(
        updateNotifications,
        notificationsWorkerUpdateMillisecond
    );
}

navigator.connection.onchange = () => {
    clearTimeout(updateNotificationsTimeout);

    if (navigator.onLine && config && notificationsWorkerUpdateMillisecond) {
        updateNotifications();
    }
};

onmessage = ({ data }) => {
    if (data && data.config && data.notificationsWorkerUpdateMillisecond) {
        config = data.config;
        notificationsWorkerUpdateMillisecond =
            data.notificationsWorkerUpdateMillisecond;

        updateNotifications();
    }
};
