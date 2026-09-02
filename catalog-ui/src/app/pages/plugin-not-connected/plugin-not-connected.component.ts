import {Component, Input} from "@angular/core";

@Component({
    selector: 'plugin-not-connected',
    templateUrl: './plugin-not-connected.component.html',
    styleUrls:['plugin-not-connected.component.less']
})
export class PluginNotConnectedComponent {

    @Input() pluginName: string;

    constructor() {

    }
}
