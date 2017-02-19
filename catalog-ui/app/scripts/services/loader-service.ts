/**
 * Created by obarda on 3/13/2016.
 */
/// <reference path="../references"/>
module Sdc.Services {
    'use strict';

    export class LoaderService {


        constructor(private eventListenerService: Services.EventListenerService) {

        }

        public showLoader(...args) {
            this.eventListenerService.notifyObservers(Utils.Constants.EVENTS.SHOW_LOADER_EVENT, ...args);
        }

        public hideLoader(...args) {
            this.eventListenerService.notifyObservers(Utils.Constants.EVENTS.HIDE_LOADER_EVENT, ...args);
        }

    }

    LoaderService.$inject = ['EventListenerService'];
}
