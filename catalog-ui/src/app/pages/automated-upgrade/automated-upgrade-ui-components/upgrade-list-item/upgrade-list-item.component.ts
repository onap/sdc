import { Component, EventEmitter, Input, Output } from '@angular/core';
import { AutomatedUpgradeInstanceType, ServiceContainerToUpgradeUiObject } from '../../automated-upgrade-models/ui-component-to-upgrade';

@Component({
    selector: 'upgrade-list-item',
    templateUrl: './upgrade-list-item.component.html',
    styleUrls: ['../upgrade-list-item.component.less']
})
export class UpgradeListItemComponent {

    @Input() componentToUpgrade: ServiceContainerToUpgradeUiObject;
    @Input() disabled: boolean;
    @Output() onCheckedChange: EventEmitter<any> = new EventEmitter<any>();

    constructor() {
    }

    onComponentChecked = (): void => {
        this.onCheckedChange.emit();
    }

}
