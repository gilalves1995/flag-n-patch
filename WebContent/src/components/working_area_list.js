import React, { Component } from 'react';

// Components 
import WorkingAreaItem from './working_area_item';

class WorkingAreaList extends Component {

    removeCounty(county) {
        this.props.onDelete(county);
    }
    
    render() {
        let workingAreaList;
        if(this.props.workingArea) {
            workingAreaList = this.props.workingArea.map(item => {
                return (<WorkingAreaItem key={item} item={item} onDelete={this.removeCounty.bind(this)} />);
            })
        }
        return (
            <div className="typeList">
                {workingAreaList}
            </div>
        );
    }
}

export default WorkingAreaList;