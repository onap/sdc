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
/**
 * Created by obarda on 7/4/2016.
 */
/// <reference path="../references"/>
module Sdc.Services {

    'use strict';

    interface IEventListenerService {

    }

    interface ICallbackData {
        callback:Function;
        args:any[];
    }

    export class EventListenerService implements IEventListenerService {

        public observerCallbacks:Utils.Dictionary<string, ICallbackData[]> = new Utils.Dictionary<string, Array<ICallbackData>>();

        //register an observer + callback
        public registerObserverCallback = (eventName:string, callback:Function, ...args) => {
            let callbackData = {
                callback: callback,
                args: args
            }

            if (this.observerCallbacks.containsKey(eventName)) {
                let callbacks = this.observerCallbacks.getValue(eventName);

                // Only insert the callback if the callback is different from existing callbacks.
                for (let i = 0; i < callbacks.length; i++) {
                    if (callbacks[i].toString() === callback.toString()) {
                        return; // Do not add this callback.
                    }
                }

                callbacks.push(callbackData);
                this.observerCallbacks.setValue(eventName, callbacks);
            } else {
                this.observerCallbacks.setValue(eventName, [callbackData]);
            }
        };

        //unregister an observer
        public unRegisterObserver = (eventName:string) => {
            if (this.observerCallbacks.containsKey(eventName)) {
                this.observerCallbacks.remove(eventName);
            }
        };

        public notifyObservers = function (eventName:string, ...args) {
            _.forEach(this.observerCallbacks.getValue(eventName), (callbackData:ICallbackData) => {
                callbackData.callback(...args);
            });
        };
    }
}
