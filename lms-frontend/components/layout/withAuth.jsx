'use client';

import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '../../context/AuthContext';
import Spinner from '../ui/Spinner';

export const withAuth = (allowedRoles) => {
  return (WrappedComponent) => {
    const ComponentWithAuth = (props) => {
      const { user, role, isLoading } = useAuth();
      const router = useRouter();

      useEffect(() => {
        if (!isLoading) {
          if (!user) {
            router.push('/login');
          } else if (allowedRoles && role && !allowedRoles.includes(role)) {
            router.push('/dashboard');
          }
        }
      }, [user, role, isLoading, router]);

      if (isLoading) {
        return (
          <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight: '100vh',
            backgroundColor: 'var(--bg-primary)'
          }}>
            <Spinner />
          </div>
        );
      }

      if (!user || (allowedRoles && role && !allowedRoles.includes(role))) {
        return null; // Don't render component if redirecting
      }

      return <WrappedComponent {...props} />;
    };

    return ComponentWithAuth;
  };
};
export default withAuth;
