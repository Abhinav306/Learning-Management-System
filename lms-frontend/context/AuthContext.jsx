'use client';

import React, { createContext, useContext, useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '../services/authService';

const AuthContext = createContext(undefined);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [role, setRole] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    // Check if tokens exist in localStorage on mount
    const checkAuth = async () => {
      try {
        const token = localStorage.getItem('accessToken');
        const userId = localStorage.getItem('userId');
        const savedRole = localStorage.getItem('role');

        if (token && userId && savedRole) {
          setRole(savedRole);
          // Optionally fetch full profile details
          try {
            const profileResponse = await authService.getProfile(userId);
            if (profileResponse.success) {
              setUser(profileResponse.data);
            }
          } catch (e) {
            console.error('Failed to fetch profile info on mount:', e);
            // Fallback: build minimal user response from storage
            setUser({
              id: userId,
              email: localStorage.getItem('email') || '',
              firstName: '',
              lastName: '',
              role: savedRole
            });
          }
        }
      } catch (err) {
        console.error('Auth initialization error:', err);
      } finally {
        setIsLoading(false);
      }
    };

    checkAuth();
  }, []);

  const login = async (credentials) => {
    setIsLoading(true);
    try {
      const response = await authService.login(credentials);
      if (response.success && response.data) {
        const { accessToken, refreshToken, userId, email, role: userRole } = response.data;
        
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);
        localStorage.setItem('userId', userId);
        localStorage.setItem('email', email);
        localStorage.setItem('role', userRole);

        setRole(userRole);
        
        // Fetch full profile info
        try {
          const profileResponse = await authService.getProfile(userId);
          if (profileResponse.success) {
            setUser(profileResponse.data);
          }
        } catch (e) {
          // Minimal user info backup
          setUser({
            id: userId,
            email,
            firstName: '',
            lastName: '',
            role: userRole
          });
        }
        
        router.push('/dashboard');
      } else {
        throw new Error(response.message || 'Login failed');
      }
    } catch (error) {
      setIsLoading(false);
      throw error;
    } finally {
      setIsLoading(false);
    }
  };

  const signup = async (userData) => {
    setIsLoading(true);
    try {
      const response = await authService.signup(userData);
      if (!response.success) {
        throw new Error(response.message || 'Signup failed');
      }
    } finally {
      setIsLoading(false);
    }
  };

  const logout = async () => {
    setIsLoading(true);
    try {
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        await authService.logout(refreshToken);
      }
    } catch (e) {
      console.error('Failed to call logout endpoint on backend:', e);
    } finally {
      localStorage.clear();
      setUser(null);
      setRole(null);
      setIsLoading(false);
      router.push('/login');
    }
  };

  return (
    <AuthContext.Provider value={{ user, role, isLoading, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
