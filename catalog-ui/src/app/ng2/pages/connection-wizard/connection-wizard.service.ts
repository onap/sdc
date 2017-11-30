import {ConnectRelationModel} from "../../../models/graph/connectRelationModel";
import {Injectable} from "@angular/core";
import { Requirement, Capability} from "app/models";
import {Dictionary} from "lodash";
import {Match} from "../../../models/graph/match-relation";
import {Component} from "../../../models/components/component";
@Injectable()
export class ConnectionWizardService {
    
    connectRelationModel:ConnectRelationModel;
    currentComponent:Component;
    selectedMatch:Match;

    public setRelationMenuDirectiveObj = (connectRelationModel:ConnectRelationModel) => {
        this.connectRelationModel = connectRelationModel;
        // this.selectedCapability = rel
    }


    // getComponentInstanceIdOfSelectedCapability = (): string => {
    //     if(this.selectedMatch.capability){
    //         if(this.selectedMatch.isFromTo) {
    //             return this.selectedMatch.toNode;
    //         } else {
    //             return this.selectedMatch.fromNode;
    //         }
    //     }
    //     return '';
    //
    // }

    getOptionalRequirementsByInstanceUniqueId = (isFromTo: boolean, matchWith?:Capability): Dictionary<Requirement[]> => {
       let requirements: Array<Requirement> = [];
        _.forEach(this.connectRelationModel.possibleRelations, (match: Match) => {
            if(!matchWith || match.capability.uniqueId == matchWith.uniqueId){
                if(match.isFromTo == isFromTo){
                    requirements.push(match.requirement);
                }
            }
        });
        requirements = _.uniqBy(requirements, (req:Requirement)=>{
            return req.ownerId + req.uniqueId + req.name;
        });
        return _.groupBy(requirements, 'capability');
    }

    getOptionalCapabilitiesByInstanceUniqueId = (isFromTo: boolean, matchWith?:Requirement): Dictionary<Capability[]> => {
        let capabilities: Array<Capability> = [];
        _.forEach(this.connectRelationModel.possibleRelations, (match: Match) => {
            if(!matchWith || match.requirement.uniqueId == matchWith.uniqueId){
                if(match.isFromTo == isFromTo){
                    capabilities.push(match.capability);
                }
            }
        });
        capabilities = _.uniqBy(capabilities, (cap:Capability)=>{
            return cap.ownerId + cap.uniqueId + cap.name;
        });
        return _.groupBy(capabilities, 'type');
    }
    constructor() {}

}

