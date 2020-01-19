import { Component, OnInit, ViewChild } from '@angular/core';
import { Select } from '@ngxs/store';
import { IAttributeModel } from 'app/models';
import * as _ from 'lodash';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { ModalComponent } from 'onap-ui-angular/dist/modals/modal.component';
import { AttributeModel } from '../../../../models';
import { Resource } from '../../../../models';
import { ModalsHandler } from '../../../../utils';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { TranslateService } from '../../../shared/translator/translate.service';
import { WorkspaceState } from '../../../store/states/workspace.state';
import { WorkspaceService } from '../workspace.service';
import { AttributeModalComponent } from './attribute-modal.component';

@Component({
    selector: 'attributes',
    templateUrl: './attributes.component.html',
    styleUrls: ['./attributes.component.less', '../../../../../assets/styles/table-style.less']
})
export class AttributesComponent implements OnInit {

    @Select(WorkspaceState.isViewOnly)
    isViewOnly$: boolean;

    @ViewChild('componentAttributesTable')
    private table: any;

    private componentType: string;
    private componentUid: string;

    private attributes: IAttributeModel[] = [];
    private temp: IAttributeModel[] = [];
    private customModalInstance: ModalComponent;

    constructor(private workspaceService: WorkspaceService,
                private topologyTemplateService: TopologyTemplateService,
                private modalsHandler: ModalsHandler,
                private modalService: SdcUiServices.ModalService,
                private loaderService: SdcUiServices.LoaderService,
                private translateService: TranslateService) {

        this.componentType = this.workspaceService.metadata.componentType;
        this.componentUid = this.workspaceService.metadata.uniqueId;
    }

    ngOnInit(): void {
        this.asyncInitComponent();
    }

    async asyncInitComponent() {
        this.loaderService.activate();
        const response = await this.topologyTemplateService.getComponentAttributes(this.componentType, this.componentUid);
        this.attributes = response.attributes;
        this.temp = [...response.attributes];
        this.loaderService.deactivate();
    }

    getAttributes(): IAttributeModel[] {
        return this.attributes;
    }

    addOrUpdateAttribute = async (attribute: AttributeModel, isEdit: boolean) => {
        this.loaderService.activate();
        let attributeFromServer: AttributeModel;
        this.temp = [...this.attributes];

        const deactivateLoader = () => {
            this.loaderService.deactivate();
            return undefined;
        };

        if (isEdit) {
            attributeFromServer = await this.topologyTemplateService
                                    .updateAttributeAsync(this.componentType, this.componentUid, attribute)
                                    .catch(deactivateLoader);
            if (attributeFromServer) {
                const indexOfUpdatedAttribute = _.findIndex(this.temp, (e) => e.uniqueId === attributeFromServer.uniqueId);
                this.temp[indexOfUpdatedAttribute] = attributeFromServer;
            }
        } else {
            attributeFromServer = await this.topologyTemplateService
                                    .addAttributeAsync(this.componentType, this.componentUid, attribute)
                                    .catch(deactivateLoader);
            if (attributeFromServer) {
                this.temp.push(attributeFromServer);
            }
        }
        this.attributes = this.temp;
        this.loaderService.deactivate();
    }

    deleteAttribute = async (attributeToDelete: AttributeModel) => {
        this.loaderService.activate();
        this.temp = [...this.attributes];
        const res = await this.topologyTemplateService.deleteAttributeAsync(this.componentType, this.componentUid, attributeToDelete);
        _.remove(this.temp, (attr) => attr.uniqueId === attributeToDelete.uniqueId);
        this.attributes = this.temp;
        this.loaderService.deactivate();
    };

    openAddEditModal(selectedRow: AttributeModel, isEdit: boolean) {
        const component = new Resource(undefined, undefined, undefined);
        component.componentType = this.componentType;
        component.uniqueId = this.componentUid;

        const title: string = this.translateService.translate('ATTRIBUTE_DETAILS_MODAL_TITLE');
        const attributeModalConfig = {
            title,
            size: 'md',
            type: SdcUiCommon.ModalType.custom,
            buttons: [
                {
                    id: 'save',
                    text: 'Save',
                    // spinner_position: Placement.left,
                    size: 'sm',
                    callback: () => this.modalCallBack(isEdit),
                    closeModal: true,
                    disabled: false,
                }
            ] as SdcUiCommon.IModalButtonComponent[]
        };

        this.customModalInstance = this.modalService.openCustomModal(attributeModalConfig, AttributeModalComponent, { attributeToEdit: selectedRow });
        this.customModalInstance.innerModalContent.instance.
            onValidationChange.subscribe((isValid) => this.customModalInstance.getButtonById('save').disabled = !isValid);
    }

    /***********************
     * Call Backs from UI  *
     ***********************/

    /**
     * Called when 'Add' is clicked
     */
    onAddAttribute() {
        this.openAddEditModal(new AttributeModel(), false);
    }

    /**
     * Called when 'Edit' button is clicked
     */
    onEditAttribute(event, row) {
        event.stopPropagation();

        const attributeToEdit: AttributeModel = new AttributeModel(row);
        this.openAddEditModal(attributeToEdit, true);
    }

    /**
     * Called when 'Delete' button is clicked
     */
    onDeleteAttribute(event, row: AttributeModel) {
        event.stopPropagation();
        const onOk = () => {
            this.deleteAttribute(row);
        };

        const title: string = this.translateService.translate('ATTRIBUTE_VIEW_DELETE_MODAL_TITLE');
        const message: string = this.translateService.translate('ATTRIBUTE_VIEW_DELETE_MODAL_TEXT');
        const okButton = new SdcUiComponents.ModalButtonComponent();
        okButton.testId = 'OK';
        okButton.text = 'OK';
        okButton.type = SdcUiCommon.ButtonType.info;
        okButton.closeModal = true;
        okButton.callback = onOk;

        this.modalService.openInfoModal(title, message, 'delete-modal', [okButton]);
    }

    onExpandRow(event) {
        if (event.type === 'click') {
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }

    /**
     * Callback from Modal after "Save" is clicked
     *
     * @param {boolean} isEdit - Whether modal is edit or add attribute
     */
    modalCallBack = (isEdit: boolean) => {
        const attribute: AttributeModel = this.customModalInstance.innerModalContent.instance.attributeToEdit;
        this.addOrUpdateAttribute(attribute, isEdit);
    }

}
