/**
 * Populate DB with sample data on server start
 * to disable, edit config/environment/index.js, and set `seedDB: false`
 */

'use strict';

var User = require('../api/user/user.model');

User.find({}).remove(function() {
	User.create({
		provider: 'local',
		role: 'admin',
		name: 'livesapp.io',
		email: 'admin@livesapp.net',
		password: 'emurgency'
	}, function() {
			console.log('finished populating users');
		}
	);
});