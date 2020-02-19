// 3rd Party
import React from 'react';


/*
    Componente Alert do Bootstrap.

    Faz render dos elementos nested dentro dele.
*/
const Alert = (props) => {
    const header = props.type === 'danger' ? 'Erro' : 'Sucesso';
    setTimeout(() => {
        props.timeoutCallback();
    }, props.timeout * 1000);

    return(
        <div className={`alert alert-${props.type} alert-dismissible fade show alert-custom`} role="alert">
            <button type="button" className="close" data-dismiss="alert" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
            {props.children}
        </div>
    );
};

export default Alert;