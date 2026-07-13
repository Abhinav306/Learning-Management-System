import React from 'react';
import styles from './GlassCard.module.css';

const GlassCard = ({ 
  children, 
  hoverable = false, 
  className = '',
  onClick
}) => {
  const cardClass = `${styles.card} ${hoverable ? styles.hoverEffect : ''} ${className}`;
  
  return (
    <div className={cardClass} onClick={onClick}>
      {children}
    </div>
  );
};

export default GlassCard;
