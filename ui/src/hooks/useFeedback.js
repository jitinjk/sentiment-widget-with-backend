import { useState, useCallback, useEffect } from 'react';

const SPAM_COOLDOWN_MS = 3000;
const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '';

export function useFeedback() {
  const [selectedRating, setSelectedRating] = useState(null);
  const [comment, setComment] = useState('');
  const [summary, setSummary] = useState(null);
  const [isDisabled, setIsDisabled] = useState(false);
  const [confirmation, setConfirmation] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const fetchSummary = useCallback(async () => {
    try {
      const res = await fetch(`${API_BASE}/api/feedback/summary`);
      if (!res.ok) throw new Error('Failed to fetch summary');
      setSummary(await res.json());
    } catch {
      // summary stays null — widget still usable without it
    }
  }, []);

  useEffect(() => { fetchSummary(); }, [fetchSummary]);

  const selectRating = useCallback((rating) => {
    if (isDisabled) return;
    setSelectedRating(rating);
    setError('');
  }, [isDisabled]);

  const updateComment = useCallback((value) => {
    setComment(value);
  }, []);

  const submit = useCallback(async () => {
    if (!selectedRating) {
      setError('Please select a rating before submitting.');
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/feedback`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ rating: selectedRating, comment: comment.trim() }),
      });

      if (!res.ok) throw new Error('Submission failed');

      setConfirmation(true);
      setIsDisabled(true);
      setSelectedRating(null);
      setComment('');
      setError('');

      await fetchSummary();

      setTimeout(() => {
        setIsDisabled(false);
        setConfirmation(false);
      }, SPAM_COOLDOWN_MS);
    } catch {
      setError('Something went wrong. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [selectedRating, comment, fetchSummary]);

  return {
    selectedRating,
    comment,
    summary,
    isDisabled,
    confirmation,
    error,
    loading,
    selectRating,
    updateComment,
    submit,
  };
}
