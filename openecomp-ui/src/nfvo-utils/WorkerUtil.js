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

import Configuration from 'sdc-app/config/Configuration.js';
import { applySecurity } from 'nfvo-utils/RestAPIUtil.js';
import store from 'sdc-app/AppStore.js';
import { actionTypes } from 'sdc-app/onboarding/userNotifications/UserNotificationsConstants.js';

import Worker from 'nfvo-utils/Notifications.worker.js';

class WorkerUtil {
    open() {
        this.worker = new Worker();

        const url = `${Configuration.get('restPrefix')}/v1.0/notifications`;
        const notificationsWorkerUpdateMillisecond = Configuration.get(
            'notificationsWorkerUpdateMillisecond'
        );
        const options = {};

        applySecurity(options);

        const config = {
            method: 'GET',
            url: url,
            headers: options.headers,
            data: null
        };

        this.worker.postMessage({
            config,
            notificationsWorkerUpdateMillisecond
        });

        this.worker.onmessage = event => {
            const result = event.data;
            store.dispatch({
                type: actionTypes.LOAD_NOTIFICATIONS,
                result
            });
        };
    }

    close() {
        if (this.worker !== undefined) {
            this.worker.terminate();
        }
    }
}

export default WorkerUtil;
