/**
 * Created by obarda on 3/13/2016.
 */
'use strict';
import {EventListenerService} from "./event-listener-service";
import {EVENTS} from "../utils/constants";

export class LoaderService {


    constructor(private eventListenerService:EventListenerService) {

    }

    public showLoader(...args) {
        this.eventListenerService.notifyObservers(EVENTS.SHOW_LOADER_EVENT, ...args);
    }

    public hideLoader(...args) {
        this.eventListenerService.notifyObservers(EVENTS.HIDE_LOADER_EVENT, ...args);
    }
}

LoaderService.$inject = ['EventListenerService'];
