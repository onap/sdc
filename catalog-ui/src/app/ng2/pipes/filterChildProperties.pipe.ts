import { Pipe, PipeTransform } from '@angular/core';
import { DerivedFEProperty } from 'app/models';

@Pipe({
    name: 'filterChildProperties',
})
export class FilterChildPropertiesPipe implements PipeTransform {
    public transform(childProperties: Array<DerivedFEProperty>, parentId: string) {
        if (!parentId || !childProperties) return childProperties;

        let validParents: Array<string> = [parentId];
        while (parentId.lastIndexOf('#') > 0) {
            parentId = parentId.substring(0, parentId.lastIndexOf('#'));
            validParents.push(parentId);
        }
        return childProperties.filter(derivedProp => validParents.indexOf(derivedProp.parentName) > -1);
    }
}