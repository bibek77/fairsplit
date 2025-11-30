import apiClient from './api';

const settlementService = {
    // Get settlements for a group
    getSettlements: async (groupId) => {
        const response = await apiClient.get(`/groups/${groupId}/settlements`);
        return response.data;
    },
};

export default settlementService;
