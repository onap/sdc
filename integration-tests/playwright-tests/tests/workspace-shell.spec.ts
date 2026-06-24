import { test, expect } from '@playwright/test';

const PAGE_LOAD_WAIT = `
  try {
    if (document.readyState !== 'complete') return false;
    if (window.jQuery && window.jQuery.active) return false;
    if (window.angular) {
      if (!window.qa) { window.qa = { doneRendering: false }; }
      var injector = window.angular.element('body').injector();
      var $rootScope = injector.get('$rootScope');
      var $http = injector.get('$http');
      var $timeout = injector.get('$timeout');
      if ($rootScope.$$phase === '$apply' || $rootScope.$$phase === '$digest' || $http.pendingRequests.length !== 0) {
        window.qa.doneRendering = false; return false;
      }
      if (!window.qa.doneRendering) { $timeout(function() { window.qa.doneRendering = true; }, 0); return false; }
    }
    return true;
  } catch (ex) { return false; }
`;

async function login(page: any) {
  await page.goto('/login');
  await page.locator('input[name="userId"]').fill('cs0008');
  await page.locator('input[name="password"]').fill('123123a');
  await page.locator('input[value="Login"]').click();
  await page.waitForURL('**/sdc1**', { timeout: 30000 });
  await page.waitForTimeout(5000);
}

async function settles(page: any): Promise<boolean> {
  const start = Date.now();
  while (Date.now() - start < 30000) {
    const r = await page.evaluate(new Function(PAGE_LOAD_WAIT) as any).catch(() => 'FROZEN');
    if (r === 'FROZEN') return false;
    if (r === true) return true;
    await page.waitForTimeout(500);
  }
  return false;
}

test('VIEW/EDIT mode: workspace shell + child form render and settle', async ({ page }) => {
  await login(page);
  const created = await page.evaluate(() => {
    const $http = (window as any).angular.element('body').injector().get('$http');
    const name = 'TestVF' + Date.now();
    return $http.post('/sdc1/feProxy/rest/v1/catalog/resources', {
      name, description: 'test', componentType: 'RESOURCE', resourceType: 'VF',
      categories: [{ name: 'Generic', normalizedName: 'generic', uniqueId: 'resourceNewCategory.generic', subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }] }],
      vendorName: 'test', vendorRelease: '1.0', contactId: 'cs0008', icon: 'defaulticon', tags: [name]
    }).then((r: any) => ({ id: r.data.uniqueId }), (e: any) => ({ error: e.data }));
  });
  expect(created.id).toBeTruthy();

  await page.evaluate((id) => {
    (window as any).angular.element('body').injector().get('$state').go('workspace.general', { id, type: 'resource', previousState: 'dashboard' });
  }, created.id);

  expect(await settles(page)).toBe(true);
  await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 5000 });
  await expect(page.locator('top-nav')).toHaveCount(1);
  await expect(page.locator('top-nav .top-menu, top-nav [class*="breadcrumb"], top-nav .breadcrumbs')).not.toHaveCount(0);
  await expect(page.locator('[data-tests-id="name"]')).toBeVisible({ timeout: 5000 });
  await expect(page.locator('[data-tests-id="versionHeader"]')).toBeVisible({ timeout: 5000 });
  console.log('✓ VIEW/EDIT mode fully verified');
});

test('CREATE mode: workspace shell + empty form render and settle', async ({ page }) => {
  await login(page);
  await page.evaluate(() => {
    (window as any).angular.element('body').injector().get('$state').go('workspace.general', { type: 'resource', resourceType: 'VF', previousState: 'dashboard' });
  });

  expect(await settles(page)).toBe(true);
  await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 5000 });
  await expect(page.locator('top-nav')).toHaveCount(1);
  await expect(page.locator('top-nav .top-menu, top-nav [class*="breadcrumb"], top-nav .breadcrumbs')).not.toHaveCount(0);
  await expect(page.locator('[data-tests-id="name"]')).toBeVisible({ timeout: 5000 });
  // The chrome's Create button (workspace-container renders it as a plain Angular button)
  await expect(page.locator('.sdc-workspace-top-bar-buttons [data-tests-id="create/save"]')).toBeVisible({ timeout: 5000 });
  console.log('✓ CREATE mode fully verified');
});

test('Tab navigation: switch from General to Deployment Artifact', async ({ page }) => {
  await login(page);
  const created = await page.evaluate(() => {
    const $http = (window as any).angular.element('body').injector().get('$http');
    const name = 'TestVF' + Date.now();
    return $http.post('/sdc1/feProxy/rest/v1/catalog/resources', {
      name, description: 'test', componentType: 'RESOURCE', resourceType: 'VF',
      categories: [{ name: 'Generic', normalizedName: 'generic', uniqueId: 'resourceNewCategory.generic', subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }] }],
      vendorName: 'test', vendorRelease: '1.0', contactId: 'cs0008', icon: 'defaulticon', tags: [name]
    }).then((r: any) => ({ id: r.data.uniqueId }), (e: any) => ({ error: e.data }));
  });
  await page.evaluate((id) => {
    (window as any).angular.element('body').injector().get('$state').go('workspace.general', { id, type: 'resource', previousState: 'dashboard' });
  }, created.id);
  await settles(page);
  await expect(page.locator('[data-tests-id="name"]')).toBeVisible({ timeout: 10000 });

  // Click a different tab in the sidebar
  await page.locator('[data-tests-id="Information ArtifactLeftSideMenu"]').click();
  expect(await settles(page)).toBe(true);
  // Sidebar must still be there after tab switch
  await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 5000 });
  console.log('✓ Tab navigation verified (sidebar persists, page settles)');
});
