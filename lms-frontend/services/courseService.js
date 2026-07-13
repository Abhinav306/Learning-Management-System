import api from './api';

export const courseService = {
  async getCategories() {
    const response = await api.get('/categories');
    return response.data;
  },

  async getCourses(filters = {}) {
    const { keyword, categoryId, difficulty, status, instructorId, page = 0, size = 10 } = filters;
    const params = { page, size };
    
    if (keyword) params.keyword = keyword;
    if (categoryId) params.categoryId = categoryId;
    if (difficulty) params.difficulty = difficulty;
    if (status) params.status = status;
    if (instructorId) params.instructorId = instructorId;

    const response = await api.get('/courses/search', { params });
    return response.data;
  },

  async getCourseById(id) {
    const response = await api.get(`/courses/${id}`);
    return response.data;
  },

  async getCourseSections(courseId) {
    const response = await api.get(`/courses/${courseId}/sections`);
    return response.data;
  },

  // Instructor/Admin operations
  async createCourse(request) {
    const response = await api.post('/courses', request);
    return response.data;
  },

  async updateCourse(id, request) {
    const response = await api.put(`/courses/${id}`, request);
    return response.data;
  },

  async deleteCourse(id) {
    const response = await api.delete(`/courses/${id}`);
    return response.data;
  },

  // Section operations
  async createSection(courseId, request) {
    const response = await api.post(`/courses/${courseId}/sections`, request);
    return response.data;
  },

  async updateSection(courseId, sectionId, request) {
    const response = await api.put(`/courses/${courseId}/sections/${sectionId}`, request);
    return response.data;
  },

  async deleteSection(courseId, sectionId) {
    const response = await api.delete(`/courses/${courseId}/sections/${sectionId}`);
    return response.data;
  },

  // Lesson operations
  async createLesson(courseId, sectionId, request) {
    const response = await api.post(`/courses/${courseId}/sections/${sectionId}/lessons`, request);
    return response.data;
  },

  async updateLesson(courseId, sectionId, lessonId, request) {
    const response = await api.put(`/courses/${courseId}/sections/${sectionId}/lessons/${lessonId}`, request);
    return response.data;
  },

  async deleteLesson(courseId, sectionId, lessonId) {
    const response = await api.delete(`/courses/${courseId}/sections/${sectionId}/lessons/${lessonId}`);
    return response.data;
  }
};

export default courseService;
