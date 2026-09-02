import {ComponentState, ComponentType, WorkspaceMode} from 'app/utils/constants';

export function createMockComponent(overrides: any = {}): any {
    return {
        uniqueId: 'unique-id-1',
        name: 'TestComponent',
        componentType: ComponentType.RESOURCE,
        resourceType: 'VF',
        lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
        lastUpdaterUserId: 'cs0008',
        version: '0.1',
        uuid: 'uuid-1234',
        lastUpdateDate: Date.now(),
        isService: jest.fn(() => overrides.componentType === ComponentType.SERVICE),
        isResource: jest.fn(() => (overrides.componentType || ComponentType.RESOURCE) !== ComponentType.SERVICE),
        ...overrides
    };
}

export function createCheckedOutComponent(userId: string = 'cs0008') {
    return createMockComponent({
        lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
        lastUpdaterUserId: userId,
        componentType: ComponentType.RESOURCE
    });
}

export function createCertifiedComponent() {
    return createMockComponent({
        lifecycleState: ComponentState.CERTIFIED
    });
}

export function createServiceComponent(overrides: any = {}) {
    return createMockComponent({
        componentType: ComponentType.SERVICE,
        resourceType: undefined,
        ...overrides
    });
}
