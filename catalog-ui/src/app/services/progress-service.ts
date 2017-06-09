/**
 * Created by obarda on 7/7/2016.
 */

'use strict';
import IIntervalService = angular.IIntervalService;

export class ProgressService {

    public progresses:any = {};

    static '$inject' = ['$interval'];

    constructor(protected $interval:any) {
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
        this.$interval.cancel(this.createComponentInterval);
    };


    public initCreateComponentProgress = (componentId:string):void => {
        let progressValue:number = this.startProgress;
        if (!this.getProgressValue(componentId)) {
            this.stopCreateComponentInterval();
            this.setProgressValue(componentId, this.startProgress);
            this.createComponentInterval = this.$interval(():void => {
                //TODO replace getProgressMockData to real data after BE provide the API
                let progressValue = this.getProgressMockData(componentId);
                if (progressValue <= this.totalProgress) {
                    this.setProgressValue(componentId, progressValue);
                } else {
                    /**
                     * Currently the progress is not really checking against the BE.
                     * So the progress can pass 100. So the workaround for now, in case we pass 90 (totalProgress)
                     * stop the interval, so the progress will be kept at 90 until the promise will return value and set
                     * the progress to 100.
                     */
                    this.deleteProgressValue(componentId);
                }
            }, this.onePercentIntervalSeconds * 1000);
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
