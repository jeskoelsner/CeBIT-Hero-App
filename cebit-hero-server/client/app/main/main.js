'use strict';

angular.module('cebitHeroRevisedApp')
	.config(function ($stateProvider) {
		$stateProvider
			.state('main', {
				url: '/main',
				templateUrl: 'app/main/main.html',
				controller: 'MainCtrl'
			});
	});