import { renderHook, act, waitFor } from '@testing-library/react';
import { useFeedback } from '../hooks/useFeedback';

const mockSummary = { totalSubmissions: 0, averageRating: null, recentComments: [] };
const updatedSummary = { totalSubmissions: 1, averageRating: '5.0', recentComments: [{ comment: 'Great!', rating: 5 }] };

function makeFetchMock(postOk = true) {
  return vi.fn((url, options) => {
    if (options?.method === 'POST') {
      return Promise.resolve({ ok: postOk });
    }
    return Promise.resolve({
      ok: true,
      json: () => Promise.resolve(mockSummary),
    });
  });
}

describe('useFeedback', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    global.fetch = makeFetchMock();
  });

  afterEach(() => {
    vi.useRealTimers();
    vi.restoreAllMocks();
  });

  it('initialises with null summary and fetches it on mount', async () => {
    const { result } = renderHook(() => useFeedback());
    expect(result.current.selectedRating).toBeNull();
    expect(result.current.comment).toBe('');
    await waitFor(() => expect(result.current.summary).toEqual(mockSummary));
  });

  it('selects a rating', () => {
    const { result } = renderHook(() => useFeedback());
    act(() => result.current.selectRating(4));
    expect(result.current.selectedRating).toBe(4);
  });

  it('shows error when submitting without a rating', async () => {
    const { result } = renderHook(() => useFeedback());
    await act(() => result.current.submit());
    expect(result.current.error).not.toBe('');
    expect(fetch).not.toHaveBeenCalledWith('/api/feedback', expect.anything());
  });

  it('POSTs to the API and shows confirmation on success', async () => {
    global.fetch = vi.fn((url, options) => {
      if (options?.method === 'POST') return Promise.resolve({ ok: true });
      return Promise.resolve({ ok: true, json: () => Promise.resolve(updatedSummary) });
    });

    const { result } = renderHook(() => useFeedback());
    act(() => result.current.selectRating(5));
    act(() => result.current.updateComment('Great!'));
    await act(() => result.current.submit());

    expect(result.current.confirmation).toBe(true);
    expect(result.current.isDisabled).toBe(true);
    await waitFor(() => expect(result.current.summary).toEqual(updatedSummary));
  });

  it('re-enables widget after 3 seconds', async () => {
    const { result } = renderHook(() => useFeedback());
    act(() => result.current.selectRating(3));
    await act(() => result.current.submit());

    expect(result.current.isDisabled).toBe(true);
    act(() => vi.advanceTimersByTime(3000));
    expect(result.current.isDisabled).toBe(false);
    expect(result.current.confirmation).toBe(false);
  });

  it('sets an error message when the POST request fails', async () => {
    global.fetch = makeFetchMock(false);
    const { result } = renderHook(() => useFeedback());
    act(() => result.current.selectRating(2));
    await act(() => result.current.submit());
    expect(result.current.error).not.toBe('');
    expect(result.current.confirmation).toBe(false);
  });
});
