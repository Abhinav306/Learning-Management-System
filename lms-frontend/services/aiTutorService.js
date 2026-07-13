import api from './api';

export const aiTutorService = {
  async createSession(courseId = null, title = 'New Study Session') {
    const response = await api.post('/ai/tutor/sessions', {
      courseId,
      title
    });
    return response.data;
  },

  async getUserSessions(courseId = null) {
    const params = courseId ? { courseId } : {};
    const response = await api.get('/ai/tutor/sessions', { params });
    return response.data;
  },

  async getSessionMessages(sessionId) {
    const response = await api.get(`/ai/tutor/sessions/${sessionId}/messages`);
    return response.data;
  },

  async deleteSession(sessionId) {
    await api.delete(`/ai/tutor/sessions/${sessionId}`);
  }
};

export default aiTutorService;
