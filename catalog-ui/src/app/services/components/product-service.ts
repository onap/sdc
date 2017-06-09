/**
 * Created by obarda on 2/8/2016.
 */
'use strict';
import {IComponentService, ComponentService} from "./component-service";
import {SharingService} from "../sharing-service";
import {Product, Component, IAppConfigurtaion} from "../../models";

export interface IProductService extends IComponentService {

}

export class ProductService extends ComponentService implements IProductService {

    static '$inject' = [
        'Restangular',
        'sdcConfig',
        'Sdc.Services.SharingService',
        '$q',
        '$base64'
    ];

    constructor(protected restangular:restangular.IElement,
                protected sdcConfig:IAppConfigurtaion,
                protected sharingService:SharingService,
                protected $q:ng.IQService,
                protected $base64:any) {
        super(restangular, sdcConfig, sharingService, $q, $base64);
        this.restangular = restangular.one("products");
    }

    createComponentObject = (component:Component):Component => {
        return new Product(this, this.$q, <Product>component);
    };
}
