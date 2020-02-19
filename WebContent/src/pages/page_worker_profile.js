import React, { Component } from 'react';
import axios from 'axios';
import BASE_URL from '../utils/config';

// Components 
import WorkingAreaList from '../components/working_area_list';

class WorkerProfilePage extends Component {
    constructor(props) {
        super(props);
        this.state = {
            workerInfo: null,
            workingArea: null,
            districts: null,
            selectedWorkArea: null,
            selectedCounties: null,
            selectedCounty: null,
            output: null
        }
    }

    fetchDistricts() {
        axios.get(`${BASE_URL}/operation/address`)
            .then(response => {
                if (response.status === 200) {
                    console.log("counties", response.data);
                    this.setState({ districts: response.data })
                }
            })
            .catch(err => {
                console.log("An error occured while trying to load counties.", err);
            })
    }

    getWorkerDetails() {
        const token = JSON.parse(localStorage.getItem('token'));
        axios.post(`${BASE_URL}/backoffice/report/details`, token)
            .then(response => {
                if (response.status === 200) {
                    console.log("RESPONSE", response);
                    this.setState({ workerInfo: response.data.workerInfo, workingArea: response.data.workingArea });
                    $('#additionalInfoTextarea').val(this.state.workerInfo);

                    console.log("on componentWillMount from server: ", response.data);
                }
            })
            .catch(err => {
                console.log("An error has occured while trying to load this worker details.", err);
            })
    }

    componentWillMount() {
        this.getWorkerDetails();
        this.fetchDistricts();
    }

    handleSelectDistrict(event) {
        const newDistrict = event.currentTarget.value;
        if (newDistrict !== "Nenhum distrito seleccionado") {
            const counties = this.state.districts[newDistrict];
            this.setState({ selectedCounties: counties });
        }
        else {
            this.setState({ selectedCounties: null });
        }
    }

    handleSelectCounty(event) {
        const newCounty = event.currentTarget.value;
        if (newCounty !== "Nenhum concelho seleccionado") {
            console.log("selectedCounty", newCounty);
            this.setState({ selectedCounty: newCounty });
        }
        else {
            this.setState({ selectedCounty: null });
        }
    }

    canAddWorkingArea(county) {
        console.log("county parameter", county);
        console.log("array of areas", this.state.workingArea);

        if (this.state.workingArea.indexOf(county) === -1) {
            return true;
        }
        return false;
    }

    handleAddCounty() {
        const { alert } = this.refs;
        console.log("handleAddCounty", this.state.workingArea);

        if (this.state.selectedCounty && this.state.selectedCounty !== "Nenhum concelho seleccionado") {
            if (this.canAddWorkingArea(this.state.selectedCounty)) {
                console.log("handle AddC County", this.state.workingArea);

                const tmp = this.state.workingArea;
                tmp.push(this.state.selectedCounty);
                this.setState({ workingArea: tmp });
            }
            else {
                alert.classList.remove('d-none');
                alert.classList.add('text-danger');
                alert.innerHTML = 'Área de actuação já seleccionada.';
                setTimeout(() => {
                    alert.classList.add('d-none');
                    alert.classList.remove('alert-danger');
                    alert.innerHTML = '';
                }, 3000);
            }
        }
        else {
            alert.classList.remove('d-none');
            alert.classList.add('text-danger');
            alert.innerHTML = 'Nenhum concelho foi seleccionado.';
            setTimeout(() => {
                alert.classList.add('d-none');
                alert.classList.remove('alert-danger');
                alert.innerHTML = '';
            }, 3000);
        }
    }

    handleRemoveCounty(county) {
        let index = this.state.workingArea.findIndex(item => item === county);
        const tmp = this.state.workingArea;
        tmp.splice(index, 1);
        this.setState({ workingArea: tmp });
    }

    deleteOutputs() {
        this.setState({ output: null });
    }

    addOutputs(outputArray) {
        this.setState({ output: outputArray });
    }

