// 3rd Party
import React from 'react';


/*

*/
const LoadingText = (props) => {
    return(
        <div className="loading-text">
            {props.message}
            <span className="blink">.</span>
            <span className="blink">.</span>
            <span className="blink">.</span>
        </div>
    );
};

export default LoadingText;