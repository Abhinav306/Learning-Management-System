'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { assignmentService } from '@/services/assignmentService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { Calendar, Target, ChevronRight } from 'lucide-react';
import styles from './page.module.css';

const AssignmentsPage = () => {
  const { courseId } = useParams();
  const router = useRouter();

  const [assignments, setAssignments] = useState([]);
  const [isLoading, setIsLoading] = useState(true);

  const fetchAssignments = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await assignmentService.getCourseAssignments(courseId);
      if (res.success && res.data) {
        setAssignments(res.data);
      }
    } catch (e) {
      console.error('Failed to load course assignments:', e);
    } finally {
      setIsLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    fetchAssignments();
  }, [fetchAssignments]);

  const handleViewDetail = (id) => {
    router.push(`/courses/${courseId}/assignments/${id}`);
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
        <h1>Course Assignments</h1>
        <p>Review assignment briefs, submit your coursework, and check instructor grading reports.</p>
      </section>

      {assignments.length === 0 ? (
        <div className={styles.emptyState}>
          <h3>No Assignments Available</h3>
          <p style={{ marginTop: 'var(--space-sm)' }}>
            Your instructor has not posted any coursework assignments yet.
          </p>
        </div>
      ) : (
        <div className={styles.list}>
          {assignments.map((asm) => (
            <GlassCard key={asm.id} className={styles.card}>
              <div className={styles.assignmentMeta}>
                <h3 className={styles.title}>{asm.title}</h3>
                <div className={styles.dateRow}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Calendar size={12} />
                    Due: {new Date(asm.dueDate).toLocaleDateString()}
                  </span>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Target size={12} />
                    Max Score: {asm.maxScore} pts
                  </span>
                </div>
              </div>

              <div className={styles.statusRow}>
                <Button 
                  variant="secondary" 
                  className={styles.viewBtn}
                  onClick={() => handleViewDetail(asm.id)}
                >
                  <span>Submit / View</span>
                  <ChevronRight size={14} style={{ marginLeft: '4px', verticalAlign: 'middle' }} />
                </Button>
              </div>
            </GlassCard>
          ))}
        </div>
      )}
    </div>
  );
};

export default AssignmentsPage;
