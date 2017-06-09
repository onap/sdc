/**
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {ComponentInstance} from "./componentInstance";

export class ServiceInstance extends ComponentInstance {

    constructor(componentInstance?:ServiceInstance) {
        super(componentInstance);
        this.iconSprite = "sprite-services-icons";
    }
}

