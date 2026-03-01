import api from './api';

export const getAll = async () => {
    const response = await api.get('/users');
    return response.data;
};

export const getById = async (id) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
};

export const create = async (userData) => {
    const response = await api.post('/users', userData);
    return response.data;
};

export const update = async (id, userData) => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
};

export const remove = async (id) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
};

export const savePersonalDetails = async (userId, details) => {
    const response = await api.post(`/users/${userId}/details`, details);
    return response.data;
};

export const generateEmpId = async () => {
    const response = await api.get('/users/generate-emp-id');
    return response.data;
};

export const checkEmpId = async (empId) => {
    const response = await api.get('/users/check-emp-id', { params: { empId } });
    return response.data; // data.data = true if taken
};

export const checkEmail = async (email) => {
    const response = await api.get('/users/check-email', { params: { email } });
    return response.data; // data.data = true if taken
};
