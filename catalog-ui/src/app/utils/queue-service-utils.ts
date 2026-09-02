/**
 * Created by ob0695 on 6/3/2018.
 */
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
import {Injectable} from "@angular/core";

@Injectable()
export class QueueServiceUtils {

    private executionQueue:any;

    constructor() {
        if(!this.executionQueue) {
            this.executionQueue = this.getQueue();
        }
    }

    private getQueue = () => new Promise((resolve, reject) => {
       resolve(true);
    });

    private addMethodToQueue = (runMe:Function):void => {
        this.executionQueue = this.executionQueue.then(runMe, runMe);
    };

    addNonBlockingUIAction = (update:Function, releaseUIcallBack?:Function):void => {
        // releaseUIcallBack();
        this.addMethodToQueue(update);
    };

    // The Method call is responsible for releasing the UI
    addBlockingUIAction = (blockingServerRequest:Function):void => {
        this.addMethodToQueue(blockingServerRequest);
    };

    addBlockingUIActionWithReleaseCallback = (blockingServerRequest:Function, releaseUIcallBack:Function):void=> {
        this.addMethodToQueue(blockingServerRequest);
        // this.addMethodToQueue(releaseUIcallBack);
    };
}
