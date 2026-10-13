import { Injectable } from '@angular/core';
import { Store } from '@ngxs/store';
import { Capability } from 'app/models/capability';
import { Component } from 'app/models/components/component';
import { ConnectRelationModel } from 'app/models/graph/connect-relation-model';
import { Match } from 'app/models/graph/match-relation';
import { PropertyFEModel } from 'app/models/properties-inputs/property-fe-model';
import { Requirement } from 'app/models/requirement';
import * as _ from 'lodash';
import { Dictionary } from 'lodash';
import { WorkspaceService } from '../../../workspace/workspace.service';

@Injectable()
export class ConnectionWizardService {

    connectRelationModel: ConnectRelationModel;
    selectedMatch: Match;
    changedCapabilityProperties: PropertyFEModel[];

    constructor(private workspaceService: WorkspaceService) {
        this.changedCapabilityProperties = [];

    }

    public setRelationMenuDirectiveObj = (connectRelationModel: ConnectRelationModel) => {
        this.connectRelationModel = connectRelationModel;
        // this.selectedCapability = rel
    }

    getOptionalRequirementsByInstanceUniqueId = (isFromTo: boolean, matchWith?: Capability): Dictionary<Requirement[]> => {
       let requirements: Requirement[] = [];
       _.forEach(this.connectRelationModel.possibleRelations, (match: Match) => {
            if (!matchWith || match.capability.uniqueId == matchWith.uniqueId) {
                if (match.isFromTo == isFromTo) {
                    requirements.push(match.requirement);
                }
            }
        });
       requirements = _.uniqBy(requirements, (req: Requirement) => {
            return req.ownerId + req.uniqueId + req.name;
        });
       return _.groupBy(requirements, 'capability');
    }

    getOptionalCapabilitiesByInstanceUniqueId = (isFromTo: boolean, matchWith?: Requirement): Dictionary<Capability[]> => {
        let capabilities: Capability[] = [];
        _.forEach(this.connectRelationModel.possibleRelations, (match: Match) => {
            if (!matchWith || match.requirement.uniqueId == matchWith.uniqueId) {
                if (match.isFromTo == isFromTo) {
                    capabilities.push(match.capability);
                }
            }
        });
        capabilities = _.uniqBy(capabilities, (cap: Capability) => {
            return cap.ownerId + cap.uniqueId + cap.name;
        });
        return _.groupBy(capabilities, 'type');
    }
}
