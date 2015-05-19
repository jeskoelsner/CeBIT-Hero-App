'use strict';

angular.module('cebitHeroRevisedApp')
	.config(function ($stateProvider) {
		$stateProvider
			.state('heromap', {
				url: '/',
				templateUrl: 'app/heromap/heromap.html',
				controller: 'HeromapCtrl'
			});
	});