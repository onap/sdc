'use strict';

let gulp = require('gulp');
let gulpHelpers = require('gulp-helpers');
let replace = require('gulp-replace');
let taskMaker = gulpHelpers.taskMaker(gulp);
let runSequence = gulpHelpers.framework('run-sequence');
let i18nTask = require('./tools/gulp/tasks/i18n');
let prodTask = require('./tools/gulp/tasks/prod');
let gulpCssUsage = require('gulp-css-usage').default;
let jsonConfig = {
	"appContextPath" : "/onboarding"
};

try {
	jsonConfig = require('./src/sdc-app/config/config.json');
} catch (e) {
	console.log('could not load config. using deault value instead');
}

const appName = 'onboarding';
const dist = 'dist';

const path = {
	jetty: './webapp-onboarding/WEB-INF/jetty-web.xml',
	appinf: './webapp-onboarding/**/*.*',
	appinf_output: dist + '/webapp-onboarding',
	locales: dist + '/i18n/',
	output: dist,
	json: './src/**/*.json',
	index: './src/index.html',
	heat: './src/heat.html',
	scss: './resources/scss/**/*.scss',
	css: dist + '/css',
	svgSrc: './resources/images/svg/*.svg',
	svg: dist + '/resources/images/svg',
	war: [dist + '/index.html', dist + '/punch-outs_en.js', dist + '/**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '/**/*(config.json|locale.json)', 'tools/gulp/deployment/**', dist + '/webapp-onboarding/**'],
	heatWar: [dist + '/heat.html', dist + '/heat-validation_en.js', dist + '/**/*.{css,png,svg,eot,ttf,woff,woff2,otf}', dist + '/**/*(config.json|locale.json)', 'webapp-heat-validation/**'],
	wardest: dist,
	storybookFonts: './.storybook/fonts/*',
	storybookDist: './.storybook-dist',
	storybookResources: './.storybook/resources/onboarding/resources/images/svg',
	storybookDistResources: './.storybook-dist/onboarding/resources/images/svg'
};

taskMaker.defineTask('clean', {taskName: 'clean', src: path.output});
taskMaker.defineTask('copy', {taskName: 'copy-json', src: path.json, dest: path.output, changed: {extension: '.json'}});
taskMaker.defineTask('copy', {taskName: 'copy-index.html', src: path.index, dest: path.output, rename: 'index.html'});
taskMaker.defineTask('copy', {taskName: 'copy-heat.html', src: path.heat, dest: path.output, rename: 'heat.html'});
taskMaker.defineTask('copy', {taskName: 'copy-svg', src: path.svgSrc, dest: path.svg});
//TODO: delete this task after gulp-css-usage support for SCSS files
taskMaker.defineTask('sass', {taskName: 'sass', src: path.scss, dest: path.css, config: {outputStyle: 'compressed'}});
taskMaker.defineTask('compress', {taskName: 'compress-war', src: path.war, filename: appName + '.war', dest: path.wardest});
taskMaker.defineTask('compress', {taskName: 'compress-heat-war', src: path.heatWar, filename: 'heat-validation.war', dest: path.wardest});
taskMaker.defineTask('watch', {taskName: 'watch-stuff', src: [path.json, path.index, path.heat], tasks: ['copy-stuff']});
taskMaker.defineTask('copy', {taskName: 'copy-storybook-fonts', src: path.storybookFonts, dest: path.storybookDist});
taskMaker.defineTask('copy', {taskName: 'copy-storybook-resources', src: path.svgSrc, dest: path.storybookResources});
taskMaker.defineTask('copy', {taskName: 'copy-storybook-resources-prod', src: path.svgSrc, dest: path.storybookDistResources});

gulp.task('app-context', function(){
	gulp.src([path.appinf])
		.pipe(gulp.dest(path.appinf_output))
		.on('end', function () {
			gulp.src([path.jetty])
				.pipe(replace(/<Set name="contextPath">.*<\/Set>/g, '<Set name="contextPath">'+jsonConfig.appContextPath+'</Set>'))
				.pipe(gulp.dest(path.appinf_output + '/WEB-INF'));
		})
});

gulp.task('copy-stuff', callback => runSequence(['copy-json', 'copy-index.html', 'copy-heat.html', 'copy-svg', 'app-context'], callback));

gulp.task('i18n', () =>
	i18nTask({outputPath: path.output, localesPath: path.locales, lang: 'en'}).catch(err => {
		console.log('i18n Task : Error! ', err);
		throw err;
	})
);

gulp.task('dev', callback => runSequence('clean', ['i18n', 'copy-stuff'], callback));
gulp.task('build', callback => runSequence('clean', ['copy-stuff', 'i18n'], 'prod', ['compress-war', 'compress-heat-war'], callback));

gulp.task('default', ['dev']);

gulp.task('prod', () => prodTask({outDir: path.output})
	.catch(err => {
		if (err && err.stack) {
			console.error(err, err.stack);
		}
		throw new Error('Webpack build FAILED');
	})
);


gulp.task('gulp-css-usage', () => {
	return gulp.src('src/**/*.jsx').pipe(gulpCssUsage({css: path.css + '/style.css', babylon: ['objectRestSpread']}));
});

gulp.task('css-usage', () => {
	runSequence('sass', 'gulp-css-usage');
});

