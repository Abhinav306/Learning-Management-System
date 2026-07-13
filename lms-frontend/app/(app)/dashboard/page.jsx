'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { recommendationService } from '@/services/recommendationService';
import { enrollmentService } from '@/services/enrollmentService';
import { courseService } from '@/services/courseService';
import CourseCard from '@/components/course/CourseCard';
import GlassCard from '@/components/ui/GlassCard';
import Spinner from '@/components/ui/Spinner';
import { BookOpen, GraduationCap, Award, Sparkles, BookOpenCheck, Settings } from 'lucide-react';
import styles from './page.module.css';

const DashboardPage = () => {
  const { user, role } = useAuth();
  const router = useRouter();
  
  const [enrollments, setEnrollments] = useState([]);
  const [teachingCourses, setTeachingCourses] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [isAiRecommended, setIsAiRecommended] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadDashboardData = async () => {
      setIsLoading(true);
      try {
        // 1. Role-specific dashboard data fetching
        if (role === 'STUDENT' || role === 'ADMIN') {
          try {
            const enrollRes = await enrollmentService.getMyEnrollments(0, 100);
            if (enrollRes.success && enrollRes.data) {
              setEnrollments(enrollRes.data.content || []);
            }
          } catch (e) {
            console.error('Failed to load student enrollments:', e);
          }
        } else if (role === 'INSTRUCTOR') {
          try {
            const coursesRes = await courseService.getCourses({ instructorId: user.id });
            if (coursesRes.success && coursesRes.data) {
              setTeachingCourses(coursesRes.data.content || []);
            }
          } catch (e) {
            console.error('Failed to load instructor courses:', e);
          }
        }

        // 2. Fetch course recommendations (personalized for students/admins, popular for instructors)
        try {
          if (role === 'STUDENT' || role === 'ADMIN') {
            const personalizedRes = await recommendationService.getPersonalizedRecommendations();
            if (personalizedRes.success && personalizedRes.data && personalizedRes.data.length > 0) {
              setRecommendations(personalizedRes.data);
              setIsAiRecommended(true);
            } else {
              const popularRes = await recommendationService.getPopularCourses();
              if (popularRes.success && popularRes.data) {
                setRecommendations(popularRes.data);
              }
            }
          } else {
            // Instructors do not get personalized student course recommendations, show popular catalog instead
            const popularRes = await recommendationService.getPopularCourses();
            if (popularRes.success && popularRes.data) {
              setRecommendations(popularRes.data);
            }
          }
        } catch (recommendError) {
          console.error('Failed to load recommendations, loading fallback:', recommendError);
          const popularRes = await recommendationService.getPopularCourses();
          if (popularRes.success && popularRes.data) {
            setRecommendations(popularRes.data);
          }
        }
      } catch (e) {
        console.error('Failed to load dashboard data:', e);
      } finally {
        setIsLoading(false);
      }
    };

    if (user?.id) {
      loadDashboardData();
    }
  }, [user, role]);

  const handleStudyCourse = (courseId) => {
    router.push(`/courses/${courseId}/study`);
  };

  const handleManageCourse = (courseId) => {
    router.push(`/instructor/courses/${courseId}`);
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
        <Spinner />
      </div>
    );
  }

  // Student metrics
  const enrolledCount = enrollments.length;
  const completedCount = enrollments.filter((e) => e.status === 'COMPLETED').length;
  const averageProgress = enrolledCount > 0 
    ? Math.round(enrollments.reduce((acc, curr) => acc + (curr.progress || 0), 0) / enrolledCount) 
    : 0;

  // Instructor metrics
  const totalCreated = teachingCourses.length;
  const totalPublished = teachingCourses.filter(c => c.status === 'PUBLISHED').length;
  const totalDrafts = teachingCourses.filter(c => c.status === 'DRAFT').length;

  return (
    <div className={styles.dashboard}>
      <section className={styles.welcomeSection}>
        <h1>Welcome Back, {user?.firstName || 'Learner'}!</h1>
        <p>
          {role === 'INSTRUCTOR' 
            ? 'Manage your syllabus structures, configure course pricing, or edit lesson plans.'
            : 'Explore your learning metrics, resume study rooms, and review recommendations.'}
        </p>
      </section>

      {/* Dynamic KPI Stats Widgets depending on Role */}
      {role === 'INSTRUCTOR' ? (
        <section className={styles.statsGrid}>
          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.cyanIcon}`}>
              <BookOpen size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{totalCreated}</span>
              <span className={styles.statLabel}>Courses Created</span>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.purpleIcon}`}>
              <BookOpenCheck size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{totalPublished}</span>
              <span className={styles.statLabel}>Published Courses</span>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.greenIcon}`}>
              <Award size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{totalDrafts}</span>
              <span className={styles.statLabel}>Draft Courses</span>
            </div>
          </div>
        </section>
      ) : (
        <section className={styles.statsGrid}>
          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.cyanIcon}`}>
              <BookOpen size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{enrolledCount}</span>
              <span className={styles.statLabel}>Enrolled Courses</span>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.purpleIcon}`}>
              <BookOpenCheck size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{completedCount}</span>
              <span className={styles.statLabel}>Completed Courses</span>
            </div>
          </div>

          <div className={styles.statCard}>
            <div className={`${styles.statIcon} ${styles.greenIcon}`}>
              <Award size={24} />
            </div>
            <div className={styles.statMeta}>
              <span className={styles.statValue}>{averageProgress}%</span>
              <span className={styles.statLabel}>Average Progress</span>
            </div>
          </div>
        </section>
      )}

      {/* Instructor Workstation shortcut list */}
      {role === 'INSTRUCTOR' && teachingCourses.length > 0 && (
        <section className={styles.myCourses}>
          <div className={styles.sectionHeader}>
            <h2>Manage My Courses</h2>
          </div>
          <div className={styles.grid}>
            {teachingCourses.map((course) => (
              <GlassCard 
                key={course.id} 
                className={styles.courseProgressCard}
                onClick={() => handleManageCourse(course.id)}
                hoverable
              >
                <h3 className={styles.courseProgressTitle}>{course.title}</h3>
                
                <div className={styles.progressContainer}>
                  <div className={styles.progressText}>
                    <span>{course.difficulty}</span>
                    <span style={{ display: 'flex', alignItems: 'center', gap: '4px', color: 'var(--accent-cyan)' }}>
                      <Settings size={12} /> Manage Curriculum
                    </span>
                  </div>
                </div>
              </GlassCard>
            ))}
          </div>
        </section>
      )}

      {/* Student Enrolled Courses Workstation */}
      {(role === 'STUDENT' || role === 'ADMIN') && enrollments.length > 0 && (
        <section className={styles.myCourses}>
          <div className={styles.sectionHeader}>
            <h2>My Active Classroom Sessions</h2>
          </div>
          <div className={styles.grid}>
            {enrollments.map((item) => (
              <GlassCard 
                key={item.id} 
                className={styles.courseProgressCard}
                onClick={() => handleStudyCourse(item.courseId)}
                hoverable
              >
                <h3 className={styles.courseProgressTitle}>{item.courseTitle}</h3>
                
                <div className={styles.progressContainer}>
                  <div className={styles.progressBarTrack}>
                    <div 
                      className={styles.progressBarFill} 
                      style={{ width: `${item.progress || 0}%` }}
                    ></div>
                  </div>
                  <div className={styles.progressText}>
                    <span>{item.status}</span>
                    <span>{Math.round(item.progress || 0)}% Complete</span>
                  </div>
                </div>
              </GlassCard>
            ))}
          </div>
        </section>
      )}

      {/* Recommendations Feed Section */}
      <section className={styles.recommendations}>
        <div className={styles.sectionHeader}>
          <h2>
            <Sparkles size={20} style={{ color: 'var(--accent-purple)' }} />
            {isAiRecommended ? 'Recommended For You (AI-Tailored)' : 'Popular Courses'}
          </h2>
        </div>

        {recommendations.length === 0 ? (
          <div className={styles.emptyState}>
            <p>No courses found in the system database catalogue.</p>
          </div>
        ) : (
          <div className={styles.grid}>
            {recommendations.slice(0, 3).map((course) => (
              <CourseCard key={course.id} course={course} />
            ))}
          </div>
        )}
      </section>
    </div>
  );
};

export default DashboardPage;
