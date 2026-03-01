import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Table, Badge, Spinner, Alert, Button, Modal, Form, Pagination } from 'react-bootstrap';
import * as feedbackService from '../services/feedbackService';
import * as departmentService from '../services/departmentService';

const round = (v, d = 2) => (v == null ? null : +v.toFixed(d));
const pct = (norm) => (norm == null ? '—' : `${(norm * 100).toFixed(0)}%`);

function ScoreBadge({ value, outOf5 = false }) {
    if (value == null) return <span className="text-muted small">—</span>;
    const n = outOf5 ? value / 5 : value;
    const bg = n >= 0.8 ? 'success' : n >= 0.6 ? 'warning' : n >= 0.4 ? 'secondary' : 'danger';
    return <Badge bg={bg}>{outOf5 ? value.toFixed(1) : pct(value)}</Badge>;
}

const PAGE_SIZE = 8;

export default function SessionScoresPage() {
    const { sessionId } = useParams();
    const navigate = useNavigate();

    const [allScores, setAllScores] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Filter state
    const [selectedDeptId, setSelectedDeptId] = useState('');

    // Pagination
    const [page, setPage] = useState(0);

    // Breakdown modal
    const [showBreakdown, setShowBreakdown] = useState(false);
    const [selected, setSelected] = useState(null);

    // ── Load scores + departments ─────────────────────────────────────────
    useEffect(() => {
        const load = async () => {
            setLoading(true);
            setError('');
            try {
                const [scoreRes, deptRes] = await Promise.allSettled([
                    feedbackService.getSessionScores(sessionId),
                    departmentService.getAll(),
                ]);
                setAllScores(scoreRes.status === 'fulfilled' ? (scoreRes.value.data || []) : []);
                setDepartments(deptRes.status === 'fulfilled' ? (deptRes.value.data || []) : []);
                if (scoreRes.status === 'rejected') {
                    setError('Score endpoint not available yet.');
                }
            } finally {
                setLoading(false);
            }
        };
        load();
    }, [sessionId]);

    // Re-fetch from backend when department filter changes (uses optional ?departmentId=)
    const handleDeptChange = async (deptId) => {
        setSelectedDeptId(deptId);
        setPage(0);
        setLoading(true);
        try {
            const res = await feedbackService.getSessionScores(sessionId, deptId || undefined);
            setAllScores(res.data || []);
        } catch {
            setError('Could not apply department filter.');
        } finally {
            setLoading(false);
        }
    };

    // Client-side pagination over the (already filtered) allScores array
    const totalPages = Math.ceil(allScores.length / PAGE_SIZE);
    const pageSlice = allScores.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

    const openBreakdown = (row) => { setSelected(row); setShowBreakdown(true); };

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" /></div>;

    return (
        <div>
            {/* ── Header ── */}
            <div className="d-flex align-items-center gap-3 mb-3 flex-wrap">
                <Button variant="outline-secondary" size="sm" onClick={() => navigate('/appraisals')}>
                    ← Back
                </Button>
                <h3 className="mb-0 me-auto">Session Scores</h3>

                {/* Department filter dropdown */}
                <div className="d-flex align-items-center gap-2">
                    <label className="small fw-semibold mb-0 text-nowrap">Filter by Dept:</label>
                    <Form.Select
                        size="sm"
                        style={{ minWidth: '180px' }}
                        value={selectedDeptId}
                        onChange={(e) => handleDeptChange(e.target.value)}
                    >
                        <option value="">All Departments</option>
                        {departments.map((d) => (
                            <option key={d.id} value={d.id}>{d.name}</option>
                        ))}
                    </Form.Select>
                    {selectedDeptId && (
                        <Button variant="outline-secondary" size="sm" onClick={() => handleDeptChange('')}>
                            ✕ Clear
                        </Button>
                    )}
                </div>
            </div>

            {/* Scoring legend */}
            <Alert variant="light" className="py-2 mb-3 border small">
                <strong>Weights:</strong>&nbsp;
                Self <Badge bg="primary">30%</Badge>&nbsp;
                Manager <Badge bg="success">50%</Badge>&nbsp;
                Peer <Badge bg="secondary">20%</Badge>&nbsp;
                — Averages on 1–5 scale. Normalised &amp; weighted score shown as percentage.
            </Alert>

            {error && <Alert variant="warning" className="py-2"><small>⚠️ {error}</small></Alert>}

            {/* ── Score table ── */}
            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>Employee</th>
                        <th>Emp ID</th>
                        <th>Department</th>
                        <th title="Raw avg 1–5">Self avg</th>
                        <th title="Raw avg 1–5">Peer avg</th>
                        <th title="Raw avg 1–5">Mgr avg</th>
                        <th title="÷5 → 0–1">Self %</th>
                        <th title="÷5 → 0–1">Peer %</th>
                        <th title="÷5 → 0–1">Mgr %</th>
                        <th className="bg-warning-subtle" title="0.30·self + 0.50·mgr + 0.20·peer">⭐ Weighted</th>
                        <th style={{ width: '70px' }}></th>
                    </tr>
                </thead>
                <tbody>
                    {pageSlice.length === 0 ? (
                        <tr>
                            <td colSpan="11" className="text-center text-muted py-4">
                                {selectedDeptId ? 'No employees in this department for this session.' : 'No employees assigned yet.'}
                            </td>
                        </tr>
                    ) : (
                        pageSlice.map((row) => (
                            <tr key={row.employeeId}>
                                <td>{row.firstName} {row.lastName}</td>
                                <td><code>{row.empId}</code></td>
                                <td>
                                    {row.departmentName
                                        ? <Badge bg="primary" className="fw-normal">{row.departmentName}</Badge>
                                        : <span className="text-muted">—</span>
                                    }
                                </td>
                                <td><ScoreBadge value={round(row.selfAvg)} outOf5 /></td>
                                <td><ScoreBadge value={round(row.peerAvg)} outOf5 /></td>
                                <td><ScoreBadge value={round(row.managerAvg)} outOf5 /></td>
                                <td>{pct(row.selfNorm)}</td>
                                <td>{pct(row.peerNorm)}</td>
                                <td>{pct(row.managerNorm)}</td>
                                <td className="fw-bold"><ScoreBadge value={row.weightedScore} /></td>
                                <td>
                                    <Button variant="outline-info" size="sm" onClick={() => openBreakdown(row)}>
                                        Detail
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

            {/* Showing X of Y */}
            {allScores.length > 0 && (
                <p className="text-center text-muted small mt-1">
                    Showing {pageSlice.length} of {allScores.length} employee{allScores.length !== 1 ? 's' : ''}
                    {selectedDeptId && departments.find(d => String(d.id) === selectedDeptId)
                        ? ` in ${departments.find(d => String(d.id) === selectedDeptId).name}`
                        : ''}
                </p>
            )}

            {/* ── Detail breakdown modal ── */}
            <Modal show={showBreakdown} onHide={() => setShowBreakdown(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>
                        {selected?.firstName} {selected?.lastName}
                        {selected?.departmentName && (
                            <Badge bg="primary" className="ms-2 fw-normal small">{selected.departmentName}</Badge>
                        )}
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <Table size="sm" bordered className="mb-0">
                        <thead className="table-light">
                            <tr>
                                <th>Component</th>
                                <th>Avg (1–5)</th>
                                <th>Normalised</th>
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
