'use client';

import React from 'react';
import { useRouter } from 'next/navigation';
import { Star, Users } from 'lucide-react';
import styles from './CourseCard.module.css';

const CourseCard = ({ course }) => {
  const router = useRouter();

  const handleCardClick = () => {
    router.push(`/courses/${course.id}`);
  };

  // Helper to render star rating stars dynamically
  const renderStars = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating || 0);
    const hasHalf = (rating || 0) % 1 >= 0.5;

    for (let i = 1; i <= 5; i++) {
      if (i <= fullStars) {
        stars.push(<Star key={i} size={14} fill="currentColor" stroke="none" />);
      } else if (i === fullStars + 1 && hasHalf) {
        stars.push(<Star key={i} size={14} fill="currentColor" stroke="none" style={{ opacity: 0.6 }} />);
      } else {
        stars.push(<Star key={i} size={14} stroke="currentColor" fill="none" style={{ opacity: 0.3 }} />);
      }
    }
    return stars;
  };

  // Format currency helper
  const formatPrice = (price) => {
    if (price === 0) return 'Free';
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  // Get difficulty color style
  const getDifficultyColor = (diff) => {
    switch (diff) {
      case 'BEGINNER': return 'var(--accent-green)';
      case 'INTERMEDIATE': return 'var(--accent-amber)';
      case 'ADVANCED': return 'var(--accent-pink)';
      default: return 'var(--text-muted)';
    }
  };

  return (
    <div className={styles.card} onClick={handleCardClick}>
      <div className={styles.thumbnailWrapper}>
        <img 
          src={course.thumbnailUrl || '/api/placeholder/400/250'} 
          alt={course.title}
          className={styles.thumbnail}
          onError={(e) => {
            e.target.src = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600&auto=format&fit=crop';
          }}
        />
      </div>

      <div className={styles.content}>
        <div className={styles.metaRow}>
          <span className={`${styles.badge} ${styles.category}`}>{course.categoryName || 'General'}</span>
          <span 
            className={`${styles.badge} ${styles.difficulty}`}
            style={{ color: getDifficultyColor(course.difficulty), borderColor: getDifficultyColor(course.difficulty) }}
          >
            {course.difficulty}
          </span>
        </div>

        <h3 className={styles.title}>{course.title}</h3>
        <p className={styles.description}>{course.shortDescription}</p>

        <div className={styles.ratingRow}>
          <div className={styles.stars}>{renderStars(course.averageRating)}</div>
          <span className={styles.ratingVal}>{(course.averageRating || 0).toFixed(1)}</span>
          <span className={styles.students}>
            <Users size={12} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'text-bottom' }} />
            {course.totalStudents || 0}
          </span>
        </div>
      </div>

      <div className={styles.footer}>
        <span className={styles.instructor}>
          By {course.instructor ? `${course.instructor.firstName} ${course.instructor.lastName}` : 'Instructor'}
        </span>
        <span className={styles.price}>{formatPrice(course.price)}</span>
      </div>
    </div>
  );
};

export default CourseCard;
