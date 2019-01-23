import http from 'axios';
import {AsyncWidget} from './AsyncWidget';

export interface DocumentListItem {
    _id: string;
    filename: string;
    users: number
}

export interface DocumentList {
    data: DocumentListItem[];

    sync(): Promise<void>;
}

export class DocumentList extends AsyncWidget {

    constructor() {
        super();
        this.data = [];
    }

    async sync(): Promise<void> {
        this.loading = true;
        try {
            const {data} = await http.get('/lool/monitoring/documents');
            this.data = data;
        } catch (err) {
            throw err;
        }
        this.loading = false;
    }
}