import { useState } from 'react';
import { Card, Badge, Button, Modal, Form, Alert, ProgressBar } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';

// ── Hardcoded demo questions per review type ─────────────────────────────────
const QUESTIONS = {
    self: [
        'How effectively did you meet your goals this period?',
        'How well did you communicate with your team?',
        'How do you rate your time management and productivity?',
        'What is your overall self-assessment of your performance?',
    ],
    peer: [
        'How effectively does this colleague collaborate with the team?',
        'Rate their communication and responsiveness.',
        'How do they handle feedback and criticism?',
        'Overall rating for this colleague\'s performance.',
    ],
    manager: [
        'How well does this employee meet their targets?',
        'Rate their initiative and problem-solving ability.',
        'How effective is their communication and reporting?',
        'Overall performance rating for this employee.',
    ],
};

// ── Demo sessions (replace with API call when backend is ready) ───────────────
const DEMO_SESSIONS = [
    {
        id: 1, sessionName: 'Q1 2025 Performance Review', month: 3, year: 2025, isActive: true,
        selfSubmitted: false, hasPeerReview: true, peerSubmitted: false,
        hasManagerReview: false, managerSubmitted: false
    },
    {
        id: 2, sessionName: 'February Appraisal', month: 2, year: 2025, isActive: false,
        selfSubmitted: true, hasPeerReview: true, peerSubmitted: true,
        hasManagerReview: false, managerSubmitted: false
    },
];

const MONTH_NAMES = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

// ── Star rating component ─────────────────────────────────────────────────────
function Stars({ value, onChange }) {
    const labels = ['', 'Poor', 'Below avg', 'Average', 'Good', 'Excellent'];
    return (
        <div className="d-flex align-items-center gap-1 flex-wrap">
            {[1, 2, 3, 4, 5].map((s) => (
                <span
                    key={s}
                    role="button"
                    style={{
                        fontSize: '1.6rem', cursor: 'pointer', userSelect: 'none',
                        color: s <= value ? '#f5a623' : '#dee2e6',
                        transition: 'color 0.15s'
                    }}
                    onClick={() => onChange(s)}
                    title={labels[s]}
                >★</span>
            ))}
            {value > 0 && (
                <Badge bg="secondary" className="ms-1">{labels[value]}</Badge>
            )}
        </div>
    );
}

