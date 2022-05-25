import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {EventCounter} from "../EventCounter";


describe('DocumentList', () => {
    const eventCounter = new EventCounter('version');
    test('returns data of event when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = 1
        mock.onGet(`/lool/monitoring/events/${eventCounter.eventName}/count`).reply(200, data);
        eventCounter.sync().then(() => {
            eventCounter.data = 1;
            expect(eventCounter.data).toEqual(data);
            done();
        });
    });

    test('returns data of event when retrieve is correctly called other method', done => {
        let spy = jest.spyOn(axios, "get");
        eventCounter.sync().then(() => {
            expect(spy).toHaveBeenCalledWith(`/lool/monitoring/events/${eventCounter.eventName}/count`);
            done();
        })
    })
});