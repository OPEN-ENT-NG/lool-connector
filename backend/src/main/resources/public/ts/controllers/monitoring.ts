import {ng, template} from 'entcore';
import {DocumentList, EventCounter, ExtensionGraph, UserCounter} from '../widgets'

interface MonitoringScope {
    documentList: DocumentList;
    userCounter: UserCounter;
    accessCounter: EventCounter;
    newVersionCounter: EventCounter;
    extensionGraph: ExtensionGraph;
    $apply: any;
}

export const mainController = ng.controller('MainController', ['$scope',
    ($scope: MonitoringScope) => {
        $scope.documentList = new DocumentList();
        $scope.userCounter = new UserCounter();
        $scope.accessCounter = new EventCounter('ACCESS');
        $scope.newVersionCounter = new EventCounter('NEW_VERSION');
        $scope.extensionGraph = new ExtensionGraph('#extension-graph');

        template.open('main', 'monitoring');
        $scope.documentList.sync().then($scope.$apply);
        $scope.userCounter.sync().then($scope.$apply);
        $scope.accessCounter.sync().then($scope.$apply);
        $scope.newVersionCounter.sync().then($scope.$apply);
        $scope.extensionGraph.sync().then($scope.$apply);
    }]);