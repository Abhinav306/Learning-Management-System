import api from './api';

export const reviewService = {
  async getCourseReviews(courseId, page = 0, size = 10) {
    const response = await api.get(`/courses/${courseId}/reviews`, {
      params: { page, size }
    });
    return response.data;
  },

  async createReview(courseId, reviewData) {
    const response = await api.post(`/courses/${courseId}/reviews`, reviewData);
    return response.data;
  },

  async updateReview(courseId, reviewId, reviewData) {
    const response = await api.put(`/courses/${courseId}/reviews/${reviewId}`, reviewData);
    return response.data;
  },

  async deleteReview(courseId, reviewId) {
    await api.delete(`/courses/${courseId}/reviews/${reviewId}`);
  }
};

export default reviewService;
