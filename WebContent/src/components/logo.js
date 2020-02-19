// 3rd Party
import React from 'react';


/*
    Logotipo Flag N' Patch versão aumentada para apresentação principal.
    (Para mais pequeno usar o da barra da aplicação)
*/
const Logo = (props) => {
    return(
        <div className="d-flex justify-content-center">
            <figure className="figure w-50">
                <img src="/img/icon.png" alt="Icon" className="figure-img img-fluid"/>
            </figure>
        </div>
    );
}

export default Logo;