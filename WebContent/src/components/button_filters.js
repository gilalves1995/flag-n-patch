// 3rd Party
import React from 'react';


/*
    
*/
const FilterButton = (props) => {
    const btnClasses = `d-flex justify-content-center align-items-center btn btn-info rounded-circle filter-button`;

    return(
        <div className="d-flex justify-content-center align-items-center filter-button-div">
            <button className={btnClasses} onClick={props.onClick}>
                <i className="ion-ios-settings" />
            </button>
        </div>
    );
}

export default FilterButton;