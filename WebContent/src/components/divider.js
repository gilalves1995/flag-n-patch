// 3rd Party
import React from 'react';


/*
    Linha divisória que consiste em duas linhas com 'OU' no centro.
    Divisão de secção na página de login.
*/
const Divider = (props) => {
    return(
        <div className="clearfix">
            <div className="float-left login-divider-hr"><hr/></div>
            <div className="float-left login-divider-text h-100">OU</div>
            <div className="float-left login-divider-hr"><hr/></div>
        </div>
    );
}

export default Divider;