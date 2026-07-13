'use client';

import React, { useState, useEffect, useCallback, use } from 'react';
import { useRouter } from 'next/navigation';
import withAuth from '@/components/layout/withAuth';
import { courseService } from '@/services/courseService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import { 
  ArrowLeft, 
  Settings, 
  BookOpen, 
  Plus, 
  Trash, 
  Edit, 
  Video, 
  FileText, 
  FileDown, 
  PlusCircle, 
  CheckCircle,
  AlertCircle 
} from 'lucide-react';
import styles from './page.module.css';

const CourseEditorPage = ({ params }) => {
  const unwrappedParams = use(params);
  const courseId = unwrappedParams.courseId;
  const router = useRouter();

  // Data State
  const [course, setCourse] = useState(null);
  const [categories, setCategories] = useState([]);
  const [sections, setSections] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [successBanner, setSuccessBanner] = useState('');
  const [errorBanner, setErrorBanner] = useState('');

  // Course Details Form State
  const [title, setTitle] = useState('');
  const [shortDescription, setShortDescription] = useState('');
  const [description, setDescription] = useState('');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [price, setPrice] = useState('0.00');
  const [difficulty, setDifficulty] = useState('BEGINNER');
  const [status, setStatus] = useState('DRAFT');
  const [language, setLanguage] = useState('English');
  const [categoryId, setCategoryId] = useState('');

  // Section Modal State
  const [isSectionModalOpen, setIsSectionModalOpen] = useState(false);
  const [sectionTitle, setSectionTitle] = useState('');
  const [sectionSortOrder, setSectionSortOrder] = useState(1);
  const [editingSectionId, setEditingSectionId] = useState(null);

  // Lesson Modal State
  const [isLessonModalOpen, setIsLessonModalOpen] = useState(false);
  const [activeSectionId, setActiveSectionId] = useState(null);
  const [lessonTitle, setLessonTitle] = useState('');
  const [lessonContent, setLessonContent] = useState('');
  const [lessonVideoUrl, setLessonVideoUrl] = useState('');
  const [lessonResourceUrl, setLessonResourceUrl] = useState('');
  const [lessonDuration, setLessonDuration] = useState(10);
  const [lessonSortOrder, setLessonSortOrder] = useState(1);
  const [lessonContentType, setLessonContentType] = useState('VIDEO'); // VIDEO, TEXT, PDF
  const [editingLessonId, setEditingLessonId] = useState(null);

  // Fetch Course details and Categories
  const loadData = useCallback(async () => {
    setIsLoading(true);
    try {
      // 1. Course Details
      const courseRes = await courseService.getCourseById(courseId);
      if (courseRes.success && courseRes.data) {
        const c = courseRes.data;
        setCourse(c);
        setTitle(c.title || '');
        setShortDescription(c.shortDescription || '');
        setDescription(c.description || '');
        setThumbnailUrl(c.thumbnailUrl || '');
        setPrice(c.price !== undefined ? c.price.toString() : '0.00');
        setDifficulty(c.difficulty || 'BEGINNER');
        setStatus(c.status || 'DRAFT');
        setLanguage(c.language || 'English');
        setCategoryId(c.categoryId || '');
      }

      // 2. Categories
      const categoriesRes = await courseService.getCategories();
      if (categoriesRes.success) {
        setCategories(categoriesRes.data || []);
      }

      // 3. Sections & Lessons
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
      }
    } catch (e) {
      console.error('Failed to load course details for edit:', e);
      setErrorBanner('Failed to load course data.');
    } finally {
      setIsLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // Handle Course updates
  const handleUpdateCourseDetails = async (e) => {
    e.preventDefault();
    setSuccessBanner('');
    setErrorBanner('');

    try {
      const request = {
        title,
        shortDescription,
        description,
        thumbnailUrl,
        price: parseFloat(price),
        difficulty,
        status,
        language,
        categoryId: categoryId || null
      };

      const res = await courseService.updateCourse(courseId, request);
      if (res.success) {
        setSuccessBanner('Course details updated successfully.');
        setTimeout(() => setSuccessBanner(''), 3000);
      } else {
        setErrorBanner(res.message || 'Failed to update course.');
      }
    } catch (err) {
      setErrorBanner(err.response?.data?.message || 'Error updating course details.');
    }
  };

  // Section CRUD
  const handleSaveSection = async (e) => {
    e.preventDefault();
    setIsSectionModalOpen(false);
    try {
      const requestBody = { title: sectionTitle, sortOrder: sectionSortOrder };
      
      if (editingSectionId) {
        await courseService.updateSection(courseId, editingSectionId, requestBody);
      } else {
        await courseService.createSection(courseId, requestBody);
      }

      // Reset
      setSectionTitle('');
      setSectionSortOrder(1);
      setEditingSectionId(null);
      
      // Reload Sections
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
      }
    } catch (e) {
      console.error('Section save error:', e);
      setErrorBanner('Failed to save section.');
    }
  };

  const handleEditSection = (sec) => {
    setEditingSectionId(sec.id);
    setSectionTitle(sec.title);
    setSectionSortOrder(sec.sortOrder || 1);
    setIsSectionModalOpen(true);
  };

  const handleDeleteSection = async (sectionId) => {
    if (!confirm('Are you sure you want to delete this section? All lessons inside will be lost.')) return;
    try {
      await courseService.deleteSection(courseId, sectionId);
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
      }
    } catch (e) {
      console.error('Delete section error:', e);
      setErrorBanner('Failed to delete section.');
    }
  };

  // Lesson CRUD
  const handleSaveLesson = async (e) => {
    e.preventDefault();
    setIsLessonModalOpen(false);
    try {
      const requestBody = {
        title: lessonTitle,
        content: lessonContent,
        videoUrl: lessonVideoUrl,
        resourceUrl: lessonResourceUrl,
        duration: parseInt(lessonDuration),
        sortOrder: parseInt(lessonSortOrder),
        contentType: lessonContentType
      };

      if (editingLessonId) {
        await courseService.updateLesson(courseId, activeSectionId, editingLessonId, requestBody);
      } else {
        await courseService.createLesson(courseId, activeSectionId, requestBody);
      }

      // Reset
      setLessonTitle('');
      setLessonContent('');
      setLessonVideoUrl('');
      setLessonResourceUrl('');
      setLessonDuration(10);
      setLessonSortOrder(1);
      setLessonContentType('VIDEO');
      setEditingLessonId(null);
      setActiveSectionId(null);

      // Reload
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
      }
    } catch (e) {
      console.error('Lesson save error:', e);
      setErrorBanner('Failed to save lesson.');
    }
  };

  const handleEditLesson = (secId, les) => {
    setActiveSectionId(secId);
    setEditingLessonId(les.id);
    setLessonTitle(les.title);
    setLessonContent(les.content || '');
    setLessonVideoUrl(les.videoUrl || '');
    setLessonResourceUrl(les.resourceUrl || '');
    setLessonDuration(les.duration || 10);
    setLessonSortOrder(les.sortOrder || 1);
    setLessonContentType(les.contentType || 'VIDEO');
    setIsLessonModalOpen(true);
  };

  const handleDeleteLesson = async (secId, lesId) => {
    if (!confirm('Are you sure you want to delete this lesson?')) return;
    try {
      await courseService.deleteLesson(courseId, secId, lesId);
      const sectionsRes = await courseService.getCourseSections(courseId);
      if (sectionsRes.success) {
        setSections(sectionsRes.data || []);
      }
    } catch (e) {
      console.error('Delete lesson error:', e);
      setErrorBanner('Failed to delete lesson.');
    }
  };

  const getLessonIcon = (type) => {
    switch (type) {
      case 'VIDEO': return <Video size={16} />;
      case 'PDF': return <FileDown size={16} />;
      default: return <FileText size={16} />;
    }
  };

  if (isLoading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
        <Spinner />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <section className={styles.header}>
        <div className={styles.headerLeft}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', marginBottom: '8px', color: 'var(--accent-cyan)' }} onClick={() => router.push('/instructor/courses')}>
            <ArrowLeft size={16} />
            <span>Back to Workspace</span>
          </div>
          <h1>Syllabus & Course Workspace</h1>
          <p>Edit curriculum outline structure or modify details for course: <strong>{course?.title}</strong></p>
        </div>
      </section>

      {successBanner && (
        <div style={{ background: 'rgba(16, 185, 129, 0.1)', color: 'var(--accent-green)', padding: '12px', borderRadius: 'var(--radius-md)', display: 'flex', gap: '8px', alignItems: 'center' }}>
          <CheckCircle size={18} />
          <span>{successBanner}</span>
        </div>
      )}

      {errorBanner && (
        <div style={{ background: 'rgba(239, 68, 68, 0.1)', color: 'var(--accent-pink)', padding: '12px', borderRadius: 'var(--radius-md)', display: 'flex', gap: '8px', alignItems: 'center' }}>
          <AlertCircle size={18} />
          <span>{errorBanner}</span>
        </div>
      )}

      <div className={styles.editorGrid}>
        {/* Course Details Configuration */}
        <GlassCard className={styles.formCard}>
          <h3 className={styles.sectionTitle}>
            <Settings size={20} />
            Configure Course Settings
          </h3>
          <form onSubmit={handleUpdateCourseDetails} className={styles.form}>
            <div className={styles.formGroup}>
              <label className={styles.label}>Course Title</label>
              <input 
                type="text" 
                className={styles.input}
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Short Summary</label>
              <input 
                type="text" 
                className={styles.input}
                value={shortDescription}
                onChange={(e) => setShortDescription(e.target.value)}
              />
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Detailed Description</label>
              <textarea 
                className={styles.textarea}
                rows={5}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
              />
            </div>

            <div className={styles.formRow}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Category</label>
                <select 
                  className={styles.select}
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                >
                  <option value="">Select Category</option>
                  {categories.map((c) => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Difficulty</label>
                <select 
                  className={styles.select}
                  value={difficulty}
                  onChange={(e) => setDifficulty(e.target.value)}
                >
                  <option value="BEGINNER">Beginner</option>
                  <option value="INTERMEDIATE">Intermediate</option>
                  <option value="ADVANCED">Advanced</option>
                </select>
              </div>
            </div>

            <div className={styles.formRow}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Price (USD)</label>
                <input 
                  type="number" 
                  step="0.01"
                  className={styles.input}
                  value={price}
                  onChange={(e) => setPrice(e.target.value)}
                />
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Language</label>
                <input 
                  type="text" 
                  className={styles.input}
                  value={language}
                  onChange={(e) => setLanguage(e.target.value)}
                />
              </div>
            </div>

            <div className={styles.formGroup}>
              <label className={styles.label}>Course Status</label>
              <select 
                className={styles.select}
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="DRAFT">Draft</option>
                <option value="PUBLISHED">Published</option>
                <option value="ARCHIVED">Archived</option>
              </select>
            </div>

            <Button type="submit" variant="primary" style={{ marginTop: '8px' }}>
              Save Course Settings
            </Button>
          </form>
        </GlassCard>

        {/* Syllabus / Curriculum breakdown */}
        <GlassCard className={styles.syllabusCard}>
          <div className={styles.syllabusHeader}>
            <h3 className={styles.sectionTitle} style={{ borderLeftColor: 'var(--accent-purple)', marginBottom: 0 }}>
              <BookOpen size={20} />
              Syllabus Outline
            </h3>
            <Button variant="secondary" size="small" onClick={() => { setEditingSectionId(null); setSectionTitle(''); setSectionSortOrder(sections.length + 1); setIsSectionModalOpen(true); }}>
              <Plus size={14} style={{ marginRight: '6px' }} />
              Add Section
            </Button>
          </div>

          <div style={{ marginTop: 'var(--space-md)' }}>
            {sections.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center', padding: '24px' }}>No sections added yet. Create one above to start defining lessons.</p>
            ) : (
              sections.map((section) => (
                <div key={section.id} className={styles.sectionItem}>
                  <div className={styles.sectionHeaderRow}>
                    <div className={styles.sectionInfo}>
                      <h4>{section.title}</h4>
                    </div>
                    <div className={styles.actionRow}>
                      <button style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }} onClick={() => handleEditSection(section)} title="Edit Section"><Edit size={16} /></button>
                      <button style={{ background: 'none', border: 'none', color: 'var(--accent-pink)', cursor: 'pointer' }} onClick={() => handleDeleteSection(section.id)} title="Delete Section"><Trash size={16} /></button>
                      <button style={{ background: 'none', border: 'none', color: 'var(--accent-cyan)', cursor: 'pointer', marginLeft: '8px', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '0.85rem' }} onClick={() => { setActiveSectionId(section.id); setEditingLessonId(null); setLessonTitle(''); setLessonContent(''); setLessonVideoUrl(''); setLessonResourceUrl(''); setLessonDuration(10); setLessonSortOrder((section.lessons?.length || 0) + 1); setLessonContentType('VIDEO'); setIsLessonModalOpen(true); }}><PlusCircle size={14} /> Add Lesson</button>
                    </div>
                  </div>

                  {/* Lessons inside Section */}
                  <div className={styles.lessonList}>
                    {(!section.lessons || section.lessons.length === 0) ? (
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', fontStyle: 'italic', padding: '4px' }}>No lessons added in this section.</p>
                    ) : (
                      section.lessons.map((lesson) => (
                        <div key={lesson.id} className={styles.lessonItem}>
                          <div className={styles.lessonInfo} style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            {getLessonIcon(lesson.contentType)}
                            <span>{lesson.title}</span>
                            <span className={styles.lessonMeta}>{lesson.contentType} • {lesson.duration} mins</span>
                          </div>
                          <div className={styles.actionRow}>
                            <button style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }} onClick={() => handleEditLesson(section.id, lesson)} title="Edit Lesson"><Edit size={14} /></button>
                            <button style={{ background: 'none', border: 'none', color: 'var(--accent-pink)', cursor: 'pointer' }} onClick={() => handleDeleteLesson(section.id, lesson.id)} title="Delete Lesson"><Trash size={14} /></button>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        </GlassCard>
      </div>

      {/* Section Dialog Modal */}
      {isSectionModalOpen && (
        <div className={styles.dialogOverlay}>
          <GlassCard className={styles.dialogContent}>
            <div className={styles.dialogHeader}>
              <h2>{editingSectionId ? 'Edit Section' : 'Create Section'}</h2>
              <button style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', fontSize: '1.25rem', cursor: 'pointer' }} onClick={() => setIsSectionModalOpen(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleSaveSection} className={styles.form}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Section Title *</label>
                <input 
                  type="text"
                  className={styles.input}
                  placeholder="e.g. Introduction to REST APIs"
                  value={sectionTitle}
                  onChange={(e) => setSectionTitle(e.target.value)}
                  required
                />
              </div>
              <div className={styles.formGroup}>
                <label className={styles.label}>Sorting Order (Priority) *</label>
                <input 
                  type="number"
                  className={styles.input}
                  value={sectionSortOrder}
                  onChange={(e) => setSectionSortOrder(e.target.value)}
                  required
                />
              </div>
              <div className={styles.formActions}>
                <Button type="button" variant="secondary" onClick={() => setIsSectionModalOpen(false)}>Cancel</Button>
                <Button type="submit" variant="primary">Save Section</Button>
              </div>
            </form>
          </GlassCard>
        </div>
      )}

      {/* Lesson Dialog Modal */}
      {isLessonModalOpen && (
        <div className={styles.dialogOverlay}>
          <GlassCard className={styles.dialogContent} style={{ maxWidth: '550px' }}>
            <div className={styles.dialogHeader}>
              <h2>{editingLessonId ? 'Edit Lesson' : 'Create Lesson'}</h2>
              <button style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', fontSize: '1.25rem', cursor: 'pointer' }} onClick={() => setIsLessonModalOpen(false)}><X size={18} /></button>
            </div>
            <form onSubmit={handleSaveLesson} className={styles.form}>
              <div className={styles.formGroup}>
                <label className={styles.label}>Lesson Title *</label>
                <input 
                  type="text"
                  className={styles.input}
                  placeholder="e.g. Understanding HTTP Methods"
                  value={lessonTitle}
                  onChange={(e) => setLessonTitle(e.target.value)}
                  required
                />
              </div>

              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.label}>Content Type *</label>
                  <select 
                    className={styles.select}
                    value={lessonContentType}
                    onChange={(e) => setLessonContentType(e.target.value)}
                    required
                  >
                    <option value="VIDEO">Video Lecture</option>
                    <option value="TEXT">Text Article</option>
                    <option value="PDF">PDF Slides</option>
                  </select>
                </div>

                <div className={styles.formGroup}>
                  <label className={styles.label}>Duration (Minutes) *</label>
                  <input 
                    type="number"
                    className={styles.input}
                    value={lessonDuration}
                    onChange={(e) => setLessonDuration(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className={styles.formRow}>
                <div className={styles.formGroup}>
                  <label className={styles.label}>Video URL (e.g. YouTube/Vimeo)</label>
                  <input 
                    type="text"
                    className={styles.input}
                    placeholder="https://youtube.com/..."
                    value={lessonVideoUrl}
                    onChange={(e) => setLessonVideoUrl(e.target.value)}
                  />
                </div>

                <div className={styles.formGroup}>
                  <label className={styles.label}>Attachment Resource URL</label>
                  <input 
                    type="text"
                    className={styles.input}
                    placeholder="https://drive.google.com/..."
                    value={lessonResourceUrl}
                    onChange={(e) => setLessonResourceUrl(e.target.value)}
                  />
                </div>
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Sorting Order *</label>
                <input 
                  type="number"
                  className={styles.input}
                  value={lessonSortOrder}
                  onChange={(e) => setLessonSortOrder(e.target.value)}
                  required
                />
              </div>

              <div className={styles.formGroup}>
                <label className={styles.label}>Lesson Content body (Optional)</label>
                <textarea 
                  className={styles.textarea}
                  rows={4}
                  placeholder="Enter study text content, notes, or code blocks..."
                  value={lessonContent}
                  onChange={(e) => setLessonContent(e.target.value)}
                />
              </div>

              <div className={styles.formActions}>
                <Button type="button" variant="secondary" onClick={() => setIsLessonModalOpen(false)}>Cancel</Button>
                <Button type="submit" variant="primary">Save Lesson</Button>
              </div>
            </form>
          </GlassCard>
        </div>
      )}
    </div>
  );
};

export default withAuth(['INSTRUCTOR'])(CourseEditorPage);
