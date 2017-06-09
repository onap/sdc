'use strict';
// import 'angular-resource';
import {IAppConfigurtaion, IMainCategory, IUserProperties} from "../models";
import {Categories} from "../models/categories";

// Define an interface of the object you want to use, providing it's properties
export interface ICategoryResource extends IUserProperties,ng.resource.IResource<ICategoryResource> {
    name:string;
    uniqueId:string;
    subcategories:Array<ICategoryResource>;

    getAllCategories(params?:Object, success?:Function, error?:Function):Categories;
    $saveSubCategory(params?:Object, success?:Function, error?:Function):any;
    $deleteSubCategory(params?:Object, success?:Function, error?:Function):any;
}

// Define your resource, adding the signature of the custom actions
export interface ICategoryResourceClass extends ng.resource.IResourceClass<ICategoryResource> {
    getAllCategories(params?:Object, success?:Function, error?:Function):Categories;
    saveSubCategory(params?:Object, success?:Function, error?:Function):any;
    deleteSubCategory(params?:Object, success?:Function, error?:Function):any;
}

export class CategoryResourceService {

    public static getResource = ($resource:ng.resource.IResourceService,
                                 sdcConfig:IAppConfigurtaion):ICategoryResourceClass => {

        // Define your custom actions here as IActionDescriptor
        let getAllCategoriesAction:ng.resource.IActionDescriptor = {
            method: 'GET',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.GET_categories
        };
        let saveSubCategory:ng.resource.IActionDescriptor = {
            method: 'POST',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_subcategory
        };
        let deleteSubCategory:ng.resource.IActionDescriptor = {
            method: 'DELETE',
            isArray: false,
            url: sdcConfig.api.root + sdcConfig.api.POST_subcategory
        };


        let url:string = sdcConfig.api.root + sdcConfig.api.POST_category;
        let categoryResource:ICategoryResourceClass = <ICategoryResourceClass>$resource(
            url,
            {types: '@types', categoryId: '@categoryId'},
            {
                getAllCategories: getAllCategoriesAction,
                saveSubCategory: saveSubCategory,
                deleteSubCategory: deleteSubCategory
            }
        );

        return categoryResource;
    }
}
CategoryResourceService.getResource.$inject = ['$resource', 'sdcConfig'];
