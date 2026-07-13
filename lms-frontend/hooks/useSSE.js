'use client';

import { useCallback } from 'react';

export const useSSE = () => {
  const streamMessage = useCallback(async (sessionId, message, { onChunk, onDone, onError }) => {
    try {
      let token = localStorage.getItem('accessToken');
      const refreshToken = localStorage.getItem('refreshToken');
      const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

      if (!token) {
        throw new Error('Unauthorized. Please log in again.');
      }
      
      let response = await fetch(`${baseUrl}/ai/tutor/sessions/${sessionId}/messages`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'Accept': 'text/event-stream'
        },
        body: JSON.stringify({ message })
      });

      // Handle 401 Unauthorized by attempting a token refresh
      if (response.status === 401 && refreshToken) {
        try {
          const refreshRes = await fetch(`${baseUrl}/auth/refresh`, {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json'
            },
            body: JSON.stringify({ refreshToken })
          });

          if (refreshRes.ok) {
            const refreshData = await refreshRes.json();
            if (refreshData.success && refreshData.data?.accessToken) {
              const newAccessToken = refreshData.data.accessToken;
              const newRefreshToken = refreshData.data.refreshToken;
              
              // Store new rotated tokens
              localStorage.setItem('accessToken', newAccessToken);
              if (newRefreshToken) {
                localStorage.setItem('refreshToken', newRefreshToken);
              }
              
              token = newAccessToken;

              // Retry original SSE request with rotated token
              response = await fetch(`${baseUrl}/ai/tutor/sessions/${sessionId}/messages`, {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json',
                  'Authorization': `Bearer ${newAccessToken}`,
                  'Accept': 'text/event-stream'
                },
                body: JSON.stringify({ message })
              });
            }
          }
        } catch (refreshErr) {
          console.error('SSE token refresh failed:', refreshErr);
        }
      }

      if (!response.ok) {
        if (response.status === 401) {
          // Clear credentials and force reload to login
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          if (typeof window !== 'undefined') {
            window.location.href = '/login';
          }
          throw new Error('Unauthorized. Please log in again.');
        }
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `SSE request failed with status: ${response.status}`);
      }

      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (true) {
        const { value, done } = await reader.read();
        if (done) {
          break;
        }

        const chunkText = decoder.decode(value, { stream: true });
        buffer += chunkText;

        const lines = buffer.split('\n');
        buffer = lines.pop();

        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed) continue;

          if (trimmed.startsWith('data:')) {
            const dataContent = trimmed.substring(5).trim();
            onChunk(dataContent);
          } else {
            onChunk(trimmed);
          }
        }
      }

      if (buffer.trim()) {
        const trimmed = buffer.trim();
        if (trimmed.startsWith('data:')) {
          onChunk(trimmed.substring(5).trim());
        } else {
          onChunk(trimmed);
        }
      }

      if (onDone) onDone();
    } catch (err) {
      console.error('SSE streaming error:', err);
      if (onError) onError(err);
    }
  }, []);

  return { streamMessage };
};

export default useSSE;
