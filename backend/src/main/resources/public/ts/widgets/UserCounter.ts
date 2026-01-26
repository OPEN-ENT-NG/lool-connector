import http from 'axios';
import {AsyncWidget} from './AsyncWidget';

export interface UserCounter {
    data: number;

    sync(): Promise<void>;
}

export class UserCounter extends AsyncWidget {

    constructor() {
        super();
        this.data = 0;
    }

    async sync(): Promise<void> {
        this.loading = true;
        try {
            const {data} = await http.get('/lool/monitoring/users/count');
            this.data = data.count;
        } catch (err) {
            throw err;
        }
        this.loading = false;
    }
}