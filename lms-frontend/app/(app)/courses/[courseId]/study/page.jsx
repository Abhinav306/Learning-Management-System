'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { courseService } from '@/services/courseService';
import { enrollmentService } from '@/services/enrollmentService';
import { lessonProgressService } from '@/services/lessonProgressService';
import Spinner from '@/components/ui/Spinner';
import Button from '@/components/ui/Button';
import TutorSidebar from '@/components/ai/TutorSidebar';
import GlassCard from '@/components/ui/GlassCard';
import { 
  Play, 
  FileText, 
  CheckCircle, 
  Download, 
  BookOpen,
  ChevronDown,
  ChevronUp,
  Sparkles,
  AlertCircle
} from 'lucide-react';
import styles from './page.module.css';

const StudyPage = () => {
  const { courseId } = useParams();
  const router = useRouter();
  const { user, role } = useAuth();

  const [course, setCourse] = useState(null);
  const [sections, setSections] = useState([]);
  const [activeLesson, setActiveLesson] = useState(null);
  const [enrollment, setEnrollment] = useState(null);
  const [progressData, setProgressData] = useState({ progress: 0, completedLessonIds: [] });
  const [expandedSections, setExpandedSections] = useState({});
  const [isLoading, setIsLoading] = useState(true);

  // Load course study outline, lessons, and progress data
  const loadStudyRoomData = useCallback(async () => {
    setIsLoading(true);
    try {
      let currentEnrollment = null;

      // 1. Verify enrollment details for students (Instructors and Admins skip verification)
      if (role !== 'INSTRUCTOR' && role !== 'ADMIN') {
        const enrollRes = await enrollmentService.checkEnrollmentStatus(courseId);
        if (!enrollRes.enrolled) {
          // Redirect to detail page if user is not enrolled in this course
          router.push(`/courses/${courseId}`);
          return;
        }
        currentEnrollment = enrollRes.enrollment;
        setEnrollment(currentEnrollment);
      }

      // 2. Fetch course detail
      const courseRes = await courseService.getCourseById(courseId);
      if (courseRes.success) {
        setCourse(courseRes.data);
      }

      // 3. Fetch syllabus sections and lessons
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success && sectionsRes.data.length > 0) {
        setSections(sectionsRes.data);
        
        // Auto-select the first lesson of the first section as active
        const firstSec = sectionsRes.data[0];
        if (firstSec.lessons && firstSec.lessons.length > 0) {
          setActiveLesson(firstSec.lessons[0]);
        }

        // Expand sections by default
        const expansions = {};
        sectionsRes.data.forEach((sec) => {
          expansions[sec.id] = true;
        });
        setExpandedSections(expansions);
      }

      // 4. Load initial progress percentages and completed IDs
      if (currentEnrollment) {
        const progressRes = await lessonProgressService.getProgress(currentEnrollment.id);
        if (progressRes.success && progressRes.data) {
          setProgressData({
            progress: progressRes.data.progress || 0,
            completedLessonIds: progressRes.data.completedLessonIds || []
          });
        }
      }
    } catch (e) {
      console.error('Failed to load study room settings:', e);
    } finally {
      setIsLoading(false);
    }
  }, [courseId, router, role]);

  useEffect(() => {
    loadStudyRoomData();
  }, [loadStudyRoomData]);

  const toggleSection = (sectionId) => {
    setExpandedSections((prev) => ({
      ...prev,
      [sectionId]: !prev[sectionId]
    }));
  };

  const isCompleted = (lessonId) => {
    return progressData.completedLessonIds.includes(lessonId);
  };

  // Toggle completion status on click
  const handleToggleComplete = async (lessonId) => {
    if (!enrollment) return;
    
    try {
      let res;
      if (isCompleted(lessonId)) {
        res = await lessonProgressService.markIncomplete(enrollment.id, lessonId);
      } else {
        res = await lessonProgressService.markComplete(enrollment.id, lessonId);
      }

      if (res.success && res.data) {
        setProgressData({
          progress: res.data.progress || 0,
          completedLessonIds: res.data.completedLessonIds || []
        });
      }
    } catch (e) {
      console.error('Failed to toggle completion status:', e);
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className={styles.studyRoom}>
      
      {/* ── Left Column: Course outline navigator sidebar ── */}
      <aside className={styles.outlineSidebar}>
        <div className={styles.sidebarHeader}>
          <h3>{course?.title || 'Syllabus'}</h3>
          <div className={styles.progressContainer}>
            <div className={styles.progressBarTrack}>
              <div 
                className={styles.progressBarFill} 
                style={{ width: `${progressData.progress}%` }}
              ></div>
            </div>
            <span className={styles.progressText}>
              {progressData.progress.toFixed(0)}% Complete
            </span>
          </div>
        </div>

        <div className={styles.sectionsList}>
          {sections.map((section) => (
            <div key={section.id} className={styles.sectionBlock}>
              <div className={styles.sectionHeader} onClick={() => toggleSection(section.id)}>
                <span className={styles.sectionTitle}>{section.title}</span>
                {expandedSections[section.id] ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
              </div>

              {expandedSections[section.id] && (
                <div className={styles.lessonsList}>
                  {section.lessons.map((lesson) => (
                    <div 
                      key={lesson.id} 
                      className={`${styles.lessonItem} ${activeLesson?.id === lesson.id ? styles.activeLesson : ''}`}
                      onClick={() => setActiveLesson(lesson)}
                    >
                      <button 
                        className={`${styles.checkbox} ${isCompleted(lesson.id) ? styles.checkboxChecked : ''}`}
                        onClick={(e) => {
                          e.stopPropagation();
                          handleToggleComplete(lesson.id);
                        }}
                        aria-label={isCompleted(lesson.id) ? "Mark incomplete" : "Mark complete"}
                      />
                      <span className={styles.lessonTitleText}>{lesson.title}</span>
                      {lesson.contentType === 'VIDEO' ? <Play size={12} /> : <FileText size={12} />}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </aside>

      {/* ── Right Column: Selected Lesson Canvas ── */}
      <div className={styles.contentCanvas}>
        {activeLesson ? (
          <>
            <div className={styles.canvasHeader}>
              <div>
                <h1>{activeLesson.title}</h1>
              </div>
              <Button 
                variant={isCompleted(activeLesson.id) ? 'glass' : 'primary'}
                className={styles.completeActionBtn}
                onClick={() => handleToggleComplete(activeLesson.id)}
              >
                <CheckCircle size={16} fill={isCompleted(activeLesson.id) ? "var(--accent-green)" : "none"} />
                <span>{isCompleted(activeLesson.id) ? 'Completed ✓' : 'Mark as Complete'}</span>
              </Button>
            </div>

            {/* Video Player */}
            {activeLesson.contentType === 'VIDEO' && activeLesson.videoUrl && (
              <div className={styles.videoContainer}>
                <video 
                  src={activeLesson.videoUrl} 
                  controls 
                  className={styles.videoPlayer}
                />
              </div>
            )}

            {/* Text Reading Block */}
            <div className={styles.readingMaterial}>
              {activeLesson.content ? (
                <p>{activeLesson.content}</p>
              ) : (
                <p style={{ fontStyle: 'italic', color: 'var(--text-muted)' }}>
                  No reading content provided. Use the players above to review the lesson topics.
                </p>
              )}
            </div>

            {/* Resource Files Downloader */}
            {activeLesson.resourceUrl && (
              <div className={styles.resourcePanel}>
                <div className={styles.resourceMeta}>
                  <Download size={18} style={{ color: 'var(--accent-cyan)' }} />
                  <span>Download Course Resource File</span>
                </div>
                <a href={activeLesson.resourceUrl} download target="_blank" rel="noopener noreferrer">
                  <Button variant="secondary" style={{ fontSize: '0.8rem', padding: '6px 12px' }}>
                    Download
                  </Button>
                </a>
              </div>
            )}
          </>
        ) : (
          <div className={styles.emptyState}>
            <BookOpen size={48} />
            <h3>No Lesson Selected</h3>
            <p>Select a lesson from the left syllabus outline to begin studying.</p>
          </div>
        )}
      </div>

      <TutorSidebar courseId={courseId} />
    </div>
  );
};

export default StudyPage;
