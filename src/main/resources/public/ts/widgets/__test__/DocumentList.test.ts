import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {DocumentList} from "../DocumentList";


describe('DocumentList', () => {
    let documentList = new DocumentList();
    test('returns data when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true}
        mock.onGet('/lool/monitoring/documents').reply(200, data);
        documentList.sync().then(response => {
            expect(data).toEqual(data);
            done();
        });
    });

    test('returns data when retrieve is correctly called other method', done => {
        let spy = jest.spyOn(axios, "get");
        documentList.sync().then(response => {
            expect(spy).toHaveBeenCalledWith("/lool/monitoring/documents");
            done();
        })
    })
});