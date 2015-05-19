'use strict';

angular.module('cebitHeroRevisedApp')
	.controller('MainCtrl', function ($scope, $http, socket) {
		$scope.awesomeThings = [];

		$http.get('/api/heroes').success(function(awesomeThings) {
			$scope.awesomeThings = awesomeThings;
			socket.syncUpdates('hero', $scope.awesomeThings);
		});

		$scope.addThing = function() {
			if($scope.newThing === '') {
				return;
			}
			$http.post('/api/heroes', { name: $scope.newThing });
			$scope.newThing = '';
		};

		$scope.deleteThing = function(thing) {
			$http.delete('/api/heroes/' + thing._id);
		};

		$scope.$on('$destroy', function () {
			socket.unsyncUpdates('hero');
		});
	});
