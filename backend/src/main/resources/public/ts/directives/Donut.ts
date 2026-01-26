import {ng} from 'entcore';
import * as ApexCharts from 'apexcharts';

export const Donut = ng.directive('donut', () => {
    return {
        restrict: 'E',
        scope: {
            series: '=',
            labels: '=',
            width: '=?',
            height: '=?',
            options: '=?'
        },
        replace: true,
        template: `<div class="chart"></div>`,
        controller: function ($scope, $element, $attrs) {
            console.log('Donut directive');
            $scope.options = $scope.options || {};
            const chartDef = {
                chart: {
                    type: 'donut',
                    width: $scope.width,
                },
                legend: {
                    position: 'bottom'
                },
                series: $scope.series,
                labels: $scope.labels,
                ...$scope.options
            };

            const chart = new ApexCharts($element[0], chartDef);
            chart.render();
        }
    };
});