import api from './api';

export const quizService = {
  async getCourseQuizzes(courseId) {
    const response = await api.get(`/courses/${courseId}/quizzes`);
    return response.data;
  },

  async getQuizDetail(quizId) {
    const response = await api.get(`/quizzes/${quizId}`);
    return response.data;
  },

  async startAttempt(quizId) {
    const response = await api.post(`/quizzes/${quizId}/attempts`);
    return response.data;
  },

  async submitAttempt(quizId, attemptId, submissionData) {
    // submissionData format: { answers: { [questionId]: selectedAnswerString } }
    // Backend QuizSubmissionRequest usually expects: { answers: Map<UUID, String> }
    const response = await api.post(`/quizzes/${quizId}/attempts/${attemptId}/submit`, submissionData);
    return response.data;
  },

  async getQuizAttempts(quizId) {
    const response = await api.get(`/quizzes/${quizId}/attempts`);
    return response.data;
  }
};

export default quizService;
