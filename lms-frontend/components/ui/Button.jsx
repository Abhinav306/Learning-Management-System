import React from 'react';
import styles from './Button.module.css';

const Button = ({ 
  variant = 'secondary', 
  children, 
  className = '', 
  ...props 
}) => {
  const btnClass = `${styles.btn} ${styles[variant]} ${className}`;
  
  return (
    <button className={btnClass} {...props}>
      {children}
    </button>
  );
};

export default Button;
