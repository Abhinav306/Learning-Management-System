'use client';

import React, { useState, useEffect, useRef } from 'react';
import Link from 'next/link';
import { Bell } from 'lucide-react';
import { useWebSocket } from '@/context/WebSocketContext';
import styles from './NotificationBell.module.css';

const NotificationBell = () => {
  const { notifications, unreadCount, markAsRead, markAllAsRead } = useWebSocket();
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleToggle = () => {
    setIsOpen(!isOpen);
  };

  const handleItemClick = (id, read) => {
    if (!read) {
      markAsRead(id);
    }
    setIsOpen(false);
  };

  return (
    <div className={styles.container} ref={dropdownRef}>
      <button className={styles.bellButton} onClick={handleToggle} aria-label="Notifications">
        <Bell size={20} />
        {unreadCount > 0 && (
          <span className={styles.badge}>{unreadCount > 9 ? '9+' : unreadCount}</span>
        )}
      </button>

      {isOpen && (
        <div className={styles.dropdown}>
          <div className={styles.header}>
            <h3>Notifications</h3>
            {unreadCount > 0 && (
              <span className={styles.markAllBtn} onClick={markAllAsRead}>
                Mark all as read
              </span>
            )}
          </div>

          <div className={styles.list}>
            {notifications.length === 0 ? (
              <div className={styles.empty}>No notifications yet</div>
            ) : (
              notifications.map((item) => (
                <div
                  key={item.id}
                  className={`${styles.item} ${!item.read ? styles.unread : ''}`}
                  onClick={() => handleItemClick(item.id, item.read)}
                >
                  <span className={styles.itemTitle}>{item.title}</span>
                  <p className={styles.itemMessage}>{item.message}</p>
                  <span className={styles.itemTime}>
                    {new Date(item.createdAt).toLocaleTimeString([], {
                      hour: '2-digit',
                      minute: '2-digit',
                    })}
                  </span>
                </div>
              ))
            )}
          </div>

          <div className={styles.footer}>
            <Link href="/notifications" className={styles.viewAllLink} onClick={() => setIsOpen(false)}>
              View all history
            </Link>
          </div>
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
