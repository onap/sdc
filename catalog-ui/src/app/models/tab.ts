/**
 * Created by obarda on 7/31/2016.
 */
'use strict';

export class Tab {

    public templateUrl:string;
    public controller:string;
    public data:any;
    public icon:string;
    public name:string;
    public isViewMode:boolean;

    constructor(templateUrl:string, controller:string, name:string, isViewMode:boolean, data?:any, icon?:string) {

        this.templateUrl = templateUrl;
        this.controller = controller;
        this.icon = icon;
        this.data = data;
        this.name = name;
        this.isViewMode = isViewMode;
    }
}



