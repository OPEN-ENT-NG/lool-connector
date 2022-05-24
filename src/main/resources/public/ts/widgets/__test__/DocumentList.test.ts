import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {DocumentList, DocumentListItem} from "../DocumentList";


describe('DocumentList', () => {
    const documentListItem: DocumentListItem = {"_id": "id", "filename": "red", "users": 1};
    const documentList = new DocumentList();
    documentList.data = [documentListItem]
    test('returns data when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {"_id": "id", "filename": "red", "users": 1};
        mock.onGet('/lool/monitoring/documents').reply(200, data);
        documentList.sync().then(() => {
            expect(documentList.data).toEqual(data);
            done();
        });
    });

    test('returns data when retrieve is correctly called other method', done => {
        let spy = jest.spyOn(axios, "get");
        documentList.sync().then(() => {
            expect(spy).toHaveBeenCalledWith("/lool/monitoring/documents");
            done();
        })
    })
});