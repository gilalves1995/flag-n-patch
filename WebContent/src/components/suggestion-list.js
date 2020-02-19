// 3rd Party
import React, { Component } from 'react';

// Internal Modules
import SuggestionItem from './suggestion-item';


/*

*/
class SuggestionList extends Component {
    render() {
        return(
            <div>
                <a href="/index.html" className="dropdown-toggle d-block" onClick={event => event.preventDefault()}>
                    Sugestões: ({this.props.suggestions.length})
                </a>
                <div className="d-flex justify-content-between mb-4 suggestion-list">
                    {this.props.suggestions.map((suggestion, i) => {
                        return(
                            <SuggestionItem key={i} suggestion={suggestion} />
                        );
                    })}
                </div>
            </div>
        );
    }
}

export default SuggestionList;
