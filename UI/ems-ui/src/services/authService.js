import api from './api';

export const login = async (identifier, password) => {
    const response = await api.post('/auth/login', { identifier, password });
    return response.data; // ApiResponseDto<LoginResponseDto>
};

export const changePassword = async (userId, newPassword) => {
    const response = await api.patch(`/users/${userId}/password`, { newPassword });
    return response.data;
};
