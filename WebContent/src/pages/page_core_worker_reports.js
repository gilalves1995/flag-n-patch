import React, { Component } from 'react';
import { connect } from 'react-redux';

// Components
import ReportItem from '../components/core_report_item';

// Actions 
import { getReportsByType } from '../actions/actions_reports';

class ReportPage extends Component {


    componentWillMount() {
        console.log("on ComponentWillMount", this.props.reportsByType);
        this.getReportsByType();
    }

    getReportsByType() {
        console.log("PARAMS ADDED NOW: ",  this.props.match.params);
        const token = JSON.parse(localStorage.getItem('token'));
        this.props.getReportsByType(token, this.props.match.params.id, this.props.match.params.email);
        console.log("REPORTS: ", this.props.reportsByType);
        
    }

    goBack() {
        this.props.history.push('/app/core/management/workers');
    }

    render() {
        let reports;
        if(this.props.reportsByType) {
            reports = this.props.reportsByType.map(item => {
                return (<ReportItem key={item.id} report={item} />);
            });
        }

        return(
            <div>
                <div className="goBackBttnItem">
                    <i onClick={this.goBack.bind(this)} className="fa fa-arrow-left" aria-hidden="true"> <span id="goBackLabel"> Voltar </span></i>
                </div>
                <div className="ReportList">
                    {reports}
                </div>
                
            </div>
        );
    }
}

function mapStateToProps({ reportsByType }) {
    return { reportsByType };
}

export default connect(mapStateToProps, { getReportsByType })(ReportPage);