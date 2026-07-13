'use client';

import React from 'react';
import Button from '../ui/Button';
import styles from './CourseFilter.module.css';

const CourseFilter = ({ categories = [], filters, onChange, onReset }) => {
  
  const handleCategoryChange = (e) => {
    onChange({ ...filters, categoryId: e.target.value });
  };

  const handleDifficultyChange = (level) => {
    // If it's already selected, clear it. In backend, we query single difficulty at a time
    const newDifficulty = filters.difficulty === level ? '' : level;
    onChange({ ...filters, difficulty: newDifficulty });
  };

  return (
    <div className={styles.filterContainer}>
      <div className={styles.section}>
        <span className={styles.label}>Category</span>
        <select 
          className={styles.selectInput}
          value={filters.categoryId || ''}
          onChange={handleCategoryChange}
        >
          <option value="">All Categories</option>
          {categories.map((cat) => (
            <option key={cat.id} value={cat.id}>
              {cat.name}
            </option>
          ))}
        </select>
      </div>

      <div className={styles.section}>
        <span className={styles.label}>Difficulty</span>
        <div className={styles.difficultyGroup}>
          {['BEGINNER', 'INTERMEDIATE', 'ADVANCED'].map((level) => (
            <label key={level} className={styles.checkboxLabel}>
              <input
                type="checkbox"
                className={styles.checkbox}
                checked={filters.difficulty === level}
                onChange={() => handleDifficultyChange(level)}
              />
              <span>{level.charAt(0) + level.slice(1).toLowerCase()}</span>
            </label>
          ))}
        </div>
      </div>

      <Button variant="secondary" className={styles.resetBtn} onClick={onReset}>
        Reset Filters
      </Button>
    </div>
  );
};

export default CourseFilter;
