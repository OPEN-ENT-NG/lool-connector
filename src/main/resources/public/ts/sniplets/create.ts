import {Eventer} from 'entcore-toolkit';
import {idiom as lang} from 'entcore';

export const extensions = ['odt', 'odp', 'ods', 'odg'];
export const initPostMessage = (callback) => {
    const eventMethod = window.addEventListener ? 'addEventListener' : 'attachEvent';
    const eventer = window[eventMethod];
    const messageEvent = eventMethod == 'attachEvent' ? 'onmessage' : 'message';

    return eventer(messageEvent, callback, false);
};
export const capabilities = {};

declare let window: any;

let loolDocument = {
    extension: '',
    name: '',
    folder: null
};

export const create = {
    title: 'lool.sniplet.create-document.title',
    description: 'lool.sniplet.create-document.description',
    controller: {
        eventer: new Eventer(),
        lightbox: {
            display: false
        },
        document: loolDocument,
        lang,
        documentTypeList: ([] as any[]),
        init: function () {
            const that: any = this;
            lang.addBundle('/lool/i18n', () => {
                if (this.documentTypeList.length === 0) {
                    extensions.forEach((ext) => this.documentTypeList.push({
                        extension: ext,
                        title: lang.translate(`lool.sniplet.create-document.type.${ext}`)
                    }));
                }
                this.initDocument();
                initPostMessage((event) => {
                    const data = JSON.parse(event.data);
                    if (data.id === 'lool@getFolderResponse') {
                        that.eventer.trigger(data.id, data);
                        if ('folderId' in data) {
                            loolDocument.folder = data.folderId;
                        }
                        that.lightbox.display = true;
                        that.$apply();
                        console.warn(this.document);
                    }
                });
            });
        },
        initDocument: function () {
            console.warn('initDocument');
            loolDocument = {
                extension: this.documentTypeList[0].extension,
                name: '',
                folder: null
            };
            this.document = loolDocument;
        },
        openLightbox: function () {
            this.initDocument();
            const event = {
                id: 'lool@getFolder'
            };
            this.eventer.once('lool@getFolderResponse', (data) => loolDocument.folder = data.folderId);
            window.postMessage(JSON.stringify(event), window.location.origin);
        },
        createDocument: function (e: Event) {
            e.preventDefault();
            console.error(this.document);
            window.open(`/lool/document?type=${this.document.extension}&name=${this.document.name}${loolDocument.folder ? `&folder=${loolDocument.folder}` : ''}`);
            this.lightbox.display = false;
            this.initDocument();
        }
    }
}