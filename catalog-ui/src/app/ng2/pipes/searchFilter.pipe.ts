import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'searchFilter',
})
export class SearchFilterPipe implements PipeTransform {
    public transform(value, key: string, term: string) {
        if (!term || !term.length) return value;
        return value.filter((item) => {
            if (item.hasOwnProperty(key)) {
                let regExp = new RegExp(term, 'gi');
                return regExp.test(item[key]);
            } else {
                return false;
            }
        });
    }
}