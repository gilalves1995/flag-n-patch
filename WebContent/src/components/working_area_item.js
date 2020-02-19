import React, { Component } from 'react';

class WorkingAreaItem extends Component {

    removeCounty() {
        this.props.onDelete(this.props.item);
    }

    render() {
        return (
            <div className="reportType">
                <a href="#"><i onClick={this.removeCounty.bind(this)} className="fa fa-times edit-button" aria-hidden="true"></i></a>
                <li >{this.props.item} </li>
            </div>
        );
    }
}

export default WorkingAreaItem;

