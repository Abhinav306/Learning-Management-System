'use client';

import React from 'react';
import { FileText } from 'lucide-react';
import styles from './RagSourceCard.module.css';

const RagSourceCard = ({ source }) => {
  // Check if source is a string name or carries detailed excerpt
  const isString = typeof source === 'string';
  const fileName = isString ? source : (source.filename || 'Source Document');
  const excerpt = isString ? '' : source.excerpt;

  return (
    <div className={styles.sourceCard}>
      <span className={styles.sourceTitle}>
        <FileText size={12} />
        {fileName}
      </span>
      {excerpt && <p className={styles.sourceExcerpt}>"{excerpt}"</p>}
    </div>
  );
};

export default RagSourceCard;
