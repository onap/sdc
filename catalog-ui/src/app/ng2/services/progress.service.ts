/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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
 * Created by obarda on 7/7/2016.
 */

'use strict';
import {Injectable, NgZone} from "@angular/core";

@Injectable()
export class ProgressService {

    public progresses:any = {};

    constructor(private ngZone:NgZone) {
    }

    private totalProgress:number = 90;
    private startProgress:number = 10;
    private onePercentIntervalSeconds:number = 5;
    private createComponentInterval;

    public setProgressValue(name:string, value:number):void {
        if (!this.progresses[name]) {
            this.progresses[name] = {};
        }
        this.progresses[name].value = value;
    }

    public getProgressValue(name:string):number {
        if (this.progresses[name]) {
            return this.progresses[name].value;
        }
        return 0;
    }

    public deleteProgressValue(name:string):void {
        this.stopCreateComponentInterval();
        delete this.progresses[name];
    }


    private stopCreateComponentInterval = ():void => {
        if (this.createComponentInterval) {
            clearInterval(this.createComponentInterval);
            this.createComponentInterval = undefined;
        }
    };


    public initCreateComponentProgress = (componentId:string):void => {
        let progressValue:number = this.startProgress;
        if (!this.getProgressValue(componentId)) {
            this.stopCreateComponentInterval();
            this.setProgressValue(componentId, this.startProgress);
            // Run the polling timer OUTSIDE the Angular zone so Zone.js does not treat it as a
            // never-settling macrotask (which would hang Protractor/Selenium waitForAngular — see
            // ngUpgrade failure-catalog §B). Re-enter the zone only for the progress-value write so
            // change detection still fires.
            this.createComponentInterval = this.ngZone.runOutsideAngular(():any =>
                setInterval(():void => {
                    //TODO replace getProgressMockData to real data after BE provide the API
                    let progressValue = this.getProgressMockData(componentId);
                    if (progressValue <= this.totalProgress) {
                        this.ngZone.run(():void => this.setProgressValue(componentId, progressValue));
                    } else {
                        /**
                         * Currently the progress is not really checking against the BE.
                         * So the progress can pass 100. So the workaround for now, in case we pass 90 (totalProgress)
                         * stop the interval, so the progress will be kept at 90 until the promise will return value and set
                         * the progress to 100.
                         */
                        this.ngZone.run(():void => this.deleteProgressValue(componentId));
                    }
                }, this.onePercentIntervalSeconds * 1000));
        }

    };


    private getProgressMockData = (id:string):number => {
        let progressValue = this.getProgressValue(id);
        if (progressValue > 0) {
            progressValue = progressValue + 1;
        }
        //if not finish always stay on 90%
        if (progressValue > 90) {
            progressValue = 90;
        }

        return progressValue;
    }

}
