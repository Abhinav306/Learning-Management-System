import api from './api';

export const recommendationService = {
  async getPersonalizedRecommendations() {
    const response = await api.get('/ai/recommendations');
    return response.data;
  },

  async getPopularCourses() {
    const response = await api.get('/ai/recommendations/popular');
    return response.data;
  }
};

export default recommendationService;
