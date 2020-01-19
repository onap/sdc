import { ServerErrors } from '../utils/constants';
import { ServerErrorResponse } from './server-error-response';

describe('Test Error Response', () => {

    const requestError = {
        serviceException: {
            ecompRequestId: 'd01d4bca-2afa-4394-97c5-6d1b04409545',
            messageId: 'SVC4558',
            text: 'Error: Action is not permitted as your \'%1\' includes non-validated \'%2\' resource.',
            variables: ['service', 'vf1']
        }
    };

    const ng1ErrorResponse = {
        status: 403,
        data: { requestError },
        statusText: 'Forbidden'
    };

    const ng5ErrorResponse = {
        status: 403,
        error: { requestError },
        statusText: 'Forbidden'
    };

    const ng5InternalServerError = {
        status: 500,
        error: 'Oops, server error has occurred...',
        statusText: 'Internal Server Error'
    };

    const ng1InternalServerError = {
        status: 500,
        data: 'Oops, server error has occurred...',
        statusText: 'Internal Server Error'
    };

    it('NG1: Verify that server error response is constructed correctly from NG1 structure', () => {
        const response: ServerErrorResponse = new ServerErrorResponse(ng1ErrorResponse, true);
        const formatterMessage = 'Action is not permitted as your \'service\' includes non-validated \'vf1\' resource.';

        expect(response.ecompRequestId).toEqual(ng1ErrorResponse.data.requestError.serviceException.ecompRequestId);
        expect(response.message).toEqual(formatterMessage);
        expect(response.status).toEqual(ng1ErrorResponse.status);
        expect(response.title).toEqual(ServerErrors.ERROR_TITLE);
    });

    it('NG5: Verify that server error response is constructed correctly from NG5 structure', () => {
        const response: ServerErrorResponse = new ServerErrorResponse(ng5ErrorResponse);
        const formatterMessage = 'Action is not permitted as your \'service\' includes non-validated \'vf1\' resource.';

        expect(response.ecompRequestId).toEqual(ng5ErrorResponse.error.requestError.serviceException.ecompRequestId);
        expect(response.message).toEqual(formatterMessage);
        expect(response.status).toEqual(ng5ErrorResponse.status);
        expect(response.title).toEqual(ServerErrors.ERROR_TITLE);
    });

    it('NG1: Verify that internal server error produce generic message', () => {
        const response: ServerErrorResponse = new ServerErrorResponse(ng1InternalServerError, true);

        expect(response.message).toEqual(ServerErrors.DEFAULT_ERROR);
        expect(response.status).toEqual(ng5InternalServerError.status);
        expect(response.title).toEqual(ServerErrors.ERROR_TITLE);
    });

    it('NG5: Verify that internal server error produce generic message', () => {
        const response: ServerErrorResponse = new ServerErrorResponse(ng5InternalServerError);

        expect(response.message).toEqual(ServerErrors.DEFAULT_ERROR);
        expect(response.status).toEqual(ng5InternalServerError.status);
        expect(response.title).toEqual(ServerErrors.ERROR_TITLE);
    });
});
