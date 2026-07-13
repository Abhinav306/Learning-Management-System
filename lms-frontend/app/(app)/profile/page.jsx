'use client';

import React, { useState, useEffect } from 'react';
import { useAuth } from '@/context/AuthContext';
import { authService } from '@/services/authService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { AlertCircle, CheckCircle } from 'lucide-react';
import styles from './page.module.css';

const UserProfilePage = () => {
  const { user, role } = useAuth();
  
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phoneNumber: '',
    bio: '',
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    if (!user) return;
    
    const loadProfile = async () => {
      try {
        const res = await authService.getProfile(user.id);
        if (res.success && res.data) {
          setFormData({
            firstName: res.data.firstName || '',
            lastName: res.data.lastName || '',
            email: res.data.email || '',
            phoneNumber: res.data.phoneNumber || '',
            bio: res.data.bio || '',
          });
        }
      } catch (e) {
        console.error('Failed to load profile details:', e);
      } finally {
        setIsLoading(false);
      }
    };
    loadProfile();
  }, [user]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData({
      ...formData,
      [name]: value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');
    setIsUpdating(true);

    try {
      const res = await authService.updateProfile(user.id, {
        firstName: formData.firstName,
        lastName: formData.lastName,
        phoneNumber: formData.phoneNumber,
        bio: formData.bio,
      });

      if (res.success) {
        setSuccessMsg('Your profile has been updated successfully.');
        // Optionally trigger hard reload to synchronize local storage context if needed
        setTimeout(() => {
          window.location.reload();
        }, 1500);
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to update profile details.');
    } finally {
      setIsUpdating(false);
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <section className={styles.header}>
        <h1>My Profile</h1>
        <p>Edit your personal settings, display names, and contact details below.</p>
      </section>

      <GlassCard className={styles.profileCard}>
        <div className={styles.avatarRow}>
          <div className={styles.avatar}>
            {formData.firstName ? formData.firstName.charAt(0) : formData.email.charAt(0).toUpperCase()}
          </div>
          <div className={styles.avatarMeta}>
            <span style={{ fontSize: '1.15rem', fontWeight: 700 }}>
              {formData.firstName ? `${formData.firstName} ${formData.lastName}` : formData.email}
            </span>
            <span className={styles.roleBadge}>{role}</span>
          </div>
        </div>

        {errorMsg && (
          <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
            <AlertCircle size={16} />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div style={{ color: 'var(--accent-green)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
            <CheckCircle size={16} />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.formGrid}>
            <div className={styles.inputGroup}>
              <label className={styles.label}>First Name</label>
              <input
                type="text"
                name="firstName"
                className={styles.input}
                value={formData.firstName}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className={styles.inputGroup}>
              <label className={styles.label}>Last Name</label>
              <input
                type="text"
                name="lastName"
                className={styles.input}
                value={formData.lastName}
                onChange={handleInputChange}
                required
              />
            </div>
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Email Address (Read-only)</label>
            <input
              type="email"
              className={styles.input}
              value={formData.email}
              disabled
              style={{ opacity: 0.5, cursor: 'not-allowed' }}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Phone Number</label>
            <input
              type="text"
              name="phoneNumber"
              className={styles.input}
              placeholder="+1 (555) 000-0000"
              value={formData.phoneNumber}
              onChange={handleInputChange}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.label}>Bio / Biography</label>
            <textarea
              name="bio"
              className={styles.textarea}
              placeholder="Tell us about yourself..."
              value={formData.bio}
              onChange={handleInputChange}
            />
          </div>

          <Button 
            type="submit" 
            variant="primary" 
            className={styles.submitBtn}
            disabled={isUpdating}
          >
            {isUpdating ? 'Updating Settings...' : 'Save Profile Settings'}
          </Button>
        </form>
      </GlassCard>
    </div>
  );
};

export default UserProfilePage;
