import { test, expect } from '@playwright/test';

const SIM_PASSWORD = '123123a';
const USER_ID = 'cs0008';

test.describe('Workspace Navigation', () => {

  test.beforeEach(async ({ page }) => {
    // Login via simulator
    await page.goto('/login');
    await page.locator('input[name="userId"]').fill(USER_ID);
    await page.locator('input[name="password"]').fill(SIM_PASSWORD);
    await page.locator('input[value="Login"]').click();
    await page.waitForURL('**/sdc1**', { timeout: 30_000 });
    await expect(page.locator('[data-tests-id="main-menu-button-home"]')).toBeVisible({ timeout: 30_000 });
  });

  test('should render workspace left sidebar menu after creating a VF', async ({ page }) => {
    // Create a VF resource via the UI - click Add button
    await page.locator('[data-tests-id="AddButtonsArea"]').click();
    await page.locator('[data-tests-id="createResourceButton"]').click({ timeout: 10_000 });

    // Fill the create form
    await page.locator('[data-tests-id="name"]').fill('TestVF_' + Date.now());
    await page.locator('[data-tests-id="description"]').fill('Test VF for workspace navigation');

    // Select category
    const categoryDropdown = page.locator('[data-tests-id="selectGeneralCategory"]');
    await categoryDropdown.selectOption({ index: 1 });

    // Click Create button
    await page.locator('[data-tests-id="create/save"]').click({ timeout: 10_000 });

    // After create, we should be on the workspace general page
    // The GeneralLeftSideMenu should be visible
    await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 30_000 });

    // Also verify the page settled using the pageLoadWait logic
    const settled = await page.evaluate(() => {
      return new Promise<boolean>((resolve) => {
        let attempts = 0;
        const check = () => {
          attempts++;
          if (attempts > 40) { resolve(false); return; }
          try {
            const injector = (window as any).angular.element('body').injector();
            const $rootScope = injector.get('$rootScope');
            const $http = injector.get('$http');
            if ($rootScope.$$phase || $http.pendingRequests.length !== 0) {
              setTimeout(check, 500);
              return;
            }
            resolve(true);
          } catch { setTimeout(check, 500); }
        };
        check();
      });
    });
    expect(settled).toBe(true);
  });

  test('should render workspace sidebar when navigating to existing resource', async ({ page }) => {
    // Use the API to find an existing resource
    const response = await page.evaluate(async () => {
      const resp = await fetch('/sdc1/feProxy/rest/v1/screen?excludeTypes=VFCMT&excludeTypes=Configuration', {
        headers: { 'USER_ID': 'cs0008' }
      });
      const data = await resp.json();
      const resources = data.resources || [];
      if (resources.length === 0) return null;
      return { uniqueId: resources[0].uniqueId, type: resources[0].componentType.toLowerCase() };
    });

    if (!response) {
      test.skip(true, 'No resources in catalog');
      return;
    }

    // Navigate to workspace via $state.go
    await page.evaluate(({ id, type }) => {
      const injector = (window as any).angular.element('body').injector();
      const $state = injector.get('$state');
      $state.go('workspace.general', { id, type });
    }, { id: response.uniqueId, type: response.type });

    // Wait for workspace to render
    await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 30_000 });

    // Check other sidebar items exist too
    const menuItems = page.locator('.i-sdc-designer-sidebar-section-content-item');
    await expect(menuItems).not.toHaveCount(0, { timeout: 10_000 });
  });

  test('pageLoadWait should settle on home page', async ({ page }) => {
    // This test verifies the exact check that Selenium uses
    const result = await page.evaluate(() => {
      return new Promise<{ settled: boolean; attempts: number; lastReason: string }>((resolve) => {
        let attempts = 0;
        const qa = ((window as any).qa = (window as any).qa || { doneRendering: false });
        const check = () => {
          attempts++;
          if (attempts > 20) { resolve({ settled: false, attempts, lastReason: 'timeout' }); return; }
          try {
            if (document.readyState !== 'complete') { setTimeout(check, 500); return; }
            const angular = (window as any).angular;
            if (!angular) { resolve({ settled: true, attempts, lastReason: 'no angular' }); return; }
            const injector = angular.element('body').injector();
            if (!injector) { setTimeout(check, 500); return; }
            const $rootScope = injector.get('$rootScope');
            const $http = injector.get('$http');
            const $timeout = injector.get('$timeout');
            if ($rootScope.$$phase === '$apply' || $rootScope.$$phase === '$digest' || $http.pendingRequests.length !== 0) {
              qa.doneRendering = false;
              setTimeout(check, 500);
              return;
            }
            if (!qa.doneRendering) {
              $timeout(() => { qa.doneRendering = true; }, 0);
              setTimeout(check, 500);
              return;
            }
            resolve({ settled: true, attempts, lastReason: 'all checks passed' });
          } catch (ex: any) {
            setTimeout(check, 500);
          }
        };
        check();
      });
    });

    expect(result.settled).toBe(true);
    console.log(`pageLoadWait settled in ${result.attempts} attempts`);
  });
});
