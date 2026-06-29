import { test, expect } from '@playwright/test';

// Phase 4 verification: the interface_operation and interface-definition workspace
// child states now render the downgraded Angular components directly (the AngularJS
// view-model shim + wrapper HTML were removed). This confirms each tab still renders.

const SIM_PASSWORD = '123123a';
const USER_ID = 'cs0008';

test.describe('Interface tabs render after AngularJS wrapper removal', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.locator('input[name="userId"]').fill(USER_ID);
    await page.locator('input[name="password"]').fill(SIM_PASSWORD);
    await page.locator('input[value="Login"]').click();
    await page.waitForURL('**/sdc1**', { timeout: 30_000 });
    await expect(page.locator('[data-tests-id="main-menu-button-home"]')).toBeVisible({ timeout: 30_000 });
  });

  test('interface_operation + interface-definition states render their Angular components', async ({ page }) => {
    // pick any catalog component to open a workspace
    const found = await page.evaluate(async () => {
      const resp = await fetch('/sdc1/feProxy/rest/v1/screen?excludeTypes=VFCMT&excludeTypes=Configuration',
        { headers: { 'USER_ID': 'cs0008' } });
      const data = await resp.json();
      const services = data.services || [];
      const resources = data.resources || [];
      const pick = services[0] || resources[0];
      return pick ? { id: pick.uniqueId, type: pick.componentType.toLowerCase() } : null;
    });
    test.skip(!found, 'No component in catalog to open');

    // navigate into the workspace shell first
    await page.evaluate(({ id, type }) => {
      const inj = (window as any).angular.element('body').injector();
      inj.get('$state').go('workspace.general', { id, type });
    }, found);
    await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 30_000 });

    // ---- Interface Operation tab ----
    await page.evaluate(({ id, type }) => {
      const inj = (window as any).angular.element('body').injector();
      inj.get('$state').go('workspace.interface_operation', { id, type });
    }, found);
    // the downgraded <interface-operation> element should be attached and rendered
    await expect(page.locator('interface-operation')).toBeAttached({ timeout: 20_000 });
    await expect(page.locator('.workspace-interface-operation')).toBeAttached({ timeout: 5_000 });
    const opLanded = await page.evaluate(() =>
      (window as any).angular.element('body').injector().get('$state').current.name);
    expect(opLanded).toBe('workspace.interface_operation');

    // ---- Interface Definition tab ----
    await page.evaluate(({ id, type }) => {
      const inj = (window as any).angular.element('body').injector();
      inj.get('$state').go('workspace.interface-definition', { id, type });
    }, found);
    await expect(page.locator('interface-definition')).toBeAttached({ timeout: 20_000 });
    await expect(page.locator('.workspace-interface-definition')).toBeAttached({ timeout: 5_000 });
    const defLanded = await page.evaluate(() =>
      (window as any).angular.element('body').injector().get('$state').current.name);
    expect(defLanded).toBe('workspace.interface-definition');
  });
});
