// 3rd Party
import React, { Component } from 'react';
import { connect } from 'react-redux';
import axios from 'axios';

// Internal Modules
import AddReportForm from '../components/form_add_report';
import Modal from '../components/modal';
import Spinner from '../components/spinner';
import LoadingText from '../components/loading_text';
import Alert from '../components/alert';
import BASE_URL, { BUCKET } from '../utils/config';

// Actions
import { getNewReportTypes, unsetNewReportLocation, addReport, compareSuggestions } from '../actions/actions_reports';


// Default IMG src
const DEFAULT_SRC = '/img/missing.png';

/*
    
*/
class ReportPage extends Component {
    constructor(props) {
        super(props);

        this.state = {
            image: null,
            submitting: false,
            successMsg: '',
            errorMsg: '',
            suggestions: []
        };
    }

    componentWillMount() {
        if (this.props.token.token && this.props.newReportLocation) {
            const { token } = this.props.token;
            const { county } = this.props.newReportLocation.address;
            this.props.dispatch(getNewReportTypes(token, county));
        }
    }

    /*
        Se não existirem coordenadas para adicionar um novo
        report, faz redirect para a página do mapa.
    */
    componentWillReceiveProps(nextProps) {
        if (!nextProps.newReportLocation)
            this.props.history.push('/app/user/map');
    }
    
    render() {
        // Não mostra nada no caso de não ter todas as propriedades necessárias do estado
        if (!this.props.token || !this.props.newReportLocation || !this.props.newReportTypes) {
            return <noscript />;
        }

        return(
            <div className="container h-100">
                <div className="row justify-content-center custom-container">
                    <div className="col-md-7">
                        <img className="w-100 details-image mb-3 rounded" src={this.state.image ? this.state.image : DEFAULT_SRC} />
                        <AddReportForm
                            address={this.props.newReportLocation.addressAsStreet}
                            types={this.props.newReportTypes}
                            suggestions={this.state.suggestions}
                            getSuggestions={this.getSuggestions.bind(this)}
                            onImageSelect={this.onImageSelect.bind(this)}
                            onSubmit={this.onSubmit.bind(this)}
                        />
                        <button
                            className="btn btn-danger form-control mt-2"
                            onClick={this.onCancelClick.bind(this)}
                        >
                            Cancelar
                        </button>
                        {/* Modal */}
                        {this.state.submitting &&
                        <Modal>
                            <div className="d-flex justify-content-center align-items-center">
                                <div className="mr-3">
                                    <Spinner />
                                </div>
                                <LoadingText message="A registar" />
                            </div>
                        </Modal>}
                        {/* Alert - only visible in case of error or success */}
                        {(this.state.successMsg && 
                        <Alert type="success" timeout={2} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                            <strong>{this.state.successMsg}</strong> {'A redireccionar...'}
                        </Alert>)
                        ||
                        (this.state.errorMsg &&
                        <Alert type="danger" timeout={4} timeoutCallback={this.resetStateOrRedirect.bind(this)}>
                            <strong>{this.state.errorMsg}</strong>
                        </Alert>)}
                    </div>
                </div>
            </div>
        );
    }

    onSubmit(values) {
        this.setState({ submitting: true });

        if (values.image) {
            //const bucket = 'flag-n-patch.appspot.com';
            const filename = values.image.name;
            const imageUrl = `https://${BUCKET}/gcs/${BUCKET}/${filename}`;
            axios({
                method: 'post',
                url: imageUrl,
                headers: { 'Content-Type': values.image.type },
                data: values.image
            })
            .then(response => {
                const data = this.buildDataToSubmit(values, imageUrl);
                this.submitReportData(data);
            })
            .catch(error => {
                console.log('Error while submitting image to GCS...', error);
            });
        } else {
            const data = this.buildDataToSubmit(values);
            this.submitReportData(data);
        }
    }

    onImageSelect(image) {
        this.setState({ image });
    }

    onCancelClick() {
        localStorage.setItem('previousLocation', '/app/user/report');
        this.props.dispatch(unsetNewReportLocation());
    }

    buildDataToSubmit(values, imageUrl = DEFAULT_SRC) {
        if (values.image) delete values.image;
        return {
            token: this.props.token.token,
            report: {
                ...values,
                imageUrl,
                address: this.props.newReportLocation.address,
                addressAsStreet: this.props.newReportLocation.addressAsStreet,
                lat: this.props.newReportLocation.coords.lat,
                lng: this.props.newReportLocation.coords.lng
            }
        };
    }

    submitReportData(data) {
        axios({
            method: 'post',
            responseType: 'json',
            url: `${BASE_URL}/report/register`,
            data: data
        })
        .then(response => {
            this.setState({ submitting: false, successMsg: 'Sucesso!' });
            this.props.dispatch(addReport(response.data));
        })
        .catch(error => {
            console.log('Failed to submit report...', {...error});
            this.setState({ submitting: false, errorMsg: error.response.data.output });
        });
    }

    /*
        
        Faz dismiss do alert de erro em caso de insucesso.
    */
    resetStateOrRedirect() {
        if (this.state.successMsg) {
            this.onCancelClick();
        }
        else
            this.setState({ successMsg: '', errorMsg: '' });
    }

    getSuggestions(reportType) {
        const { lat, lng } = this.props.newReportLocation.coords;
        const data = {
            token: this.props.token.token,
            type: reportType,
            lat,
            lng
        };

        axios.post(`${BASE_URL}/report/suggestions`, data)
            .then(response => {
                const suggestions = response.data || [];
                console.log('SUGGESTIONS:', suggestions);
                this.setState({ suggestions });
                this.props.dispatch(compareSuggestions(suggestions));
            })
            .catch(error => {
                console.log('Failed to get suggestions...', error);
            });
    }
}

function mapStateToProps(store) {
    return {
        token: store.token,
        newReportLocation: store.newReportLocation,
        newReportTypes: store.newReportTypes
    };
}

export default connect(mapStateToProps)(ReportPage);