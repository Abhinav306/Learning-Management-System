'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { assignmentService } from '@/services/assignmentService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { 
  Calendar, 
  Target, 
  UploadCloud, 
  FileText, 
  X, 
  ExternalLink,
  CheckCircle,
  AlertCircle,
  ArrowLeft
} from 'lucide-react';
import styles from './page.module.css';

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB limit matching backend config
const ALLOWED_TYPES = [
  'application/pdf', 
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
  'text/plain'
];

const AssignmentDetailPage = () => {
  const { courseId, assignmentId } = useParams();
  const router = useRouter();

  const [assignment, setAssignment] = useState(null);
  const [submission, setSubmission] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  // Form submit state
  const [content, setContent] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragActive, setIsDragActive] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fileInputRef = useRef(null);

  const loadData = useCallback(async () => {
    setIsLoading(true);
    try {
      const assignmentRes = await assignmentService.getAssignmentDetail(assignmentId);
      if (assignmentRes.success) {
        setAssignment(assignmentRes.data);
      }

      // Check if student has already submitted
      try {
        const subRes = await assignmentService.getMySubmission(assignmentId);
        if (subRes.success && subRes.data) {
          setSubmission(subRes.data);
        }
      } catch (e) {
        // 404 is expected if student hasn't submitted yet
        setSubmission(null);
      }
    } catch (e) {
      console.error('Failed to load assignment details:', e);
    } finally {
      setIsLoading(false);
    }
  }, [assignmentId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Drag & drop handlers
  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setIsDragActive(true);
    } else if (e.type === 'dragleave') {
      setIsDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragActive(false);
    setErrorMsg('');

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    setErrorMsg('');
    if (e.target.files && e.target.files[0]) {
      validateAndSelectFile(e.target.files[0]);
    }
  };

  const validateAndSelectFile = (file) => {
    if (file.size > MAX_FILE_SIZE) {
      setErrorMsg('File size exceeds the maximum allowed 5MB limit.');
      return;
    }
    if (!ALLOWED_TYPES.includes(file.type)) {
      setErrorMsg('Invalid file type. Only PDF, DOCX, and TXT files are allowed.');
      return;
    }
    setSelectedFile(file);
  };

  const removeSelectedFile = () => {
    setSelectedFile(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setIsSubmitting(true);

    try {
      let fileUrl = '';
      
      // Upload attachment first if selected
      if (selectedFile) {
        const uploadRes = await assignmentService.uploadAttachment(selectedFile);
        if (uploadRes.success && uploadRes.data) {
          fileUrl = uploadRes.data.fileUrl;
        } else {
          throw new Error('File upload failed');
        }
      }

      // Submit assignment coursework
      const submissionData = {
        content: content || null,
        fileUrl: fileUrl || null
      };

      const res = await assignmentService.submitAssignment(assignmentId, submissionData);
      if (res.success && res.data) {
        setSubmission(res.data);
      }
    } catch (err) {
      setErrorMsg(err.response?.data?.message || 'Failed to submit coursework.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const openFileSelector = () => {
    fileInputRef.current?.click();
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <Spinner />
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className={styles.assignmentCanvas} style={{ textAlign: 'center', padding: 'var(--space-2xl) 0' }}>
        <GlassCard>
          <AlertCircle size={40} style={{ color: 'var(--accent-pink)', marginBottom: 'var(--space-sm)' }} />
          <h3>Assignment Brief Not Found</h3>
          <p>The coursework brief details could not be retrieved from the database.</p>
        </GlassCard>
      </div>
    );
  }

  const getStatusBadgeClass = (status) => {
    switch (status) {
      case 'GRADED': return styles.graded;
      case 'SUBMITTED': return styles.submitted;
      case 'LATE': return styles.late;
      default: return styles.submitted;
    }
  };

  return (
    <div className={styles.assignmentCanvas}>
      
      {/* ── Brief Summary Container ── */}
      <GlassCard className={styles.briefCard}>
        <h2>{assignment.title}</h2>
        <div className={styles.introMeta}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Calendar size={14} />
            Due: {new Date(assignment.dueDate).toLocaleDateString()}
          </span>
          <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Target size={14} />
            Points Limit: {assignment.maxScore}
          </span>
        </div>

        <h3>Description & Guidelines</h3>
        <p className={styles.bodyText} style={{ marginTop: 'var(--space-xs)' }}>
          {assignment.description}
        </p>

        {assignment.instructions && (
          <>
            <h3 style={{ marginTop: 'var(--space-md)' }}>Submission Instructions</h3>
            <p className={styles.bodyText} style={{ marginTop: 'var(--space-xs)' }}>
              {assignment.instructions}
            </p>
          </>
        )}
      </GlassCard>

      {/* ── Submissions Layer ── */}
      {submission ? (
        // ── Submitted Dashboard Screen ──
        <div style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-lg)' }}>
          <GlassCard>
            <div className={styles.submissionMeta}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <CheckCircle size={18} style={{ color: 'var(--accent-green)' }} />
                Coursework Submitted
              </h3>
              <span className={`${styles.badge} ${getStatusBadgeClass(submission.status)}`}>
                {submission.status}
              </span>
            </div>

            <div className={styles.submissionBody}>
              <span className={styles.label}>Submission Time:</span>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                {new Date(submission.submittedAt).toLocaleString()}
              </p>

              {submission.content && (
                <>
                  <span className={styles.label} style={{ marginTop: 'var(--space-xs)' }}>Submitted Content:</span>
                  <div className={styles.contentBlock}>{submission.content}</div>
                </>
              )}

              {submission.fileUrl && (
                <>
                  <span className={styles.label} style={{ marginTop: 'var(--space-xs)' }}>Coursework Attachment:</span>
                  <div>
                    <a href={submission.fileUrl} target="_blank" rel="noopener noreferrer" className={styles.attachmentLink}>
                      <FileText size={16} />
                      <span>View Uploaded Document</span>
                      <ExternalLink size={12} />
                    </a>
                  </div>
                </>
              )}
            </div>
          </GlassCard>

          {/* Teacher Grading Feedback Block */}
          {submission.status === 'GRADED' && (
            <GlassCard className={styles.feedbackCard}>
              <div className={styles.feedbackTitle}>
                <span>Grading Report Details</span>
                <span className={styles.scoreVal}>
                  Score: {submission.grade} / {assignment.maxScore} pts
                </span>
              </div>
              <p className={styles.feedbackBody}>
                <strong>Feedback:</strong> {submission.feedback || 'No comments provided.'}
              </p>
              <span className={styles.evalBy}>
                Graded on {new Date(submission.gradedAt).toLocaleDateString()} by {submission.gradedBy || 'Instructor'}
              </span>
            </GlassCard>
          )}
        </div>
      ) : (
        // ── Incomplete Form Upload Screen ──
        <form onSubmit={handleFormSubmit} className={styles.submitForm}>
          <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
            <h3>Coursework Submission Form</h3>
            
            {errorMsg && (
              <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
                <AlertCircle size={16} />
                <span>{errorMsg}</span>
              </div>
            )}

            <div className={styles.submitForm} style={{ gap: 'var(--space-xs)' }}>
              <span className={styles.label}>Notes / Comments</span>
              <textarea
                className={styles.textarea}
                placeholder="Type any submission notes for the instructor here..."
                value={content}
                onChange={(e) => setContent(e.target.value)}
              />
            </div>

            <div className={styles.submitForm} style={{ gap: 'var(--space-xs)' }}>
              <span className={styles.label}>Assignment File Attachment</span>
              
              {selectedFile ? (
                <div className={styles.selectedFileCard}>
                  <div className={styles.fileMeta}>
                    <FileText size={18} style={{ color: 'var(--accent-cyan)' }} />
                    <span>{selectedFile.name} ({(selectedFile.size / 1024).toFixed(0)} KB)</span>
                  </div>
                  <span className={styles.removeFileBtn} onClick={removeSelectedFile}>
                    <X size={16} />
                  </span>
                </div>
              ) : (
                <div 
                  className={`${styles.dropzone} ${isDragActive ? styles.dropzoneHover : ''}`}
                  onDragEnter={handleDrag}
                  onDragOver={handleDrag}
                  onDragLeave={handleDrag}
                  onDrop={handleDrop}
                  onClick={openFileSelector}
                >
                  <UploadCloud size={32} style={{ color: 'var(--text-muted)' }} />
                  <span className={styles.dropzoneText}>
                    Drag & drop files here or <strong>browse folders</strong>
                  </span>
                  <span className={styles.dropzoneSub}>
                    Supports PDF, DOCX, TXT files up to 5MB maximum limit.
                  </span>
                  <input
                    type="file"
                    ref={fileInputRef}
                    className={styles.fileInput}
                    onChange={handleFileChange}
                    accept=".pdf,.docx,.txt"
                  />
                </div>
              )}
            </div>

            <Button 
              type="submit" 
              variant="primary" 
              className={styles.submitBtn} 
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Uploading & Submitting...' : 'Submit Coursework'}
            </Button>
          </GlassCard>
        </form>
      )}

      <Button 
        variant="secondary" 
        className={styles.backBtn}
        onClick={() => router.push(`/courses/${courseId}/assignments`)}
      >
        <ArrowLeft size={16} style={{ marginRight: '8px' }} />
        Back to Assignments list
      </Button>
    </div>
  );
};

export default AssignmentDetailPage;
