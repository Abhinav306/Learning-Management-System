import api from './api';

export const ragService = {
  async uploadDocument(file, courseId = null) {
    const formData = new FormData();
    formData.append('file', file);
    if (courseId) {
      formData.append('courseId', courseId);
    }
    const response = await api.post('/ai/documents/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    return response.data;
  },

  async listDocuments(courseId = null) {
    const params = courseId ? { courseId } : {};
    const response = await api.get('/ai/documents', { params });
    return response.data;
  },

  async deleteDocument(id) {
    const response = await api.delete(`/ai/documents/${id}`);
    return response.data;
  },

  async queryRag(query, courseId = null) {
    const response = await api.post('/ai/rag/query', {
      query,
      courseId
    });
    return response.data;
  }
};

export default ragService;
