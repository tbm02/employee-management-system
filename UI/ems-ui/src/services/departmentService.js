import api from './api';

export const getAll = async () => {
    const response = await api.get('/departments');
    return response.data;
};

export const getById = async (id) => {
    const response = await api.get(`/departments/${id}`);
    return response.data;
};

export const create = async (deptData) => {
    const response = await api.post('/departments', deptData);
    return response.data;
};

export const update = async (id, deptData) => {
    const response = await api.put(`/departments/${id}`, deptData);
    return response.data;
};

export const remove = async (id) => {
    const response = await api.delete(`/departments/${id}`);
    return response.data;
};
