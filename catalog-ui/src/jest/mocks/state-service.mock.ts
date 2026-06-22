import {States, WorkspaceMode} from 'app/utils/constants';

export function createMockState(overrides: any = {}): any {
    return {
        current: {name: States.WORKSPACE_GENERAL, data: {unsavedChanges: false}},
        params: {id: 'unique-id-1', type: 'resource', previousState: 'dashboard', components: []},
        go: jest.fn(),
        ...overrides
    };
}

export function createMockStateParams(overrides: any = {}): any {
    return {
        id: 'unique-id-1',
        type: 'resource',
        previousState: 'dashboard',
        componentCsar: undefined,
        disableButtons: false,
        ...overrides
    };
}

export function createMockQ(): any {
    return {
        defer: jest.fn(() => {
            let resolveFn: Function;
            let rejectFn: Function;
            const promise = new Promise((resolve, reject) => {
                resolveFn = resolve;
                rejectFn = reject;
            });
            return {promise, resolve: resolveFn, reject: rejectFn};
        }),
        when: jest.fn((val) => Promise.resolve(val)),
        resolve: jest.fn((val) => Promise.resolve(val)),
        reject: jest.fn((val) => Promise.reject(val))
    };
}

export function createMockFilter(): any {
    return jest.fn((filterName: string) => {
        if (filterName === 'translate') return (key: string) => key;
        if (filterName === 'resourceName') return (name: string) => name;
        return (val: any) => val;
    });
}
