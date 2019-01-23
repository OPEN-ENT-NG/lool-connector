import {Eventer} from 'entcore-toolkit'

export interface AsyncWidget {
    _loading: boolean;
    eventer: Eventer;
}

export class AsyncWidget {

    constructor() {
        this._loading = false;
        this.eventer = new Eventer();
    }

    set loading(state: boolean) {
        this._loading = state;
        this.eventer.trigger(`loading::${this._loading}`);
    }

    get loading() {
        return this._loading;
    }
}