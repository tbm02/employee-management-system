import api from './api';

// ── Sessions ───────────────────────────────────────────────────────────────
export const getSessions = async ({ page = 0, size = 10 } = {}) => {
    const res = await api.get('/feedback/sessions', { params: { page, size, sort: 'createdAt,desc' } });
    return res.data;
};

export const createSession = async (payload) => {
    // payload: { name, month, year }
    const res = await api.post('/feedback/sessions', payload);
    return res.data;
};

// ── Templates (seeded — read only) ─────────────────────────────────────────
export const getTemplates = async () => {
    const res = await api.get('/feedback/templates');
    return res.data;
};

export const getTemplateQuestions = async (templateId) => {
    const res = await api.get(`/feedback/templates/${templateId}/questions`);
    return res.data;
};

// ── Assign employees to a session ─────────────────────────────────────────
export const assignEmployees = async (sessionId, payload) => {
    // payload: { assignments:[{employeeId, peerReviewerId}], selfTemplateId, peerTemplateId, managerTemplateId }
    const res = await api.post(`/feedback/sessions/${sessionId}/assign`, payload);
    return res.data;
};

// ── Session scores ─────────────────────────────────────────────────────────
export const getSessionScores = async (sessionId, departmentId) => {
    const params = departmentId ? { departmentId } : {};
    const res = await api.get(`/feedback/sessions/${sessionId}/scores`, { params });
    return res.data;
};

// ── Review submission ──────────────────────────────────────────────────────
export const submitSelfReview = async (sessionId, answers) => {
    const res = await api.post(`/feedback/sessions/${sessionId}/self-review`, { answers });
    return res.data;
};

export const submitPeerReview = async (sessionId, revieweeId, answers) => {
    const res = await api.post(`/feedback/sessions/${sessionId}/peer-review`, { revieweeId, answers });
    return res.data;
};

export const submitManagerReview = async (sessionId, employeeId, answers) => {
    const res = await api.post(`/feedback/sessions/${sessionId}/manager-review`, { employeeId, answers });
    return res.data;
};

// ── My reviews (what I need to fill) ──────────────────────────────────────
export const getMyReviews = async () => {
    const res = await api.get('/feedback/sessions/my-reviews');
    return res.data;
};
