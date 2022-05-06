import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {EventCounter} from "../EventCounter";


describe('DocumentList', () => {
    let eventCounter = new EventCounter('version');
    test('returns data of event when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true}
        mock.onGet('/lool/monitoring/events/version/count').reply(200, data);
        eventCounter.sync().then(response => {
            expect(data).toEqual(data);
            done();
        });
    });

    test('returns data of event when retrieve is correctly called other method', done => {
        let spy = jest.spyOn(axios, "get");
        eventCounter.sync().then(response => {
            expect(spy).toHaveBeenCalledWith("/lool/monitoring/events/version/count");
            done();
        })
    })
});