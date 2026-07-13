'use client';

import React from 'react';
import { Search, LogOut } from 'lucide-react';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import NotificationBell from '../notifications/NotificationBell';
import styles from './TopBar.module.css';

const TopBar = () => {
  const pathname = usePathname();
  const { user, role, logout } = useAuth();

  // Helper to resolve page titles from active route paths
  const getPageTitle = () => {
    if (pathname.startsWith('/dashboard')) return 'Dashboard';
    if (pathname.startsWith('/courses')) return 'Courses';
    if (pathname.startsWith('/documents')) return 'AI RAG Knowledgebase';
    if (pathname.startsWith('/profile')) return 'My Profile';
    if (pathname.startsWith('/admin')) return 'Admin Panel';
    return 'LMS';
  };

  return (
    <header className={styles.topbar}>
      <h2 className={styles.title}>{getPageTitle()}</h2>
      
      <div className={styles.actions}>
        <div className={styles.searchWrapper}>
          <Search size={16} style={{ color: 'var(--text-muted)' }} />
          <input 
            type="text" 
            placeholder="Search lessons or topics..." 
            className={styles.searchInput} 
          />
        </div>
        
        <NotificationBell />

        {user && (
          <div className={styles.profileMenu}>
            <div className={styles.userInfo}>
              <span className={styles.userName}>
                {user.firstName ? `${user.firstName} ${user.lastName}` : user.email}
              </span>
              <span className={styles.userRole}>{role}</span>
            </div>
            <div className={styles.avatar}>
              {user.firstName ? user.firstName.charAt(0) : user.email.charAt(0).toUpperCase()}
            </div>
            <button onClick={logout} className={styles.logoutBtn} title="Logout">
              <LogOut size={16} />
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

export default TopBar;
