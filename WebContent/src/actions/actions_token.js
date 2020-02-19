// 3rd Party
import axios from 'axios';

// Internal Modules
import BASE_URL from '../utils/config';


export const FETCHED_TOKEN_FROM_LOCALSTORAGE = 'FETCHED_TOKEN_FROM_LOCALSTORAGE';
export const LOGIN = 'LOGIN';
export const LOGOUT = 'LOGOUT';

/*
    Acção a ser dispatched quando se tenciona procurar o token
    na localstorage.

    Payload pode ser o token ou null caso não se encontre nenhum
    valor na localstorage.
*/
export function fetchTokenFromLocalstorage() {
    return {
        type: FETCHED_TOKEN_FROM_LOCALSTORAGE,
        payload: JSON.parse(localStorage.getItem('token'))
    };
}

/*
    Acção a ser dispatched no acto de login.

    Payload é uma promise que será tratada pelo
    redux-promise-middleware. Este fará dispatch de
    várias acções para informar dos vários estados
    da promise.
*/
export function doLogin(data) {
    return {
        type: LOGIN,
        payload: axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/login`,
            data: data
        })
    };
}

/*
    Acção a ser dispatched no acto de logout.

    Payload é uma promise que será tratada pelo
    redux-promise-middleware. Este fará dispatch de
    várias acções para informar dos vários estados
    da promise.
*/
export function doLogout(token) {
    return {
        type: LOGOUT,
        payload: axios.post(`${BASE_URL}/logout`, token)
    };
}