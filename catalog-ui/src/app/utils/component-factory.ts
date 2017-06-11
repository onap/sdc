'use strict';
import {DEFAULT_ICON, ResourceType, ComponentType} from "./constants";
import {ServiceService, CacheService, ResourceService, ProductService} from "app/services";
import {IMainCategory, ISubCategory, ICsarComponent, Component, Resource, Service, Product} from "app/models";
import {ComponentMetadata} from "../models/component-metadata";
import {ComponentServiceNg2} from "../ng2/services/component-services/component.service";
import {ComponentGenericResponse} from "../ng2/services/responses/component-generic-response";


export class ComponentFactory {

    static '$inject' = [
        'Sdc.Services.Components.ResourceService',
        'Sdc.Services.Components.ServiceService',
        'Sdc.Services.Components.ProductService',
        'Sdc.Services.CacheService',
        '$q',
        'ComponentServiceNg2'
    ];

    constructor(private ResourceService:ResourceService,
                private ServiceService:ServiceService,
                private ProductService:ProductService,
                private cacheService:CacheService,
                private $q:ng.IQService,
                private ComponentServiceNg2: ComponentServiceNg2) {
    }

    public createComponent = (component:Component):Component => {
        let newComponent:Component;
        switch (component.componentType) {

            case 'SERVICE':
                newComponent = new Service(this.ServiceService, this.$q, <Service> component);
                break;

            case 'RESOURCE':
                newComponent = new Resource(this.ResourceService, this.$q, <Resource> component);
                break;

            case 'PRODUCT':
                newComponent = new Product(this.ProductService, this.$q, <Product> component);
                break;
        }
        return newComponent;
    };

    public createProduct = (product:Product):Product => {
        let newProduct:Product = new Product(this.ProductService, this.$q, <Product> product);
        return newProduct;
    };

    public createService = (service:Service):Service => {
        let newService:Service = new Service(this.ServiceService, this.$q, <Service> service);
        return newService;
    };

    public createResource = (resource:Resource):Resource => {
        let newResource:Resource = new Resource(this.ResourceService, this.$q, <Resource> resource);
        return newResource;
    };

    public createFromCsarComponent = (csar:ICsarComponent):Component => {
        let newResource:Resource = <Resource>this.createEmptyComponent(ComponentType.RESOURCE);
        newResource.name = csar.vspName;

        /**
         * Onboarding CSAR contains category and sub category that are uniqueId.
         * Need to find the category and sub category and extract the name from them.
         * First concat all sub categories to one array.
         * Then find the selected sub category and category.
         * @type {any}
         */
        let availableCategories = angular.copy(this.cacheService.get('resourceCategories'));
        let allSubs = [];
        _.each(availableCategories, (main:IMainCategory)=> {
            if (main.subcategories) {
                allSubs = allSubs.concat(main.subcategories);
            }
        });

        let selectedCategory:IMainCategory = _.find(availableCategories, function (main:IMainCategory) {
            return main.uniqueId === csar.category;
        });

        let selectedSubCategory:ISubCategory = _.find(allSubs, (sub:ISubCategory)=> {
            return sub.uniqueId === csar.subCategory;
        });

        // Build the categories and sub categories array (same format as component category)
        let categories:Array<IMainCategory> = new Array();
        let subcategories:Array<ISubCategory> = new Array();
        if (selectedCategory && selectedSubCategory) {
            subcategories.push(selectedSubCategory);
            selectedCategory.subcategories = subcategories;
            categories.push(selectedCategory);
        }

        // Fill the component with details from CSAR
        newResource.selectedCategory = selectedCategory && selectedSubCategory ? selectedCategory.name + "_#_" + selectedSubCategory.name : '';
        newResource.categories = categories;
        newResource.vendorName = csar.vendorName;
        newResource.vendorRelease = csar.vendorRelease;
        newResource.csarUUID = csar.packageId;
        newResource.csarPackageType = csar.packageType;
        newResource.csarVersion = csar.version;
        newResource.packageId = csar.packageId;
        newResource.description = csar.description;
        newResource.filterTerm = newResource.name +  ' '  + newResource.description + ' ' + newResource.vendorName + ' ' + newResource.csarVersion
        return newResource;
    };

    public createEmptyComponent = (componentType:string):Component => {
        let newComponent:Component;

        switch (componentType) {

            case ComponentType.SERVICE:
                newComponent = new Service(this.ServiceService, this.$q);
                break;

            case ComponentType.RESOURCE:
            case ResourceType.VF:
            case ResourceType.VL:
            case ResourceType.VFC:
            case ResourceType.CP:
                newComponent = new Resource(this.ResourceService, this.$q);
                break;

            case ComponentType.PRODUCT:
                newComponent = new Product(this.ProductService, this.$q);
                break;
        }
        newComponent.componentType = componentType;
        newComponent.tags = [];
        newComponent.icon = DEFAULT_ICON;
        return newComponent;
    };

    public getComponentFromServer = (componentType:string, componentId:string):ng.IPromise<Component> => {
        let newComponent:Component = this.createEmptyComponent(componentType);
        newComponent.setUniqueId(componentId);
        return newComponent.getComponent();
    };

    public createComponentOnServer = (componentObject:Component):ng.IPromise<Component> => {
        let component:Component = this.createComponent(componentObject);
        return component.createComponentOnServer();

    };

    public getComponentWithMetadataFromServer = (componentType:string, componentId:string):ng.IPromise<Component> => {
        let deferred = this.$q.defer();
        let component = this.createEmptyComponent(componentType);
        component.setUniqueId(componentId);
        this.ComponentServiceNg2.getComponentMetadata(component).subscribe((response:ComponentGenericResponse) => {
            component.setComponentMetadata(response.metadata);
            deferred.resolve(component);
        });
        return deferred.promise;
    }
}
