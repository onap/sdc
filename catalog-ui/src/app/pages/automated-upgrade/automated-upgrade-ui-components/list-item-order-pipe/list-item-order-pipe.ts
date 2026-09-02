import {Pipe, PipeTransform} from "@angular/core";
import {ServiceContainerToUpgradeUiObject} from "../../automated-upgrade-models/ui-component-to-upgrade";

/*
 This filter needs to return all not upgraded components sorted by name first, after that all upgraded components sorted by name
 And in the end all the locked components sorted by name
 */

@Pipe({
    name: 'upgradeListItemOrderBy'
})
export class UpgradeListItemOrderPipe implements PipeTransform {

    private orderByName = (firstName:string, secondName:string):number => {
        var textA = firstName.toLocaleLowerCase();
        var textB = secondName.toLocaleLowerCase();
        return (textA < textB) ? -1 : (textA > textB) ? 1 : 0;
    }

    transform(array:Array<ServiceContainerToUpgradeUiObject>):Array<ServiceContainerToUpgradeUiObject> {
        array.sort((first:ServiceContainerToUpgradeUiObject, second:ServiceContainerToUpgradeUiObject) => {
            if (first.isLock && second.isLock) {
                return this.orderByName(first.name, second.name);
            } else if (first.isLock) {
                return 1;
            } else if (second.isLock) {
                return -1;
            } else {
                if (first.isAlreadyUpgrade && second.isAlreadyUpgrade) {
                    return this.orderByName(first.name, second.name);
                } else if (first.isAlreadyUpgrade) {
                    return 1;
                } else if (second.isAlreadyUpgrade) {
                    return -1;
                } else {
                    return this.orderByName(first.name, second.name);
                }
            }
        });
        return array;
    }
}