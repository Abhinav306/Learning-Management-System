'use client';

import React, { useState, useEffect, useCallback } from 'react';
import CourseCard from '@/components/course/CourseCard';
import CourseFilter from '@/components/course/CourseFilter';
import Spinner from '@/components/ui/Spinner';
import Button from '@/components/ui/Button';
import { courseService } from '@/services/courseService';
import styles from './page.module.css';

const CoursesPage = () => {
  const [categories, setCategories] = useState([]);
  const [courses, setCourses] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [filters, setFilters] = useState({
    keyword: '',
    categoryId: '',
    difficulty: '',
  });
  const [pagination, setPagination] = useState({
    page: 0,
    size: 6,
    totalPages: 1,
    totalElements: 0,
  });

  // Fetch categories on mount
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const res = await courseService.getCategories();
        if (res.success && res.data) {
          setCategories(res.data);
        }
      } catch (e) {
        console.error('Failed to fetch categories:', e);
      }
    };
    fetchCategories();
  }, []);

  // Fetch courses with current filters and page
  const fetchCourses = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await courseService.getCourses({
        ...filters,
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
      console.error('Failed to fetch courses:', e);
    } finally {
      setIsLoading(false);
    }
  }, [filters, pagination.page, pagination.size]);

  useEffect(() => {
    fetchCourses();
  }, [fetchCourses]);

  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
    setPagination((prev) => ({ ...prev, page: 0 })); // Reset to first page on filter change
  };

  const handleResetFilters = () => {
    setFilters({
      keyword: '',
      categoryId: '',
      difficulty: '',
    });
    setPagination((prev) => ({ ...prev, page: 0 }));
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      setPagination((prev) => ({ ...prev, page: newPage }));
    }
  };

  return (
    <div className={styles.coursesPage}>
      {/* Dynamic Left Column Filter Sidebar */}
      <CourseFilter 
        categories={categories}
        filters={filters}
        onChange={handleFilterChange}
        onReset={handleResetFilters}
      />

      {/* Right Column Catalogue Listing Grid */}
      <div className={styles.catalogSection}>
        {isLoading ? (
          <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
            <Spinner />
          </div>
        ) : courses.length === 0 ? (
          <div className={styles.emptyState}>
            <h3>No Courses Found</h3>
            <p style={{ marginTop: 'var(--space-sm)' }}>
              We couldn't find any courses matching your search criteria. Try modifying your filters.
            </p>
          </div>
        ) : (
          <>
            <div className={styles.grid}>
              {courses.map((course) => (
                <CourseCard key={course.id} course={course} />
              ))}
            </div>

            {/* Pagination Controls */}
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
    </div>
  );
};

export default CoursesPage;
