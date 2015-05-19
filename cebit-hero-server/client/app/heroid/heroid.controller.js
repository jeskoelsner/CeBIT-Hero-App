'use strict';

angular.module('cebitHeroRevisedApp')
	.controller('HeroidCtrl', function ($scope, $http, socket) {
		$scope.heroMgr = [];

		//INDEX
		$http.get('/api/heroes').success(function(heroes) {
			$scope.heroMgr = heroes;
			socket.syncUpdates('hero', $scope.heroMgr);
		});

		//Create:
		$scope.createHero = function() {
			if($scope.newHero === '') {
				return;
			}
			$http.post('/api/heroes', { name: $scope.newHero });
			$scope.newHero = '';
		};

		//Read:
		$scope.showHero = function() {
			//TODO
		};

		//Update:
		$scope.scoreHero = function() {
			//TODO
		};

		//Update:
		$scope.deleteHero = function(hero) {
			$http.delete('/api/heroes/' + hero._id);
		};

		$scope.$on('$destroy', function () {
			socket.unsyncUpdates('hero');
		});
	});
