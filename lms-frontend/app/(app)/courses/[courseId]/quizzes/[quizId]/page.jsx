'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { quizService } from '@/services/quizService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { Clock, CheckCircle2, XCircle, ArrowLeft, AlertCircle } from 'lucide-react';
import styles from './page.module.css';

const ActiveQuizPage = () => {
  const { courseId, quizId } = useParams();
  const router = useRouter();

  const [quiz, setQuiz] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isAttempting, setIsAttempting] = useState(false);
  const [attempt, setAttempt] = useState(null);
  const [timeLeft, setTimeLeft] = useState(0);
  const [selectedAnswers, setSelectedAnswers] = useState({}); // { [questionId]: "optionText" }
  const [result, setResult] = useState(null);
  const [errorMsg, setErrorMsg] = useState('');
  
  const timerRef = useRef(null);

  // Load quiz details on mount
  useEffect(() => {
    const loadQuiz = async () => {
      try {
        const res = await quizService.getQuizDetail(quizId);
        if (res.success) {
          setQuiz(res.data);
        }
      } catch (e) {
        console.error('Failed to load quiz details:', e);
      } finally {
        setIsLoading(false);
      }
    };
    loadQuiz();
  }, [quizId]);

  // Clean up timer on unmount
  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const handleStartAttempt = async () => {
    setErrorMsg('');
    setIsLoading(true);
    try {
      const res = await quizService.startAttempt(quizId);
      if (res.success && res.data) {
        setAttempt(res.data);
        setIsAttempting(true);
        setTimeLeft((quiz.timeLimit || 20) * 60); // Set time limit in seconds
        
        // Start countdown timer
        startTimer();
      }
    } catch (e) {
      setErrorMsg(e.response?.data?.message || 'Failed to start quiz attempt. You may have exceeded your maximum attempts.');
    } finally {
      setIsLoading(false);
    }
  };

  // Submit answers logic
  const handleSubmit = useCallback(async (isAutoSubmit = false) => {
    if (timerRef.current) clearInterval(timerRef.current);
    setIsLoading(true);
    setErrorMsg('');

    try {
      const submission = {
        answers: selectedAnswers // Matches Map<UUID, String> backend format
      };

      const res = await quizService.submitAttempt(quizId, attempt.attemptId, submission);
      if (res.success && res.data) {
        setResult(res.data);
        setIsAttempting(false);
      }
    } catch (e) {
      setErrorMsg(e.response?.data?.message || 'Failed to submit quiz attempt.');
    } finally {
      setIsLoading(false);
    }
  }, [quizId, attempt, selectedAnswers]);

  // Timer runner
  const startTimer = () => {
    timerRef.current = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timerRef.current);
          // Auto-submit when time expires
          handleSubmit(true);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const handleOptionSelect = (questionId, option, isMulti = false) => {
    if (isMulti) {
      // Multiple Choice selection handles comma-joined strings
      const current = selectedAnswers[questionId] ? selectedAnswers[questionId].split(',') : [];
      let next;
      if (current.includes(option)) {
        next = current.filter((o) => o !== option);
      } else {
        next = [...current, option];
      }
      setSelectedAnswers({
        ...selectedAnswers,
        [questionId]: next.join(',')
      });
    } else {
      setSelectedAnswers({
        ...selectedAnswers,
        [questionId]: option
      });
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spinner />
      </div>
    );
  }

  // 1. Result Dashboard Screen
  if (result) {
    const percentage = result.totalPoints > 0 ? (result.score / result.totalPoints) * 100 : 0;
    
    return (
      <div className={styles.quizCanvas}>
        <GlassCard className={styles.resultHeader}>
          <div className={`${styles.scoreCircle} ${result.passed ? styles.passScore : styles.failScore}`}>
            <span className={styles.percent}>{percentage.toFixed(0)}%</span>
            <span className={styles.label}>Score</span>
          </div>

          <span className={`${styles.badgeResult} ${result.passed ? styles.passBadge : styles.failBadge}`}>
            {result.passed ? 'PASSED ✓' : 'FAILED ✗'}
          </span>

          <h2 style={{ marginTop: 'var(--space-sm)' }}>Quiz Evaluation Report</h2>
          <p style={{ color: 'var(--text-secondary)' }}>
            You scored {result.score} out of {result.totalPoints} total points.
          </p>
        </GlassCard>

        {/* Question-by-Question Review List */}
        <div className={styles.questionList}>
          {quiz.questions.map((q, idx) => {
            // Find graded feedback details for this question
            const answerFeedback = result.answers ? result.answers.find((ans) => ans.questionId === q.id) : null;
            const isQCorrect = answerFeedback ? answerFeedback.correct : false;

            return (
              <GlassCard 
                key={q.id} 
                className={`${styles.reviewQuestionCard} ${isQCorrect ? styles.correctReview : styles.incorrectReview}`}
              >
                <div className={styles.resultQuestionHeader}>
                  <span className={styles.questionText}>
                    Question {idx + 1}: {q.questionText}
                  </span>
                  <span className={`${styles.qStatusText} ${isQCorrect ? styles.qCorrect : styles.qIncorrect}`}>
                    {isQCorrect ? 'CORRECT' : 'INCORRECT'}
                  </span>
                </div>

                <div className={styles.optionsGroup}>
                  {q.options && q.options.map((opt) => {
                    const isSelected = selectedAnswers[q.id]?.split(',').includes(opt);
                    const isCorrectOption = answerFeedback?.correctAnswer?.split(',').includes(opt);

                    let optionClass = styles.optionReviewLabel;
                    if (isCorrectOption) optionClass += ` ${styles.optCorrect}`;
                    else if (isSelected && !isCorrectOption) optionClass += ` ${styles.optIncorrect}`;

                    return (
                      <div key={opt} className={optionClass}>
                        <input
                          type={q.type === 'MULTIPLE_CHOICE' ? 'checkbox' : 'radio'}
                          className={q.type === 'MULTIPLE_CHOICE' ? styles.checkbox : styles.radio}
                          checked={isSelected}
                          disabled
                        />
                        <span>{opt}</span>
                      </div>
                    );
                  })}
                </div>

                {/* Explanation text block */}
                {answerFeedback?.explanation && (
                  <div className={styles.explanationBox}>
                    <strong>Explanation:</strong> {answerFeedback.explanation}
                  </div>
                )}
              </GlassCard>
            );
          })}
        </div>

        <Button 
          variant="secondary" 
          className={styles.backBtn}
          onClick={() => router.push(`/courses/${courseId}/quizzes`)}
        >
          <ArrowLeft size={16} style={{ marginRight: '8px' }} />
          Back to Quizzes list
        </Button>
      </div>
    );
  }

  // 2. Active Test Questionnaire Screen
  if (isAttempting && attempt) {
    const isWarnTime = timeLeft < 60; // Warn color if under 1 minute remaining

    return (
      <div className={styles.quizCanvas}>
        <div className={styles.timerBanner}>
          <span>Remaining Time:</span>
          <span className={`${styles.timer} ${isWarnTime ? styles.timerWarning : ''}`}>
            <Clock size={16} style={{ display: 'inline', marginRight: '6px', verticalAlign: 'text-bottom' }} />
            {formatTime(timeLeft)}
          </span>
        </div>

        <div className={styles.questionList}>
          {quiz.questions && quiz.questions.map((q, idx) => {
            const isMulti = q.type === 'MULTIPLE_CHOICE';

            return (
              <GlassCard key={q.id} className={styles.questionCard}>
                <span className={styles.questionText}>
                  Question {idx + 1}: {q.questionText}
                </span>

                {q.type === 'SHORT_ANSWER' ? (
                  <input
                    type="text"
                    className={styles.textInput}
                    placeholder="Type your answer here..."
                    value={selectedAnswers[q.id] || ''}
                    onChange={(e) => handleOptionSelect(q.id, e.target.value)}
                  />
                ) : (
                  <div className={styles.optionsGroup}>
                    {q.options && q.options.map((opt) => {
                      const isSelected = selectedAnswers[q.id]?.split(',').includes(opt);
                      
                      return (
                        <label 
                          key={opt} 
                          className={`${styles.optionLabel} ${isSelected ? styles.optionSelected : ''}`}
                        >
                          <input
                            type={isMulti ? 'checkbox' : 'radio'}
                            name={q.id}
                            className={isMulti ? styles.checkbox : styles.radio}
                            checked={isSelected}
                            onChange={() => handleOptionSelect(q.id, opt, isMulti)}
                          />
                          <span>{opt}</span>
                        </label>
                      );
                    })}
                  </div>
                )}
              </GlassCard>
            );
          })}
        </div>

        <Button 
          variant="primary" 
          className={styles.submitBtn}
          onClick={() => handleSubmit(false)}
        >
          Submit Quiz Attempt
        </Button>
      </div>
    );
  }

  // 3. Start Warning / Briefing Screen
  return (
    <div className={styles.quizCanvas}>
      <GlassCard className={styles.introCard}>
        <h2>{quiz.title}</h2>
        <p style={{ color: 'var(--text-secondary)', maxWidth: '580px' }}>
          {quiz.description || 'Review the time limits and passing criteria before beginning this assessment attempt.'}
        </p>

        {errorMsg && (
          <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem', marginTop: '4px' }}>
            <AlertCircle size={16} />
            <span>{errorMsg}</span>
          </div>
        )}

        <div className={styles.introMeta}>
          <span>Passing Score: <strong>{quiz.passingScore}%</strong></span>
          <span>Time Limit: <strong>{quiz.timeLimit} minutes</strong></span>
        </div>

        <Button variant="primary" className={styles.beginBtn} onClick={handleStartAttempt}>
          Begin Assessment
        </Button>
      </GlassCard>
    </div>
  );
};

export default ActiveQuizPage;
