import {Component, OnInit, Input} from "@angular/core";
import { URLSearchParams } from '@angular/http';
import {Plugin} from "app/models";

@Component({
    selector: 'plugin-frame',
    templateUrl: './plugin-frame.component.html',
    styleUrls:['plugin-frame.component.less']
})

export class PluginFrameComponent implements OnInit {

    @Input() plugin: Plugin;
    @Input() queryParams: Object;
    pluginUrl: string;
    private urlSearchParams: URLSearchParams;

    constructor() {
        this.urlSearchParams = new URLSearchParams();
    }

    ngOnInit(): void {

        this.pluginUrl = this.plugin.pluginProtocol + "://" +
            this.plugin.pluginHost + ":" +
            this.plugin.pluginPort +
            this.plugin.pluginPath;

        if (this.queryParams && !_.isEmpty(this.queryParams)) {
            _.forOwn(this.queryParams, (value, key) => {
                this.urlSearchParams.set(key, value);
            });

            this.pluginUrl += '?';
            this.pluginUrl += this.urlSearchParams.toString();
        }
    }
}
