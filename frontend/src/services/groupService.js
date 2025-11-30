import apiClient from './api';

const groupService = {
    // Create a new group
    create: async (groupData) => {
        const response = await apiClient.post('/groups', groupData);
        return response.data;
    },

    // Get all groups
    getAll: async () => {
        const response = await apiClient.get('/groups');
        return response.data;
    },

    // Get group by ID
    getById: async (groupId) => {
        const response = await apiClient.get(`/groups/${groupId}`);
        return response.data;
    },

    // Delete a group
    delete: async (groupId) => {
        const response = await apiClient.delete(`/groups/${groupId}`);
        return response.data;
    },
};

export default groupService;
