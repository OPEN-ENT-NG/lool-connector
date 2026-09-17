jest.mock('entcore-toolkit', () => Object.assign(
    {},
    (jest as any).requireActual('entcore-toolkit'),
    {http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()}}
));

import {http} from 'entcore-toolkit';
import {mockHttpResponse} from '../../test-utils/httpMock';
import {EventCounter} from "../EventCounter";


describe('DocumentList', () => {
    const eventCounter = new EventCounter('version');
    test('returns data of event when retrieve is correctly called', done => {
        const data = 1;
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        eventCounter.sync().then(() => {
            eventCounter.data = 1;
            expect(eventCounter.data).toEqual(data);
            done();
        });
    });

    test('returns data of event when retrieve is correctly called other method', done => {
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(1));
        eventCounter.sync().then(() => {
            expect(http.get).toHaveBeenCalledWith(`/lool/monitoring/events/${eventCounter.eventName}/count`);
            done();
        })
    })
});
