jest.mock('entcore-toolkit', () => Object.assign(
    {},
    (jest as any).requireActual('entcore-toolkit'),
    {http: {get: jest.fn(), post: jest.fn(), put: jest.fn(), delete: jest.fn(), postFile: jest.fn(), putFile: jest.fn()}}
));

import {http} from 'entcore-toolkit';
import {mockHttpResponse} from '../../test-utils/httpMock';
import {DocumentList, DocumentListItem} from "../DocumentList";


describe('DocumentList', () => {
    const documentListItem: DocumentListItem = {"_id": "id", "filename": "red", "users": 1};
    const documentList = new DocumentList();
    documentList.data = [documentListItem]
    test('returns data when retrieve is correctly called', done => {
        const data = {"_id": "id", "filename": "red", "users": 1};
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse(data));
        documentList.sync().then(() => {
            expect(documentList.data).toEqual(data);
            done();
        });
    });

    test('returns data when retrieve is correctly called other method', done => {
        (http.get as jest.Mock).mockResolvedValueOnce(mockHttpResponse([documentListItem]));
        documentList.sync().then(() => {
            expect(http.get).toHaveBeenCalledWith("/lool/monitoring/documents");
            done();
        })
    })
});
