'use strict';

var Q = require('q');
var _ = require('lodash');
var mqtt    = require('mqtt');
var moment = require('moment');
var Mission = require('./mission.model');

var options = {
	port: 1883,
	host: 'rabbit.livesapp.io',
	keepalive: 20,
	username: 'bucksbunny_6_emurgency.io',
	password: 'jackrabbit_9_emurgency.io',

	protocolId: 'MQIsdp',
	protocolVersion: 3
};

// Get list of missions
exports.index = function(req, res) {
	Mission.find(function (err, missions) {
		if(err) { return handleError(res, err); }
		return res.json(200, missions);
	});
};

// Get a single mission
exports.show = function(req, res) {
	if(req.params.id == 0){
		Mission.findOne()
		.sort('-created')
		.exec(function (err, mission) {
			if(err) { return handleError(res, err); }
			if(!mission) { return res.send(404); }
			return res.json(mission);
		});
	}else{
		Mission.findById(req.params.id, function (err, mission) {
			if(err) { return handleError(res, err); }
			if(!mission) { return res.send(404); }
			return res.json(mission);
		});
	}
};

exports.create = function(req, res) {
	req.body.created = moment().utc().valueOf();
	req.body.score = 10;

	var body = req.body;
	var id = req.params.id;

	Mission.create(body, function(err, mission){
		if(err) { return handleError(res, err); }
		if(!mission) { return res.send(404); }

		var client = mqtt.connect(options);
		client.on('connect', deferred.notify(client, mission));
		client.on('message', function(topic, message){
			deferred.resolve(client, mission);
		});
		client.on('error', deferred.reject);

		return res.json(200, mission);
	});
};

exports.shoot = function(req, res) {
	//do nothing... for now..
};

// Updates an existing mission in the DB.
exports.update = function(req, res) {
	if(req.body._id) { delete req.body._id; }
	Mission.findById(req.params.id, function (err, mission) {
		if (err) { return handleError(res, err); }
		if(!mission) { return res.send(404); }
		var updated = _.merge(mission, req.body);
		updated.save(function (err) {
			if (err) { return handleError(res, err); }
			return res.json(200, mission);
		});
	});
};

// Deletes a mission from the DB.
exports.destroy = function(req, res) {
	Mission.findById(req.params.id, function (err, mission) {
		if(err) { return handleError(res, err); }
		if(!mission) { return res.send(404); }
		mission.remove(function(err) {
			if(err) { return handleError(res, err); }
			return res.send(204);
		});
	});
};

exports.drop = function(req, res){
	var allofthem = req.body;
	var errored = false;
	var errormsg;
	_.each(allofthem,function(element){
		Mission.findById(element._id, function (err, mission) {
									if(err) { errormsg = err; errored = true; }
									mission.remove(function(err) {
													if(err) { errormsg = err; errored = true; }
									});
					});
	});
	if(err) res.send(304);
	return res.send(204);
}

function handleError(res, err) {
	return res.send(500, err);
}
