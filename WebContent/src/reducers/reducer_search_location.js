import { SEARCH_LOCATION } from '../actions/actions_search_location';


export default function(state = null, action) {
    switch (action.type) {
        case SEARCH_LOCATION: {
            return action.payload;
        }
    }
    return state;
}