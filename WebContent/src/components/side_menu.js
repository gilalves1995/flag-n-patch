// 3rd Party
import React from 'react';


/*

*/
const SideMenu = (props) => {
    return(
        <div className={`side-menu ${props.side}`}>
            {props.children}
        </div>
    );
};

export default SideMenu;