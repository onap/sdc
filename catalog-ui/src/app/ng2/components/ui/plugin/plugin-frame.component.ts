import {Component, Inject, Input, Output, OnInit, EventEmitter, ViewChild, ElementRef} from "@angular/core";
import {URLSearchParams} from '@angular/http';
import {Plugin} from "app/models";
import {EventBusService} from "../../../services/event-bus.service";

@Component({
    selector: 'plugin-frame',
    templateUrl: './plugin-frame.component.html',
    styleUrls: ['plugin-frame.component.less']
})

export class PluginFrameComponent implements OnInit {

    @Input() plugin: Plugin;
    @Input() queryParams: Object;
    @Output() onLoadingDone: EventEmitter<void> = new EventEmitter<void>();
    pluginUrl: string;
    private urlSearchParams: URLSearchParams;
    private isClosed: boolean;

    constructor(private eventBusService: EventBusService,
                @Inject('$scope') private $scope: ng.IScope,
                @Inject('$state') private $state: ng.ui.IStateService) {
        this.urlSearchParams = new URLSearchParams();
    }

    ngOnInit(): void {
        this.pluginUrl = this.plugin.pluginSourceUrl;
        this.isClosed = false;

        if (this.queryParams && !_.isEmpty(this.queryParams)) {
            _.forOwn(this.queryParams, (value, key) => {
                this.urlSearchParams.set(key, value);
            });

            this.pluginUrl += '?';
            this.pluginUrl += this.urlSearchParams.toString();
        }

        this.eventBusService.on((eventData) => {
            if (eventData.originId === this.plugin.pluginId) {
                if (eventData.type == "READY") {
                    this.onLoadingDone.emit();
                }
            }
        });

        // Listening to the stateChangeStart event in order to notify the plugin about it being closed
        // before moving to a new state
        this.$scope.$on('$stateChangeStart', (event, toState, toParams, fromState, fromParams) => {
            if ((fromState.name !== toState.name) || (fromState.name === toState.name) && (toParams.path !== fromParams.path)) {
                if (!this.isClosed) {
                    event.preventDefault();

                    this.eventBusService.notify("WINDOW_OUT");

                    this.isClosed = true;

                    setTimeout(() => {
                        this.$state.go(toState.name, toParams);
                    });
                }
            }
        });
    }
}
