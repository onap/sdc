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



(function (window) {
    "use strict";

    if (window.PunchOutRegistry) {
        return;
    }

    var queuedFactoryRequests = new Map();
    var factoryPromises = new Map();
    var instancePromises = new Map();

    function loadOnBoarding(callback) {

        if (factoryPromises.has("onboarding/vendor") && !queuedFactoryRequests.has("onboarding/vendor")) {
            callback();
        }
        else {
            console.log("Load OnBoarding");
            $.getScript("/onboarding/punch-outs_en.js").then(callback);
        }
    }

    function registerFactory(name, factory) {
        if (factoryPromises.has(name) && !queuedFactoryRequests.has(name)) {
            // console.error("PunchOut \"" + name + "\" has been already registered");
            return;
        }
        if (queuedFactoryRequests.has(name)) {
            var factoryRequest = queuedFactoryRequests.get(name);
            factoryRequest(factory);
            queuedFactoryRequests.delete(name);
        } else {
            factoryPromises.set(name, Promise.resolve(factory));
        }
    }

    function getFactoryPromise(name) {
        var factoryPromise = factoryPromises.get(name);
        if (!factoryPromise) {
            factoryPromise = new Promise(function (resolveFactory) {
                queuedFactoryRequests.set(name, resolveFactory);
            });
            factoryPromises.set(name, factoryPromise);
        }
        return factoryPromise;
    }

    function getInstancePromise(name, element) {
        var factoryPromise;
        var instancePromise = instancePromises.get(element);
        if (!instancePromise) {
            instancePromise = getFactoryPromise(name).then(function (factory) {
                return factory();
            });
            instancePromises.set(element, instancePromise);
        }
        return instancePromise;
    }

    function renderPunchOut(params, element) {
        var name = params.name;
        var options = params.options || {};
        var onEvent = params.onEvent || function () {
            };

        getInstancePromise(name, element).then(function (punchOut) {
            punchOut.render({options: options, onEvent: onEvent}, element);
        });
    }

    function unmountPunchOut(element) {
        if (!instancePromises.has(element)) {
            console.error("There is no PunchOut in element", element);
            return;
        }
        instancePromises.get(element).then(function (punchOut) {
            punchOut.unmount(element);
        });
        instancePromises.delete(element);
    }

    var PunchOutRegistry = Object.freeze({
        register: registerFactory,
        render: renderPunchOut,
        unmount: unmountPunchOut,
        loadOnBoarding: loadOnBoarding
    });

    window.PunchOutRegistry = PunchOutRegistry;

})(window);
