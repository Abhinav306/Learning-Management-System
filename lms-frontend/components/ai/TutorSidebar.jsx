'use client';

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useSSE } from '@/hooks/useSSE';
import { aiTutorService } from '@/services/aiTutorService';
import Spinner from '../ui/Spinner';
import { 
  Sparkles, 
  Send, 
  MessageSquare, 
  Plus, 
  ChevronLeft, 
  ChevronRight, 
  Bot,
  AlertCircle
} from 'lucide-react';
import styles from './TutorSidebar.module.css';

const TutorSidebar = ({ courseId }) => {
  const [isOpen, setIsOpen] = useState(true);
  const [sessions, setSessions] = useState([]);
  const [activeSession, setActiveSession] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isGenerating, setIsGenerating] = useState(false);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);

  const chatEndRef = useRef(null);
  const { streamMessage } = useSSE();

  // Scroll to bottom helper
  const scrollToBottom = () => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // Load chat sessions for this course
  const loadSessions = useCallback(async () => {
    try {
      const res = await aiTutorService.getUserSessions(courseId);
      if (res.success && res.data) {
        setSessions(res.data);
        if (res.data.length > 0) {
          // Select the latest session
          setActiveSession(res.data[0]);
        } else {
          // Auto-create a default session if none exists
          handleCreateSession();
        }
      }
    } catch (e) {
      console.error('Failed to load tutor sessions:', e);
    }
  }, [courseId]);

  useEffect(() => {
    if (courseId) {
      loadSessions();
    }
  }, [courseId, loadSessions]);

  // Load message logs of active session
  useEffect(() => {
    if (!activeSession) return;

    const loadMessages = async () => {
      setIsLoadingHistory(true);
      try {
        const res = await aiTutorService.getSessionMessages(activeSession.id);
        if (res.success && res.data) {
          setMessages(res.data);
        }
      } catch (e) {
        console.error('Failed to fetch session messages:', e);
      } finally {
        setIsLoadingHistory(false);
      }
    };
    loadMessages();
  }, [activeSession]);

  const handleCreateSession = async () => {
    try {
      const title = `Session #${sessions.length + 1}`;
      const res = await aiTutorService.createSession(courseId, title);
      if (res.success && res.data) {
        setSessions((prev) => [res.data, ...prev]);
        setActiveSession(res.data);
      }
    } catch (e) {
      console.error('Failed to create new tutor session:', e);
    }
  };

  const handleSessionChange = (e) => {
    const session = sessions.find((s) => s.id === e.target.value);
    if (session) setActiveSession(session);
  };

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || !activeSession || isGenerating) return;

    const prompt = inputMessage.trim();
    setInputMessage('');
    setIsGenerating(true);

    // 1. Append user message locally
    const userMsg = {
      id: `user-${Date.now()}`,
      role: 'USER',
      content: prompt,
      createdAt: new Date().toISOString()
    };
    setMessages((prev) => [...prev, userMsg]);

    // 2. Append empty AI generating bubble placeholder
    const aiMsgId = `ai-${Date.now()}`;
    const aiPlaceholder = {
      id: aiMsgId,
      role: 'ASSISTANT',
      content: '',
      createdAt: new Date().toISOString(),
      generating: true
    };
    setMessages((prev) => [...prev, aiPlaceholder]);

    // 3. Initiate SSE Streaming
    try {
      await streamMessage(activeSession.id, prompt, {
        onChunk: (token) => {
          // Update the AI placeholder message content chunk-by-chunk
          setMessages((prev) => 
            prev.map((msg) => 
              msg.id === aiMsgId 
                ? { ...msg, content: msg.content + token } 
                : msg
            )
          );
        },
        onDone: () => {
          setIsGenerating(false);
          setMessages((prev) => 
            prev.map((msg) => 
              msg.id === aiMsgId 
                ? { ...msg, generating: false } 
                : msg
            )
          );
        },
        onError: (err) => {
          setIsGenerating(false);
          setMessages((prev) => 
            prev.map((msg) => 
              msg.id === aiMsgId 
                ? { 
                    ...msg, 
                    content: 'Error: Failed to stream AI tutor response. Please check your backend connection.', 
                    generating: false,
                    error: true 
                  } 
                : msg
            )
          );
        }
      });
    } catch (err) {
      setIsGenerating(false);
    }
  };

  return (
    <div className={`${styles.sidebar} ${!isOpen ? styles.collapsed : ''}`}>
      {/* Slide Drawer Toggle Trigger */}
      <button className={styles.toggleBtn} onClick={() => setIsOpen(!isOpen)} aria-label="Toggle AI Tutor">
        {isOpen ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
      </button>

      {isOpen && (
        <>
          <div className={styles.header}>
            <span className={styles.headerTitle}>
              <Sparkles size={16} style={{ color: 'var(--accent-purple)' }} />
              AI Tutor Chat
            </span>
          </div>

          {/* Session Switcher Row */}
          <div className={styles.sessionSelector}>
            <select 
              className={styles.selectInput}
              value={activeSession?.id || ''}
              onChange={handleSessionChange}
            >
              {sessions.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.title}
                </option>
              ))}
            </select>
            <button className={styles.newSessionBtn} onClick={handleCreateSession} title="New Session">
              <Plus size={16} />
            </button>
          </div>

          {/* Chat Logs Screen Area */}
          <div className={styles.chatArea}>
            {isLoadingHistory ? (
              <div style={{ display: 'flex', justifyContent: 'center', margin: 'auto' }}>
                <Spinner />
              </div>
            ) : messages.length === 0 ? (
              <div className={styles.emptyState}>
                <Bot size={28} style={{ color: 'var(--accent-purple)' }} />
                <h3>Study Session Assistant</h3>
                <p>Type a prompt below to ask questions about this course, explain coding examples, or summarize concepts.</p>
              </div>
            ) : (
              messages.map((msg) => {
                const isAi = msg.role === 'ASSISTANT' || msg.role === 'SYSTEM';
                let bubbleClass = `${styles.messageBubble} ${isAi ? styles.aiMessage : styles.userMessage}`;
                if (msg.generating) bubbleClass += ` ${styles.aiGenerating}`;

                return (
                  <div key={msg.id} className={bubbleClass}>
                    {msg.error && <AlertCircle size={12} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'middle', color: 'var(--accent-pink)' }} />}
                    <span>{msg.content}</span>
                  </div>
                );
              })
            )}
            <div ref={chatEndRef} />
          </div>

          {/* Chat Form Input Box Footer */}
          <form onSubmit={handleSendMessage} className={styles.footerInputRow}>
            <textarea
              className={styles.chatTextarea}
              placeholder="Ask a question..."
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage(e);
                }
              }}
              required
            />
            <button 
              type="submit" 
              className={styles.sendBtn}
              disabled={isGenerating || !inputMessage.trim()}
            >
              <Send size={14} />
            </button>
          </form>
        </>
      )}
    </div>
  );
};

export default TutorSidebar;