// ── Main page ─────────────────────────────────────────────────────────────────
export default function MyReviewsPage() {
    const { user } = useAuth();

    const [sessions, setSessions] = useState(DEMO_SESSIONS);

    // Modal state
    const [show, setShow] = useState(false);
    const [activeSession, setActiveSession] = useState(null);
    const [reviewType, setReviewType] = useState('self');
    const [answers, setAnswers] = useState({}); // { [idx]: { score, comment } }
    const [submitted, setSubmitted] = useState(false);
    const [error, setError] = useState('');

    const openReview = (session, type) => {
        setActiveSession(session);
        setReviewType(type);
        const init = {};
        QUESTIONS[type].forEach((_, i) => { init[i] = { score: 0, comment: '' }; });
        setAnswers(init);
        setSubmitted(false);
        setError('');
        setShow(true);
    };

    const handleSubmit = () => {
        const unrated = Object.values(answers).filter((a) => a.score === 0);
        if (unrated.length > 0) {
            setError(`Please rate all ${unrated.length} remaining question(s) before submitting.`);
            return;
        }
        setSubmitted(true);
        // Mark submitted locally
        setTimeout(() => {
            setSessions((prev) => prev.map((s) =>
                s.id === activeSession.id
                    ? { ...s, [`${reviewType}Submitted`]: true }
                    : s
            ));
            setShow(false);
        }, 1000);
    };

    const questions = QUESTIONS[reviewType] || [];
    const rated = Object.values(answers).filter((a) => a.score > 0).length;
    const progress = questions.length ? Math.round((rated / questions.length) * 100) : 0;

    const typeLabel = { self: 'Self Review', peer: 'Peer Review', manager: 'Manager Review' };
    const typeVariant = { self: 'primary', peer: 'secondary', manager: 'warning' };

    return (
        <div>
            <h3 className="mb-1">My Reviews</h3>
            <p className="text-muted small mb-3">Complete your assigned review forms for each session.</p>

            {sessions.length === 0 && (
                <Alert variant="info">No review sessions assigned to you yet.</Alert>
            )}

            <div className="row g-3">
                {sessions.map((s) => (
                    <div className="col-md-6 col-lg-4" key={s.id}>
                        <Card className="h-100 shadow-sm border-0">
                            <Card.Header className="d-flex justify-content-between align-items-center bg-white border-bottom">
                                <div>
                                    <strong className="d-block">{s.sessionName}</strong>
                                    <small className="text-muted">
                                        {MONTH_NAMES[s.month - 1]} {s.year}
                                    </small>
                                </div>
                                <Badge bg={s.isActive ? 'success' : 'secondary'}>
                                    {s.isActive ? 'Active' : 'Closed'}
                                </Badge>
                            </Card.Header>
                            <Card.Body className="d-flex flex-column gap-2">
                                {/* Self Review */}
                                <ReviewRow
                                    label="📝 Self Review"
                                    submitted={s.selfSubmitted}
                                    active={s.isActive}
                                    onStart={() => openReview(s, 'self')}
                                />

                                {/* Peer Review */}
                                {s.hasPeerReview && (
                                    <ReviewRow
                                        label="👥 Peer Review"
                                        submitted={s.peerSubmitted}
                                        active={s.isActive}
                                        onStart={() => openReview(s, 'peer')}
                                        variant="secondary"
                                    />
                                )}

                                {/* Manager Review */}
                                {s.hasManagerReview && (
                                    <ReviewRow
                                        label="🏆 Manager Review"
                                        submitted={s.managerSubmitted}
                                        active={s.isActive}
                                        onStart={() => openReview(s, 'manager')}
                                        variant="warning"
                                    />
                                )}
                            </Card.Body>
                        </Card>
                    </div>
                ))}
            </div>

            {/* ── Review modal ── */}
            <Modal show={show} onHide={() => setShow(false)} centered size="lg" backdrop="static">
                <Modal.Header closeButton className={`bg-${typeVariant[reviewType]} bg-opacity-10`}>
                    <Modal.Title>
                        <Badge bg={typeVariant[reviewType]} className="me-2">{typeLabel[reviewType]}</Badge>
                        {activeSession?.sessionName}
                    </Modal.Title>
                </Modal.Header>

                <Modal.Body>
                    {error && <Alert variant="danger" className="py-2 small">{error}</Alert>}

                    {submitted ? (
                        <div className="text-center py-4">
                            <div style={{ fontSize: '3rem' }}>✅</div>
                            <h5 className="mt-2">Review Submitted!</h5>
                            <p className="text-muted small">Thank you for completing your {typeLabel[reviewType].toLowerCase()}.</p>
                        </div>
                    ) : (
                        <>
                            {/* Progress bar */}
                            <div className="mb-3">
                                <div className="d-flex justify-content-between small text-muted mb-1">
                                    <span>{rated} of {questions.length} questions rated</span>
                                    <span>{progress}%</span>
                                </div>
                                <ProgressBar now={progress} variant={typeVariant[reviewType]} style={{ height: '6px' }} />
                            </div>

                            {/* Questions */}
                            {questions.map((q, idx) => (
                                <div key={idx} className="mb-4 p-3 rounded border bg-light bg-opacity-50">
                                    <p className="fw-semibold mb-2 small">
                                        <span className="text-muted me-1">{idx + 1}.</span> {q}
                                    </p>
                                    <Stars
                                        value={answers[idx]?.score || 0}
                                        onChange={(score) =>
                                            setAnswers((prev) => ({ ...prev, [idx]: { ...prev[idx], score } }))
                                        }
                                    />
                                    <Form.Control
                                        as="textarea"
                                        rows={2}
                                        className="mt-2"
                                        size="sm"
                                        placeholder="Add a comment (optional)..."
                                        value={answers[idx]?.comment || ''}
                                        onChange={(e) =>
                                            setAnswers((prev) => ({ ...prev, [idx]: { ...prev[idx], comment: e.target.value } }))
                                        }
                                    />
                                </div>
                            ))}
                        </>
                    )}
                </Modal.Body>

                {!submitted && (
                    <Modal.Footer>
                        <Button variant="secondary" size="sm" onClick={() => setShow(false)}>Cancel</Button>
                        <Button
                            variant={typeVariant[reviewType]}
                            size="sm"
                            onClick={handleSubmit}
                            disabled={rated === 0}
                        >
                            Submit {typeLabel[reviewType]} ({rated}/{questions.length} rated)
                        </Button>
                    </Modal.Footer>
                )}
            </Modal>
        </div>
    );
}

// ── Helper: one review row inside a card ──────────────────────────────────────
function ReviewRow({ label, submitted, active, onStart, variant = 'primary' }) {
    return (
        <div className="d-flex justify-content-between align-items-center py-1 border-bottom">
            <span className="small">{label}</span>
            {submitted ? (
                <Badge bg="success">✓ Submitted</Badge>
            ) : active ? (
                <Button size="sm" variant={`outline-${variant}`} onClick={onStart}>
                    Start
                </Button>
            ) : (
                <Badge bg="secondary">Closed</Badge>
            )}
        </div>
    );
}
