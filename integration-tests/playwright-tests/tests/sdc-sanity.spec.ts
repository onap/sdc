import { test, expect } from '@playwright/test';

/**
 * SDC Sanity Test - Demonstrates Playwright e2e testing with the integration-test Docker stack.
 *
 * This test uses the webseal-simulator (sdc-sim) login page to authenticate,
 * then verifies that the SDC home page loads successfully.
 *
 * When running locally:   SDC_BASE_URL=http://localhost:8285 npx playwright test
 * When running in CI:     The Maven profile sets the URL automatically.
 */

const SIM_PASSWORD = '123123a';

test.describe('SDC Sanity', () => {

  test('should login via simulator and reach the SDC home page', async ({ page }) => {
    // Navigate to the simulator login page
    await page.goto('/login');

    // Verify the login page rendered
    await expect(page.locator('h1')).toContainText('Webseal simulator');

    // Fill in credentials for the Designer role
    await page.locator('input[name="userId"]').fill('cs0008');
    await page.locator('input[name="password"]').fill(SIM_PASSWORD);

    // Submit the login form
    await page.locator('input[value="Login"]').click();

    // After login the simulator redirects to /sdc1 which loads the SDC UI.
    // Wait for the URL to contain /sdc1 (the redirect target).
    await page.waitForURL('**/sdc1**', { timeout: 30_000 });

    // The SDC UI should render – verify the page title or a known element.
    // The SDC app sets the document title to "SDC" or similar.
    await expect(page).toHaveTitle(/SDC|STARTER/i, { timeout: 30_000 });

    // The HOME button in the main menu should be visible
    await expect(page.locator('[data-tests-id="main-menu-button-home"]')).toBeVisible({ timeout: 30_000 });
  });

  test('should display user quick-links table on the login page', async ({ page }) => {
    await page.goto('/login');

    // The simulator renders a table of preconfigured users
    const table = page.locator('table');
    await expect(table).toBeVisible();

    // At least one user row should be present
    const rows = table.locator('tr');
    await expect(rows).not.toHaveCount(0);
  });
});
