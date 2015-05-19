'use strict';

describe('Controller: HeroidCtrl', function () {

  // load the controller's module
  beforeEach(module('cebitHeroRevisedApp'));

  var HeroidCtrl, scope;

  // Initialize the controller and a mock scope
  beforeEach(inject(function ($controller, $rootScope) {
    scope = $rootScope.$new();
    HeroidCtrl = $controller('HeroidCtrl', {
      $scope: scope
    });
  }));

  it('should ...', function () {
    expect(1).toEqual(1);
  });
});
