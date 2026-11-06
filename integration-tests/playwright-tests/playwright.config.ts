import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  fullyParallel: true,
  workers: process.env.CI ? 4 : undefined,
  retries: 1,
  // A test that only passes on its retry still fails the run. The JUnit XML the Gerrit vote reads
  // reports such a test as passed, so without this a real flake (e.g. a backend read-after-write
  // 404) is visible only in the HTML report. The retry is kept for its trace.
  failOnFlakyTests: !!process.env.CI,
  // The @helm-validator specs need the sdc-helm-validator container, which the Maven profiles start.
  // Set SDC_SKIP_HELM_VALIDATOR when pointing the suite at a stack without it (dev server, a cluster).
  grepInvert: process.env.SDC_SKIP_HELM_VALIDATOR ? /@helm-validator/ : undefined,
  reporter: [
    ['list'],
    ['html', { outputFolder: '../target/playwright-report', open: 'never' }],
    ['junit', { outputFile: '../target/playwright-reports/results.xml' }],
  ],
  use: {
    baseURL: process.env.SDC_BASE_URL || 'http://localhost:8285',
    trace: 'on',
    screenshot: 'only-on-failure',
    ignoreHTTPSErrors: true,
  },
  projects: [
    {
      name: 'chromium',
      use: {
        browserName: 'chromium',
        viewport: { width: 1920, height: 1080 },
      },
    },
  ],
  outputDir: '../target/playwright-results',
});
