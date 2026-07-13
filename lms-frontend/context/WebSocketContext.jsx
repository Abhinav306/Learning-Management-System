'use client';

import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { useAuth } from './AuthContext';
import { notificationService } from '@/services/notificationService';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const WebSocketContext = createContext(undefined);

export const WebSocketProvider = ({ children }) => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const stompClientRef = useRef(null);
  const subscriptionRef = useRef(null);

  // Fetch initial notifications and unread count from REST API
  const fetchInitialData = async () => {
    try {
      const historyRes = await notificationService.getNotifications(0, 5);
      if (historyRes.success && historyRes.data) {
        setNotifications(historyRes.data.content);
      }
      const countRes = await notificationService.getUnreadCount();
      if (countRes.success) {
        setUnreadCount(countRes.data);
      }
    } catch (e) {
      console.error('Failed to load notifications history:', e);
    }
  };

  useEffect(() => {
    if (!user) {
      // Clear notifications on logout
      setNotifications([]);
      setUnreadCount(0);
      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
      }
      return;
    }

    fetchInitialData();

    // Setup STOMP over SockJS connection
    const socketUrl = process.env.NEXT_PUBLIC_WS_URL || 'http://localhost:8080/ws';
    const client = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
      },
      debug: (msg) => {
        // console.log('[STOMP]:', msg);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    });

    client.onConnect = () => {
      // Subscribe to user-specific notification queue
      subscriptionRef.current = client.subscribe('/user/queue/notifications', (message) => {
        try {
          const newNotification = JSON.parse(message.body);
          
          setNotifications((prev) => [newNotification, ...prev.slice(0, 4)]);
          setUnreadCount((prev) => prev + 1);
        } catch (e) {
          console.error('Failed to parse WebSocket notification message:', e);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('Broker reported error: ' + frame.headers['message']);
      console.error('Additional details: ' + frame.body);
    };

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (subscriptionRef.current) {
        subscriptionRef.current.unsubscribe();
      }
      if (client) {
        client.deactivate();
      }
    };
  }, [user]);

  const markAsRead = async (id) => {
    try {
      const res = await notificationService.markAsRead(id);
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.id === id ? { ...n, read: true } : n))
        );
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (e) {
      console.error('Failed to mark notification as read:', e);
    }
  };

  const markAllAsRead = async () => {
    try {
      const res = await notificationService.markAllAsRead();
      if (res.success) {
        setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
        setUnreadCount(0);
      }
    } catch (e) {
      console.error('Failed to mark all notifications as read:', e);
    }
  };

  return (
    <WebSocketContext.Provider value={{ notifications, unreadCount, markAsRead, markAllAsRead }}>
      {children}
    </WebSocketContext.Provider>
  );
};

export const useWebSocket = () => {
  const context = useContext(WebSocketContext);
  if (context === undefined) {
    throw new Error('useWebSocket must be used within a WebSocketProvider');
  }
  return context;
};
export default useWebSocket;
