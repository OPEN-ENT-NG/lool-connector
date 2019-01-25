import http from 'axios';
import {Behaviours} from 'entcore';
import {capabilities, create, extensions, initPostMessage} from './sniplets/create';

console.log('lool behaviours');

Behaviours.register('lool', {
    rights: {
        workflow: {
            openFile: 'fr.openent.lool.controller.LoolController|open',
            createDocument: 'fr.openent.lool.controller.LoolController|createDocumentFromTemplate'
        },
        resource: {}
    },
    initPostMessage,
    extensions,
    init: function () {
        return this.getCapabilities()
    },
    capabilities,
    canBeOpenOnLool: ({metadata}): boolean => {
        return metadata.extension in capabilities && metadata["content-type"] === capabilities[metadata.extension];
    },
    getCapabilities: async function () {
        try {
            const {data} = await http.get('/lool/capabilities');
            data.forEach((capability) => capabilities[capability.extension] = capability['content-type']);
        } catch (err) {
            throw err;
        }
    },
    openOnLool: (file: { _id }) => {
        window.open(`/lool/documents/${file._id}/open`);
    },
    sniplets: {
        create
    }
});
