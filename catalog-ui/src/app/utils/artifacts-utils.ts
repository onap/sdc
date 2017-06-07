import {ArtifactModel} from "../models/artifacts";
import {IArtifactResourceFormViewModelScope} from "../view-models/forms/artifact-form/artifact-form-view-model";
import {Component} from "../models/components/component";
import {ArtifactGroupType, ArtifactType} from "./constants";
export class ArtifactsUtils {

    static '$inject' = [
        '$filter'
    ];

    constructor(private $filter:ng.IFilterService) {

    }

    public getArtifactTypeByState(currentState:string):string {
        switch (currentState) {
            case "workspace.composition.lifecycle":
                return "interface";
            case "workspace.composition.api":
                return "api";
            case "workspace.deployment_artifacts":
            case "workspace.composition.deployment":
                return "deployment";
            case "workspace.composition.artifacts":
                return "informational";
            default:
                return "informational";
        }
    }

    public getTitle(artifactType:string, selectedComponent:Component):string {
        switch (artifactType) {
            case "interface":
                return "Lifecycle Management";
            case "api":
                return "API Artifacts";
            case "deployment":
                return "Deployment Artifacts";
            case "informational":
                return "Informational Artifacts";
            default:
                if (!selectedComponent) {
                    return "";
                } else {
                    return this.$filter("resourceName")(selectedComponent.name) + ' Artifacts';
                }
        }
    }

    public setArtifactType = (artifact:ArtifactModel, artifactType:string):void => {
        switch (artifactType) {
            case "api":
                artifact.artifactGroupType = ArtifactGroupType.SERVICE_API;
                break;
            case "deployment":
                artifact.artifactGroupType = ArtifactGroupType.DEPLOYMENT;
                break;
            default:
                artifact.artifactGroupType = ArtifactGroupType.INFORMATION;
                break;
        }
    };

    public isLicenseType = (artifactType:string):boolean => {
        let isLicense:boolean = false;

        if (ArtifactType.VENDOR_LICENSE === artifactType || ArtifactType.VF_LICENSE === artifactType) {
            isLicense = true;
        }

        return isLicense;
    };

    public removeArtifact = (artifact:ArtifactModel, artifactsArr:Array<ArtifactModel>):void => {

        if (!artifact.mandatory && (ArtifactGroupType.INFORMATION == artifact.artifactGroupType ||
            ArtifactGroupType.DEPLOYMENT == artifact.artifactGroupType)) {
            _.remove(artifactsArr, {uniqueId: artifact.uniqueId});
        }
        else {
            let artifactToDelete = _.find(artifactsArr, {uniqueId: artifact.uniqueId});

            delete artifactToDelete.esId;
            delete artifactToDelete.description;
            delete artifactToDelete.artifactName;
            delete artifactToDelete.apiUrl;
        }
    };

    public addAnotherAfterSave(scope:IArtifactResourceFormViewModelScope) {
        let newArtifact = new ArtifactModel();
        this.setArtifactType(newArtifact, scope.artifactType);
        scope.editArtifactResourceModel.artifactResource = newArtifact;

        scope.forms.editForm['description'].$setPristine();
        if (scope.forms.editForm['artifactLabel']) {
            scope.forms.editForm['artifactLabel'].$setPristine();
        }
        if (scope.forms.editForm['type']) {
            scope.forms.editForm['type'].$setPristine();
        }

    }
}
