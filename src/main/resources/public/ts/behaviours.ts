import {Behaviours} from 'entcore';

Behaviours.register('lool', {
    rights: {
        workflow: {
            openFile: 'fr.openent.lool.controller.LoolController|open'
        },
        resource: {}
    }
});
