import * as _ from "lodash";
import {ConnectRelationModel} from "app/models/graph/connectRelationModel";
import {Injectable} from "@angular/core";
import { Requirement, Capability} from "app/models";
import {Dictionary} from "lodash";
import {Match, Component, PropertyFEModel} from "app/models";
import {Store} from "@ngxs/store";
import {WorkspaceService} from "../../../workspace/workspace.service";

@Injectable()
export class ConnectionWizardService {
    
    connectRelationModel:ConnectRelationModel;
    selectedMatch:Match;
    changedCapabilityProperties:PropertyFEModel[];


    constructor(private workspaceService: WorkspaceService) {
        this.changedCapabilityProperties = [];

    }

    public setRelationMenuDirectiveObj = (connectRelationModel:ConnectRelationModel) => {
        this.connectRelationModel = connectRelationModel;
        // this.selectedCapability = rel
    }

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
}

