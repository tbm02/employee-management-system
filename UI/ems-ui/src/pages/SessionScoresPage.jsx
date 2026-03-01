import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Table, Badge, Spinner, Alert, Button, Modal, Pagination } from 'react-bootstrap';
import * as feedbackService from '../services/feedbackService';

const round = (v, d = 2) => (v == null ? null : +v.toFixed(d));

const pct = (norm) => (norm == null ? '—' : `${(norm * 100).toFixed(0)}%`);

function ScoreBadge({ value, outOf5 = false }) {
    if (value == null) return <span className="text-muted small">No data</span>;
    const n = outOf5 ? value / 5 : value;        // normalise to 0-1 for colour
    const bg = n >= 0.8 ? 'success' : n >= 0.6 ? 'warning' : n >= 0.4 ? 'secondary' : 'danger';
    return <Badge bg={bg}>{outOf5 ? value.toFixed(1) : pct(value)}</Badge>;
}

const PAGE_SIZE = 8;

export default function SessionScoresPage() {
    const { sessionId } = useParams();
    const navigate = useNavigate();
    const [scores, setScores] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [page, setPage] = useState(0);

    // Breakdown modal
    const [showBreakdown, setShowBreakdown] = useState(false);
    const [selected, setSelected] = useState(null);

    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError('');
            try {
                const res = await feedbackService.getSessionScores(sessionId);
                setScores(res.data || []);
            } catch {
                setError('Score endpoint not yet available — add GET /api/feedback/sessions/' + sessionId + '/scores.');
                setScores([]);
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [sessionId]);

    // Client-side pagination
    const totalPages = Math.ceil(scores.length / PAGE_SIZE);
    const pageSlice = scores.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" /></div>;

    return (
        <div>
            <div className="d-flex align-items-center gap-3 mb-3">
                <Button variant="outline-secondary" size="sm" onClick={() => navigate('/appraisals')}>
                    ← Back
                </Button>
                <h3 className="mb-0">Session Scores</h3>
            </div>

            {/* Scoring legend */}
            <Alert variant="light" className="py-2 mb-3 border small">
                <strong>Scoring weights:</strong>&nbsp;
                Self <Badge bg="primary">30%</Badge>&nbsp;
                Manager <Badge bg="success">50%</Badge>&nbsp;
                Peer <Badge bg="secondary">20%</Badge>&nbsp;
                — Individual averages on 1–5 scale. Normalised &amp; weighted score shown as 0–1 (%).
            </Alert>

            {error && <Alert variant="warning" className="py-2"><small>⚠️ {error}</small></Alert>}

            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>Employee</th>
                        <th>Emp ID</th>
                        {/* Raw averages */}
                        <th title="Raw avg 1–5">Self avg</th>
                        <th title="Raw avg 1–5">Peer avg</th>
                        <th title="Raw avg 1–5">Manager avg</th>
                        {/* Normalised */}
                        <th title="÷5 → 0–1">Self (norm)</th>
                        <th title="÷5 → 0–1">Peer (norm)</th>
                        <th title="÷5 → 0–1">Mgr (norm)</th>
                        {/* Composite */}
                        <th title="0.30·self + 0.50·manager + 0.20·peer" className="bg-warning-subtle">
                            ⭐ Weighted Score
                        </th>
                        <th style={{ width: '80px' }}>Details</th>
                    </tr>
                </thead>
                <tbody>
                    {pageSlice.length === 0 ? (
                        <tr>
                            <td colSpan="10" className="text-center text-muted py-4">
                                {error ? 'Scores will appear here once the backend endpoint is ready.' : 'No employees assigned yet.'}
                            </td>
                        </tr>
                    ) : (
                        pageSlice.map((row) => (
                            <tr key={row.employeeId}>
                                <td>{row.firstName} {row.lastName}</td>
                                <td><code>{row.empId}</code></td>
                                <td><ScoreBadge value={round(row.selfAvg)} outOf5 /></td>
                                <td><ScoreBadge value={round(row.peerAvg)} outOf5 /></td>
                                <td><ScoreBadge value={round(row.managerAvg)} outOf5 /></td>
                                <td>{pct(row.selfNorm)}</td>
                                <td>{pct(row.peerNorm)}</td>
                                <td>{pct(row.managerNorm)}</td>
                                <td className="fw-bold">
                                    <ScoreBadge value={row.weightedScore} />
                                </td>
                                <td>
                                    <Button variant="outline-info" size="sm" onClick={() => { setSelected(row); setShowBreakdown(true); }}>
                                        View
                                    </Button>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </Table>

            {/* Pagination */}
            {totalPages > 1 && (
                <Pagination size="sm" className="justify-content-center mt-2">
                    <Pagination.Prev disabled={page === 0} onClick={() => setPage(p => p - 1)} />
                    {[...Array(totalPages)].map((_, i) => (
                        <Pagination.Item key={i} active={i === page} onClick={() => setPage(i)}>{i + 1}</Pagination.Item>
                    ))}
                    <Pagination.Next disabled={page === totalPages - 1} onClick={() => setPage(p => p + 1)} />
                </Pagination>
            )}

            {/* Per-employee score breakdown modal */}
            <Modal show={showBreakdown} onHide={() => setShowBreakdown(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>
                        Score Detail — {selected?.firstName} {selected?.lastName}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Table size="sm" bordered className="mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>Component</th>
                                <th>Raw avg (1–5)</th>
                                <th>Norm (0–1)</th>
                                <th>Weight</th>
                                <th>Contribution</th>
                            </tr>
                        </thead>
                        <tbody>
                            {[
                                { label: 'Self', avg: selected?.selfAvg, norm: selected?.selfNorm, w: 0.30 },
                                { label: 'Manager', avg: selected?.managerAvg, norm: selected?.managerNorm, w: 0.50 },
                                { label: 'Peer', avg: selected?.peerAvg, norm: selected?.peerNorm, w: 0.20 },
                            ].map(({ label, avg, norm, w }) => (
                                <tr key={label}>
                                    <td>{label}</td>
                                    <td>{avg != null ? avg.toFixed(1) : <span className="text-muted">—</span>}</td>
                                    <td>{pct(norm)}</td>
                                    <td>{(w * 100).toFixed(0)}%</td>
                                    <td>{norm != null ? pct(norm * w) : <span className="text-muted">—</span>}</td>
                                </tr>
                            ))}
                        </tbody>
                        <tfoot className="table-dark">
                            <tr>
                                <td colSpan="4"><strong>Weighted Score</strong></td>
                                <td><strong>{pct(selected?.weightedScore)}</strong></td>
                            </tr>
                        </tfoot>
                    </Table>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowBreakdown(false)}>Close</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
