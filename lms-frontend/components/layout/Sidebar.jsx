'use client';

import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { 
  LayoutDashboard, 
  BookOpen, 
  FileText, 
  User, 
  Shield, 
  Sparkles
} from 'lucide-react';
import styles from './Sidebar.module.css';

const Sidebar = () => {
  const pathname = usePathname();
  const { user, role, logout } = useAuth();

  const isActive = (path) => {
    return pathname === path ? styles.active : '';
  };

  return (
    <aside className={styles.sidebar}>
      <div className={styles.brand}>
        <div className={styles.logo}>
          AuraLMS <span className={styles.glowdot}></span>
        </div>
      </div>

      <nav className={styles.nav}>
        <Link href="/dashboard" className={`${styles.link} ${isActive('/dashboard')}`}>
          <LayoutDashboard size={20} />
          <span>Dashboard</span>
        </Link>

        <Link href="/courses" className={`${styles.link} ${isActive('/courses')}`}>
          <BookOpen size={20} />
          <span>Courses</span>
        </Link>

        <Link href="/documents" className={`${styles.link} ${isActive('/documents')}`}>
          <FileText size={20} />
          <span style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            AI RAG
            <Sparkles size={14} style={{ color: 'var(--accent-purple)' }} />
          </span>
        </Link>

        <Link href="/profile" className={`${styles.link} ${isActive('/profile')}`}>
          <User size={20} />
          <span>My Profile</span>
        </Link>

        {role === 'INSTRUCTOR' && (
          <>
            <div className={styles.navSection}>Instructor Area</div>
            <Link href="/instructor/courses" className={`${styles.link} ${isActive('/instructor/courses')}`}>
              <BookOpen size={20} />
              <span>Manage Courses</span>
            </Link>
          </>
        )}

        {role === 'ADMIN' && (
          <>
            <div className={styles.navSection}>Administration</div>
            <Link href="/admin/courses" className={`${styles.link} ${isActive('/admin/courses')}`}>
              <Shield size={20} />
              <span>Admin Panel</span>
            </Link>
          </>
        )}
      </nav>
    </aside>
  );
};

export default Sidebar;
