/**
 * Created by rc2122 on 5/17/2017.
 */
import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'groupBy'})
export class GroupByPipe implements PipeTransform {
    transform(value: Array<any>, field: string): Array<any> {
        const groupedObj = value.reduce((prev, cur)=> {
            if(!prev[cur[field]]) {
                prev[cur[field]] = [cur];
            } else {
                prev[cur[field]].push(cur);
            }
            return prev;
        }, {});
        return Object.keys(groupedObj).map((key:string) => {return { key, value: groupedObj[key] }; });
    }
}
