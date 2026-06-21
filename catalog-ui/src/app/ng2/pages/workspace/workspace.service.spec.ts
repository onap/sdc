import {TestBed} from '@angular/core/testing';
import {WorkspaceService} from './workspace.service';
import {CacheService} from '../../services/cache.service';
import {ComponentState, ComponentType, Role, WorkspaceMode} from '../../../utils/constants';
import {createMockComponent, createServiceComponent} from '../../../../jest/mocks/workspace-component.mock';
import {createMockUser} from '../../../../jest/mocks/user-data.mock';

describe('WorkspaceService', () => {
    let service: WorkspaceService;
    let cacheServiceMock: any;
    const designer = createMockUser({userId: 'cs0008', role: Role.DESIGNER});

    beforeEach(() => {
        cacheServiceMock = {
            get: jest.fn((key) => {
                if (key === 'user') return designer;
                return undefined;
            }),
            set: jest.fn(),
            contains: jest.fn()
        };

        TestBed.configureTestingModule({
            providers: [
                WorkspaceService,
                {provide: CacheService, useValue: cacheServiceMock}
            ]
        });
        service = TestBed.get(WorkspaceService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('getComponentMode', () => {
        it('should return EDIT when component is checked out by current user who is DESIGNER', () => {
            const component = createMockComponent({
                lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
                lastUpdaterUserId: 'cs0008',
                componentType: ComponentType.RESOURCE
            });
            const mode = service.getComponentMode(component);
            expect(mode).toBe(WorkspaceMode.EDIT);
        });

        it('should return VIEW when component is checked out by different user', () => {
            const component = createMockComponent({
                lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
                lastUpdaterUserId: 'other_user',
                componentType: ComponentType.RESOURCE
            });
            const mode = service.getComponentMode(component);
            expect(mode).toBe(WorkspaceMode.VIEW);
        });

        it('should return VIEW when component is CERTIFIED', () => {
            const component = createMockComponent({
                lifecycleState: ComponentState.CERTIFIED,
                lastUpdaterUserId: 'cs0008',
                componentType: ComponentType.RESOURCE
            });
            const mode = service.getComponentMode(component);
            expect(mode).toBe(WorkspaceMode.VIEW);
        });

        it('should return VIEW when user is not DESIGNER', () => {
            cacheServiceMock.get.mockReturnValue(createMockUser({userId: 'cs0008', role: Role.TESTER}));
            const component = createMockComponent({
                lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
                lastUpdaterUserId: 'cs0008',
                componentType: ComponentType.RESOURCE
            });
            const mode = service.getComponentMode(component);
            expect(mode).toBe(WorkspaceMode.VIEW);
        });

        it('should return EDIT for service checked out by current DESIGNER', () => {
            const component = createServiceComponent({
                lifecycleState: ComponentState.NOT_CERTIFIED_CHECKOUT,
                lastUpdaterUserId: 'cs0008'
            });
            const mode = service.getComponentMode(component);
            expect(mode).toBe(WorkspaceMode.EDIT);
        });
    });

    describe('getMetadataType', () => {
        it('should return SERVICE for service component', () => {
            service.setComponentMetadata({componentType: ComponentType.SERVICE} as any);
            expect(service.getMetadataType()).toBe(ComponentType.SERVICE);
        });

        it('should return resourceType for resource component', () => {
            service.setComponentMetadata({componentType: ComponentType.RESOURCE, resourceType: 'VF'} as any);
            expect(service.getMetadataType()).toBe('VF');
        });

        it('should return resourceType for VFC', () => {
            service.setComponentMetadata({componentType: ComponentType.RESOURCE, resourceType: 'VFC'} as any);
            expect(service.getMetadataType()).toBe('VFC');
        });
    });

    describe('setComponentMetadata', () => {
        it('should store metadata', () => {
            const metadata = {componentType: ComponentType.SERVICE, name: 'test'} as any;
            service.setComponentMetadata(metadata);
            expect(service.metadata).toBe(metadata);
        });
    });
});
