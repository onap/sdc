import { HttpClient } from '@angular/common/http';
import { Inject, Injectable } from '@angular/core';
import { tap } from 'rxjs/operators';
import { Distribution } from '../../../../models/distribution';
import { ISdcConfig, SdcConfigToken } from '../../../config/sdc-config.config';

@Injectable()
export class DistributionService {
    protected baseUrl;
    private distributionList = [];
    private distributionStatusesMap = {};

    // tslint:disable:no-string-literal

    constructor(protected http: HttpClient, @Inject(SdcConfigToken) sdcConfig: ISdcConfig) {
        this.baseUrl = sdcConfig.api.root + sdcConfig.api.component_api_root;
    }

    // Once the distribution page is loaded or when the user wants to refresh the list
     async initDistributionsList(componentUuid: string): Promise<object> {
        const distributionsListURL = this.baseUrl + 'services/' + componentUuid + '/distribution';
        const res = this.http.get<Distribution[]>(distributionsListURL).pipe(tap( (result) => {
            this.distributionList = result['distributionStatusOfServiceList'];
            this.insertDistrbutionsToMap();
        }  ));
        return res.toPromise();
    }

    // Once the user click on the relevant distribution ID in the distribution table (open and close)
    async initDistributionsStatusForDistributionID(distributionID: string): Promise<object> {
        const distributionStatus = this.baseUrl + 'services/distribution/' + distributionID;
        const res = this.http.get<object>(distributionStatus).pipe(tap( (result) => {
            this.insertDistributionStatusToDistributionsMap(distributionID, result['distributionStatusList']);
        }  ));
        return res.toPromise();
    }

    public getDistributionList(specificDistributionID?: string) {
        if (specificDistributionID) {
            return this.distributionList.filter((distribution) => {
                return distribution['distributionID'] === specificDistributionID;
            });
        } else {
            return this.distributionList;
        }
    }

    public getComponentsByDistributionID(distributionID: string) {
        const components = [];
        const distributionStatusMap = this.getStatusMapForDistributionID(distributionID);
        if (distributionStatusMap) {
            distributionStatusMap.forEach((component) => components.push(component.componentID));
        }
        return components;
    }

    // get array of artifacts per distributionID w/o componentName, sliced by artifact status
    public getArtifactsForDistributionIDAndComponentByStatus(distributionID: string, statusToSearch: string, componentName?: string) {
        const filteredArtifactsByStatus = [];

        if (componentName) {
            this.getArtifactstByDistributionIDAndComponentsName(distributionID, componentName).forEach ( (artifact) => {
                if (this.artifactStatusHasMatch(artifact, statusToSearch)) {
                    filteredArtifactsByStatus.push(artifact);
                }
            } );
        } else {
            this.getArtifactstByDistributionIDAndComponentsName(distributionID).forEach ( (artifact) => {
                if (this.artifactStatusHasMatch(artifact, statusToSearch)) {
                    filteredArtifactsByStatus.push(artifact);
                }
            } );
        }
        return filteredArtifactsByStatus;
    }

    public getArtifactstByDistributionIDAndComponentsName(distributionID: string, componentName?: string): any[] {
        const artifacts = [];
        if (this.getStatusMapForDistributionID(distributionID)) {
            if (componentName) {
                if (this.getStatusMapForDistributionID(distributionID).filter((component) => component.componentID === componentName).length > 0) {
                    const artifactsArr = this.getStatusMapForDistributionID(distributionID).filter((component) => component.componentID === componentName)[0]['artifacts']
                    if (artifactsArr.length > 0) {
                        artifactsArr.forEach((artifact) => {
                            const artifactObj = {
                                url: artifact.artifactUrl,
                                name: artifact.artifactName,
                                statuses: artifact.statuses
                            };
                            artifacts.push(artifactObj);
                        });
                    }
                }
            } else {
                const components = this.getComponentsByDistributionID(distributionID);
                components.forEach((componentName) => {
                    if (this.getStatusMapForDistributionID(distributionID).filter((component) => component.componentID === componentName).length > 0) {
                        const artifactsArr = this.getStatusMapForDistributionID(distributionID).filter((component) => component.componentID === componentName)[0]['artifacts']
                        if (artifactsArr.length > 0) {
                            artifactsArr.forEach((artifact) => {
                                const artifactObj = {
                                    url: artifact.artifactUrl,
                                    name: artifact.artifactName,
                                    statuses: artifact.statuses
                                };
                                artifacts.push(artifactObj);
                            });
                        }
                    }
                });
            }
        }
        return artifacts;
    }

