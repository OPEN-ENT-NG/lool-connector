jest.mock('entcore-toolkit', () => Object.assign(
    {},
    (jest as any).requireActual('entcore-toolkit'),
    {http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()}}
));

import {http} from 'entcore-toolkit';
import {mockHttpResponse} from '../../test-utils/httpMock';
import {UserCounter} from "../UserCounter";


describe('UserCounter', () => {
    const userCounter = new UserCounter();
    test('returns data of user when retrieve is correctly called', done => {
        const data = 1;
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        userCounter.sync().then(() => {
            userCounter.data = 1
            expect(userCounter.data).toEqual(data);
            done();
        });
    });

    test('returns data of user when retrieve is correctly called other method', done => {
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(1));
        userCounter.sync().then(() => {
            expect(http.get).toHaveBeenCalledWith("/lool/monitoring/users/count");
            done();
        })
    })
});
