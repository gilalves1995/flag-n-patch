export const SEARCH_LOCATION = 'SEARCH_LOCATION';

export function searchLocation(coords) {
    return {
        type: SEARCH_LOCATION,
        payload: coords
    };
}