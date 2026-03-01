import api from './api';

export const getAll = async () => {
    const response = await api.get('/goals');
    return response.data;
};

export const getById = async (id) => {
    const response = await api.get(`/goals/${id}`);
    return response.data;
};

export const create = async (goalData) => {
    const response = await api.post('/goals', goalData);
    return response.data;
};

export const update = async (id, goalData) => {
    const response = await api.put(`/goals/${id}`, goalData);
    return response.data;
};

export const markCompleted = async (id) => {
    const response = await api.patch(`/goals/${id}/complete`);
    return response.data;
};

export const remove = async (id) => {
    const response = await api.delete(`/goals/${id}`);
    return response.data;
};

export const getAssignableUsers = async () => {
    const response = await api.get('/goals/assignable-users');
    return response.data;
};
