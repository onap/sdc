import * as _ from 'lodash';
(window as any)._ = _;

import {EventBusService} from './event-bus.service';

describe('EventBusService', () => {
    let service: EventBusService;

    beforeEach(() => {
        service = new EventBusService();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should initialize with NoWindowOutEvents', () => {
        expect(service.NoWindowOutEvents).toContain('CHECK_IN');
        expect(service.NoWindowOutEvents).toContain('SUBMIT_FOR_TESTING');
        expect(service.NoWindowOutEvents).toContain('UNDO_CHECK_OUT');
    });

    describe('unregister', () => {
        it('should call notify with PLUGIN_CLOSE event', () => {
            const notifySpy = jest.spyOn(service, 'notify').mockReturnValue({
                subscribe: (cb) => cb()
            });
            const superUnregister = jest.fn();
            Object.getPrototypeOf(Object.getPrototypeOf(service)).unregister = superUnregister;

            service.unregister('test-plugin');

            expect(notifySpy).toHaveBeenCalledWith('PLUGIN_CLOSE', {pluginId: 'test-plugin'});
        });
    });

    describe('disableNavigation', () => {
        beforeEach(() => {
            document.body.innerHTML = '<iframe class="plugin-iframe"></iframe>';
        });

        afterEach(() => {
            document.body.innerHTML = '';
        });

        it('should add disable-navigation-div when disabling', () => {
            service.disableNavigation(true);
            const disableDiv = document.getElementsByClassName('disable-navigation-div');
            expect(disableDiv.length).toBe(1);
        });

        it('should set iframe z-index when disabling', () => {
            service.disableNavigation(true);
            const iframe = document.getElementsByClassName('plugin-iframe')[0] as HTMLElement;
            expect(iframe.style.zIndex).toBe('1300');
        });

        it('should remove disable-navigation-div when enabling', () => {
            service.disableNavigation(true);
            service.disableNavigation(false);
            const disableDiv = document.getElementsByClassName('disable-navigation-div');
            expect(disableDiv.length).toBe(0);
        });

        it('should reset iframe z-index when enabling', () => {
            service.disableNavigation(true);
            service.disableNavigation(false);
            const iframe = document.getElementsByClassName('plugin-iframe')[0] as HTMLElement;
            expect(iframe.style.zIndex).toBe('');
        });
    });
});