    public getStatusMapForDistributionID(distributionID: string) {
        return this.distributionStatusesMap[distributionID];
    }

    public markDeploy(uniqueId: string, distributionID: string): Promise<object> {
        const distributionStatus = this.baseUrl + 'services/' + uniqueId + '/distribution/' + distributionID + '/markDeployed';
        const res = this.http.post<object>(distributionStatus, {}).pipe(tap( (result) => {
            console.log(result);
        }  ));
        return res.toPromise();
    }

    public getMSOStatus(distributionID: string, componentName: string): string {
        const msoStatus = this.distributionStatusesMap[distributionID].filter((component) => component.componentID === componentName)[0].msoStatus;
        return msoStatus ? msoStatus : '';
    }

    private artifactStatusHasMatch(artifact: any, statusToSerach: string) {
        for (let i = 0; i < artifact.statuses.length; i++) {
            if (artifact.statuses[i].status === statusToSerach) {
                return true;
            }
        }
        return false;
    }

    private insertDistributionStatusToDistributionsMap(distributionID: string, distributionStatusMapResponseFromServer: object[]) {

        // // Clear the Distribution ID array - to avoid statuses duplications
        const distribution = this.distributionStatusesMap[distributionID];
        distribution.length = 0;

        // Sort the response of statuses from Server, so it will be easy to pop the latest status when it will be required
        const sortedResponseByTimeStamp = distributionStatusMapResponseFromServer.sort((a, b) => b['timestamp'] - a['timestamp'])

        sortedResponseByTimeStamp.map((distributionStatus) => {
            const formattedDate = this.formatDate(distributionStatus['timestamp']);

            // if (distributionStatus['url'] === null) {
            //     distributionStatus['url'] = "";
            // }

            const detailedArtifactStatus = {
                componentID: distributionStatus['omfComponentID'],
                artifactName: distributionStatus['url']? distributionStatus['url'].split('/').pop() : '',
                url: distributionStatus['url'],
                time: distributionStatus['timestamp'],
                status: distributionStatus['status'],
            };



            // Add Component to this.distributionStatusesMap in case not exist.
            let componentPosition = _.findIndex(distribution, {componentID: detailedArtifactStatus.componentID})

            if (componentPosition === -1) {
                this.addComponentIdToDistributionStatusMap(distributionID, detailedArtifactStatus.componentID);
                componentPosition = distribution.length - 1;
            }

            const component = distribution[componentPosition];


            // Add Artifact to this.distributionStatusesMap[componentID] in case not exist.
            let artifactPosition = _.findIndex(component.artifacts, {artifactUrl: detailedArtifactStatus.url})

            if (artifactPosition === -1) {
                this.addArtifactToComponentId(distributionID, componentPosition, detailedArtifactStatus.artifactName, detailedArtifactStatus.url);
                artifactPosition = component.artifacts.length - 1;
            }


            // Add status to relevat artifact in relevent componentID.
            if (detailedArtifactStatus.url) {
                // Case where there is a url -> should add its status
                component.artifacts[artifactPosition].statuses.push({
                    timeStamp: detailedArtifactStatus.time,
                    status: detailedArtifactStatus.status
                });
            } else {
                // Should update the Component -> status from MSO
                this.distributionStatusesMap[distributionID][componentPosition].msoStatus = detailedArtifactStatus.status;
            }


        });
    }

    private addComponentIdToDistributionStatusMap(distributionID: string, componentIDValue: string) {
        this.distributionStatusesMap[distributionID].push({
            componentID: componentIDValue,
            msoStatus: null,
            artifacts: []
            });
    }

    private addArtifactToComponentId(distributionID: string, componentPosition: number, artifactNameValue: string, artifactURLValue: any) {
        if (artifactNameValue) {
            this.distributionStatusesMap[distributionID][componentPosition].artifacts.push({
                artifactName: artifactNameValue,
                artifactUrl: artifactURLValue,
                statuses: []
            });
        }
    }

    private insertDistrbutionsToMap() {
        this.distributionList.map((distribution) => this.distributionStatusesMap[distribution.distributionID] = []);
    }

    private formatDate(epochTime: string) {
        const intEpochTime = new Date(parseInt(epochTime, 10));
        const amOrPm = (intEpochTime.getHours() + 24) % 24 > 12 ? 'PM' : 'AM';
        const formattedDate = (intEpochTime.getMonth() + 1) + '/' + intEpochTime.getDate() + '/' +  intEpochTime.getFullYear() + ' ' + intEpochTime.getHours() + ':' +
            intEpochTime.getMinutes() + amOrPm;
        return formattedDate;
    }
}
