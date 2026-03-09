import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './tests',
  timeout: 60_000,
  expect: {
    timeout: 10_000,
  },
  fullyParallel: false,
  retries: 1,
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
