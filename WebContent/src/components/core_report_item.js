import React, { Component } from 'react';
import $ from 'jquery';

const statuses = {
    WAITING: "Pendente",
    IN_RESOLUTION: "Em Resolução",
    REJECTED: "Rejeitado",
    SOLVED: "Resolvido"
}

class ReportItem extends Component {

    changeStatusColorProperty(color) {
        const id = this.props.report.id;
        $(`#${id} i`).css('color', color);
        $(`#${id} p`).css('color', color);
    }

    setStatusProperties() {
        switch (this.props.report.statusDescription) {
            case statuses.WAITING: 
                this.changeStatusColorProperty('#959595');
                break;
            case statuses.IN_RESOLUTION: 
                this.changeStatusColorProperty('#FCF004');
                break;
            case statuses.REJECTED: 
                this.changeStatusColorProperty('#FF0000');
                break;
            case statuses.SOLVED: this.changeStatusColorProperty('#00FF00');
        }
    }

    componentWillMount() {
        this.setStatusProperties();
    }

    componentDidMount() {
        this.setStatusProperties();
    }

    render() {
        const report = this.props.report;
        let image;
        if (report.imageUrl === "url-null") {
            image = (<img className="figure-img img-fluid rounded" src='/img/no-content-image.png' />);
        }
        else {
            image = (<img className="figure-img img-fluid rounded" src={report.imageUrl} />);
        }

        return (
            <div className="Report">
                <div id={`${report.id}`} className="reportStatus">
                    <i className="fa fa-circle" aria-hidden="true"></i>
                    <p>{report.statusDescription}</p>
                </div>

                <figure className="figure">
                    {image}
                </figure>
                <div className="reportInfo">
                    <ul className="info-list">
                        <li className="report-info-item">Ocorrência de tipo {report.type} </li>
                        <li className="report-info-item">{report.description}</li>
                        <li className="report-info-item">{report.numOfFollows} pessoa(s) confirma(m) a existência desta ocorrência</li>
                        <li className="report-info-item">{report.addressAsStreet}</li>
                        <li className="report-info-item"> A cargo de: {report.responsible}</li>
                    </ul>
                </div>
            </div>
        );
    }
}

export default ReportItem;

