/**
 * Created by obarda on 2/4/2016.
 */
'use strict';
import {ComponentInstance} from "./componentInstance";

export class ProductInstance extends ComponentInstance {

    constructor(componentInstance?:ProductInstance) {
        super(componentInstance);
        this.iconSprite = "sprite-product-icons";
    }
}
