'use strict';

var _ = require('lodash'),
		moment = require('moment');
var Hero = require('./hero.model');
var Mission = require('../mission/mission.model');
var json2csv = require('json2csv');

// Get list of heros
exports.index = function(req, res) {
	Hero.find(function (err, heros) {
		if(err) { return handleError(res, err); }
		return res.json(200, heros);});
};

// Get a single hero
exports.show = function(req, res) {
	Hero.findOne({code: req.params.code}, function(err, hero) {
		if(err) { return handleError(res, err); }
		if(!hero) { return res.send(404); }
		return res.json(hero);
	});
};

//_.shuffle([1, 2, 3, 4]);
//_.sample([1, 2, 3, 4]);

// Get list of missions
exports.csv = function(req, res) {
	Hero.find(function (err, heroes) {
		if(err) { return handleError(res, err); }
		var all = [];
		_.each(heroes, function(hero){
			for(var i = hero.score; i > 0; i--){
				delete hero.scorehistory;
				delete hero.servertime;
				delete hero.lat;
				delete hero.lng;
				all.push(hero);
			}
		});

		if(req.params.v == 1){
			all = _.shuffle(all);
		}

		json2csv({data: all, fields: ['id','code','name','gender','company','phone','email','anon','score']}, function(err, csv) {
			if(err) { return handleError(res, err); }
			res.setHeader('Content-disposition', 'attachment; filename=all.csv');
			res.setHeader('Content-type', 'text/comma-separated-values');

			res.end(csv, 'utf-8');
		});
	});
};

// Creates a new hero in the DB.
exports.create = function(req, res) {
	if(req.body._id) { delete req.body._id; }
	if(req.body.id) { delete req.body.id; }
	if(req.body.score) { delete req.body.score; }

	Hero.find(function (err, heroes) {
		if (err) { return handleError(res, err); }

		req.body.id = !heroes ? 1 : heroes.length+1;

		Hero.create(req.body, function(err, hero) {
			if(err) { return handleError(res, err); }
			return res.json(201, hero);
		});
	});
};

// Creates a new hero in the DB.
exports.register = function(req, res) {
	if(req.body._id) { delete req.body._id; }
	if(req.body.score) { delete req.body.score; }
	req.body.code = req.params.code;
	req.body.id = req.params.id;

	Hero.findOne({code: req.params.code}, function (err, found) {
		if (err) { return handleError(res, err); }
		if (found) { return res.json(304); }

		Hero.create(req.body, function(err, hero) {
			if(err) { return handleError(res, err); }
			return res.json(201, hero);
		});
	});
};

// Updates an existing hero in the DB.
exports.score = function(req, res) {
	Hero.findOne({code: req.params.code}, function(err, hero) {
		if (err) { return handleError(res, err); }
		if(!hero) { return res.send(404); }

		Mission.findOne()
		.sort('-created')
		.exec(function (err, mission) {
			if (err) { return handleError(res, err); }
			if(!mission) { return res.send(404); }
			var maxtime = mission.created + mission.runtime;
			if(maxtime < mission.servertime){
				return res.send(410);
			}
			if(mission.score == 0) { return res.send(204); }
			//_.result(_.find(hero.scorehistory, { 'mission': mission.id }), 'score')
			if(!_.isEmpty(_.find(hero.scorehistory, { 'mission': mission._id }))) { return res.send(304); }

			var newscore = hero.score ? hero.score : 0 ;
					newscore += mission.score;
			var newscorehistory = hero.scorehistory ? hero.scorehistory : [];
				newscorehistory.push({ score: mission.score, timestamp: moment().utc().valueOf(), mission: mission._id });
			var updated = _.merge(hero, {score: newscore, scorehistory: _.merge(hero.scorehistory, newscorehistory) });
			updated.save(function (err) {
				if (err) { return handleError(res, err); }
				mission.score -= 1;
				mission.save(function (err) {
					if (err) { return handleError(res, err); }
					return res.json(202, hero);
				});
			});
		});
	});
};

// Updates an existing hero in the DB.
exports.update = function(req, res) {
	if(req.body._id) { delete req.body._id; }
	if(req.body.id) { delete req.body.id; }
	if(req.body.score) { delete req.body.score; }
	if(req.body.scorehistory) { delete req.body.scorehistory; }

	Hero.findOne({code: req.params.code}, function(err, hero) {
		if (err) { return handleError(res, err); }
		if(!hero) { return res.send(404); }
		var updated = _.merge(hero, req.body);
		updated.save(function (err) {
			if (err) { return handleError(res, err); }
			return res.json(202, hero);
		});
	});
};

// Deletes a hero from the DB.
exports.destroy = function(req, res) {
	Hero.findOne({code: req.params.code}, function(err, hero) {
		if(err) { return handleError(res, err); }
		if(!hero) { return res.send(404); }
		var removedhero = _.clone(hero,true);
		hero.remove(function(err) {
			if(err) { return handleError(res, err); }
			return res.send(204, removedhero);
		});
	});
};

function handleError(res, err) {
	return res.send(500, err);
}
