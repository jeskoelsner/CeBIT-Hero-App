/**
 * Main application routes
 */

'use strict';
var subdomain = require('express-subdomain');
var express = require('express');
var errors = require('./components/errors');

//CORS middleware
var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
    res.header('Access-Control-Allow-Headers', 'Content-Type');
    next();
}

module.exports = function(app) {
	var router = express.Router();
	app.use(allowCrossDomain);
	app.use(subdomain('heroid', router));

	// Insert routes below
	app.use('/api/missions', require('./api/mission'));
	app.use('/api/heroes', require('./api/hero'));
	app.use('/api/users', require('./api/user'));

	app.use('/auth', require('./auth'));

	// All undefined asset or api routes should return a 404
	app.route('/:url(api|auth|components|app|bower_components|assets)/*')
	 .get(errors[404]);

	// All other routes should redirect to the index.html
	app.route('/*')
		.get(function(req, res) {
			res.sendfile(app.get('appPath') + '/index.html');
		});
};
