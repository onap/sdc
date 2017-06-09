import {Module} from "app/models";
/**
 * Created by obarda on 12/21/2016.
 */

export class DeploymentGraphGeneralUtils {

    constructor() {

    }

    public findInstanceModule = (groupsArray:Array<Module>, componentInstanceId:string):string => {
        let parentGroup:Module = _.find(groupsArray, (group:Module) => {
            return _.find(group.members, (member) => {
                return member === componentInstanceId;
            });
        });
        return parentGroup ? parentGroup.uniqueId : "";
    };
}

DeploymentGraphGeneralUtils.$inject = [];
