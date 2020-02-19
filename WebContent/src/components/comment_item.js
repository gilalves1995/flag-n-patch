// 3rd Party
import React from 'react';


/*

*/
const CommentItem = (props) => {
    return(
        props.comment &&
        <div className="card mb-2">
            <div className="card-header d-flex justify-content-between">
                <span>{props.comment.authorName} <small className="text-muted">({props.comment.authorIdentifier})</small></span>
                <span>{props.comment.registerDate}</span>
            </div>
            <div className="card-block">
                <blockquote className="card-blockquote">
                    <p>{props.comment.content}</p>
                </blockquote>
            </div>
        </div>
    );
};

export default CommentItem;