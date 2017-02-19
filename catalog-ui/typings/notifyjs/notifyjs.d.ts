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
// Type definitions for notify.js 1.2.0
// Project: https://github.com/alexgibson/notify.js
// Definitions by: soundTricker <https://github.com/soundTricker>
// Definitions: https://github.com/borisyankov/DefinitelyTyped

declare var Notify: {
    new (title : string , options? : notifyjs.INotifyOption): notifyjs.INotify;

    /**
     * Check is permission is needed for the user to receive notifications.
     * @return true : needs permission, false : does not need
     */
    needsPermission() : boolean;

    /**
     * Asks the user for permission to display notifications
     * @param onPermissionGrantedCallback A callback for permmision is granted.
     * @param onPermissionDeniedCallback  A callback for permmision is denied.
     */
    requestPermission(onPermissionGrantedCallback?: ()=> any, onPermissionDeniedCallback? : ()=> any) : void;

    /**
     * return true if the browser supports HTML5 Notification
     * @param true : the browser supports HTML5 Notification, false ; the browswer does not supports HTML5 Notification.
     */
    isSupported() : boolean;
}

declare module notifyjs {
    
    /**
     * Interface for Web Notifications API Wrapper.
     */
     interface INotify {
        /**
         * Show the notification.
         */
        show() : void;

        /**
         * Remove all event listener.
         */
        destroy() : void;

        /**
         * Close the notification.
         */
        close() : void;
        onShowNotification(e : Event) : void;
        onCloseNotification() : void;
        onClickNotification() : void;
        onErrorNotification() : void;
        handleEvent(e : Event) : void;
    }
    
    /**
     * Interface for the Notify's optional parameter.
     */
    interface INotifyOption {

        /**
         * notification message body
         */
        body? : string;

        /**
         * path for icon to display in notification
         */
        icon? : string;

        /**
         * unique identifier to stop duplicate notifications
         */
        tag? : string;
        
         /**
         * number of seconds to close the notification automatically
         */
        timeout? : number;

        /**
         * callback when notification is shown
         */
        notifyShow? (e : Event): any;
        /**
         * callback when notification is closed
         */
        notifyClose? : Function;
        /**
         * callback when notification is clicked
         */
        notifyClick? : Function;
        /**
         * callback when notification throws an error
         */
        notifyError? : Function;
        /**
         *  callback when user has granted permission
         */
        permissionGranted? : Function;
        /**
         * callback when user has denied permission
         */
        permissionDenied? : Function;
    }
}
