import {ng} from 'entcore';
import {mainController} from './controllers/monitoring';
import * as directives from "./directives";

for (let directive in directives) {
    ng.directives.push(directives[directive]);
}

ng.controllers.push(mainController);