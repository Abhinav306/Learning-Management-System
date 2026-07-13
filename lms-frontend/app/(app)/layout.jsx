'use client';

import React from 'react';
import { WebSocketProvider } from '@/context/WebSocketContext';
import withAuth from '@/components/layout/withAuth';
import Sidebar from '@/components/layout/Sidebar';
import TopBar from '@/components/layout/TopBar';
import styles from './layout.module.css';

const AppLayout = ({ children }) => {
  return (
    <WebSocketProvider>
      <div className={styles.layoutWrapper}>
        <Sidebar />
        <div className={styles.mainContent}>
          <TopBar />
          <main className={styles.pageBody}>
            {children}
          </main>
        </div>
      </div>
    </WebSocketProvider>
  );
};

// Wrap with standard authentication HOC - default allows all authenticated roles
export default withAuth()(AppLayout);
