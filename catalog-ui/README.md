# CatalogUi

## Development server
Run "npm start" for a dev server. 
Navigate to "http://localhost:9000/". 
The app will automatically reload if you change any of the source files.

--- Specify role ---
npm start -- --env.role designer

npm run <role>

## Build
--- dev ---
Run "npm run build" to build the project. 
The build artifacts will be stored in the `dist/` directory. 

--- prod ---
Run "npm run build:prod" to build the project. 
The build artifacts will be stored in the `dist/` directory. 

## Configuration
Dev server is configured in webpack.server.js file.
App configuration dev.js or prod.js and menu.js are located in configuration folder and required by app.ts according to __ENV__ parameter from webpack.

## Running unit tests
## Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests
## Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).
## Before running the tests make sure you are serving the app via `ng serve`.

-- Working with WebStorm -- 

go to File --> Settings --> Languages & Frameworks

Under JavaScript -> set JavaScript language version to ECMAScript6
Under TypeScript -> select 'Enable TypeScript Compiler' and choose 'Use tsconfige.json'

