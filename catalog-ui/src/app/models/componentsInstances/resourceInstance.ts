/**
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {ComponentInstance} from "./componentInstance";

export class ResourceInstance extends ComponentInstance {

    constructor(componentInstance?:ResourceInstance) {
        super(componentInstance);

        this.iconSprite = "sprite-resource-icons";
    }
}

