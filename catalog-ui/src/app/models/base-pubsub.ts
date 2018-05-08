declare const window: Window;

export class BasePubSub {

    subscribers: Map<string, ISubscriber>;
    eventsCallbacks: Array<Function>;
    clientId: string;
    eventsToWait: Map<string, Array<string>>;
    lastEventNotified: string;

    constructor(pluginId: string) {
        this.subscribers = new Map<string, ISubscriber>();
        this.eventsCallbacks = [];
        this.eventsToWait = new Map<string, Array<string>>();
        this.clientId = pluginId;
        this.lastEventNotified = "";
        this.onMessage = this.onMessage.bind(this);

        window.addEventListener("message", this.onMessage);
    }

    public register(subscriberId: string, subscriberWindow: Window, subscriberUrl: string) {
        const subscriber = {
            window: subscriberWindow,
            locationUrl: subscriberUrl || subscriberWindow.location.href
        } as ISubscriber;

        this.subscribers.set(subscriberId, subscriber);
    }

    public unregister(subscriberId: string) {
        this.subscribers.delete(subscriberId);
    }

    public on(callback: Function) {
        let functionExists = this.eventsCallbacks.find((func: Function) => {
            return callback.toString() == func.toString()
        });

        if (!functionExists) {
            this.eventsCallbacks.push(callback);
        }
    }

    public off(callback: Function) {
        let index = this.eventsCallbacks.indexOf(callback);
        this.eventsCallbacks.splice(index, 1)
    }

    public notify(eventType:string, eventData?:any) {
        let eventObj = {
            type: eventType,
            data: eventData,
            originId: this.clientId
        } as IPubSubEvent;

        this.subscribers.forEach( (subscriber: ISubscriber, subscriberId: string) => {
            subscriber.window.postMessage(eventObj, subscriber.locationUrl);
        });

        this.lastEventNotified = eventType;

        return {
            subscribe: function(callbackFn) {

                if(this.subscribers.size !== 0) {
                    let subscribersToNotify = Array.from(this.subscribers.keys());

                    const checkNotifyComplete = (subscriberId: string) => {

                        let index = subscribersToNotify.indexOf(subscriberId);
                        subscribersToNotify.splice(index, 1);

                        if (subscribersToNotify.length === 0) {
                            callbackFn();
                        }
                    };

                    this.subscribers.forEach((subscriber: ISubscriber, subscriberId: string) => {
                        if (this.eventsToWait.has(subscriberId) && this.eventsToWait.get(subscriberId).indexOf(eventType) !== -1) {

                            const actionCompletedFunction = (eventData, subId = subscriberId) => {
                                if (eventData.type == "ACTION_COMPLETED") {
                                    checkNotifyComplete(subId);
                                }
                                this.off(actionCompletedFunction);

                            };
                            this.on(actionCompletedFunction);
                        }
                        else {
                            checkNotifyComplete(subscriberId);
                        }
                    });
                }
                else {
                    callbackFn();
                }
            }.bind(this)
        }
    }

    public isWaitingForEvent(eventName: string) : boolean {
        return Array.from(this.eventsToWait.values()).some((eventsList: Array<string>) =>
            eventsList.indexOf(eventName) !== -1
        );
    }

    protected onMessage(event: any) {
        if (this.subscribers.has(event.data.originId)) {
            this.eventsCallbacks.forEach((callback: Function) => {
                callback(event.data, event);
            })
        }
    }
}

export interface IPubSubEvent {
    type: string;
    originId: string;
    data: any;
}

export interface ISubscriber {
    window: Window;
    locationUrl: string;
}
