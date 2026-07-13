'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import withAuth from '@/components/layout/withAuth';
import { useAuth } from '@/context/AuthContext';
import { courseService } from '@/services/courseService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { Sparkles, Plus, X, BookOpen, AlertCircle } from 'lucide-react';
import styles from './page.module.css';

const InstructorCoursesPage = () => {
  const { user } = useAuth();
  const router = useRouter();

  const [courses, setCourses] = useState([]);
  const [categories, setCategories] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Form State
  const [title, setTitle] = useState('');
  const [shortDescription, setShortDescription] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [price, setPrice] = useState('0.00');
  const [difficulty, setDifficulty] = useState('BEGINNER');
  const [status, setStatus] = useState('DRAFT');
  const [language, setLanguage] = useState('English');
  const [categoryId, setCategoryId] = useState('');
  
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Fetch teaching courses list
  const fetchInstructorCourses = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await courseService.getCourses({ instructorId: user.id });
      if (res.success && res.data) {
        setCourses(res.data.content || []);
      }
    } catch (e) {
      console.error('Failed to fetch instructor courses:', e);
    } finally {
      setIsLoading(false);
    }
  }, [user.id]);

  // Fetch categories for course assignment dropdown selection
  const fetchCategories = useCallback(async () => {
    try {
      const res = await courseService.getCategories();
      if (res.success && res.data) {
        setCategories(res.data || []);
      }
    } catch (e) {
      console.error('Failed to fetch categories:', e);
    }
  }, []);

  useEffect(() => {
    if (user?.id) {
      fetchInstructorCourses();
      fetchCategories();
    }
  }, [user, fetchInstructorCourses, fetchCategories]);

  const handleCreateCourse = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setIsSubmitting(true);

    try {
      const reqBody = {
        title,
        shortDescription,
        description,
        thumbnailUrl: thumbnailUrl || 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3',
        price: parseFloat(price),
        difficulty,
        status,
        language,
        categoryId: categoryId || null
      };

      const res = await courseService.createCourse(reqBody);
      if (res.success) {
        setIsModalOpen(false);
        // Reset Form
        setTitle('');
        setShortDescription('');
        setDescription('');
        setThumbnailUrl('');
        setPrice('0.00');
        setDifficulty('BEGINNER');
        setStatus('DRAFT');
        setLanguage('English');
        setCategoryId('');
        
        fetchInstructorCourses();
      } else {
        setErrorMsg(res.message || 'Failed to create course');
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Error occurred while creating course');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCardClick = (courseId) => {
    router.push(`/instructor/courses/${courseId}`);
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'PUBLISHED': return styles.published;
      case 'DRAFT': return styles.draft;
      case 'ARCHIVED': return styles.archived;
      default: return styles.draft;
    }
  };

  return (
    <div className={styles.container}>
      <section className={styles.header}>
        <div className={styles.headerLeft}>
          <h1>My Teaching Workstation</h1>
          <p>Create, update, and manage your courses, sections, and lesson videos.</p>
        </div>
        <Button variant="primary" onClick={() => setIsModalOpen(true)}>
          <Plus size={16} style={{ marginRight: '8px' }} />
          Create Course
        </Button>
      </section>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
          <Spinner />
        </div>
      ) : courses.length === 0 ? (
        <div className={styles.emptyState}>
          <AlertCircle size={40} style={{ color: 'var(--text-muted)', marginBottom: '12px' }} />
          <p>You have not created any courses yet.</p>
          <Button variant="primary" onClick={() => setIsModalOpen(true)} style={{ marginTop: '16px' }}>
            Get Started — Create Your First Course
          </Button>
        </div>
      ) : (
        <div className={styles.grid}>
          {courses.map((course) => (
            <GlassCard 
              key={course.id} 
              className={styles.courseCard}
              onClick={() => handleCardClick(course.id)}
              hoverable
            >
              <h3 className={styles.courseTitle}>{course.title}</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                {course.shortDescription || 'No description provided.'}
              </p>
              
              <div className={styles.courseMeta}>
                <span className={`${styles.badge} ${getStatusClass(course.status)}`}>
                  {course.status}
                </span>
                <span>{course.difficulty}</span>
              </div>
              
              <div className={styles.cardActions}>
                <Button variant="secondary" size="small" style={{ width: '100%' }}>
                  Manage Course & Syllabus
                </Button>
              </div>
            </GlassCard>
          ))}
        </div>
      )}

      {/* Course Creation Modal */}
      {isModalOpen && (
        <div className={styles.modalOverlay}>
          <GlassCard className={styles.modalContent}>
            <div className={styles.modalHeader}>
              <h2>Create New Course</h2>
              <button className={styles.closeBtn} onClick={() => setIsModalOpen(false)}>
                <X size={20} />
              </button>
            </div>

            {errorMsg && (
              <div style={{ color: 'var(--accent-pink)', display: 'flex', gap: '8px', fontSize: '0.85rem', alignItems: 'center' }}>
                <AlertCircle size={16} />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleCreateCourse} className={styles.form}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Course Title *</label>
                <input 
                  type="text" 
                  className={styles.input}
                  placeholder="e.g. Master Spring Boot 3"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Short Summary (Max 500 chars)</label>
                <input 
                  type="text" 
                  className={styles.input}
                  placeholder="e.g. Build enterprise web apps with Spring Boot"
                  value={shortDescription}
                  onChange={(e) => setShortDescription(e.target.value)}
                />
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Detailed Description</label>
                <textarea 
                  className={styles.textarea}
                  rows={4}
                  placeholder="Write course outline, requirements, and outcomes..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </div>

              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.label}>Category</label>
                  <select 
                    className={styles.select}
                    value={categoryId}
                    onChange={(e) => setCategoryId(e.target.value)}
                  >
                    <option value="">Select Category</option>
                    {categories.map((c) => (
                      <option key={c.id} value={c.id}>{c.name}</option>
                    ))}
                  </select>
                </div>

                <div className={styles.formGroup}>
                  <label className={styles.label}>Difficulty Level *</label>
                  <select 
                    className={styles.select}
                    value={difficulty}
                    onChange={(e) => setDifficulty(e.target.value)}
                    required
                  >
                    <option value="BEGINNER">Beginner</option>
                    <option value="INTERMEDIATE">Intermediate</option>
                    <option value="ADVANCED">Advanced</option>
                  </select>
                </div>
              </div>

              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.label}>Price (USD) *</label>
                  <input 
                    type="number" 
                    step="0.01"
                    min="0"
                    className={styles.input}
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    required
                  />
                </div>

                <div className={styles.formGroup}>
                  <label className={styles.label}>Language</label>
                  <input 
                    type="text" 
                    className={styles.input}
                    value={language}
                    onChange={(e) => setLanguage(e.target.value)}
                  />
                </div>
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Course Status *</label>
                <select 
                  className={styles.select}
                  value={status}
                  onChange={(e) => setStatus(e.target.value)}
                  required
                >
                  <option value="DRAFT">Draft</option>
                  <option value="PUBLISHED">Published</option>
                  <option value="ARCHIVED">Archived</option>
                </select>
              </div>

              <div className={styles.formActions}>
                <Button 
                  type="button" 
                  variant="secondary" 
                  onClick={() => setIsModalOpen(false)}
                >
                  Cancel
                </Button>
                <Button 
                  type="submit" 
                  variant="primary" 
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Creating...' : 'Create Course'}
                </Button>
              </div>
            </form>
          </GlassCard>
        </div>
      )}
    </div>
  );
};

export default withAuth(['INSTRUCTOR'])(InstructorCoursesPage);
