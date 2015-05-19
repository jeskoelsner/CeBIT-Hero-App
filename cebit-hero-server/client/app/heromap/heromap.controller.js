'use strict';

angular.module('cebitHeroRevisedApp')
	.controller('HeromapCtrl',function ($scope, $http, socket, mapboxService, windowDimensions) {
		mapboxService.init({ accessToken: 'pk.eyJ1IjoicHJveHlsaXR0bGUiLCJhIjoiRmxJZ1dsYyJ9.qCRLx6TCUEb1PF-rvId7pA' });

		$scope.allHeroes = [];
		$scope.lastMission = null;
		$scope.counter = 200;

		var countdown;

		var today_start = moment().utc().startOf('day').valueOf();
		var today_end = moment().utc().endOf('day').valueOf();
		var last_start;
		var last_end = moment().utc().valueOf();

		$scope.height = windowDimensions.height()-20;
		$scope.width = windowDimensions.width()/2-20;


		$scope.getCSVSort = function (){
			$location.path( '/api/heroes/csv/0' );
		};

		$scope.getCSVRand = function(){
			$location.path( '/api/heroes/csv/1' );
		};

		$scope.finished = function(){
			//TODO video
		}

		$scope.getAllUsers = function(users){
			return _.map(_.cloneDeep(users), function(hero){
				if(hero.anon) hero.name = "anonymous-" + hero.code;
				if(hero.company == null) hero.company = "private";
				return hero;
			});
		}

		function isNull(n) {
			return n == null;
		}


		$scope.getTodayUsers = function(users){
			var filtered = _.map(_.cloneDeep(users), function(hero){
				if(hero.anon) hero.name = "anonymous-" + hero.code;
				if(hero.company == null) hero.company = "private";
				var anytoday = false;
				hero.score = _.reduce(hero.scorehistory, function(memo, scorehistory) {
						if(_.inRange(today_start, scorehistory.timestamp, today_end)){
							anytoday = true;
							return scorehistory.score;
						}else{
							return memo;
						}
					}, 1);

				if(_.inRange(today_start, hero.created, today_end)){
					anytoday = true;
				}

				if (anytoday){
					return hero;
				}else{
					return false;
				}
			});

			filtered = _.filter(filtered, function(el){
				return el != false;
			});

			return filtered;
		}

		$scope.getLastUsers = function(users, lastm){
			var filtered = _.map(_.cloneDeep(users), function(hero){
				if(hero.anon) hero.name = "anonymous-" + hero.code;
				if(hero.company == null) hero.company = "private";
				var anytoday = false;
				hero.score = _.reduce(hero.scorehistory, function(memo, scorehistory) {
						if(_.inRange(lastm.created, scorehistory.timestamp, moment().utc().valueOf())){
							anytoday = true;
							return scorehistory.score;
						}else{
							return memo;
						}
					}, 1);

					if (anytoday){
						return hero;
					}else{
						return false;
					}
				});

				filtered = _.filter(filtered, function(el){
					return el != false;
				});

				return filtered;
		}


		$http.get('/api/heroes').success(function(allHeroes) {
			$scope.allHeroes = allHeroes;

			/*
			var dayHeroes = _.cloneDeep(allHeroes);
			_.each(dayHeroes, function(hero){
				_.pluck(_.filter(hero.scorehistory, function(triple){ return _.inRange(today_start, triple.timestamp, today_end); }), 'score')
				hero.score = 1 + _.reduce(, function(sum, n) {return sum + n});
			});
			$scope.todayHeroes = dayHeroes;
			*/

			$http.get('/api/missions/0').success(function(lastMission){
				last_start = lastMission.created;

				$scope.lastMission = [ lastMission ];

				var countdown = (lastMission.created + lastMission.runtime + lastMission.pausetime - moment().utc().valueOf() ) / 1000;
				$scope.countdown = countdown;

				$scope.$broadcast('timer-set-countdown-seconds', countdown );
				socket.syncUpdates('mission', $scope.lastMission);
				socket.syncUpdates('hero', $scope.allHeroes);
			});
		});

		$scope.$on('$destroy', function () {
			socket.unsyncUpdates('mission');
			socket.unsyncUpdates('hero');
		});
	});
