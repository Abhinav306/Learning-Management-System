'use client';

import React, { useState, useEffect, useCallback } from 'react';
import withAuth from '@/components/layout/withAuth';
import { courseService } from '@/services/courseService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { AlertCircle } from 'lucide-react';
import styles from './page.module.css';

const AdminCoursesPage = () => {
  const [courses, setCourses] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalPages: 1,
    totalElements: 0,
  });

  const fetchAllCourses = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await courseService.getCourses({
        page: pagination.page,
        size: pagination.size,
      });
      if (res.success && res.data) {
        setCourses(res.data.content || []);
        setPagination((prev) => ({
          ...prev,
          totalPages: res.data.totalPages || 1,
          totalElements: res.data.totalElements || 0,
        }));
      }
    } catch (e) {
      console.error('Failed to load system courses:', e);
    } finally {
      setIsLoading(false);
    }
  }, [pagination.page, pagination.size]);

  useEffect(() => {
    fetchAllCourses();
  }, [fetchAllCourses]);

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination((prev) => ({ ...prev, page: newPage }));
    }
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
        <h1>System Courses Console</h1>
        <p>Overview of all system courses, categories, active enrollments, and status configurations.</p>
      </section>

      {isLoading ? (
        <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
          <Spinner />
        </div>
      ) : courses.length === 0 ? (
        <div className={styles.emptyState}>
          <AlertCircle size={32} />
          <p style={{ marginTop: 'var(--space-sm)' }}>No courses registered in the database.</p>
        </div>
      ) : (
        <>
          <GlassCard className={styles.tableCard}>
            <table className={styles.table}>
              <thead className={styles.thead}>
                <tr>
                  <th className={styles.th}>Title</th>
                  <th className={styles.th}>Instructor</th>
                  <th className={styles.th}>Category</th>
                  <th className={styles.th}>Difficulty</th>
                  <th className={styles.th}>Status</th>
                  <th className={styles.th}>Price</th>
                </tr>
              </thead>
              <tbody>
                {courses.map((c) => (
                  <tr key={c.id} className={styles.tr}>
                    <td className={styles.td}>
                      <span className={styles.title}>{c.title}</span>
                    </td>
                    <td className={styles.td}>
                      {c.instructor ? `${c.instructor.firstName} ${c.instructor.lastName}` : 'N/A'}
                    </td>
                    <td className={styles.td}>{c.categoryName || 'General'}</td>
                    <td className={styles.td}>{c.difficulty}</td>
                    <td className={styles.td}>
                      <span className={`${styles.badge} ${getStatusClass(c.status)}`}>
                        {c.status}
                      </span>
                    </td>
                    <td className={styles.td}>
                      {c.price === 0 ? 'Free' : `$${c.price.toFixed(2)}`}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </GlassCard>

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

// Wrap with ADMIN role guard restriction HOC
export default withAuth(['ADMIN'])(AdminCoursesPage);
