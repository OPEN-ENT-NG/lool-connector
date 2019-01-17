import {idiom, ng, template} from 'entcore';
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
        idiom.addBundle('/workspace/i18n');
        $scope.message = {
            origin: null,
            source: null,
            send: function (data: any) {
                this.source.postMessage(JSON.stringify(data), this.origin);
            }
        };
        $scope.display = {
            lightbox: false
        };

        $scope.App_LoadingStatus = ({Status}: any, {source, origin}: MessageEvent) => {
            $scope.message = {...$scope.message, source, origin};
            const data: LoolEvent = {
                MessageId: EVENT_RESPONSES.App_LoadingStatus,
                SendTime: Date.now().toString()
            };

            $scope.message.send(data);
        };

        $scope.UI_InsertGraphic = () => {
            // Format de retour
            // {
            //     filename: filename,
            //     url: url
            // }
            template.open('lightbox', 'UI_InsertGraphic');
            $scope.display.lightbox = true;
            $scope.$apply();
        };

        $scope.UI_Close = () => {
            window.close();
        };

        $scope.UI_FileVersions = async () => {
            $scope.revisions = (await http.get(`/workspace/document/${window.documentId}/revisions`)).data;
            template.open('lightbox', 'UI_FileVersions');
            $scope.display.lightbox = true;
            $scope.$apply();
        };

        $scope.insertImage = (file) => {
            const data: LoolEvent = {
                MessageId: EVENT_RESPONSES.UI_InsertGraphic,
                SendTime: Date.now().toString(),
                Values: {
                    filename: file.title,
                    url: `${window.origin}/lool/documents/${file._id}`
                }
            };
            $scope.message.send(data);
            console.warn(data);
            $scope.display.lightbox = false;
            delete $scope.file;
            $scope.$apply();
        };

        const eventMethod = window.addEventListener ? 'addEventListener' : 'attachEvent';
        const eventer = window[eventMethod];
        const messageEvent = eventMethod == 'attachEvent' ? 'onmessage' : 'message';

        eventer(messageEvent, function (e: MessageEvent) {
            const event: LoolEvent = JSON.parse(e.data);
            console.log(event);
            if (event.MessageId in $scope) {
                $scope[event.MessageId](event.Values, e);
            }
        }, false);
    }]);