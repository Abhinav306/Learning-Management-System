'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { courseService } from '@/services/courseService';
import { enrollmentService } from '@/services/enrollmentService';
import { reviewService } from '@/services/reviewService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { 
  ChevronDown, 
  ChevronUp, 
  Play, 
  FileText, 
  Star, 
  Users, 
  Calendar,
  AlertCircle
} from 'lucide-react';
import styles from './page.module.css';

const CourseDetailPage = () => {
  const { courseId } = useParams();
  const router = useRouter();
  const { user, role } = useAuth();

  const [course, setCourse] = useState(null);
  const [sections, setSections] = useState([]);
  const [reviewsSummary, setReviewsSummary] = useState({ reviews: [], averageRating: 0, totalReviews: 0 });
  const [enrollmentStatus, setEnrollmentStatus] = useState({ enrolled: false, enrollment: null });
  const [expandedSections, setExpandedSections] = useState({});
  const [isLoading, setIsLoading] = useState(true);

  // Review submission state
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [isSubmittingReview, setIsSubmittingReview] = useState(false);
  const [reviewError, setReviewError] = useState('');

  // Fetch all course detail resources
  const loadData = useCallback(async () => {
    setIsLoading(true);
    try {
      const courseRes = await courseService.getCourseById(courseId);
      if (courseRes.success) {
        setCourse(courseRes.data);
      }

      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
        // Expand the first section by default
        if (sectionsRes.data.length > 0) {
          setExpandedSections({ [sectionsRes.data[0].id]: true });
        }
      }

      const reviewsRes = await reviewService.getCourseReviews(courseId, 0, 5);
      if (reviewsRes.success && reviewsRes.data) {
        setReviewsSummary({
          reviews: reviewsRes.data.reviews?.content || [],
          averageRating: reviewsRes.data.averageRating || 0,
          totalReviews: reviewsRes.data.totalReviews || 0,
        });
      }

      if (user && role !== 'INSTRUCTOR') {
        const enrollmentRes = await enrollmentService.checkEnrollmentStatus(courseId);
        setEnrollmentStatus(enrollmentRes);
      } else {
        setEnrollmentStatus({ enrolled: false, enrollment: null });
      }
    } catch (e) {
      console.error('Failed to load course details:', e);
    } finally {
      setIsLoading(false);
    }
  }, [courseId, user, role]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Toggle sections expand/collapse
  const toggleSection = (id) => {
    setExpandedSections((prev) => ({
      ...prev,
      [id]: !prev[id]
    }));
  };

  // Enroll logic
  const handleEnroll = async () => {
    try {
      const res = await enrollmentService.enrollInCourse(courseId);
      if (res.success) {
        setEnrollmentStatus({ enrolled: true, enrollment: res.data });
        router.refresh();
      }
    } catch (e) {
      console.error('Enrollment failed:', e);
    }
  };

  // Drop course logic
  const handleDrop = async () => {
    if (confirm('Are you sure you want to drop out of this course? Your progress history will be preserved if you re-enroll later.')) {
      try {
        const res = await enrollmentService.dropCourse(courseId);
        if (res.success) {
          setEnrollmentStatus({ enrolled: false, enrollment: null });
          router.refresh();
        }
      } catch (e) {
        console.error('Dropping course failed:', e);
      }
    }
  };

  // Submit review logic
  const handleReviewSubmit = async (e) => {
    e.preventDefault();
    setReviewError('');
    setIsSubmittingReview(true);

    try {
      const res = await reviewService.createReview(courseId, { rating, comment });
      if (res.success) {
        setComment('');
        // Reload reviews list
        const reviewsRes = await reviewService.getCourseReviews(courseId, 0, 5);
        if (reviewsRes.success && reviewsRes.data) {
          setReviewsSummary({
            reviews: reviewsRes.data.reviews?.content || [],
            averageRating: reviewsRes.data.averageRating || 0,
            totalReviews: reviewsRes.data.totalReviews || 0,
          });
        }
      }
    } catch (err) {
      setReviewError(err.response?.data?.message || 'Failed to submit review. You can only submit one review per course.');
    } finally {
      setIsSubmittingReview(false);
    }
  };

  const renderStars = (count, size = 14) => {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      if (i <= count) {
        stars.push(<Star key={i} size={size} fill="currentColor" stroke="none" />);
      } else {
        stars.push(<Star key={i} size={size} stroke="currentColor" fill="none" style={{ opacity: 0.3 }} />);
      }
    }
    return stars;
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spinner />
      </div>
    );
  }

  if (!course) {
    return (
      <div className={styles.container} style={{ gridTemplateColumns: '1fr', textAlign: 'center', padding: 'var(--space-2xl) 0' }}>
        <GlassCard>
          <AlertCircle size={40} style={{ color: 'var(--accent-pink)', marginBottom: 'var(--space-sm)' }} />
          <h3>Course Not Found</h3>
          <p style={{ color: 'var(--text-secondary)' }}>The course database record could not be retrieved.</p>
        </GlassCard>
      </div>
    );
  }

  const isInstructor = user && course.instructor && user.id === course.instructor.id;

  return (
    <div className={styles.container}>
      
      {/* ── Left Column: Course Detail Info ── */}
      <div className={styles.detailsCol}>
        <div className={styles.headerInfo}>
          <h1>{course.title}</h1>
          <p className={styles.shortDesc}>{course.shortDescription}</p>
        </div>

        <section>
          <h2 className={styles.sectionTitle}>Course Description</h2>
          <p className={styles.bodyText}>{course.description}</p>
        </section>

        {/* Syllabus Section Accordion */}
        <section>
          <h2 className={styles.sectionTitle}>Syllabus / Course Outline</h2>
          {sections.length === 0 ? (
            <p className={styles.bodyText} style={{ fontStyle: 'italic' }}>No sections have been added to this syllabus yet.</p>
          ) : (
            <div className={styles.accordion}>
              {sections.map((section) => (
                <div key={section.id} className={styles.sectionItem}>
                  <div className={styles.sectionHeader} onClick={() => toggleSection(section.id)}>
                    <span className={styles.sectionTitleText}>{section.title}</span>
                    {expandedSections[section.id] ? <ChevronUp size={18} /> : <ChevronDown size={18} />}
                  </div>

                  {expandedSections[section.id] && (
                    <div className={styles.lessonList}>
                      {section.lessons.length === 0 ? (
                        <div className={styles.lessonItem} style={{ fontStyle: 'italic', paddingLeft: 'var(--space-xl)' }}>
                          No lessons in this section.
                        </div>
                      ) : (
                        section.lessons.map((lesson) => (
                          <div key={lesson.id} className={styles.lessonItem}>
                            {lesson.contentType === 'VIDEO' ? <Play size={14} style={{ color: 'var(--accent-cyan)' }} /> : <FileText size={14} style={{ color: 'var(--text-muted)' }} />}
                            <span className={styles.lessonTitle}>{lesson.title}</span>
                            <span className={styles.lessonDuration}>{lesson.duration} mins</span>
                          </div>
                        ))
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* Reviews Section */}
        <section className={styles.reviewsSection}>
          <h2 className={styles.sectionTitle}>Student Feedback & Reviews</h2>
          
          {/* Review submission Form (only visible to enrolled students who are not the instructor) */}
          {enrollmentStatus.enrolled && !isInstructor && (
            <form onSubmit={handleReviewSubmit} className={styles.reviewForm}>
              <h3>Write a Review</h3>
              {reviewError && (
                <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
                  <AlertCircle size={16} />
                  <span>{reviewError}</span>
                </div>
              )}
              <div className={styles.section} style={{ flexDirection: 'row', alignItems: 'center', gap: 'var(--space-md)' }}>
                <span className={styles.label}>Rating:</span>
                <div className={styles.starRatingSelect}>
                  {[1, 2, 3, 4, 5].map((num) => (
                    <Star
                      key={num}
                      size={20}
                      className={`${styles.interactiveStar} ${rating >= num ? styles.starSelected : ''}`}
                      onClick={() => setRating(num)}
                      fill={rating >= num ? 'currentColor' : 'none'}
                    />
                  ))}
                </div>
              </div>
              <div className={styles.section}>
                <span className={styles.label}>Comment:</span>
                <textarea
                  className={styles.textarea}
                  placeholder="Share your thoughts about this course..."
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  required
                />
              </div>
              <Button type="submit" variant="primary" disabled={isSubmittingReview}>
                {isSubmittingReview ? 'Submitting...' : 'Submit Review'}
              </Button>
            </form>
          )}

          {/* Reviews list */}
          {reviewsSummary.reviews.length === 0 ? (
            <p className={styles.bodyText} style={{ fontStyle: 'italic' }}>No reviews have been written for this course yet.</p>
          ) : (
            <div>
              {reviewsSummary.reviews.map((rev) => (
                <div key={rev.id} className={styles.reviewItem}>
                  <div className={styles.reviewHeader}>
                    <div className={styles.studentInfo}>
                      <div className={styles.studentAvatar}>
                        {rev.studentName ? rev.studentName.charAt(0).toUpperCase() : 'S'}
                      </div>
                      <span className={styles.studentName}>
                        {rev.studentName || 'Student'}
                      </span>
                    </div>
                    <div className={styles.reviewStars}>{renderStars(rev.rating)}</div>
                  </div>
                  <p className={styles.comment}>{rev.comment}</p>
                  <span className={styles.date}>{new Date(rev.createdAt).toLocaleDateString()}</span>
                </div>
              ))}
            </div>
          )}
        </section>
      </div>

      {/* ── Right Column: Sidebar Panel ── */}
      <div className={styles.sidePanel}>
        <img 
          src={course.thumbnailUrl || '/api/placeholder/400/250'} 
          alt={course.title}
          className={styles.previewImage}
          onError={(e) => {
            e.target.src = 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?q=80&w=600&auto=format&fit=crop';
          }}
        />

        <GlassCard className={styles.checkoutCard}>
          <span className={styles.price}>
            {course.price === 0 ? 'Free' : `$${course.price.toFixed(2)}`}
          </span>

          {enrollmentStatus.enrolled ? (
            <>
              <Button 
                variant="primary" 
                className={styles.actionBtn}
                onClick={() => router.push(`/courses/${courseId}/study`)}
              >
                Resume Study
              </Button>
              <Button 
                variant="ghost" 
                className={styles.dropBtn}
                onClick={handleDrop}
              >
                Drop Course
              </Button>
            </>
          ) : isInstructor ? (
            <div style={{ textAlign: 'center' }}>
              <p className={styles.bodyText} style={{ marginBottom: 'var(--space-sm)', fontWeight: 600 }}>
                You are the instructor of this course.
              </p>
              <Button 
                variant="primary" 
                className={styles.actionBtn}
                onClick={() => router.push(`/courses/${courseId}/study`)}
              >
                Go to Study Room
              </Button>
            </div>
          ) : (
            <Button 
              variant="primary" 
              className={styles.actionBtn}
              onClick={handleEnroll}
            >
              Enroll Now
            </Button>
          )}

          {/* Quick Metrics */}
          <div className={styles.metaList}>
            <div className={styles.metaItem}>
              <span>Difficulty</span>
              <span className={styles.metaVal}>{course.difficulty}</span>
            </div>
            <div className={styles.metaItem}>
              <span>Language</span>
              <span className={styles.metaVal}>English</span>
            </div>
            <div className={styles.metaItem}>
              <span>Rating</span>
              <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Star size={12} fill="var(--accent-amber)" stroke="none" />
                {reviewsSummary.averageRating.toFixed(1)} ({reviewsSummary.totalReviews})
              </span>
            </div>
            <div className={styles.metaItem}>
              <span>Students Enrolled</span>
              <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Users size={12} />
                {course.totalStudents}
              </span>
            </div>
            <div className={styles.metaItem}>
              <span>Last Updated</span>
              <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                <Calendar size={12} />
                {new Date(course.updatedAt).toLocaleDateString()}
              </span>
            </div>
          </div>
        </GlassCard>
      </div>

    </div>
  );
};

export default CourseDetailPage;
