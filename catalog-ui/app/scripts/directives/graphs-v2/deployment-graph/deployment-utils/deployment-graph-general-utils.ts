/**
 * Created by obarda on 12/21/2016.
 */
/// <reference path="../../../../references"/>
module Sdc.Graph.Utils {

    export class DeploymentGraphGeneralUtils {

        constructor() {

        }

        public findInstanceModule = (groupsArray:Array<Models.Module>, componentInstanceId:string):string => {
            let parentGroup:Sdc.Models.Module = _.find(groupsArray, (group:Sdc.Models.Module) => {
                return _.find(group.members, (member) => {
                    return member === componentInstanceId;
                });
            });
            return parentGroup ? parentGroup.uniqueId : "";
        };
    }

    DeploymentGraphGeneralUtils.$inject = [];
}