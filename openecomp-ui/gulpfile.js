'use strict';

let gulp = require('gulp');
let gulpHelpers = require('gulp-helpers');
let replace = require('gulp-replace');
let taskMaker = gulpHelpers.taskMaker(gulp);
let runSequence = gulpHelpers.framework('run-sequence');
let gulpCssUsage = require('gulp-css-usage').default;

let prodTask = require('./tools/gulp/tasks/prod');
let i18nTask = require('./tools/gulp/tasks/i18n.js');

let jsonConfig = {
	"appContextPath" : "/onboarding"
};

try {
	jsonConfig = require('./src/sdc-app/config/config.json');
} catch (e) {
	console.log('could not load config. using default value instead');
}

const appName = 'onboarding';
const dist = 'dist';

const path = {
	// inputs
	json: './src/**/*.json',
	index: './src/index.html',
	heat: './src/heat.html',
	scss: './resources/scss/**/*.scss',
	i18nBundles: './src/nfvo-utils/i18n/*.json',
	svgSrc: './resources/images/svg/*.svg',
	appinf: './webapp-onboarding/**/*.*',
	jetty: './webapp-onboarding/WEB-INF/jetty-web.xml',
	healthCheckInput: './external-resources/healthcheck/healthcheck',
	srcDir: './src/',
	// output
	output: dist,
	css: dist + '/css',
//	svg: dist + '/resources/images/svg',
	appinf_output: dist + '/webapp-onboarding',
	healthCheckOutput: dist + '/v1.0',
	// war
	war: [dist + '/index.html', dist + '/punch-outs*.js', dist + '/**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '/**/*(config.json)', dist + '/webapp-onboarding/**', dist + '/**/*(healthcheck)'],
	heatWar: [dist + '/heat.html', dist + '/heat-validation_en.js', dist + '/**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '/**/*(config.json)', 'webapp-heat-validation/**'],
	wardest: dist,
	// storybook
	storybookFonts: './.storybook/fonts/*',
	storybookDist: './.storybook-dist',
	//storybookResources: './.storybook/resources/onboarding/resources/images/svg',
	//storybookDistResources: './.storybook-dist/onboarding/resources/images/svg'
};
// cleans up the output directory
taskMaker.defineTask('clean', {taskName: 'clean', src: path.output});
// copies for all relevant files to the output directory
taskMaker.defineTask('copy', {taskName: 'copy-json', src: path.json, dest: path.output, changed: {extension: '.json'}});
taskMaker.defineTask('copy', {taskName: 'copy-index.html', src: path.index, dest: path.output, rename: 'index.html'});
taskMaker.defineTask('copy', {taskName: 'copy-heat.html', src: path.heat, dest: path.output, rename: 'heat.html'});
//taskMaker.defineTask('copy', {taskName: 'copy-svg', src: path.svgSrc, dest: path.svg});
taskMaker.defineTask('copy', {taskName: 'copy-storybook-fonts', src: path.storybookFonts, dest: path.storybookDist});
//taskMaker.defineTask('copy', {taskName: 'copy-storybook-resources', src: path.svgSrc, dest: path.storybookResources});
//taskMaker.defineTask('copy', {taskName: 'copy-storybook-resources-prod', src: path.svgSrc, dest: path.storybookDistResources});
// used for compressing war files
taskMaker.defineTask('compress', {taskName: 'compress-war', src: path.war, filename: appName + '.war', dest: path.wardest});
taskMaker.defineTask('compress', {taskName: 'compress-heat-war', src: path.heatWar, filename: 'heat-validation.war', dest: path.wardest});
// used for watching for changes for test
taskMaker.defineTask('watch', {taskName: 'watch-stuff', src: [path.json, path.index, path.heat], tasks: ['copy-stuff']});


//TODO: delete this task after gulp-css-usage support for SCSS files
taskMaker.defineTask('sass', {taskName: 'sass', src: path.scss, dest: path.css, config: {outputStyle: 'compressed'}});

// copy the healthcheck file and replace the version with command line argument
gulp.task('healthcheck', function(){
	let args = process.argv;
	let versionArg = args.find(arg => arg.startsWith('--version'));
	let version = versionArg && versionArg.slice(versionArg.indexOf('=') + 1);
	if (versionArg) {
		gulp.src(path.healthCheckInput)
			.pipe(replace('{VERSION}', version))
			.pipe(gulp.dest(path.healthCheckOutput));
	}
});

// update the app-context for the web-xml file to the value from the config
gulp.task('app-context', function(){
	gulp.src([path.appinf])
		.pipe(gulp.dest(path.appinf_output))
		.on('end', function () {
			gulp.src([path.jetty])
				.pipe(replace(/<Set name="contextPath">.*<\/Set>/g, '<Set name="contextPath">'+jsonConfig.appContextPath+'</Set>'))
				.pipe(gulp.dest(path.appinf_output + '/WEB-INF'));
		})
});
// aggregates all copy tasks
gulp.task('copy-stuff', callback => runSequence(['copy-json', 'copy-index.html', 'copy-heat.html', 'app-context'], callback));

// minimum build for dev
gulp.task('dev', callback => runSequence('clean', 'copy-stuff', callback));
// build procedure for war file
gulp.task('build', callback => runSequence('clean', 'copy-stuff', 'healthcheck', 'prod', ['compress-war', 'compress-heat-war'], callback));
// default build is set to 'dev'
gulp.task('default', ['dev']);
// creating the webpack tasks for the production build
gulp.task('prod', () => prodTask({outDir: path.output, i18nBundles : path.i18nBundles})
	.catch(err => {
		if (err && err.stack) {
			console.error(err, err.stack);
		}
		throw new Error('Webpack build FAILED');
	})
);

/***
 * T O O L S .   N O T   P A R T   O F    B U I L D
 */

// this is used to manually run on the sass files to check which classes are never used. not run as part of build.
// can be run as npm task
gulp.task('gulp-css-usage', () => {
	return gulp.src('src/**/*.jsx').pipe(gulpCssUsage({css: path.css + '/style.css', babylon: ['objectRestSpread']}));
});

gulp.task('css-usage', () => {
	runSequence('sass', 'gulp-css-usage');
});


gulp.task('static-keys-bundle', () => i18nTask({outDir: path.output, srcDir: path.srcDir})
	.catch(err => {
		throw new Error('static-keys-bundle FAILED');
	})
);

gulp.task('static-keys-bundle-with-report', () => i18nTask({outDir: path.output, srcDir: path.srcDir, i18nBundles : path.i18nBundles })
	.catch(err => {
		throw new Error('static-keys-bundle FAILED');
	})
);
