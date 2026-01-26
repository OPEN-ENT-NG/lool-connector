import http from 'axios';
import {Behaviours} from 'entcore';
import {capabilities, create, initPostMessage} from './sniplets/create';
import {Metadata} from "./providers/Provider";
import {ProviderFactory} from "./providers/ProviderFactory";

console.log('lool behaviours');

type Context = {
    provider: string,
    templates: Array<string>
    capabilities: Array<Metadata>
}

Behaviours.register('lool', {
    rights: {
        workflow: {
            openFile: 'fr.openent.lool.controller.LoolController|open',
            createDocument: 'fr.openent.lool.controller.LoolController|createDocumentFromTemplate'
        },
        resource: {}
    },
    initPostMessage,
    provider: null,
    init: async function () { //FIXME Fix init issue. Use a event Listener to init sniplets
        console.debug('Init behaviours')
        const context: Context = await this.getWopiContext();
        this.provider = ProviderFactory.provider(context.provider);
        this.provider.setCapabilities(context.capabilities);
        this.provider.setTemplates(context.templates);
        if (this.initCallback.length > 0) {
            console.debug('Calling init callbacks', this.initCallback);
            this.initCallback.map(cb => cb());
        }

        console.debug(this);
    },
    initCallback: [],
    capabilities,
    canBeOpenOnLool: function ({metadata}): boolean {
        return Behaviours.applicationsBehaviours['lool'].provider.canBeOpen(metadata);
    },
    getWopiContext: async function (): Promise<Context> {
        try {
            const response: { data: Context } = await http.get('/lool/providers/context');
            return response.data;
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
