import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {UserCounter} from "../UserCounter";


describe('UserCounter', () => {
    let userCounter = new UserCounter();
    test('returns data of user when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = {response: true}
        mock.onGet('/lool/monitoring/users/count').reply(200, data);
        userCounter.sync().then(response => {
            expect(data).toEqual(data);
            done();
        });
    });

    test('returns data of user when retrieve is correctly called other method', done => {
        let spy = jest.spyOn(axios, "get");
        userCounter.sync().then(response => {
            expect(spy).toHaveBeenCalledWith("/lool/monitoring/users/count");
            done();
        })
    })
});