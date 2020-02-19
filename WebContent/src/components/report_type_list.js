import React, { Component } from 'react';
import { connect } from 'react-redux';

// Components
import ReportTypeItem from './report_type_item';

// Actions
import { loadReportTypes } from '../actions/actions_report_types';

class ReportTypeList extends Component {


    render() {
        let reportTypeItems;
        if (this.props.reportTypes) {
            reportTypeItems = this.props.reportTypes.map(item => {                
                if (this.props.selectedReportType && this.props.selectedReportType.name === item.name) {
                    return (<ReportTypeItem key={item.name} item={item} selected={true} />)
                }
                else return (<ReportTypeItem key={item.name} item={item} selected={false} />)
            });
        }
        return (
            <div className="col-sm-5 pt-5 mt-5 ml-5">
                <h4 className="form-descript"> Os seus tipos de ocorrência: </h4>
                <div className="typeList">
                    {reportTypeItems}
                </div>
            </div>
        );
    }
}

function mapStateToProps({ selectedReportType }) {
    return { selectedReportType };
}

export default connect(mapStateToProps, {})( ReportTypeList );