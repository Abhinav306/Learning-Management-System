'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { notificationService } from '@/services/notificationService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { 
  Bell, 
  GraduationCap, 
  BookOpen, 
  Award, 
  Settings, 
  Info,
  Check
} from 'lucide-react';
import styles from './page.module.css';

const NotificationsHistoryPage = () => {
  const { unreadCount, markAsRead, markAllAsRead } = useWebSocket();
  const [notifications, setNotifications] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 8,
    totalPages: 1,
    totalElements: 0,
  });

  const fetchNotifications = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await notificationService.getNotifications(pagination.page, pagination.size);
      if (res.success && res.data) {
        setNotifications(res.data.content || []);
        setPagination((prev) => ({
          ...prev,
          totalPages: res.data.totalPages || 1,
          totalElements: res.data.totalElements || 0,
        }));
      }
    } catch (e) {
      console.error('Failed to load notifications history:', e);
    } finally {
      setIsLoading(false);
    }
  }, [pagination.page, pagination.size]);

  useEffect(() => {
    fetchNotifications();
  }, [fetchNotifications]);

  const handleItemClick = async (item) => {
    if (!item.read) {
      // Mark read on backend and update local list state
      await markAsRead(item.id);
      setNotifications((prev) => 
        prev.map((n) => n.id === item.id ? { ...n, read: true } : n)
      );
    }
  };

  const handleMarkAllRead = async () => {
    await markAllAsRead();
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination((prev) => ({ ...prev, page: newPage }));
    }
  };

  // Helper to map notification type to visual icons
  const getIcon = (type) => {
    switch (type) {
      case 'ENROLLMENT': return <BookOpen size={16} style={{ color: 'var(--accent-cyan)' }} />;
      case 'GRADE': return <Award size={16} style={{ color: 'var(--accent-green)' }} />;
      case 'COURSE_UPDATE': return <GraduationCap size={16} style={{ color: 'var(--accent-purple)' }} />;
      case 'ASSIGNMENT': return <FileText size={16} style={{ color: 'var(--accent-pink)' }} />;
      case 'QUIZ': return <Target size={16} style={{ color: 'var(--accent-amber)' }} />;
      default: return <Info size={16} style={{ color: 'var(--text-muted)' }} />;
    }
  };

  return (
    <div className={styles.container}>
      <section className={styles.header}>
        <div className={styles.headerText}>
          <h1>Notifications History</h1>
          <p>Review and manage all real-time system alerts and learning milestone announcements.</p>
        </div>
        {unreadCount > 0 && (
          <Button variant="secondary" className={styles.markAllBtn} onClick={handleMarkAllRead}>
            <Check size={14} style={{ marginRight: '6px' }} />
            Mark all as read
          </Button>
        )}
      </section>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
          <Spinner />
        </div>
      ) : notifications.length === 0 ? (
        <div className={styles.emptyState}>
          <Bell size={32} style={{ color: 'var(--text-muted)' }} />
          <h3 style={{ marginTop: 'var(--space-sm)' }}>No Notifications Found</h3>
          <p style={{ color: 'var(--text-secondary)' }}>You don't have any system notifications logged.</p>
        </div>
      ) : (
        <>
          <div className={styles.list}>
            {notifications.map((item) => (
              <GlassCard 
                key={item.id} 
                className={`${styles.item} ${!item.read ? styles.unreadItem : ''}`}
                onClick={() => handleItemClick(item)}
              >
                {!item.read && <span className={styles.indicator}></span>}
                <div className={styles.iconWrapper}>{getIcon(item.type)}</div>
                
                <div className={styles.itemMeta}>
                  <span className={styles.itemTitle}>{item.title}</span>
                  <p className={styles.itemMsg}>{item.message}</p>
                  <span className={styles.itemTime}>
                    {new Date(item.createdAt).toLocaleDateString()} at{' '}
                    {new Date(item.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>
              </GlassCard>
            ))}
          </div>

          {/* Pagination */}
          {pagination.totalPages > 1 && (
            <div className={styles.pagination}>
              <Button 
                variant="secondary" 
                disabled={pagination.page === 0}
                onClick={() => handlePageChange(pagination.page - 1)}
              >
                Previous
              </Button>
              <span className={styles.pageInfo}>
                Page {pagination.page + 1} of {pagination.totalPages}
              </span>
              <Button 
                variant="secondary" 
                disabled={pagination.page === pagination.totalPages - 1}
                onClick={() => handlePageChange(pagination.page + 1)}
              >
                Next
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default NotificationsHistoryPage;
