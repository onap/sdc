# CatalogUi

## Development server
1. Run `npm start` for a dev server.
2. Navigate to <http://localhost:9000/>.
   * The app will automatically reload if you change any of the source files.


To Specify the user role:

`npm start --env.role designer`

`npm run <role>`


## Build
### dev 
Run `npm run build` to build the project. 

The build artifacts will be stored in the `dist/` directory. 


### prod
Run `npm run build:prod` to build the project. 

The build artifacts will be stored in the `dist/` directory. 

## Configuration

### webpack.server.js
Development server is configured in `/webpack.server.js` file. Configure the constants accordingly:
- const __devPort__: the webpack server port;
- const __feHost__: the catalog front-end container host;
- const __fePort__: the catalog front-end container port.

The server will create a proxy to the front end calls based on the  __feHost__ and __fePort__. 

It will also create authorization cookies to those requests. The cookies and user information comes from the 
`/configurations/mock.json` file under the nodes `sdcConfig.cookie` and `sdcConfig.userTypes` respectively.

The default user role is the node `sdcConfig.userTypes.designer` (configurable with the npm parameter --env.role).


### Application Configuration
Application configuration `dev.js` or `prod.js` and `menu.js` are located in `/configuration` folder and required by `app.ts` according to __ENV__ parameter from webpack.



## Running unit tests
Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests
Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

Before running the tests make sure you are serving the app via `ng serve`.

## Working with WebStorm  

1. go to File --> Settings --> Languages & Frameworks
2. Under JavaScript -> set JavaScript language version to ECMAScript6
3. Under TypeScript -> select `Enable TypeScript Compiler` and choose `Use tsconfige.json`

