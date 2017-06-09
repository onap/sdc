'use strict';



export class ICategoryBase {

    //server properties
    name:string;
    normalizedName:string;
    uniqueId:string;
    icons:Array<string>;

    //custom properties
    filterTerms:string;
    isDisabled:boolean;
    filteredGroup:Array<IGroup>;

    constructor(category?:ICategoryBase) {
        if (category) {
            this.name = category.name;
            this.normalizedName = category.normalizedName;
            this.icons = category.icons;
            this.filterTerms = category.filterTerms;
            this.isDisabled = category.isDisabled;
            this.filteredGroup = category.filteredGroup;
        }
    }
}

export class IMainCategory extends ICategoryBase {
    subcategories:Array<ISubCategory>;

    constructor();
    constructor(category?:IMainCategory) {
        super(category);
        if (category) {
            this.subcategories = category.subcategories;
        }
    }
}

export class ISubCategory extends ICategoryBase {
    groupings:Array<ICategoryBase>;
}

export interface IGroup extends ICategoryBase {
}
