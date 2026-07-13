import api from './api';

export const assignmentService = {
  async getCourseAssignments(courseId) {
    const response = await api.get(`/courses/${courseId}/assignments`);
    return response.data;
  },

  async getAssignmentDetail(assignmentId) {
    const response = await api.get(`/assignments/${assignmentId}`);
    return response.data;
  },

  // Helper to upload attachment file using multipart/form-data
  async uploadAttachment(file) {
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await api.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  // Submit assignment request content & fileUrl
  async submitAssignment(assignmentId, submissionData) {
    const response = await api.post(`/assignments/${assignmentId}/submissions`, submissionData);
    return response.data;
  },

  async getMySubmission(assignmentId) {
    const response = await api.get(`/assignments/${assignmentId}/my-submission`);
    return response.data;
  }
};

export default assignmentService;
