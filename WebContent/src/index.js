// 3rd Party
import React from 'react';
import ReactDOM from 'react-dom';
import { Provider } from 'react-redux';
import { createStore, applyMiddleware } from 'redux';
import thunk from 'redux-thunk';
import promiseMiddleware from 'redux-promise-middleware';

// Internal Modules
import App from './components/app';
import reducers from './reducers';


const createStoreWithMiddleware = applyMiddleware(promiseMiddleware(), thunk)(createStore);
export const store = createStoreWithMiddleware(reducers);

/*
    Ponto de entrada da aplicação.

    'Desenha' a aplicação no elemento da DOM com id #app.

    O primeiro componente que engloba toda a aplicação é o Provider.
    Este recebe a store e torna-a disponível a toda a aplicação.
    Nota: Estar disponível a toda a aplicação não quer dizer que possa ser directamente
    acedida. Os componentes devem mostrar a sua intenção de ler valores da store a partir
    do connector 'connect' e os valores da mesma só se alteram a partir do dispatching
    de acções, as quais chegarão aos reducers.
*/
ReactDOM.render(
    <Provider store={store}>
        <App />
    </Provider>
, document.getElementById('app'));
