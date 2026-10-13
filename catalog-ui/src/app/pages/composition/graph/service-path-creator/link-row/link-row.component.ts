import { Component, Input } from '@angular/core';
import { DropdownValue } from 'app/components/ui/form-components/dropdown/ui-element-dropdown.component';
import { ServicePathMapItem } from 'app/models/graph/nodes-and-links-map';
import * as _ from 'lodash';
import { Link } from './link.model';

@Component({
    selector: 'link-row',
    templateUrl: './link-row.component.html',
    styleUrls: ['./link-row.component.less']
})

export class LinkRowComponent {
    @Input() data: ServicePathMapItem[];
    @Input() link: Link;
    @Input() removeRow: Function;
    source: DropdownValue[] = [];
    target: DropdownValue[] = [];
    srcCP: DropdownValue[] = [];
    targetCP: DropdownValue[] = [];

    ngOnChanges() {
        if (this.data) {
            this.parseInitialData(this.data);
        }
    }

    parseInitialData(data: ServicePathMapItem[]) {
        this.source = this.convertValuesToDropDownOptions(data);
        if (this.link.fromNode) {
            const srcCPOptions = this.findOptions(data, this.link.fromNode);
            if (!srcCPOptions) { return; }
            this.srcCP = this.convertValuesToDropDownOptions(srcCPOptions);
            if (this.link.fromCP) {
                this.target = this.convertValuesToDropDownOptions(data);
                if (this.link.toNode) {
                    const targetCPOptions = this.findOptions(data, this.link.toNode);
                    if (!targetCPOptions) { return; }
                    this.targetCP = this.convertValuesToDropDownOptions(targetCPOptions);
                }
            }
        }
    }

    onSourceSelected(id) {
        if (id) {
            const srcCPOptions = this.findOptions(this.data, id);
            this.srcCP = this.convertValuesToDropDownOptions(srcCPOptions);
            this.link.fromCP = '';
            this.link.toNode = '';
            this.link.toCP = '';
            this.target = [];
            this.targetCP = [];
        }
    }

    onSrcCPSelected(id) {
        if (id) {
            const srcCPOptions = this.findOptions(this.data, this.link.fromNode);
            const srcCPData = srcCPOptions.find((option) => id === option.id).data;
            this.target = this.convertValuesToDropDownOptions(this.data);
            this.link.fromCPOriginId = srcCPData.ownerId;
            this.link.toNode = '';
            this.link.toCP = '';
            this.targetCP = [];
        }

    }

    onTargetSelected(id) {
        if (id) {
            const targetCPOptions = this.findOptions(this.data, id);
            this.targetCP = this.convertValuesToDropDownOptions(targetCPOptions);
            this.link.toCP = '';
        }

    }

    onTargetCPSelected(id) {
        if (id) {
            const targetCPOptions = this.findOptions(this.data, this.link.toNode);
            const targetCPDataObj = targetCPOptions.find((option) => id === option.id).data;
            this.link.toCPOriginId = targetCPDataObj.ownerId;
        }
    }

    private findOptions(items: ServicePathMapItem[], nodeOrCPId: string) {
        const item = _.find(items, (dataItem) => nodeOrCPId === dataItem.id);
        if (item && item.data && item.data.options) {
            return item.data.options;
        }
        console.warn('no option was found to match selection of Node/CP with id:' + nodeOrCPId);
        return null;
    }

    private convertValuesToDropDownOptions(values: ServicePathMapItem[]): DropdownValue[] {
        const result: DropdownValue[] = [];
        for (let i = 0; i < values.length ; i++) {
            result[result.length] =  new DropdownValue(values[i].id, values[i].data.name);
        }
        return result.sort((a, b) => a.label.localeCompare(b.label));
    }
}
