import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {UserCounter} from "../UserCounter";


describe('UserCounter', () => {
    const userCounter = new UserCounter();
    test('returns data of user when retrieve is correctly called', done => {
        const mock = new MockAdapter(axios);
        const data = 1
        mock.onGet('/lool/monitoring/users/count').reply(200, data);
        userCounter.sync().then(() => {
            userCounter.data = 1
            expect(userCounter.data).toEqual(data);
            done();
        });
    });

    test('returns data of user when retrieve is correctly called other method', done => {
        const spy = jest.spyOn(axios, "get");
        userCounter.sync().then(() => {
            expect(spy).toHaveBeenCalledWith("/lool/monitoring/users/count");
            done();
        })
    })
});