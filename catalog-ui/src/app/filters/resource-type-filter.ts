import {CacheService} from "../services/cache-service";

export class ResourceTypeFilter {
    static '$inject' = ['Sdc.Services.CacheService'];

    constructor(cacheService:CacheService) {
        let filter = <ResourceTypeFilter>(resourceType:string) => {
            let uiConfiguration:any = cacheService.get('UIConfiguration');

            if (uiConfiguration.resourceTypes && uiConfiguration.resourceTypes[resourceType]) {
                return uiConfiguration.resourceTypes[resourceType];
            }
            return resourceType;
        }
        return filter;
    }
}
