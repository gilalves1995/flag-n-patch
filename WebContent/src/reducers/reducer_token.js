// Internal Modules
import {
    LOGIN,
    LOGOUT,
    FETCHED_TOKEN_FROM_LOCALSTORAGE
} from '../actions/actions_token';
import { RESET_NETWORK_STATUSES } from '../actions/actions_network';


const initialState = {
    loggingIn: false,
    loggingOut: false,
    error: null,
    token: null
};

// Checks whether fetched token is valid
function validToken(token) {
    if (!token)
        return false;
    if (Date.now() > token.expirationDate) {
        localStorage.removeItem('token');
        return false;
    }
    return true;
}

export default function(state = initialState, action) {
    switch (action.type) {
        case LOGIN + '_PENDING': {
            return {
                ...state,
                loggingIn: true
            };
        }
        case LOGIN + '_REJECTED': {
            const error = {...action.payload};
            return {
                ...state,
                loggingIn: false,
                token: null,
                error: action.payload
            };
        }
        case LOGIN + '_FULFILLED': {
            // Save token to localstorage
            localStorage.setItem('token', JSON.stringify(action.payload.data));

            return {
                ...state,
                loggingIn: false,
                error: null,
                token: action.payload.data
            };
        }
        case LOGOUT + '_PENDING': {
            return {
                ...state,
                loggingOut: true
            };
        }
        // Mesmo comportamento em caso de erro ou sucesso
        case LOGOUT + '_REJECTED':
        case LOGOUT + '_FULFILLED': {
            // Remove token from localstorage
            localStorage.removeItem('token');

            return {
                ...state,
                loggingOut: false,
                token: null
            };
        }
        case FETCHED_TOKEN_FROM_LOCALSTORAGE: {
            if (action.payload && validToken(action.payload)) {
                return {
                    ...state,
                    token: action.payload
                };
            }
            return { ...state };
        }
        case RESET_NETWORK_STATUSES: {
            return {
                ...state,
                error: null,
                loggingIn: false,
                loggingOut: false
            };
        }
    }
    return state;
}