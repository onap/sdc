import {WorkspaceState, WorkspaceStateModel} from './workspace.state';
import {UpdateIsViewOnly, UpdateIsDesigner} from '../actions/workspace.action';

describe('WorkspaceState', () => {
    let state: WorkspaceState;

    beforeEach(() => {
        state = new WorkspaceState();
    });

    it('should be created', () => {
        expect(state).toBeTruthy();
    });

    describe('updateIsViewOnly action', () => {
        it('should set isViewOnly to true', () => {
            const currentState: WorkspaceStateModel = {isViewOnly: false, isDesigner: true};
            const context = {
                getState: jest.fn(() => currentState),
                setState: jest.fn(),
                patchState: jest.fn(),
                dispatch: jest.fn()
            };

            const action = new UpdateIsViewOnly(true);
            state.updateIsViewOnly(context, action);

            expect(context.setState).toHaveBeenCalledWith({
                isViewOnly: true,
                isDesigner: true
            });
        });

        it('should set isViewOnly to false', () => {
            const currentState: WorkspaceStateModel = {isViewOnly: true, isDesigner: true};
            const context = {
                getState: jest.fn(() => currentState),
                setState: jest.fn(),
                patchState: jest.fn(),
                dispatch: jest.fn()
            };

            const action = new UpdateIsViewOnly(false);
            state.updateIsViewOnly(context, action);

            expect(context.setState).toHaveBeenCalledWith({
                isViewOnly: false,
                isDesigner: true
            });
        });
    });

    describe('updateIsDesigner action', () => {
        it('should set isDesigner to true', () => {
            const currentState: WorkspaceStateModel = {isViewOnly: false, isDesigner: false};
            const context = {
                getState: jest.fn(() => currentState),
                setState: jest.fn(),
                patchState: jest.fn(),
                dispatch: jest.fn()
            };

            const action = new UpdateIsDesigner(true);
            state.updateIsDesigner(context, action);

            expect(context.patchState).toHaveBeenCalledWith({
                isDesigner: true
            });
        });

        it('should set isDesigner to false', () => {
            const currentState: WorkspaceStateModel = {isViewOnly: false, isDesigner: true};
            const context = {
                getState: jest.fn(() => currentState),
                setState: jest.fn(),
                patchState: jest.fn(),
                dispatch: jest.fn()
            };

            const action = new UpdateIsDesigner(false);
            state.updateIsDesigner(context, action);

            expect(context.patchState).toHaveBeenCalledWith({
                isDesigner: false
            });
        });
    });
});
