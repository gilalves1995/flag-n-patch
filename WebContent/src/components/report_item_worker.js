import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

// Actions 
import { selectReport, unselectReport } from '../actions/actions_reports';

const statuses = {
    WAITING: "Pendente",
    IN_RESOLUTION: "Em Resolução",
    REJECTED: "Rejeitado",
    SOLVED: "Resolvido"
}

class WorkerReportItem extends Component {

    changeStatusColorProperty(color) {
        const id = "workerReport-" + this.props.item.id;
        $(`#${id} i`).css('color', color);
        $(`#${id} span`).css('color', color);
    }

    setStatusProperties() {
        switch (this.props.item.statusDescription) {
            case statuses.WAITING:
                this.changeStatusColorProperty('#959595');
                break;
            case statuses.IN_RESOLUTION:
                this.changeStatusColorProperty('#FCF004');
                break;
            case statuses.REJECTED:
                this.changeStatusColorProperty('#FF0000');
                break;
            case statuses.SOLVED:
                this.changeStatusColorProperty('#00FF00');
        }
    }

    componentWillMount() {
        const id = this.props.item.id;
        const switchName = `switch-${id}`;
        const rowId = "row-" + this.props.item.id;

        const handleSwitchEvent = function(event) {
            const array = this.props.selectedWorkerReports;
            const report = this.props.item;
            const index = array.indexOf(report);

            if (index === -1) {
                this.props.selectReport(report);
                $(`#${rowId}`).css('background', '#CAD8D3');
                console.log("Report was added", this.props.selectedWorkerReports);
            }
            else {
                this.props.unselectReport(index);
                $(`#${rowId}`).css('background', 'white');
                console.log("Report was deleted", this.props.selectedWorkerReports);
            }

            event.preventDefault();
        }


        //$(`[name=${switchName}]`).on('change', handleSwitchEvent.bind(this));
        
        $(`#${rowId}`).on('click', handleSwitchEvent.bind(this));
        

        this.setStatusProperties();
    }

    componentDidMount() {
        const id = this.props.item.id;
        const switchName = `switch-${id}`;
        const rowId = "row-" + this.props.item.id;

        const handleSwitchEvent = function(event) {
            const array = this.props.selectedWorkerReports;
            const report = this.props.item;
            const index = array.indexOf(report);

            if (index === -1) {
                this.props.selectReport(report);
                $(`#${rowId}`).css('background', '#CAD8D3');
                console.log("Report was added", this.props.selectedWorkerReports);
            }
            else {
                this.props.unselectReport(index);
                $(`#${rowId}`).css('background', 'white');
                console.log("Report was deleted", this.props.selectedWorkerReports);
            }
            event.preventDefault();
        }
        //$(`[name=${switchName}]`).on('change', handleSwitchEvent.bind(this));

        
        $(`#${rowId}`).on('click', handleSwitchEvent.bind(this));
        
        this.setStatusProperties();
    }

    componentWillUpdate() {
        this.setStatusProperties();
        const report = this.props.item;
        const rowId = "row-" + this.props.item.id;
        if (this.props.selectedWorkerReports.indexOf(report) !== -1) {
            $(`#${rowId}`).css('background', '#CAD8D3');
        }
        else {
            $(`#${rowId}`).css('background', 'white');
        }
    }

    componentDidUpdate() {
        this.setStatusProperties();
        const report = this.props.item;
        const rowId = "row-" + this.props.item.id;
        if (this.props.selectedWorkerReports.indexOf(report) !== -1) {
            $(`#${rowId}`).css('background', '#CAD8D3');
        }
        else {
            $(`#${rowId}`).css('background', 'white');
        }
    }

    handleDetailsRedirect() {
        console.log("props", this.props);
        this.props.history.push(`/app/worker/reports/details/${this.props.item.id}`);
    }


    /*
    onRow() {
        const rowId = "row-" + this.props.item.id;
        $(`#${rowId}`).css('background', '#CAD8D3');
    }

    offRow() {
        const rowId = "row-" + this.props.item.id;
        $(`#${rowId}`).css('background', 'white');
    }
    */


    render() {
        const id = "workerReport-" + this.props.item.id;
        const rowId = "row-" + this.props.item.id;
        //const detailsLink = "/app/worker/reports/details/" + this.props.item.id;

        return (
            <tr 
                //onMouseLeave={this.offRow.bind(this)} 
                //onMouseEnter={this.onRow.bind(this)} 
                onDoubleClick={this.handleDetailsRedirect.bind(this)} 
                id={`${rowId}`}
            >
                <td>{this.props.item.priority}</td>
                <td>{this.props.item.type}</td>
                <td>
                    <div id={`${id}`} className="reportStatus">
                        
                        <span>{this.props.item.statusDescription}</span>
                        <i className="fa fa-circle" aria-hidden="true"></i>
                    </div>
                </td>
                <td>{this.props.item.address.county}</td>
                <td>{this.props.item.description}</td>

            </tr>
        );
    }
}

function mapStateToProps({ selectedWorkerReports }) {
    return { selectedWorkerReports };
}

export default connect(mapStateToProps, { selectReport, unselectReport })(WorkerReportItem);