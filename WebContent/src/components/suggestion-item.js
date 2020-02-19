// 3rd Party
import React from 'react';
import { Link } from 'react-router-dom';


/*

*/
const SuggestionItem = (props) => {
    return(
        <div className="card suggestion">
            <img src={props.suggestion.imageUrl} className="card-img-top suggestion-image"/>
            <div className="card-block">
                <h4 className="card-title">
                    {props.suggestion.addressAsStreet}
                </h4>
                <Link to={`/app/user/report/${props.suggestion.id}`}>
                    Detalhes >>
                </Link>
            </div>
        </div>
    );
};

export default SuggestionItem;