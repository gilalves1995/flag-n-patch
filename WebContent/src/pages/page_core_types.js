import React, { Component } from 'react';
import { connect } from 'react-redux';


// Components 
import ReportTypeForm from '../components/form_report_type';
import ReportTypeList from '../components/report_type_list';


// Actions 
import { loadReportTypes } from '../actions/actions_report_types';
import { loadWorkers } from '../actions/actions_workers';


class TypeManagementPage extends Component {
    constructor(props) {
        super(props);
        this.state = {

        }
    }

    getReportTypes() {
        const token = JSON.parse(localStorage.getItem('token'));
        this.props.loadReportTypes(token);

    }

    // Loads users with the role "work" which works in this area
    getWorkers() {
        const token = JSON.parse(localStorage.getItem('token'));
        this.props.loadWorkers(token);

    }

    componentWillMount() {
        this.getReportTypes();
        this.getWorkers();
    }


    render() {
        return (
            <div className="row grid-divider">
                <div className="col-sm-6 column-one">
                    <ReportTypeForm workers={this.props.workers} />
                </div>

                <div className="col-sm-6 column-two">
                    <ReportTypeList reportTypes={this.props.reportTypes} />
                </div>
                <div id="changeNotifyMessage"></div>
            </div>




        );
    }
}

function mapStateToProps({ reportTypes, workers }) {
    return { reportTypes, workers };
}

export default connect(mapStateToProps, { loadReportTypes, loadWorkers })(TypeManagementPage);
