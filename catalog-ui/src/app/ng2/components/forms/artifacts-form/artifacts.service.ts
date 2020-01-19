import { Injectable } from '@angular/core';
import { Store } from '@ngxs/store';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { ArtifactModel } from '../../../../models';
import { ArtifactGroupType, ArtifactType } from '../../../../utils/constants';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { TranslateService } from '../../../shared/translator/translate.service';
import { CreateOrUpdateArtifactAction, DeleteArtifactAction } from '../../../store/actions/artifacts.action';
import { EnvParamsComponent } from '../env-params/env-params.component';
import { ArtifactFormComponent } from './artifact-form.component';

import {
    CreateInstanceArtifactAction,
    DeleteInstanceArtifactAction,
    UpdateInstanceArtifactAction
} from '../../../store/actions/instance-artifacts.actions';

@Injectable()
export class ArtifactsService {

    constructor(private serviceLoader: SdcUiServices.LoaderService,
                private modalService: SdcUiServices.ModalService,
                private topologyTemplateService: TopologyTemplateService,
                private translateService: TranslateService,
                private store: Store) {
    }

    public dispatchArtifactAction = (componentId: string, componentType: string, artifact: ArtifactModel, artifactType: ArtifactGroupType, instanceId: string) => {
        const artifactObj = {
            componentType,
            componentId,
            instanceId,
            artifact
        };

        // Create or update instance artifact
        if (instanceId) {
            if (!artifact.uniqueId) {
                // create instance artifact
                return this.store.dispatch(new CreateInstanceArtifactAction(artifactObj));
            } else {
                // update instance artifact
                return this.store.dispatch(new UpdateInstanceArtifactAction(artifactObj));
            }
        } else {
            // Create or update artifact
            return this.store.dispatch(new CreateOrUpdateArtifactAction(artifactObj));
        }
    }

    public openArtifactModal = (componentId: string, componentType: string, artifact: ArtifactModel, artifactType: ArtifactGroupType, isViewOnly?: boolean, instanceId?: string) => {

        let modalInstance;

        const onOkPressed = () => {
            const updatedArtifact = modalInstance.innerModalContent.instance.artifact;
            this.serviceLoader.activate();
            this.dispatchArtifactAction(componentId, componentType, updatedArtifact, artifactType, instanceId)
                    .subscribe().add(() => this.serviceLoader.deactivate());
        };

        const addOrUpdateArtifactModalConfig = {
            title: (artifact && artifact.uniqueId) ? 'Update Artifact' : 'Create Artifact',
            size: 'md',
            type: SdcUiCommon.ModalType.custom,
            testId: 'upgradeVspModal',
            buttons: [
                {
                    id: 'done',
                    text: 'DONE',
                    disabled: isViewOnly,
                    size: 'Add Another',
                    closeModal: true,
                    callback: onOkPressed
                },
                {text: 'CANCEL', size: 'sm', closeModal: true, type: 'secondary'}
            ] as SdcUiCommon.IModalButtonComponent[]
        } as SdcUiCommon.IModalConfig;

        modalInstance = this.modalService.openCustomModal(addOrUpdateArtifactModalConfig, ArtifactFormComponent, {
            artifact: new ArtifactModel(artifact),
            artifactType,
            instanceId,
            componentType,
            isViewOnly
        });

        if (!isViewOnly) {
            modalInstance.innerModalContent.instance.onValidationChange.subscribe((isValid) => {
                modalInstance.getButtonById('done').disabled = !isValid;
            });
        }
    }

    public openViewEnvParams(componentType: string, componentId: string, artifact: ArtifactModel, instanceId?: string) {
        const envParamsModal = {
            title: artifact.artifactDisplayName,
            size: 'xl',
            type: SdcUiCommon.ModalType.custom,
            testId: 'viewEnvParams',
            isDisabled: false,
        } as SdcUiCommon.IModalConfig;

        this.modalService.openCustomModal(envParamsModal, EnvParamsComponent, {
            isInstanceSelected: !!instanceId,    // equals to instanceId ? true : false
            artifact: new ArtifactModel(artifact),
            isViewOnly: true
        });
    }

    public openUpdateEnvParams(componentType: string, componentId: string, artifact: ArtifactModel, instanceId?: string) {
        let modalInstance;
        const onOkPressed = () => {
            const updatedArtifact = modalInstance.innerModalContent.instance.artifact;
            this.serviceLoader.activate();
            this.dispatchArtifactAction(componentId, componentType, updatedArtifact, ArtifactType.DEPLOYMENT, instanceId)
                    .subscribe().add(() => this.serviceLoader.deactivate());
        };

        const envParamsModal = {
            title: artifact.artifactDisplayName,
            size: 'xl',
            type: SdcUiCommon.ModalType.custom,
            testId: 'envParams',
            isDisabled: false,
            buttons: [
                {
                    id: 'save',
                    text: 'Save',
                    spinner_position: 'left',
                    size: 'sm',
                    callback: onOkPressed,
                    closeModal: true
                },
                {text: 'Cancel', size: 'sm', closeModal: true, type: 'secondary'}
            ] as SdcUiCommon.IModalButtonComponent[]
        } as SdcUiCommon.IModalConfig;

        modalInstance = this.modalService.openCustomModal(envParamsModal, EnvParamsComponent, {
            isInstanceSelected: !!instanceId,    // equals to instanceId ? true : false
            artifact: new ArtifactModel(artifact)
        });

        modalInstance.innerModalContent.instance.onValidationChange.subscribe((isValid) => {
            modalInstance.getButtonById('save').disabled = !isValid;
        });
    }

    public deleteArtifact = (componentType: string, componentId: string, artifact: ArtifactModel, instanceId?: string) => {

        const artifactObject = {
            componentType,
            componentId,
            artifact,
            instanceId
        };

        const onOkPressed: Function = () => {
            this.serviceLoader.activate();
            this.store.dispatch((instanceId) ? new DeleteInstanceArtifactAction(artifactObject) : new DeleteArtifactAction(artifactObject))
                    .subscribe().add(() => this.serviceLoader.deactivate());
        };

        const title = this.translateService.translate('ARTIFACT_VIEW_DELETE_MODAL_TITLE');
        const text = this.translateService.translate('ARTIFACT_VIEW_DELETE_MODAL_TEXT', {name: artifact.artifactDisplayName});
        const okButton = {
            testId: 'OK',
            text: 'OK',
            type: SdcUiCommon.ButtonType.warning,
            callback: onOkPressed,
            closeModal: true
        } as SdcUiComponents.ModalButtonComponent;
        this.modalService.openWarningModal(title, text, 'delete-information-artifact-modal', [okButton]);
    }
}
