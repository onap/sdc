import {ComponentType} from "../utils/constants";
import {CacheService} from "../services/cache-service";
export class CategoryTypeFilter {

    static $inject = ['Sdc.Services.CacheService'];

    constructor(cacheService:CacheService) {
        let filter = <CategoryTypeFilter>(categories:any, selectedType:Array<string>, selectedSubResourceTypes:Array<string>) => {

            if (selectedType.indexOf(ComponentType.RESOURCE) === -1 && selectedSubResourceTypes.length > 0) {
                selectedType = selectedType.concat([ComponentType.RESOURCE]);
            }

            if (!selectedType.length)
                return categories;

            let filteredCategories:any = [];
            selectedType.forEach((type:string) => {
                filteredCategories = filteredCategories.concat(cacheService.get(type.toLowerCase() + 'Categories'));
            });

            return _.filter(categories, function (category:any) {
                return filteredCategories.indexOf(category) != -1;
            });
        };
        return filter;
    }
}
