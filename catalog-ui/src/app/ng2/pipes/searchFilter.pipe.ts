import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'searchFilter',
})
export class SearchFilterPipe implements PipeTransform {
    public transform(value, key: string, term: string) {
        if (!term || !term.length) return value;
        return value.filter((item) => {
            if (item.hasOwnProperty(key)) {
                return item[key].indexOf(term) > -1;
            } else {
                return false;
            }
        });
    }
}
