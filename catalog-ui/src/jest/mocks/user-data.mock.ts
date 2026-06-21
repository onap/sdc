import {IUserProperties} from 'app/models';

export function createMockUser(overrides: Partial<IUserProperties> = {}): IUserProperties {
    return {
        userId: 'cs0008',
        firstName: 'Carlos',
        lastName: 'Santana',
        fullName: 'Carlos Santana',
        email: 'csantana@example.com',
        role: 'DESIGNER',
        lastLoginTime: 0,
        status: 'ACTIVE',
        ...overrides
    } as IUserProperties;
}

export const mockDesigner = createMockUser({role: 'DESIGNER'});
export const mockAdmin = createMockUser({role: 'ADMIN', userId: 'admin01'});
export const mockTester = createMockUser({role: 'TESTER', userId: 'tester01'});
