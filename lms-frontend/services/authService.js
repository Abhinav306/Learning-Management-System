import api from './api';

export const authService = {
  async login(request) {
    const response = await api.post('/auth/login', request);
    return response.data;
  },

  async signup(request) {
    const response = await api.post('/auth/signup', request);
    return response.data;
  },

  async logout(refreshToken) {
    await api.post('/auth/logout', { refreshToken });
  },

  async refresh(refreshToken) {
    const response = await api.post('/auth/refresh', { refreshToken });
    return response.data;
  },

  async getProfile(userId) {
    const response = await api.get(`/users/${userId}`);
    return response.data;
  },

  async updateProfile(userId, request) {
    const response = await api.put(`/users/${userId}`, request);
    return response.data;
  }
};
