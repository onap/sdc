/**
 * Created by obarda on 19/08/2015.
 */
export class CatalogStatusFilter {

    constructor() {
        let filter = <CatalogStatusFilter>( (statuses:any) => {
            let filtered = [];
            angular.forEach(statuses, function (status) {
                filtered.push(status);
            });
            return filtered;
        });

        return filter;
    }
}

