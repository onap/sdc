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
import UUID from 'uuid-js';

export const actionTypes = {
    ADD_NOTIFICATION: 'ADD_NOTIFICATION',
    REMOVE_NOTIFICATION: 'REMOVE_NOTIFICATION'
};

export const notificationActions = {
    showNotification: item => ({
        type: actionTypes.ADD_NOTIFICATION,
        payload: {
            ...item,
            id: UUID.create().toString()
        }
    }),

    showSuccess: ({ title, message, timeout }) =>
        notificationActions.showNotification({
            title,
            message,
            timeout,
            type: 'success'
        }),
    showInfo: ({ title, message, timeout }) =>
        notificationActions.showNotification({
            title,
            message,
            timeout,
            type: 'info'
        }),
    showWarning: ({ title, message, timeout }) =>
        notificationActions.showNotification({
            title,
            message,
            timeout,
            type: 'warning'
        }),
    showError: ({ title, message, timeout }) =>
        notificationActions.showNotification({
            title,
            message,
            timeout,
            type: 'error'
        }),
    removeNotification: item => ({
        type: actionTypes.REMOVE_NOTIFICATION,
        payload: item
    })
};

export const notificationTimeout = 4000;
