/**
 * Created by rc2122 on 5/31/2018.
 */
import { Component, Input } from '@angular/core';
import * as _ from 'lodash';
import { IDropDownOption } from 'onap-ui-angular/dist/form-elements/dropdown/dropdown-models';
import { Subject } from 'rxjs/Subject';
import { ArtifactModel } from '../../../../models';
import { ArtifactType, ComponentType } from '../../../../utils';
import { Dictionary } from '../../../../utils/dictionary/dictionary';
import { CacheService } from '../../../services/cache.service';

@Component({
    selector: 'artifact-form',
    templateUrl: './artifact-form.component.html',
    styleUrls: ['./artifact-form.component.less']
})
export class ArtifactFormComponent {

    @Input() artifact: ArtifactModel;
    @Input() artifactType: ArtifactType;
    @Input() componentType: string;
    @Input() instanceId: string;
    @Input() isViewOnly: boolean;

    public artifactTypesOptions: IDropDownOption[] = [];
    public validationPatterns: Dictionary<string, RegExp>;
    public selectedFileType: IDropDownOption;
    public showTypeFields: boolean;
    private onValidationChange: Subject<boolean> = new Subject();
    private descriptionIsValid: boolean;
    private labelIsValid: boolean;

    constructor(private cacheService: CacheService) {
    }

    ngOnInit(): void {
        this.validationPatterns = this.cacheService.get('validation').validationPatterns;
        this.initArtifactTypes();
        this.artifact.artifactGroupType = this.artifact.artifactGroupType || this.artifactType.toString();
        this.showTypeFields = (this.artifact.artifactGroupType === 'DEPLOYMENT' || !this.artifact.mandatory) && this.artifact.artifactGroupType !== 'SERVICE_API';
    }

    public onTypeChange = (selectedFileType: IDropDownOption) => {
        this.artifact.artifactType = selectedFileType.value;
        this.verifyTypeAndFileWereFilled();
    }

    public onUploadFile = (file) => {
        if (file) {
            this.artifact.artifactName = file.filename;
            this.artifact.payloadData = file.base64;
            console.log('FILE UPLOADED', file);
        } else {
            this.artifact.artifactName = null;
        }
        this.verifyTypeAndFileWereFilled();
    }

    private initArtifactTypes = (): void => {
        const artifactTypes: any = this.cacheService.get('UIConfiguration');
        let validExtensions: string[];
        let artifactTypesList: string[];

        switch (this.artifactType) {
            case ArtifactType.DEPLOYMENT:
                if (this.artifact.artifactType === ArtifactType.HEAT_ENV || this.instanceId) {
                    validExtensions = artifactTypes.artifacts.deployment.resourceInstanceDeploymentArtifacts;
                } else if (this.componentType === ComponentType.RESOURCE) {
                    validExtensions = artifactTypes.artifacts.deployment.resourceDeploymentArtifacts;
                } else {
                    validExtensions = artifactTypes.artifacts.deployment.serviceDeploymentArtifacts;
                }
                if (validExtensions) {
                    artifactTypesList = Object.keys(validExtensions);
                }
                break;
            case ArtifactType.INFORMATION:
                artifactTypesList = artifactTypes.artifacts.other.map((element: any) => {
                    return element.name;
                });
                _.remove(artifactTypesList, (item: string) => {
                    return _.has(ArtifactType.THIRD_PARTY_RESERVED_TYPES, item) ||
                        _.has(ArtifactType.TOSCA, item);
                });
                break;
        }

        _.forEach(artifactTypesList, (artifactType: string) => {
            this.artifactTypesOptions.push({ label: artifactType, value: artifactType });
        });

        this.selectedFileType = _.find(this.artifactTypesOptions, (artifactType) => {
            return artifactType.value === this.artifact.artifactType;
        });

    }

    // Verify that the Type and the Name (file) are filled in the Modal
    // For Description and Label - I used this.descriptionIsValid:boolean & this.labelIsValid:boolean as part of the sdc-validation Element
    private verifyTypeAndFileWereFilled = () => {
        if (this.artifact.artifactType === 'DEPLOYMENT' || !this.artifact.mandatory && this.artifact.artifactGroupType !== 'SERVICE_API') {
            // In case of all fields are required:
            // File, Description, Type and Label
            if (this.artifact.artifactType && this.artifact.artifactName && this.descriptionIsValid && this.labelIsValid) {
                this.onValidationChange.next(true);
            } else {
                this.onValidationChange.next(false);
            }
        } else {
            // In case of like Information Artifact
            // Only file and description are required
            if (this.descriptionIsValid && this.artifact.artifactName) {
                this.onValidationChange.next(true);
            } else {
                this.onValidationChange.next(false);
            }
        }
    }

    // sdc-validation for Description
    private onDescriptionChange = (isValid: boolean): void => {
        this.descriptionIsValid = isValid;
        this.onValidationChange.next(isValid) && this.verifyTypeAndFileWereFilled();
    }

    // sdc-validation for Label
    private onLabelChange = (isValid: boolean): void => {
        this.labelIsValid = isValid;
        this.onValidationChange.next(isValid) && this.verifyTypeAndFileWereFilled();
    }
}
