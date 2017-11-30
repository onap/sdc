/**
 * Created by rc2122 on 8/16/2017.
 */

import {Type} from "@angular/core";

export interface IStepComponent {
    preventNext():boolean;
    preventBack():boolean;
}

export class StepModel{
    title: string;
    component: Type<IStepComponent>;
    constructor(title: string, component: Type<IStepComponent>){
        this.title = title;
        this.component = component;
    }
}
