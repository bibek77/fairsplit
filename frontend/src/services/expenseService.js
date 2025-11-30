import apiClient from './api';

const expenseService = {
    // Add a new expense to a group
    add: async (groupId, expenseData) => {
        const response = await apiClient.post(`/groups/${groupId}/expenses`, expenseData);
        return response.data;
    },

    // Get all expenses for a group
    getByGroup: async (groupId) => {
        const response = await apiClient.get(`/groups/${groupId}/expenses`);
        return response.data;
    },
};

export default expenseService;
