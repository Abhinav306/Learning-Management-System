import React from 'react';
import Link from 'next/link';
import styles from './page.module.css';

export default function LandingPage() {
  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.logo}>
          AuraLMS <span className={styles.glowdot}></span>
        </div>
        <div className={styles.headerLinks}>
          <Link href="/login" className={styles.navLink}>Sign In</Link>
          <Link href="/signup" className={styles.registerBtn}>Get Started</Link>
        </div>
      </header>

      <main className={styles.main}>
        <section className={styles.hero}>
          <h1 className={styles.title}>
            The Intelligent <br />
            <span className={styles.gradientText}>AI-Powered Learning Hub</span>
          </h1>
          <p className={styles.subtitle}>
            Supercharge your education with an interactive AI tutor sidebar, instant document search (RAG), 
            real-time STOMP notification feeds, and auto-graded course quizzes.
          </p>
          <div className={styles.ctas}>
            <Link href="/login" className={styles.primaryCta}>
              Explore Your Dashboard
            </Link>
            <Link href="/courses" className={styles.secondaryCta}>
              Browse Course Catalogue
            </Link>
          </div>
        </section>

        <section className={styles.features}>
          <div className={styles.card}>
            <div className={`${styles.icon} ${styles.cyan}`}>🤖</div>
            <h3>Contextual AI Tutor</h3>
            <p>Study lessons with a live chatbot sidebar that retains your course history and answers queries instantly.</p>
          </div>

          <div className={styles.card}>
            <div className={`${styles.icon} ${styles.purple}`}>📚</div>
            <h3>RAG Document Search</h3>
            <p>Upload textbooks, PDF slides, or lecture notes and perform natural language semantic queries across context chunks.</p>
          </div>

          <div className={styles.card}>
            <div className={`${styles.icon} ${styles.pink}`}>⚡</div>
            <h3>Auto-Graded Quizzes</h3>
            <p>Generate assessments dynamically from lesson text and receive immediate grading reports with correct answers explained.</p>
          </div>

          <div className={styles.card}>
            <div className={`${styles.icon} ${styles.amber}`}>📡</div>
            <h3>Real-Time STOMP Streams</h3>
            <p>Stay updated instantly with WebSocket alerts detailing completed lessons, assignment grades, and enrollments.</p>
          </div>
        </section>
      </main>

      <footer className={styles.footer}>
        <p>&copy; {new Date().getFullYear()} AuraLMS. All rights reserved. Powered by Spring Boot 3 & Next.js.</p>
      </footer>
    </div>
  );
}
