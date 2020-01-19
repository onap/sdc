import { IEntityFilterObject, ISearchFilter } from "app/ng2/pipes/entity-filter.pipe";

export interface IHomeFilterParams {
    'filter.term': string;
    'filter.distributed': string;
    'filter.status': string
}


export class HomeFilter implements IEntityFilterObject{
    selectedStatuses: Array<string>;
    distributed: Array<string>;
    search: ISearchFilter;

    constructor(params = {}) {
        this.search = { filterTerm : params['filter.term'] || "" };
        this.selectedStatuses = params['filter.status']? params['filter.status'].split(',') : [];
        this.distributed = params['filter.distributed']? params['filter.distributed'].split(',') : []
        
    }

    public toUrlParam = ():IHomeFilterParams => {
        return {
            'filter.term': this.search.filterTerm,
            'filter.distributed': this.distributed && this.distributed.join(',') || null,
            'filter.status': this.selectedStatuses && this.selectedStatuses.join(',') || null
        };
    }

}