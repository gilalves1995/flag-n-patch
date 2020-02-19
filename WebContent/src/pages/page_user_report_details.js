// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Field, reduxForm } from 'redux-form';
import axios from 'axios';

// Internal Modules
import CommentItem from '../components/comment_item';
import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Alert from '../components/alert';
import BASE_URL from '../utils/config';

// Actions
import { toggleFollow } from '../actions/actions_reports';


//
const markerColors = {
    "Pendente": "#959595",
    "Em Resolução": "#FCF004",
    "Resolvido": "#00FF00",
    "Rejeitado": "#FF0000"
};

const PRIORITY_LEVELS = [
    1, 2, 3, 4, 5
];

/*
    
*/
class ReportDetailsPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            id: '',
            report: null,
            comments: [],
            submittingComment: false,
            successMsg: '',
            errorMsg: '',
            selectingPriority: false,
            submittingFollow: false,
            isFollowing: false
        };
    }

    componentWillMount() {
        if (this.props.reports && this.props.reports.reports) {
            const id = this.props.match.params.id;
            const report = this.props.reports.reports[id].report;
            const isFollowing = report.isFollowing;
            this.setState({ id, report, isFollowing });
            this.getComments(id, this.props.token.token);
        }
    }

    renderCommentSection() {
        const commentItems = this.state.comments.map((comment, i) => <CommentItem key={i} comment={comment} />);

        return(
            <div>
                <a href="/index.html" className="dropdown-toggle d-block mb-2" onClick={event => event.preventDefault()}>
                    Comentários: ({commentItems.length})
                </a>
                {commentItems}
            </div>
        );
    }

    render() {
        //
        const { handleSubmit, pristine, submitting } = this.props;

        return(
            this.state.report &&
            <div className="container h-100">
                <div className="row justify-content-center custom-container">
                    <div className="col-md-6">
                        <div className="w-100 clearfix">
                            <a href="/index.html" className="float-left" onClick={this.onPreviousPageClick.bind(this)}>
                                {'<< Voltar'}
                            </a>
                            {(!this.state.isFollowing &&
                            <a href="/index.html" className="float-right" onClick={this.onSubscribeClick.bind(this)}>
                                {'Seguir >>'}
                            </a>)
                            ||
                            (<a href="/index.html" className="float-right" onClick={this.onUnsubscribeClick.bind(this)}>
                                {'Deixar de seguir >>'}
                            </a>)}
                        </div>
                        {this.state.selectingPriority &&
                        <div className="w-100">
                            <select className="form-control" onChange={this.onPriorityChange.bind(this)}>
                                <option defaultValue>Seleccione o nível de urgência...</option>
                                {PRIORITY_LEVELS.map(lvl => <option key={lvl} value={lvl}>{lvl}</option>)}
                            </select>
                        </div>}
                        <figure>
                            <img src={this.state.report.imageUrl} className="w-100 details-image"/>
                        </figure>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Morada:</strong> {this.state.report.addressAsStreet}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Tipo:</strong> {this.state.report.type}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Estado:</strong> {this.state.report.statusDescription}
                                <i className="fa fa-circle i" aria-hidden="true" style={{color: markerColors[this.state.report.statusDescription]}}></i>
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <strong>Descrição:</strong><br/>
                                {this.state.report.description}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                {this.renderCommentSection()}
                            </div>
                        </div>
                        <div className="row my-3">
                            <div className="col">
                                <form onSubmit={handleSubmit(this.onSubmit.bind(this))}>
                                    <Field className="form-control" id="commentSection" rows="3" name="content" component="textarea" placeholder="Escrever comentário..." />
                                    <div className="d-flex justify-content-end mt-2">
                                        <button type="submit" className="btn btn-primary" disabled={pristine || submitting}>Comentar</button>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
                {/* Modal */}
                {this.state.submittingComment &&
                <Modal>
                    <div className="d-flex justify-content-center align-items-center">
                        <div className="mr-3">
                            <Spinner />
                        </div>
                        <LoadingText message="A submeter comentário" />
                    </div>
                </Modal>}
            </div>
        );
    }

    getComments(id, token) {
        axios.post(`${BASE_URL}/report/getComments/${id}`, token)
            .then(response => {
                const comments = response.data;
                console.log('Comments:', comments);
                if (comments)
                    this.setState({ comments });
            })
            .catch(error => {
                console.log('Error fetching comments...', error);
            });
    }

    postComment(content) {
        this.setState({ submittingComment: true });
        const data = {
            comment: { content },
            token: this.props.token.token
        };
        axios.post(`${BASE_URL}/report/addComment/${this.state.id}`, data)
            .then(response => {
                this.props.reset();
                const comments = [...this.state.comments, response.data];
                this.setState({ comments, submittingComment: false, isFollowing: true });
            })
            .catch(error => {
                console.log('Failed to submit report', error);
                this.setState({ submittingComment: false });
            });
    }

    onSubmit(values) {
        console.log('Submitting comment...', values);
        this.postComment(values.content);
    }

    onPreviousPageClick(event) {
        event.preventDefault();

        // Go back to previous Route
        localStorage.setItem('previousLocation', '/app/user/report_details');
        this.props.history.goBack();
    }

    onSubscribeClick(event) {
        event.preventDefault();

        // Display the priority input
        this.setState({ selectingPriority: true });
    }

    onPriorityChange(event) {
        this.setState({ submittingFollow: true });
        const data = {
            token: this.props.token.token,
            priority: event.target.value
        };
        axios.post(`${BASE_URL}/report/follow/${this.state.id}`, data)
            .then(response => {
                console.log('Success!', response);
                this.setState({ submittingFollow: false, selectingPriority: false, isFollowing: true });
                this.props.dispatch(toggleFollow(this.props.match.params.id));
            })
            .catch(error => {
                console.log('Could not follow report...', error);
                this.setState({ submittingFollow: false, selectingPriority: false });
            });
    }

    onUnsubscribeClick(event) {
        event.preventDefault();

        axios.post(`${BASE_URL}/report/unfollow/${this.state.report.id}`, this.props.token.token)
            .then(response => {
                console.log('Unfollowed!', response);
                this.setState({ isFollowing: false });
                this.props.dispatch(toggleFollow(this.props.match.params.id));
            })
            .catch(error => {
                console.log('Could not unfollow...', error);
            });
    }
}

function mapStateToProps(store) {
    return {
        token: store.token,
        reports: store.reports
    };
}

function validate(values) {
    const errors = {};

    if (!values.content)
        errors.content = 'Deve introduzir um comentário antes de submeter';

    return errors;
}

export default reduxForm({
    form: 'Comment',
    validate
})(
    connect(mapStateToProps)(ReportDetailsPage)
);