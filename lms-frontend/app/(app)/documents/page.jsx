'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useAuth } from '@/context/AuthContext';
import { ragService } from '@/services/ragService';
import GlassCard from '@/components/ui/GlassCard';
import Button from '@/components/ui/Button';
import Spinner from '@/components/ui/Spinner';
import RagSourceCard from '@/components/ai/RagSourceCard';
import { 
  FileText, 
  UploadCloud, 
  Trash2, 
  Search, 
  Sparkles, 
  Plus, 
  AlertCircle,
  FileCheck2,
  Trash
} from 'lucide-react';
import styles from './page.module.css';

const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB limit matching backend config
const ALLOWED_TYPES = [
  'application/pdf', 
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
  'text/plain'
];

const DocumentsPage = () => {
  const { user, role } = useAuth();
  
  const [activeTab, setActiveTab] = useState('query'); // 'query' or 'ingest'
  const [documents, setDocuments] = useState([]);
  const [isLoadingDocs, setIsLoadingDocs] = useState(false);
  
  // Upload States
  const [selectedFile, setSelectedFile] = useState(null);
  const [isDragActive, setIsDragActive] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState('');
  
  // Query States
  const [query, setQuery] = useState('');
  const [isQuerying, setIsQuerying] = useState(false);
  const [queryResult, setQueryResult] = useState(null);
  const [queryError, setQueryError] = useState('');

  const fileInputRef = useRef(null);

  const fetchDocuments = useCallback(async () => {
    setIsLoadingDocs(true);
    try {
      const res = await ragService.listDocuments();
      if (res.success && res.data) {
        setDocuments(res.data);
      }
    } catch (e) {
      console.error('Failed to load documents list:', e);
    } finally {
      setIsLoadingDocs(false);
    }
  }, []);

  useEffect(() => {
    fetchDocuments();
  }, [fetchDocuments]);

  // Tab switcher (only let instructors/admins access ingest tab)
  const handleTabChange = (tab) => {
    if (tab === 'ingest' && role === 'STUDENT') return;
    setActiveTab(tab);
    setUploadError('');
    setQueryError('');
  };

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
    setUploadError('');

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      validateAndSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileChange = (e) => {
    setUploadError('');
    if (e.target.files && e.target.files[0]) {
      validateAndSelectFile(e.target.files[0]);
    }
  };

  const validateAndSelectFile = (file) => {
    if (file.size > MAX_FILE_SIZE) {
      setUploadError('File size exceeds the maximum allowed 5MB limit.');
      return;
    }
    if (!ALLOWED_TYPES.includes(file.type)) {
      setUploadError('Invalid file type. Only PDF, DOCX, and TXT files are allowed.');
      return;
    }
    setSelectedFile(file);
  };

  const removeSelectedFile = () => {
    setSelectedFile(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handleUploadSubmit = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;

    setUploadError('');
    setIsUploading(true);

    try {
      const res = await ragService.uploadDocument(selectedFile);
      if (res.success) {
        setSelectedFile(null);
        fetchDocuments(); // Refresh documents list
      }
    } catch (err) {
      setUploadError(err.response?.data?.message || 'Failed to upload and ingest document.');
    } finally {
      setIsUploading(false);
    }
  };

  const handleDeleteDoc = async (id, filename) => {
    if (confirm(`Are you sure you want to delete "${filename}"? All associated vector chunks will be permanently removed.`)) {
      try {
        const res = await ragService.deleteDocument(id);
        if (res.success) {
          fetchDocuments(); // Refresh list
        }
      } catch (e) {
        console.error('Failed to delete document:', e);
      }
    }
  };

  const handleQuerySubmit = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setQueryError('');
    setIsQuerying(true);
    setQueryResult(null);

    try {
      const res = await ragService.queryRag(query.trim());
      if (res.success && res.data) {
        setQueryResult(res.data);
      }
    } catch (err) {
      setQueryError(err.response?.data?.message || 'Failed to perform semantic document query.');
    } finally {
      setIsQuerying(false);
    }
  };

  const openFileSelector = () => {
    fileInputRef.current?.click();
  };

  const isInstructorOrAdmin = role === 'INSTRUCTOR' || role === 'ADMIN';

  return (
    <div className={styles.container}>
      <section className={styles.header}>
        <h1>AI RAG Knowledgebase</h1>
        <p>Upload textbooks or slides and perform context-aware semantic searches across the syllabus contents.</p>
      </section>

      {/* Tabs Selector Bar */}
      <div className={styles.tabsRow}>
        <button 
          className={`${styles.tabBtn} ${activeTab === 'query' ? styles.activeTab : ''}`}
          onClick={() => handleTabChange('query')}
        >
          Semantic Query Console
        </button>
        {isInstructorOrAdmin && (
          <button 
            className={`${styles.tabBtn} ${activeTab === 'ingest' ? styles.activeTab : ''}`}
            onClick={() => handleTabChange('ingest')}
          >
            Document Ingestion Manager
          </button>
        )}
      </div>

      {/* ── Tab 1: Semantic Query Console ── */}
      {activeTab === 'query' && (
        <div className={styles.searchConsole}>
          <form onSubmit={handleQuerySubmit} className={styles.searchBar}>
            <input
              type="text"
              className={styles.searchInput}
              placeholder="Ask a question about the study materials (e.g. 'What is the runtime of bubblesort?')..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              required
            />
            <Button type="submit" variant="primary" className={styles.queryBtn} disabled={isQuerying}>
              {isQuerying ? 'Searching...' : 'Search'}
            </Button>
          </form>

          {queryError && (
            <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
              <AlertCircle size={16} />
              <span>{queryError}</span>
            </div>
          )}

          {isQuerying && (
            <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-2xl) 0' }}>
              <Spinner />
            </div>
          )}

          {queryResult && (
            <GlassCard className={styles.answerCard}>
              <div className={styles.answerTitle}>
                <Sparkles size={18} style={{ color: 'var(--accent-purple)' }} />
                <span>AI Generated Synthesis</span>
              </div>
              <p className={styles.answerText}>{queryResult.answer}</p>
              
              {queryResult.sources && queryResult.sources.length > 0 && (
                <>
                  <div className={styles.sourcesTitle}>Source Citations</div>
                  <div className={styles.sourcesGrid}>
                    {queryResult.sources.map((src, idx) => (
                      <RagSourceCard key={idx} source={src} />
                    ))}
                  </div>
                </>
              )}
            </GlassCard>
          )}

          {!queryResult && !isQuerying && (
            <div className={styles.emptyState}>
              <Search size={32} style={{ color: 'var(--text-muted)' }} />
              <h3>Semantic Query Engine</h3>
              <p>Type a question above to perform contextual RAG searches across the ingested database books.</p>
            </div>
          )}
        </div>
      )}

      {/* ── Tab 2: Document Ingestion Manager ── */}
      {activeTab === 'ingest' && isInstructorOrAdmin && (
        <div className={styles.panel}>
          <div className={styles.splitLayout}>
            {/* Upload Zone */}
            <form onSubmit={handleUploadSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
              <GlassCard style={{ display: 'flex', flexDirection: 'column', gap: 'var(--space-md)' }}>
                <h3>Upload & Ingest Document</h3>
                
                {uploadError && (
                  <div style={{ color: 'var(--accent-pink)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}>
                    <AlertCircle size={16} />
                    <span>{uploadError}</span>
                  </div>
                )}

                {selectedFile ? (
                  <div className={styles.docItem} style={{ border: '1px solid var(--border-color)' }}>
                    <div className={styles.docMeta}>
                      <FileText size={18} style={{ color: 'var(--accent-cyan)' }} />
                      <span className={styles.docTitle}>{selectedFile.name}</span>
                    </div>
                    <span style={{ color: 'var(--accent-pink)', cursor: 'pointer' }} onClick={removeSelectedFile}>
                      <Trash size={16} />
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
                    <span className={styles.dropzoneTitle}>Drag & drop textbook files here</span>
                    <span className={styles.dropzoneSub}>Supports PDF, DOCX, TXT files up to 5MB.</span>
                    <input
                      type="file"
                      ref={fileInputRef}
                      className={styles.fileInput}
                      onChange={handleFileChange}
                      accept=".pdf,.docx,.txt"
                    />
                  </div>
                )}

                <Button type="submit" variant="primary" disabled={isUploading || !selectedFile}>
                  {isUploading ? 'Chunking & Ingesting...' : 'Ingest Document'}
                </Button>
              </GlassCard>
            </form>

            {/* Ingested Documents List */}
            <div>
              <h3 className={styles.fileListHeader}>Ingested Knowledgebase</h3>
              {isLoadingDocs ? (
                <div style={{ display: 'flex', justifyContent: 'center', padding: 'var(--space-lg) 0' }}>
                  <Spinner />
                </div>
              ) : documents.length === 0 ? (
                <div className={styles.emptyState}>
                  <AlertCircle size={24} style={{ color: 'var(--text-muted)' }} />
                  <p style={{ marginTop: 'var(--space-xs)' }}>No textbooks have been ingested yet.</p>
                </div>
              ) : (
                <div className={styles.documentsList}>
                  {documents.map((doc) => (
                    <div key={doc.id} className={styles.docItem}>
                      <div className={styles.docMeta}>
                        <FileText size={20} className={styles.docIcon} />
                        <div>
                          <span className={styles.docTitle}>{doc.filename}</span>
                          <div className={styles.docSub}>
                            <span>{(doc.size / 1024).toFixed(0)} KB</span>
                            <span style={{ margin: '0 6px' }}>•</span>
                            <span>{doc.chunkCount} Vector Chunks</span>
                          </div>
                        </div>
                      </div>
                      <button 
                        className={styles.deleteBtn}
                        onClick={() => handleDeleteDoc(doc.id, doc.filename)}
                        title="Delete Document Chunks"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default DocumentsPage;
