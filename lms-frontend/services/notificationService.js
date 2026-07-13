import api from './api';

export const notificationService = {
  async getNotifications(page = 0, size = 10) {
    const response = await api.get(`/notifications`, {
      params: { page, size }
    });
    return response.data;
  },

  async getUnreadCount() {
    const response = await api.get(`/notifications/unread-count`);
    return response.data;
  },

  async markAsRead(id) {
    const response = await api.put(`/notifications/${id}/read`);
    return response.data;
  },

  async markAllAsRead() {
    const response = await api.put(`/notifications/read-all`);
    return response.data;
  }
};
export default notificationService;
