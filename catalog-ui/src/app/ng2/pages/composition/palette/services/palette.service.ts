import { HttpClient, HttpParams } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { LeftPaletteComponent, LeftPaletteMetadataTypes } from 'app/models/components/displayComponent';
import { GroupMetadata } from 'app/models/group-metadata';
import { PolicyMetadata } from 'app/models/policy-metadata';
import { IComponentMetadata } from 'app/models/component-metadata';
import { SdcConfigToken } from 'app/ng2/config/sdc-config.config';
import { ISdcConfig } from 'app/ng2/config/sdc-config.config.factory';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import 'rxjs/add/observable/forkJoin';
import { Observable } from 'rxjs/Rx';
import Dictionary = _.Dictionary;



@Injectable()
export class CompositionPaletteService {

    protected baseUrl = '';

    private leftPaletteComponents: Dictionary<Dictionary<LeftPaletteComponent[]>>;
    private facadeUrl: string;
    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig, private workspaceService: WorkspaceService) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
        this.facadeUrl = sdcConfig.api.uicache_root + sdcConfig.api.GET_uicache_left_palette;

    }

    public subscribeToLeftPaletteElements(next, error) {

        let params = new HttpParams();
        params = params.append('internalComponentType', this.workspaceService.getMetadataType());
      let model = this.workspaceService.metadata.model
        if (model) {
          params = params.append('componentModel', model);
        }
        const loadInstances = this.http.get(this.facadeUrl, {params});
        const loadGroups = this.http.get(this.baseUrl + 'groupTypes', {params});
        const loadPolicies = this.http.get(this.baseUrl + 'policyTypes', {params});

        Observable.forkJoin(
            loadInstances, loadGroups, loadPolicies
        ).subscribe( ([resInstances, resGrouops, resPolicies]) => {
            const combinedDictionary = this.combineResoponses(resInstances, resGrouops, resPolicies);
            this.leftPaletteComponents = combinedDictionary;
            next(this.leftPaletteComponents);
        });
    }

    public getLeftPaletteElements = (): Dictionary<Dictionary<LeftPaletteComponent[]>> => {
        return this.leftPaletteComponents;
    }


    public convertPoliciesOrGroups = (paletteListResult, type: string ) => {
        const components: LeftPaletteComponent[] = [];

        if (type === 'Policies') {
            _.forEach(paletteListResult, (policyMetadata: PolicyMetadata) => {
                components.push(new LeftPaletteComponent(LeftPaletteMetadataTypes.Policy, policyMetadata));
            });
            return {
                Policies: components
            };
        }

        if (type === 'Groups') {
            _.forEach(paletteListResult, (groupMetadata: GroupMetadata) => {
                const item = new LeftPaletteComponent(LeftPaletteMetadataTypes.Group, groupMetadata);
                components.push(item);
            });
            return {
                Groups: components
            };
        }

        return {};
    }

    private combineResoponses(resInstances: object, resGrouops: object, resPolicies: object) {
        const retValObject = {};
        
        if (resInstances['Generic']) {
	        if (this.isSubstitutionForNestedServices()) {
	            const serviceGroup = this.createServiceGroup(resInstances);
	            if (serviceGroup) {
	                retValObject['Service'] = serviceGroup;
	            }
	        }
	        	retValObject['Generic'] = resInstances['Generic'];
	    }
        
        // Add all other categories
        for (const category in resInstances) {
            if (category === 'Generic') {
                continue;
            }
            retValObject[category] = resInstances[category];
        }

        // Add Groups
        retValObject["Groups"] = this.convertPoliciesOrGroups(resGrouops, 'Groups');

        // Add policies
        retValObject["Policies"] = this.convertPoliciesOrGroups(resPolicies, 'Policies');

        return retValObject;
    }
    
    private isSubstitutionForNestedServices(): boolean {
	    return this.workspaceService.metadata.categories[0].useServiceSubstitutionForNestedServices;
	}
    
    private createServiceGroup(resInstances: object): object {
	    const servicesList = resInstances['Generic']['Generic'];
	    if (Array.isArray(servicesList) && servicesList.length > 0) {
	        delete resInstances['Generic']['Generic'];
	        return servicesList.reduce(function (map, component) {
	            if (map[component.categories[0].name]) {
	                map[component.categories[0].name].push(component);
	            } else {
	                map[component.categories[0].name] = [component];
	            }
	            return map;
	        }, {});
	    }
	    return null;
	}
	
}
