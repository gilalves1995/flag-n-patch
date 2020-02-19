import React, { Component } from 'react';

import { Link } from 'react-router-dom';
import uuid from 'uuid';

class WorkerItem extends Component {
    
    render() {
        //console.log("services", this.props.item.services);
        let reportItems;
        if(this.props.item.reports) {
            if(this.props.item.reports.length === 0) {
                reportItems = (<p> Sem tipos de ocorrência associados.</p>);  
            } 
            else {
                reportItems = this.props.item.reports.map(i => {
                    const link = "/app/core/management/workers/reports/".concat(i.type).concat(`/${this.props.item.email}`);
                    return (<li key={uuid.v4()}><Link to={link}>{i.type}({i.numberOf})</Link></li>);
                });
            }
        }
        return (
            <tr>
                <td scope="row">{this.props.item.email}</td>
                <td>{this.props.item.name}</td>
                <td>{this.props.item.workingArea.join(", ")}</td>
                <td>{this.props.item.services.join(", ")}</td>
                <td><ul className="admin-console-list">{reportItems}</ul></td>
            </tr>
        );
    }
}
export default WorkerItem;

