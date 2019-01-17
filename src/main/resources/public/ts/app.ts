import {ng} from 'entcore';
import * as controllers from './controllers';


for (let controller in controllers) {
    ng.controllers.push(controllers[controller]);
}