import React, { Component } from 'react';

// Components
import WorkerReportItem from './report_item_worker';


class WorkerReportList extends Component {
    render() {
        let workerReports;
        if (this.props.reports) {
            workerReports = this.props.reports.map(item => {
                return (
                    <WorkerReportItem history={this.props.history} key={item.id} item={item} />
                );
            })
        }
        return (
            <div className="container-fluid workerReportList" >
                <div className="worker_report_table">
                    <table className="table header-fixed">
                        <thead>
                            <tr>
                                <th>Prioridade</th>
                                <th>Tipo</th>
                                <th>Estado</th>
                                <th>Concelho</th>
                                <th>Descrição</th>
                            </tr>
                        </thead>
                        <tbody>
                            {workerReports}
                        </tbody>
                    </table>
                </div>
            </div>
        );
    }


}

export default WorkerReportList;
