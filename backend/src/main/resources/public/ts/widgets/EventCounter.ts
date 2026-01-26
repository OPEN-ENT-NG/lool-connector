import http from 'axios';
import {AsyncWidget} from './AsyncWidget';

export interface EventCounter {
    data: number;
    eventName: string;

    sync(): Promise<void>;
}

export class EventCounter extends AsyncWidget {

    constructor(eventName: string) {
        super();
        this.eventName = eventName;
        this.data = 0;
    }

    async sync(): Promise<void> {
        this.loading = true;
        try {
            const {data} = await http.get(`/lool/monitoring/events/${this.eventName}/count`);
            this.data = data.count;
        } catch (err) {
            throw err;
        }
        this.loading = false;
    }
}