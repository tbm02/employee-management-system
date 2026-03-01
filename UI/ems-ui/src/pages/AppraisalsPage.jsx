import { useState, useEffect } from 'react';
import {
    Button, Table, Badge, Spinner, Alert, Modal, Form,
} from 'react-bootstrap';
import { useNavigate } from 'react-router-dom';
import * as feedbackService from '../services/feedbackService';
import * as userService from '../services/userService';
import * as departmentService from '../services/departmentService';

const MONTHS = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December',
];

const emptySessionForm = {
    name: '',
    month: new Date().getMonth() + 1,
    year: new Date().getFullYear(),
};

export default function AppraisalsPage() {
    const navigate = useNavigate();
    const [sessions, setSessions] = useState([]);
    const [templates, setTemplates] = useState([]);
    const [users, setUsers] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // ── Create session modal ────────────────────────────────
    const [showCreate, setShowCreate] = useState(false);
    const [sessionForm, setSessionForm] = useState({ ...emptySessionForm });
    const [creating, setCreating] = useState(false);
    const [createError, setCreateError] = useState('');

    // ── Assign modal (2-step) ───────────────────────────────
    const [showAssign, setShowAssign] = useState(false);
    const [assigningSession, setAssigningSession] = useState(null);
    const [step, setStep] = useState(1); // 1 = select employees + templates, 2 = set peer reviewer per employee

    // Step 1 state
    const [selectedEmployeeIds, setSelectedEmployeeIds] = useState([]);
    const [templates3, setTemplates3] = useState({
        selfTemplateId: '',
        peerTemplateId: '',
        managerTemplateId: '',
    });

    // Step 2 state: { [employeeId]: peerReviewerId }
    const [peerMap, setPeerMap] = useState({});

    const [assigning, setAssigning] = useState(false);
    const [assignError, setAssignError] = useState('');

    // ── Data loading ────────────────────────────────────────
    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            const [sessRes, templRes, usersRes, deptRes] = await Promise.allSettled([
                feedbackService.getSessions(),
                feedbackService.getTemplates(),
                userService.getAll(),
                departmentService.getAll(),
            ]);
            // Backend returns Page<FeedbackSession> — extract .content array
            setSessions(sessRes.status === 'fulfilled' ? (sessRes.value.data?.content || []) : []);
            setTemplates(templRes.status === 'fulfilled' ? (templRes.value.data || []) : []);
            setUsers(usersRes.status === 'fulfilled' ? (usersRes.value.data || []) : []);
            setDepartments(deptRes.status === 'fulfilled' ? (deptRes.value.data || []) : []);
            if (sessRes.status === 'rejected') {
                setError('GET /api/feedback/sessions endpoint not yet available.');
            }
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => { fetchData(); }, []);

    // ── Create session ──────────────────────────────────────
    const handleCreateSession = async () => {
        setCreateError('');
        if (!sessionForm.name.trim()) { setCreateError('Session name is required.'); return; }
        setCreating(true);
        try {
            await feedbackService.createSession({
                name: sessionForm.name.trim(),
                month: Number(sessionForm.month),
                year: Number(sessionForm.year),
            });
            setShowCreate(false);
            setSessionForm({ ...emptySessionForm });
            fetchData();
        } catch (err) {
            setCreateError(err?.response?.data?.message || 'Failed to create session.');
        } finally {
            setCreating(false);
        }
    };

    // ── Open assign modal ───────────────────────────────────
    const openAssign = (session) => {
        setAssigningSession(session);
        setStep(1);
        setSelectedEmployeeIds([]);
        setTemplates3({ selfTemplateId: '', peerTemplateId: '', managerTemplateId: '' });
        setPeerMap({});
        setAssignError('');
        setShowAssign(true);
    };

    const toggleEmployee = (id) => {
        setSelectedEmployeeIds((prev) =>
            prev.includes(id) ? prev.filter((e) => e !== id) : [...prev, id]
        );
    };

    // ── Step 1 → Step 2 ─────────────────────────────────────
    const goToStep2 = () => {
        setAssignError('');
        if (selectedEmployeeIds.length === 0) { setAssignError('Select at least one employee.'); return; }
        if (!templates3.selfTemplateId || !templates3.peerTemplateId || !templates3.managerTemplateId) {
            setAssignError('All three templates must be selected.'); return;
        }
        // Pre-fill peerMap with empty
        const map = {};
        selectedEmployeeIds.forEach((id) => { map[id] = ''; });
        setPeerMap(map);
        setStep(2);
        setAssignError('');
    };

    // ── Submit assign ───────────────────────────────────────
    const handleAssign = async () => {
        setAssignError('');
        const missing = selectedEmployeeIds.filter((id) => !peerMap[id]);
        if (missing.length > 0) {
            setAssignError(`Please select a peer reviewer for all ${missing.length} employee(s).`);
            return;
        }
        setAssigning(true);
        try {
            const assignments = selectedEmployeeIds.map((employeeId) => ({
                employeeId,
                peerReviewerId: Number(peerMap[employeeId]),
            }));
            await feedbackService.assignEmployees(assigningSession.id, {
                assignments,
                selfTemplateId: Number(templates3.selfTemplateId),
                peerTemplateId: Number(templates3.peerTemplateId),
                managerTemplateId: Number(templates3.managerTemplateId),
            });
            setShowAssign(false);
        } catch (err) {
            setAssignError(err?.response?.data?.message || 'Failed to assign employees.');
        } finally {
            setAssigning(false);
        }
    };

    const userById = (id) => users.find((u) => u.id === id);
    const userName = (id) => {
        const u = userById(id);
        return u ? `${u.firstName} ${u.lastName}` : `#${id}`;
    };
    const deptName = (deptId) => {
        if (!deptId) return '—';
        const d = departments.find((dept) => dept.id === deptId);
        return d ? d.name : `#${deptId}`;
    };

    if (loading) return <div className="text-center mt-5"><Spinner animation="border" /></div>;

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h3>Appraisal Sessions</h3>
                <Button variant="primary" size="sm" onClick={() => { setSessionForm({ ...emptySessionForm }); setCreateError(''); setShowCreate(true); }}>
                    + Create Session
                </Button>
            </div>

            {error && <Alert variant="warning" className="py-2"><small>⚠️ {error}</small></Alert>}

            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>Name</th>
                        <th>Period</th>
                        <th>Status</th>
                        <th style={{ width: '220px' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {sessions.length === 0 ? (
                        <tr><td colSpan="4" className="text-center text-muted">No sessions yet. Create one to get started.</td></tr>
                    ) : (
                        sessions.map((s) => (
                            <tr key={s.id}>
                                <td><strong>{s.name}</strong></td>
                                <td>{MONTHS[(s.month || 1) - 1]} {s.year}</td>
                                <td>
                                    <Badge bg={s.isActive ? 'success' : 'secondary'}>
                                        {s.isActive ? 'Active' : 'Closed'}
                                    </Badge>
                                </td>
                                <td>
                                    <Button variant="outline-primary" size="sm" className="me-1" onClick={() => openAssign(s)}>
                                        Assign
                                    </Button>
                                    <Button variant="outline-info" size="sm" onClick={() => navigate(`/appraisals/${s.id}/scores`)}>
                                        View Scores
                                    </Button>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </Table>

            {/* ── Create Session Modal ── */}
            <Modal show={showCreate} onHide={() => setShowCreate(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>Create Appraisal Session</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {createError && <Alert variant="danger" className="py-2">{createError}</Alert>}
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Session Name *</Form.Label>
                            <Form.Control
                                placeholder="e.g. Q1 2025 Performance Review"
                                value={sessionForm.name}
                                onChange={(e) => setSessionForm((p) => ({ ...p, name: e.target.value }))}
                            />
                        </Form.Group>
                        <div className="row">
                            <div className="col-7">
                                <Form.Group>
                                    <Form.Label>Month *</Form.Label>
                                    <Form.Select value={sessionForm.month} onChange={(e) => setSessionForm((p) => ({ ...p, month: e.target.value }))}>
                                        {MONTHS.map((m, i) => (
                                            <option key={i + 1} value={i + 1}>{m}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </div>
                            <div className="col-5">
                                <Form.Group>
                                    <Form.Label>Year *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        min="2020"
                                        max="2099"
                                        value={sessionForm.year}
                                        onChange={(e) => setSessionForm((p) => ({ ...p, year: e.target.value }))}
                                    />
                                </Form.Group>
                            </div>
                        </div>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowCreate(false)}>Cancel</Button>
                    <Button variant="primary" size="sm" onClick={handleCreateSession} disabled={creating}>
                        {creating ? 'Creating...' : 'Create'}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* ── Assign Modal (2-step) ── */}
            <Modal show={showAssign} onHide={() => setShowAssign(false)} centered size="xl">
                <Modal.Header closeButton>
                    <Modal.Title>
                        Assign Employees — <span className="text-muted fw-normal">{assigningSession?.name}</span>
                        <Badge bg="secondary" className="ms-2">Step {step} of 2</Badge>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {assignError && <Alert variant="danger" className="py-2">{assignError}</Alert>}

                    {/* ── STEP 1: Select employees + templates ── */}
                    {step === 1 && (
                        <>
                            {/* Template selectors */}
                            <h6 className="text-muted mb-2">Review Templates (applied to all employees)</h6>
                            <div className="row mb-4">
                                {[
                                    { key: 'selfTemplateId', label: 'Self Review Template' },
                                    { key: 'peerTemplateId', label: 'Peer Review Template' },
                                    { key: 'managerTemplateId', label: 'Manager Review Template' },
                                ].map(({ key, label }) => (
                                    <div className="col-4" key={key}>
                                        <Form.Group>
                                            <Form.Label className="small fw-semibold">{label} *</Form.Label>
                                            <Form.Select
                                                size="sm"
                                                value={templates3[key]}
                                                onChange={(e) => setTemplates3((p) => ({ ...p, [key]: e.target.value }))}
                                            >
                                                <option value="">— Select —</option>
                                                {templates.map((t) => (
                                                    <option key={t.id} value={t.id}>{t.name} ({t.type})</option>
                                                ))}
                                            </Form.Select>
                                        </Form.Group>
                                    </div>
                                ))}
                            </div>

                            {/* Employee multi-select table */}
                            <h6 className="text-muted mb-2 d-flex align-items-center gap-2">
                                Select Employees
                                <Badge bg="primary" pill>{selectedEmployeeIds.length} selected</Badge>
                            </h6>
                            <div style={{ maxHeight: '300px', overflowY: 'auto', border: '1px solid #dee2e6', borderRadius: '4px' }}>
                                <Table size="sm" className="mb-0">
                                    <thead className="table-light sticky-top">
                                        <tr>
                                            <th style={{ width: '40px' }}>
                                                <Form.Check
                                                    type="checkbox"
                                                    checked={selectedEmployeeIds.length === users.length && users.length > 0}
                                                    onChange={(e) => setSelectedEmployeeIds(e.target.checked ? users.map((u) => u.id) : [])}
                                                    title="Select all"
                                                />
                                            </th>
                                            <th>Emp ID</th>
                                            <th>Name</th>
                                            <th>Role</th>
                                            <th>Department</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {users.map((u) => (
                                            <tr
                                                key={u.id}
                                                style={{ cursor: 'pointer' }}
                                                onClick={() => toggleEmployee(u.id)}
                                                className={selectedEmployeeIds.includes(u.id) ? 'table-primary' : ''}
                                            >
                                                <td>
                                                    <Form.Check
                                                        type="checkbox"
                                                        checked={selectedEmployeeIds.includes(u.id)}
                                                        onChange={() => toggleEmployee(u.id)}
                                                        onClick={(e) => e.stopPropagation()}
                                                    />
                                                </td>
                                                <td><code>{u.empId}</code></td>
                                                <td>{u.firstName} {u.lastName}</td>
                                                <td><Badge bg="secondary" className="small">{u.role}</Badge></td>
                                                <td className="text-muted small">{u.departmentId ?? '—'}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </Table>
                            </div>
                        </>
                    )}

                    {/* ── STEP 2: Assign peer reviewer per employee ── */}
                    {step === 2 && (
                        <>
                            <Alert variant="info" className="py-2 mb-3">
                                <small>
                                    <strong>Assign a peer reviewer</strong> for each selected employee.
                                    The peer reviewer will receive an email notification and a review task.
                                </small>
                            </Alert>
                            <Table bordered size="sm">
                                <thead className="table-dark">
                                    <tr>
                                        <th>Employee</th>
                                        <th>Emp ID</th>
                                        <th>Role</th>
                                        <th>Peer Reviewer *</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {selectedEmployeeIds.map((empId) => {
                                        const emp = userById(empId);
                                        return (
                                            <tr key={empId}>
                                                <td>{emp ? `${emp.firstName} ${emp.lastName}` : `#${empId}`}</td>
                                                <td><code>{emp?.empId}</code></td>
                                                <td><Badge bg="secondary" className="small">{emp?.role}</Badge></td>
                                                <td>
                                                    <Form.Select
                                                        size="sm"
                                                        value={peerMap[empId] || ''}
                                                        onChange={(e) => setPeerMap((prev) => ({ ...prev, [empId]: e.target.value }))}
                                                    >
                                                        <option value="">— Select reviewer —</option>
                                                        {users
                                                            .filter((u) => u.id !== empId) // can't review yourself
                                                            .map((u) => (
                                                                <option key={u.id} value={u.id}>
                                                                    {u.firstName} {u.lastName} ({u.empId})
                                                                </option>
                                                            ))}
                                                    </Form.Select>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </Table>
                        </>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    {step === 2 && (
                        <Button variant="outline-secondary" size="sm" onClick={() => { setStep(1); setAssignError(''); }}>
                            ← Back
                        </Button>
                    )}
                    <Button variant="secondary" size="sm" onClick={() => setShowAssign(false)}>Cancel</Button>
                    {step === 1 ? (
                        <Button variant="primary" size="sm" onClick={goToStep2}>
                            Next: Assign Peer Reviewers →
                        </Button>
                    ) : (
                        <Button variant="success" size="sm" onClick={handleAssign} disabled={assigning}>
                            {assigning ? 'Assigning...' : `Confirm & Notify (${selectedEmployeeIds.length} employees)`}
                        </Button>
                    )}
                </Modal.Footer>
            </Modal>
        </div>
    );
}
