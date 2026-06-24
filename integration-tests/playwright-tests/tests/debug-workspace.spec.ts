import { test, expect } from '@playwright/test';

test('debug workspace providers', async ({ page }) => {
  page.on('console', msg => { if (msg.type() === 'error') console.log('BROWSER:', msg.text().substring(0, 300)); });
  page.on('pageerror', err => console.log('PAGE_ERROR:', err.message.substring(0, 300)));
  
  await page.goto('/login');
  await page.locator('input[name="userId"]').fill('cs0008');
  await page.locator('input[name="password"]').fill('123123a');
  await page.locator('input[value="Login"]').click();
  await page.waitForURL('**/sdc1**', { timeout: 30000 });
  await page.waitForTimeout(8000);
  
  // Check providers resolve
  const providers = await page.evaluate(() => {
    const injector = (window as any).angular.element('body').injector();
    const results: any = {};
    for (const name of ['sdcMenu', 'MenuHandler', 'ChangeLifecycleStateHandler', 'Sdc.Services.ProgressService', 'ComponentFactory']) {
      try { results[name] = typeof injector.get(name); } 
      catch(e: any) { results[name] = 'ERROR: ' + e.message; }
    }
    return results;
  });
  console.log('Providers:', JSON.stringify(providers, null, 2));
  
  // Now try navigating to workspace
  const navResult = await page.evaluate(() => {
    const injector = (window as any).angular.element('body').injector();
    const $http = injector.get('$http');
    return $http.get('/sdc1/feProxy/rest/v1/screen?excludeTypes=VFCMT&excludeTypes=Configuration')
      .then(resp => {
        const resources = resp.data.resources || [];
        return resources.length > 0 ? { id: resources[0].uniqueId, type: resources[0].componentType } : null;
      });
  });
  console.log('Nav target:', JSON.stringify(navResult));
  
  if (navResult) {
    // Create a VF resource via AngularJS $http (has auth cookies)
    const createResult = await page.evaluate(() => {
      const injector = (window as any).angular.element('body').injector();
      const $http = injector.get('$http');
      const name = 'TestVF' + Date.now();
      return $http.post('/sdc1/feProxy/rest/v1/catalog/resources', {
        name,
        description: 'test',
        componentType: 'RESOURCE',
        resourceType: 'VF',
        categories: [{ name: 'Generic', normalizedName: 'generic', uniqueId: 'resourceNewCategory.generic', subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }] }],
        vendorName: 'test',
        vendorRelease: '1.0',
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [name]
      }).then((resp: any) => ({ status: resp.status, id: resp.data.uniqueId, name: resp.data.name }),
             (err: any) => ({ status: err.status, error: err.data }));
    });
    console.log('Created resource:', JSON.stringify(createResult));

    if (!createResult || !createResult.id) {
      console.log('Failed to create resource, skipping workspace test');
      return;
    }

    // Reload and double-click first resource tile to open workspace
    await page.reload({ waitUntil: 'networkidle' });
    await page.waitForTimeout(8000);
    await page.locator('ng2-ui-tile').first().dblclick({ timeout: 10000 });
    
    await page.waitForTimeout(5000);
    
    const wsDebug = await page.locator('[data-tests-id="workspace-debug"]').textContent({ timeout: 5000 }).catch(() => 'NOT FOUND');
    console.log('Workspace debug:', wsDebug);
    
    await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 15000 });
  }
});