    handleSubmitInfo() {
        const { alert } = this.refs;
        const token = JSON.parse(localStorage.getItem('token'));
        const info = $('#additionalInfoTextarea').val();
        const data = {
            workerInfo: info,
            workingArea: this.state.workingArea
        }
        axios.post(`${BASE_URL}/backoffice/report/updateDetails`, { token, data })
            .then(response => {
                if (response.status === 200) {
                    let outputArray = [];
                    const addedCountiesJson = response.data.addedCounties;
                    const removedCountiesJson = response.data.removedCounties;
                    if (addedCountiesJson.length > 0) {
                        // build added counties
                        const addedCounties = addedCountiesJson.join(", ");
                        var string = `O(s) concelho(s) ${addedCounties} foram adicionados com sucesso às suas áreas de actuação.`;
                        const obj = {
                            output: string,
                            color: "green"
                        }
                        outputArray.push(obj);
                    }

                    const removedCounties = removedCountiesJson;
                    if (removedCounties !== {}) {
                        // build removed counties
                        $.each(removedCounties, function (key, value) {
                            if (value) {
                                var string = `O concelho ${key} foi removido com sucesso das suas áreas de actuação.`;
                                const obj = {
                                    output: string,
                                    color: "green"
                                }
                                outputArray.push(obj);
                            }
                            else {
                                var string = `O concelho ${key} não foi removido: existem ocorrências neste concelho a serem resolvidas.`;
                                const obj = {
                                    output: string,
                                    color: "red"
                                }
                                outputArray.push(obj);
                            }
                        });
                    }
                    this.setState({ output: outputArray });
                    alert.classList.remove('d-none');
                    alert.classList.add('text-success');
                    alert.innerHTML = 'O seu perfil foi actualizado com sucesso.';
                    setTimeout(() => {
                        alert.classList.add('d-none');
                        alert.classList.remove('text-success');
                        alert.innerHTML = '';
                    }, 3000);
                }
            })
            .catch(err => {
                console.log("An error occured while trying to submit worker information.", err);
            })
    }


    renderCountyList() {
        let counties;
        if (this.state.selectedCounties) {
            counties = this.state.selectedCounties.map(item => {
                return (<option key={item}> {item} </option>)
            });
            return (
                <div className="add-county-section">
                    <h6 className="mt-2"> Seleccionar concelho: </h6>
                    <select onChange={this.handleSelectCounty.bind(this)} id="selected-county" className="form-control w-75">
                        <option> Nenhum concelho seleccionado </option>
                        {counties}
                    </select>
                    <i onClick={this.handleAddCounty.bind(this)} className="fa fa-plus edit-button" aria-hidden="true"></i>
                    <div className="submit-error" ref="alert"></div>
                </div>
            );
        }
    }


    render() {
        let arr = [];
        $.each(this.state.districts, function (key, value) {
            arr.push(key);
        });

        let districts;
        districts = arr.map(item => {
            return <option key={item}> {item} </option>
        });

        let logSection;
        let outputs;
        if (this.state.output) {
            outputs = this.state.output.map(item => {
                if (item.color === "green") {
                    return (
                        <li style={{ color: 'green' }} id="outputItem" key={item.output}>{item.output}</li>
                    );
                }
                else if (item.color === "red") {
                    return (
                        <li style={{ color: 'red' }} id="outputItem" key={item.output}>{item.output}</li>
                    );
                }
            });
            if (outputs.length > 0) {
                logSection = (
                    <div className="logSection">
                        <i className="fa fa-times logSectionExitBttn" onClick={this.deleteOutputs.bind(this)} ></i>
                        {outputs}
                    </div>
                );
            }
        }

        console.log("arr", districts);
        return (
            <div className="container h-100 worker-profile-container">
                <div className="row justify-content">
                    <div className="col-md-6 data-form">
                        <div className="profile-info-block">
                            <h5> As minhas áreas de actuação: </h5>
                            <WorkingAreaList workingArea={this.state.workingArea} onDelete={this.handleRemoveCounty.bind(this)} />
                        </div>

                        <div className="profile-info-block">
                            <h5 className="mt-5"> Adicionar nova área de actuação:</h5>

                            <h6> Seleccionar distrito: </h6>
                            <select onChange={this.handleSelectDistrict.bind(this)} className="form-control w-75">
                                <option> Nenhum distrito seleccionado </option>
                                {districts}
                            </select>

                            {this.renderCountyList()}

                        </div>
                    </div>
                    <div className="col-md-6 data-form">
                        <div className="profile-info-block">
                            <h5> Informação geral de perfil: </h5>
                            <textarea
                                maxlength="20"
                                className="form-control input-sm mt-2"
                                placeholder="Informação sobre si, serviços que fornece, etc..."
                                id="additionalInfoTextarea">
                            </textarea>
                        </div>
                    </div>

                    <div className="handleSubmitSection">
                        <button onClick={this.handleSubmitInfo.bind(this)} className="btn btn-primary"> Guardar </button>
                        <div ref="alert"></div>

                    </div>

                    {logSection}

                </div>

            </div>
        );
    }

}

export default WorkerProfilePage;