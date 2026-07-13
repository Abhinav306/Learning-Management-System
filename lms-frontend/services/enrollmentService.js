import api from './api';

export const enrollmentService = {
  async enrollInCourse(courseId) {
    const response = await api.post(`/courses/${courseId}/enroll`);
    return response.data;
  },

  async dropCourse(courseId) {
    const response = await api.delete(`/courses/${courseId}/enroll`);
    return response.data;
  },

  async getMyEnrollments(page = 0, size = 100) {
    const response = await api.get(`/enrollments`, {
      params: { page, size }
    });
    return response.data;
  },

  // Client-side helper to check if student is enrolled in a specific course
  async checkEnrollmentStatus(courseId) {
    try {
      const res = await this.getMyEnrollments(0, 100);
      if (res.success && res.data && res.data.content) {
        const enrollment = res.data.content.find(
          (e) => e.courseId === courseId && (e.status === 'ACTIVE' || e.status === 'COMPLETED')
        );
        return {
          enrolled: !!enrollment,
          enrollment: enrollment || null
        };
      }
      return { enrolled: false, enrollment: null };
    } catch (e) {
      console.error('Failed to check enrollment status:', e);
      return { enrolled: false, enrollment: null };
    }
  }
};

export default enrollmentService;
