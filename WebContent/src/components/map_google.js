// 3rd Party
import React, { Component } from 'react';

// Internal Modules
import { innerCoords, outerCoords, moita, cascais } from '../utils/geojson';


/*

*/
class GoogleMap extends Component {
    /*
        Garante que só existe uma execução de render()
        para este componente.
    */
    shouldComponentUpdate() {
        return false;
    }

    /*
        Começa por instânciar o Mapa e ligá-lo à div previamente
        rendered.
    */
    componentDidMount() {
        // Instanciate the map and put it in the DOM.
        this.map = new google.maps.Map(this.refs.map, {
            scrollwheel: true,
            disableDefaultUI: true,
            zoomControl: true,
            zoomControlOptions: {
                position: google.maps.ControlPosition.LEFT_BOTTOM
            },
            disableDoubleClickZoom: true,
            zoom: 13,
            minZoom: 7,
            center: {
                lat: this.props.initialLat,
                lng: this.props.initialLng
            }
        });

        // Adiciona máscara e delimitações ao mapa
        this.map.data.add({geometry: new google.maps.Data.Polygon([outerCoords, innerCoords, cascais, moita])});
	    this.map.data.setStyle({strokeWeight: 1, strokeOpacity: 0});

        /*
            Se o utilizador der permissão para aceder à sua localização,
            centra na localização do mesmo.
        */
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(position => {
                const pos = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };
                this.map.setCenter(pos);
            });
        }

        // Current state of marker's visibility (to compare with next)
        this.hidden = false;

        // Previous searched location
        this.searchLocation = null;

        this.filters = this.props.reports.stateFilters;

        // Array de marcadores no mapa
        this.markers = this.props.reports.reports ? this.buildMarkersArray(this.props.reports.reports) : [];
        if (!this.hidden)
                this.showMarkers();

        /*
            Marcador que aparece no centro do mapa quando se vai adicionar
            um report.
        */
        this.addingMarker = new google.maps.Marker({
            position: this.map.getCenter()
        });
        google.maps.event.addListener(this.addingMarker, 'click', (event) => {
            const coords = this.getEventCoords(event);
            this.props.onLocationSelected(coords);
            this.stopInteraction();
        });

        // Regarding drag boudaries
        this.lastValidCenter = this.map.getCenter();
        if (this.props.reports.reports) {
            console.log('Setting boudaries... In Did Mount....');
            this.createDragBound();
        }
    }

    /*
        Onde toda a interação de outros componentes com o mapa
        acontece.
    */
    componentWillReceiveProps({ reports: { reports, fetching, hidden, stateFilters }, searchLocation }) {
        if (searchLocation && this.searchLocation !== searchLocation) {
            this.searchLocation = searchLocation;
            this.map.panTo(searchLocation);
        }

        if (this.hidden !== hidden) {
            this.hidden = hidden;
            this.toggleMarkers();
        }

        if (reports && !Object.is(this.reports, reports)) {
            //
            if (!this.lastValidCenter || !this.allowedBounds) {
                console.log('Setting boudaries...');
                this.createDragBound();
            }

            this.reports = reports;
            this.markers = this.buildMarkersArray(reports);
            if (!hidden)
                this.showMarkers();
        }

        if (this.hidden) {
            this.startInteraction();
        } else {
            this.stopInteraction();
        }

        if (this.filters.length !== stateFilters.length) {
            this.filters = stateFilters;
            this.applyFilters();
        }
    }

    /*
        Faz render apenas de uma div onde futuramente iremos instanciar o Mapa.
        Render só executa uma única vez (devido ao método shouldComponentUpdate).
    */
    render() {
        return <div id="map" ref="map" />;
    }

    /*
        Extrai os markers de cada propriedade de reports e constrói um array
        de markers.
    */
    buildMarkersArray(reports) {
        if (this.markers)
            this.hideMarkers();

        const arr = [];
        for (let id in reports) {
            let report = reports[id];
            let marker = report.marker;
            marker.reportId = id;
            marker.addListener('click', () => {
                console.log('ReportID:', marker.reportId);
                this.props.onMarkerClick(marker.reportId);
            });
            arr.push(marker);
        }
        return arr;
    }

    /*
        Dependendo do estado de visibilidade (mais recente), moostra
        ou esconde os marcadores no mapa.
    */
    toggleMarkers() {
        if (!this.hidden) {
            this.showMarkers();
        } else {
            this.hideMarkers();
        }
    }

    // Torna todos os marcadores visíveis no mapa
    showMarkers() {
        for (let marker of this.markers)
            marker.setMap(this.map);
    }

    // Esconde todos os marcadores no mapa
    hideMarkers() {
        for (let marker of this.markers)
            marker.setMap(null);
    }

    getEventCoords(event) {
        const { lat, lng } = event.latLng;
        this.map.panTo(event.latLng);
        return { lat: lat(), lng: lng() };
    }

    toggleAddReportListeners() {
        // Make the marker follow the mouse
        google.maps.event.addListener(this.map, 'mousemove', (event) => {
            this.addingMarker.setPosition(event.latLng);
        });

        // Add the marker's click listener to the map aswell
        google.maps.event.addListener(this.map, 'click', (event) => {
            const coords = this.getEventCoords(event);
            this.props.onLocationSelected(coords);
            this.stopInteraction();
        });
    }

    startInteraction() {
        this.addingMarker.setPosition(this.map.getCenter());
        this.addingMarker.setMap(this.map);
        this.toggleAddReportListeners();
    }

    stopInteraction() {
        this.addingMarker.setMap(null);
        google.maps.event.clearInstanceListeners(this.map);
    }

    applyFilters() {
        console.log('Inside apply filters!');
        for (let marker of this.markers) {
            const status = this.reports[marker.reportId].report.statusDescription;
            if (this.filters.includes(status))
                marker.setMap(this.map);
            else
                marker.setMap(null);
        }
    }

    createDragBound() {
        //this.lastValidCenter = this.map.getCenter();
        this.allowedBounds = new google.maps.LatLngBounds(
            new google.maps.LatLng(32, -32), 
            new google.maps.LatLng(43, -6)
        );
        setTimeout(() => {
            console.log('BAM!', google.maps.event.addListener);
            google.maps.event.addListener(this.map, 'center_changed', () => {
                if (this.allowedBounds.contains(this.map.getCenter())) {
                    // still within valid bounds, so save the last valid position
                    this.lastValidCenter = this.map.getCenter();
                    return;
                }
                // not valid anymore => return to last valid position
                this.map.panTo(this.lastValidCenter);
            });
        }, 2500);
    }
}

export default GoogleMap;