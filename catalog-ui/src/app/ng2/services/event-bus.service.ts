import { Injectable } from '@angular/core';
import {BasePubSub, IPubSubEvent} from "../../models/base-pubsub";

@Injectable()
export class EventBusService extends BasePubSub {

    constructor() {
        super("sdc-hub");
    }

    protected handlePluginRegistration(eventData: IPubSubEvent, event: any) {
        if (eventData.type === 'PLUGIN_REGISTER') {
            this.register(eventData.data.pluginId, event.source, event.origin);

            let newEventsList = [];

            if (this.eventsToWait.has(eventData.data.pluginId)) {
                newEventsList = _.union(this.eventsToWait.get(eventData.data.pluginId), eventData.data.eventsToWait);
            }
            else {
                newEventsList = eventData.data.eventsToWait;
            }

            this.eventsToWait.set(eventData.data.pluginId, newEventsList);

        } else if (eventData.type === 'PLUGIN_UNREGISTER') {
            this.unregister(eventData.data.pluginId);
        }
    }

    public unregister(pluginId: string) {
        const unregisterData = {
            pluginId: pluginId
        };

        this.notify('PLUGIN_CLOSE', unregisterData).subscribe(() => {
            super.unregister(pluginId);
        });
    }

    protected onMessage(event: any) {
        if (event.data.type === 'PLUGIN_REGISTER') {
            this.handlePluginRegistration(event.data, event);
        }

        super.onMessage(event);

        if (event.data.type === 'PLUGIN_UNREGISTER') {
            this.handlePluginRegistration(event.data, event);
        }
    }
}
