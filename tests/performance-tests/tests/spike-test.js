import { sleep } from 'k6';
import {
    createTravelPlan,
    getTravelPlan,
    deleteTravelPlan,
} from '../utils/api-client.js';
import {
    generateTravelPlan,
} from '../utils/data-generator.js';

export const options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '1m', target: 2000 },
        { duration: '1m', target: 200 },
        { duration: '5m', target: 100 },
    ],

    thresholds: {
        'http_req_failed': ['rate<0.25'],
        'http_req_duration': ['p(95)<10000'],
    },
};

export default function () {
    const newPlan = generateTravelPlan();
    const createdPlan = createTravelPlan(newPlan);

    if (createdPlan) {
        getTravelPlan(createdPlan.id);
        deleteTravelPlan(createdPlan.id);
    }

    sleep(1);
}