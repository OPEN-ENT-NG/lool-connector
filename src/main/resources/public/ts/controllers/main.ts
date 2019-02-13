import {Behaviours, moment, ng, template, workspace} from 'entcore';
import http from "axios";

declare let window: any;

interface LoolEvent {
    MessageId: string;
    SendTime: string;
    Values?: any;
}

const EVENT_RESPONSES = {
    App_LoadingStatus: 'Host_PostmessageReady',
    UI_InsertGraphic: 'Action_InsertGraphic'
};

export const mainController = ng.controller('MainController', ['$scope',
    ($scope) => {
        $scope.documentId = window.documentId;
        $scope.message = {
            origin: null,
            source: null,
            send: function (data: any) {
                this.source.postMessage(JSON.stringify(data), this.origin);
            }
        };
        $scope.fromEvent = {
            beforeunload: true
        };
        $scope.display = {
            lightbox: false
        };

        $scope.shortDate = function (dateItem) {
            if (!dateItem) {
                return moment().format('L');
            }
            if (typeof dateItem === "number")
                return moment(dateItem).format('L');

            if (typeof dateItem === "string")
                return moment(dateItem.split(' ')[0]).format('L');

            return moment().format('L');
        };

        $scope.formatDocumentSize = workspace.v2.service.formatDocumentSize;

        $scope.App_LoadingStatus = ({Status}: any, {source, origin}: MessageEvent) => {
            const data: LoolEvent = {
                MessageId: EVENT_RESPONSES.App_LoadingStatus,
                SendTime: Date.now().toString()
            };

            $scope.message.send(data);
        };

        $scope.UI_InsertGraphic = () => {
            template.open('lightbox', 'UI_InsertGraphic');
            $scope.display.lightbox = true;
            $scope.$apply();
        };

        $scope.UI_Close = () => {
            $scope.fromEvent.beforeunload = false;
            const url = `/lool/wopi/documents/${window.documentId}/tokens/${window.accessToken}`;
            if (navigator.sendBeacon) {
                navigator.sendBeacon(url, {})
            } else {
                http.delete(url);
            }
            window.close();
        };

        $scope.UI_FileVersions = async () => {
            $scope.revisions = (await http.get(`/workspace/document/${window.documentId}/revisions`)).data;
            template.open('lightbox', 'UI_FileVersions');
            $scope.display.lightbox = true;
            $scope.$apply();
        };

        $scope.insertImage = async (file) => {
            const {data} = await http.get(`/lool/documents/${window.documentId}/tokens?access_token=${window.accessToken}&image=${file._id}`);
            const {_id} = data;
            const eventResponse: LoolEvent = {
                MessageId: EVENT_RESPONSES.UI_InsertGraphic,
                SendTime: Date.now().toString(),
                Values: {
                    filename: file.title,
                    url: `${window.origin}/lool/documents/${window.documentId}/image/${file._id}?access_token=${window.accessToken}&token=${_id}`
                }
            };
            $scope.message.send(eventResponse);
            $scope.display.lightbox = false;
            delete $scope.file;
            $scope.$apply();
        };

        $scope.initMessageApi = ({source, origin}: MessageEvent) => {
            if (!$scope.message.source || !$scope.message.origin) {
                $scope.message = {...$scope.message, source, origin};
            }
        };

        Behaviours.applicationsBehaviours.lool.initPostMessage(function (e: MessageEvent) {
            $scope.initMessageApi(e);
            const event: LoolEvent = JSON.parse(e.data);
            if (event.MessageId in $scope) {
                $scope[event.MessageId](event.Values, e);
            }
        });

        const eventMethod = window.addEventListener ? 'addEventListener' : 'attachEvent';
        const eventer = window[eventMethod];
        eventer('beforeunload', () => {
            if ($scope.fromEvent.beforeunload) {
                $scope.UI_Close();
            }
        });

        if (window.opener && window.resync == 'true') {
            const message = {
                id: 'lool@resync'
            };
            window.opener.postMessage(JSON.stringify(message), window);
        }
    }]);