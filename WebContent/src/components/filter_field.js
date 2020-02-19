// 3rd Party
import React from 'react';
import SwitchButton from 'react-switch-button';


/*
    
*/
const FilterField = (props) => {
    return(
        <div className="mb-2">
            <SwitchButton name={props.name} defaultChecked={true} onChange={event => props.onChange(event)}/>
            <span className="filter-span h6 text-muted">{props.name}</span>
        </div>
    );
}

export default FilterField;