import api from './api';

export const lessonProgressService = {
  async markComplete(enrollmentId, lessonId) {
    const response = await api.post(`/enrollments/${enrollmentId}/lessons/${lessonId}/complete`);
    return response.data;
  },

  async markIncomplete(enrollmentId, lessonId) {
    const response = await api.delete(`/enrollments/${enrollmentId}/lessons/${lessonId}/complete`);
    return response.data;
  },

  async getProgress(enrollmentId) {
    const response = await api.get(`/enrollments/${enrollmentId}/progress`);
    return response.data;
  }
};

export default lessonProgressService;
