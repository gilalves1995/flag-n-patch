// 3rd Party
import React, { Component } from 'react';
import { Route, Switch } from 'react-router-dom';


/*
    Página de erro apresentada para routes que não
    estão definidas.
    
    Para esta página ser apresentada o servidor deve
    devolver index.html no caso de 404 para que a
    que BrowserRouter possa detectar o erro.
*/
const NotFoundPage =(props) => {
    return(
        <div>
            <h1>404: Page Not Found</h1>
        </div>
    );
};

export default NotFoundPage;