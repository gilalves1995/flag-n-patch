import React, { Component } from 'react';
import { connect } from 'react-redux';
import axios from 'axios';
import BASE_URL from '../utils/config';

// Components 
import CommentItem from '../components/comment_item';


const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000"
};

const PRIORITY_LEVELS = [
    1, 2, 3, 4, 5
];

class WorkerReportDetailsPage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            report: '',
            comments: []
        }
    }



    loadReportDetails() {
        const token = JSON.parse(localStorage.getItem('token'));
        const id = this.props.match.params.id;
        axios.post(`${BASE_URL}/report/reportById/${id}`, token)
            .then(response => {
                if (response.status === 200) {
                    console.log("report", response.data);
                    this.setState({ report: response.data });
                    $("#statusSelectList").val(`${this.state.report.statusDescription}`);
                }
            })
            .catch(err => {
                console.log("An error occured while trying to load report details.");
            })
    }

    componentWillMount() {
        this.loadReportDetails();
    }

    handleSubmit() {
        const { alert } = this.refs;
        const token = JSON.parse(localStorage.getItem('token'));
        const id = this.props.match.params.id;
        const newStatus = $("#statusSelectList").val();
        const description = $("#statusTextarea").val();

        console.log("newStatus", newStatus);
        console.log("desc", description);

        const status = {
            statusDescription: newStatus,
            description: description
        }

        if (this.validDescription(description)) {
            axios.post(`${BASE_URL}/backoffice/report/change/${id}`, { token, status })
                .then(response => {
                    if (response.status === 200) {
                        console.log("response", response);

                        const newReport = this.state.report;
                        newReport.statusDescription = response.data.statusDescription;
                        this.setState({ report: newReport });
                        console.log("newReport", this.state.report);

                        alert.classList.remove('d-none');
                        alert.classList.add('text-success');
                        alert.innerHTML = 'Actualização de estado realizada com sucesso.';
                        setTimeout(() => {
                            alert.classList.add('d-none');
                            alert.classList.remove('text-success');
                            alert.innerHTML = '';
                            $("#statusTextarea").val("");
                            this.goBack();
                        }, 3000);
                    }
                })
                .catch(err => {
                    console.log("An error occured when trying to update this report status.");

                    if (err.response.status === 409) {
                        alert.classList.remove('d-none');
                        alert.classList.add('text-danger');
                        alert.innerHTML = 'Esta ocorrência já possui o estado introduzido.';
                        setTimeout(() => {
                            alert.classList.add('d-none');
                            alert.classList.remove('text-danger');
                            alert.innerHTML = '';
                            $("#statusTextarea").val("");
                        }, 3000);
                    }
                })
            }
        else {
            alert.classList.remove('d-none');
            alert.classList.add('text-danger');
            alert.innerHTML = 'Descrição da mudança de estado é obrigatória.';
            setTimeout(() => {
                alert.classList.add('d-none');
                alert.classList.remove('text-danger');
                alert.innerHTML = '';
                $("#statusTextarea").val("");
            }, 3000);
        }
    }

    getComments(id, token) {
        axios.post(`${BASE_URL}/report/getComments/${id}`, token)
            .then(response => {
                const comments = response.data;
                this.setState({ comments });
                console.log('Comments:', comments);

            })
            .catch(error => {
                console.log('Error fetching comments...', error);
            });
    }

    renderCommentSection() {
        const commentItems = this.state.comments.map((comment, i) => <CommentItem key={i} comment={comment} />);
        return (
            <div>
                <a href="/index.html" className="dropdown-toggle d-block mb-2" onClick={event => event.preventDefault()}>
                    Comentários: ({commentItems.length})
                </a>
                {commentItems}
            </div>
        );
    }


    validDescription(description) {
        if (!description || description.length === 0) {
            return false;
        }
        return true;
    }

    goBack() {
        this.props.history.push('/app/worker/reports');
    }

    render() {
        const report = this.state.report;
        return (
            <div className="container h-100">
                <div className="row">
                    <div className="col-md-2 goBackBttnItem">
                        <i onClick={this.goBack.bind(this)} className="fa fa-arrow-left" aria-hidden="true"> <span id="goBackLabel"> Voltar </span></i>
                    </div>
                </div>
                <div className="row justify-content-left custom-container mt-2">

                    <div className="col-md-6">
                        <figure>
                            <img src={report.imageUrl} className="w-100 details-image" />
                        </figure>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Morada:</strong> {report.addressAsStreet}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Tipo:</strong> {report.type}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Estado:</strong> {report.statusDescription}
                                <i className="fa fa-circle i" aria-hidden="true" style={{ color: markerColors[report.statusDescription] }}></i>
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Descrição:</strong><br />
                                {report.description}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                {this.renderCommentSection()}
                            </div>
                        </div>
                    </div>

                    {/* Here will be the update report status select list  */}
                    <div className="col-md-5">
                        <div className="updateReportStatusArea">
                            <h6> Actualizar estado da ocorrência: </h6>
                            <div className="form-group">
                                <select className="form-control" id="statusSelectList">
                                    <option> Resolvido </option>
                                    <option> Em Resolução </option>
                                    <option> Rejeitado </option>
                                    <option> Pendente </option>
                                </select>
                                <textarea className="form-control input-sm mt-2" placeholder="Descrição" id="statusTextarea" />

                                <button className="btn btn-primary form-control mt-3"
                                    onClick={this.handleSubmit.bind(this)}> Actualizar </button>
                                <div className="submit-error" ref="alert"></div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

function mapStateToProps({ }) {
    return {};
}

export default connect(mapStateToProps, {})(WorkerReportDetailsPage);