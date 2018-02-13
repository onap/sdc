import { Injectable } from '@angular/core';
import {BasePubSub, IPubSubEvent} from "../../models/base-pubsub";

@Injectable()
export class EventBusService extends BasePubSub {

    constructor() {
        super("sdc-hub");
    }

    protected handlePluginRegistration(eventData: IPubSubEvent, event: any) {
        if (eventData.type === 'pluginRegister') {
            this.register(eventData.data.pluginId, event.source, event.origin);
        } else if (eventData.type === 'pluginUnregister') {
            this.unregister(eventData.data.pluginId);
        }
    }

    public unregister(pluginId: string) {
        const unregisterData = {
            pluginId: pluginId
        };

        this.notify('pluginClose', unregisterData);
        super.unregister(pluginId);
    }

    protected onMessage(event: any) {
        if (event.data.type === 'pluginRegister' || event.data.type === 'pluginUnregister') {
            this.handlePluginRegistration(event.data, event);
        }
        super.onMessage(event);
    }
}
