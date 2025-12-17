import { DEFAULT_THRESHOLDS } from '../config/endpoints.js';
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
        { duration: '5m', target: 100 },
        { duration: '10m', target: 100 },
        { duration: '5m', target: 0 },
    ],

    thresholds: {
        ...DEFAULT_THRESHOLDS,
        'http_req_duration': ['p(95)<1000'],
        'http_req_failed': ['rate<0.01'],
    },
};

export default function () {
    const newPlan = generateTravelPlan();
    const createdPlan = createTravelPlan(newPlan);

    if (!createdPlan) {
        return;
    }
    const planId = createdPlan.id;
    const initialVersion = createdPlan.version;

    thinkTime(1, 3);

    getTravelPlan(planId);
    thinkTime(1, 3);

    const updateData = generateTravelPlanUpdate(initialVersion);
    updateTravelPlan(planId, updateData);

    thinkTime(1, 3);

    deleteTravelPlan(planId);
}