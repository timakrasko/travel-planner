import {
    createTravelPlan,
    getTravelPlan,
    updateTravelPlan,
    deleteTravelPlan,
    thinkTime,
} from '../utils/api-client.js';
import {
    generateTravelPlan,
    generateTravelPlanUpdate,
} from '../utils/data-generator.js';

export const options = {
    stages: [
        { duration: '2m', target: 300 },
        { duration: '2m', target: 600 },
        { duration: '2m', target: 900 },
        { duration: '5m', target: 0 },
    ],

    thresholds: {
        'http_req_failed': ['rate<0.10'],
        'http_req_duration': ['p(95)<5000'],
    },
};

export default function () {
    const newPlan = generateTravelPlan();
    const createdPlan = createTravelPlan(newPlan);

    if (!createdPlan) return;

    const planId = createdPlan.id;
    const initialVersion = createdPlan.version;

    thinkTime(0.2, 0.5);

    getTravelPlan(planId);

    const updateData = generateTravelPlanUpdate(initialVersion);
    updateTravelPlan(planId, updateData);

    deleteTravelPlan(planId);
}