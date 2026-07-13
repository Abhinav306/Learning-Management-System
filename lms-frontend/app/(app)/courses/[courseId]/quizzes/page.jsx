'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { quizService } from '@/services/quizService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { Sparkles, Clock, Target, Award, Play } from 'lucide-react';
import styles from './page.module.css';

const QuizzesPage = () => {
  const { courseId } = useParams();
  const router = useRouter();
  
  const [quizzes, setQuizzes] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchQuizzes = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await quizService.getQuizzesByCourse(courseId);
      if (res.success && res.data) {
        setQuizzes(res.data);
      }
    } catch (e) {
      console.error('Failed to load course quizzes:', e);
    } finally {
      setIsLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    fetchQuizzes();
  }, [fetchQuizzes]);

  const handleStartAttempt = (quizId) => {
    router.push(`/courses/${courseId}/quizzes/${quizId}`);
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className={styles.quizzesContainer}>
      <section className={styles.header}>
        <h1>Course Quizzes & Assessments</h1>
        <p>Complete auto-graded quizzes to test your understanding of lesson content.</p>
      </section>

      {quizzes.length === 0 ? (
        <div className={styles.emptyState}>
          <h3>No Quizzes Available</h3>
          <p style={{ marginTop: 'var(--space-sm)' }}>
            Your instructor has not posted any assessments for this course yet.
          </p>
        </div>
      ) : (
        <div className={styles.grid}>
          {quizzes.map((quiz) => (
            <GlassCard key={quiz.id} className={styles.quizCard} hoverable>
              <div className={styles.quizHeader}>
                <h3 className={styles.quizTitle}>{quiz.title}</h3>
                {quiz.aiGenerated && (
                  <span className={`${styles.badge} ${styles.aiBadge}`}>
                    <Sparkles size={10} fill="currentColor" />
                    AI Gen
                  </span>
                )}
              </div>

              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                {quiz.description || 'No description provided.'}
              </p>

              <div className={styles.metaGrid}>
                <div className={styles.metaItem}>
                  <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Clock size={12} />
                    {quiz.timeLimit}m
                  </span>
                  <span className={styles.attemptsText}>Time Limit</span>
                </div>
                <div className={styles.metaItem}>
                  <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Target size={12} />
                    {quiz.passingScore}%
                  </span>
                  <span className={styles.attemptsText}>Passing Score</span>
                </div>
                <div className={styles.metaItem}>
                  <span className={styles.metaVal} style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Award size={12} />
                    {quiz.questions ? quiz.questions.length : 0}
                  </span>
                  <span className={styles.attemptsText}>Questions</span>
                </div>
                <div className={styles.metaItem}>
                  <span className={styles.metaVal}>
                    {quiz.maxAttempts || 'Unlimited'}
                  </span>
                  <span className={styles.attemptsText}>Max Attempts</span>
                </div>
              </div>

              <div className={styles.cardFooter}>
                <Button 
                  variant="primary" 
                  className={styles.startBtn}
                  onClick={() => handleStartAttempt(quiz.id)}
                >
                  <Play size={12} fill="currentColor" style={{ marginRight: '4px' }} />
                  Start Quiz
                </Button>
              </div>
            </GlassCard>
          ))}
        </div>
      )}
    </div>
  );
};

export default QuizzesPage;
