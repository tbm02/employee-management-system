import { useEffect, useMemo, useState } from 'react';
import { Alert, Badge, Button, Form, Modal, Spinner, Table } from 'react-bootstrap';
import { useAuth } from '../context/AuthContext';
import * as goalService from '../services/goalService';

const QUARTERS = ['Q1', 'Q2', 'Q3', 'Q4'];

const emptyForm = {
    description: '',
    quarter: 'Q1',
    year: new Date().getFullYear(),
    employeeId: '',
};

export default function GoalsPage() {
    const { user } = useAuth();
    const canAssignOthers = user?.role === 'MANAGER' || user?.role === 'ADMIN';

    const [goals, setGoals] = useState([]);
    const [assignableUsers, setAssignableUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editingGoal, setEditingGoal] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [formError, setFormError] = useState('');

    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            const [goalsRes, usersRes] = await Promise.all([
                goalService.getAll(),
                goalService.getAssignableUsers(),
            ]);
            setGoals(goalsRes.data || []);
            setAssignableUsers(usersRes.data || []);
        } catch {
            setError('Failed to load goals.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const employeeOptions = useMemo(() => {
        if (!canAssignOthers) {
            return [];
        }
        return assignableUsers;
    }, [assignableUsers, canAssignOthers]);

    const openCreate = () => {
        setEditingGoal(null);
        setForm({
            ...emptyForm,
            employeeId: user?.userId || '',
        });
        setFormError('');
        setShowModal(true);
    };

    const openEdit = (goal) => {
        setEditingGoal(goal);
        setForm({
            description: goal.description,
            quarter: goal.quarter,
            year: goal.year,
            employeeId: goal.employeeId,
        });
        setFormError('');
        setShowModal(true);
    };

    const onChange = (field, value) => {
        setForm((prev) => ({ ...prev, [field]: value }));
    };

    const handleSave = async () => {
        setFormError('');
        if (!form.description.trim()) {
            setFormError('Description is required.');
            return;
        }
        if (!form.quarter || !form.year) {
            setFormError('Quarter and year are required.');
            return;
        }

        setSaving(true);
        try {
            const payload = {
                description: form.description.trim(),
                quarter: form.quarter,
                year: Number(form.year),
            };

            if (!editingGoal) {
                if (canAssignOthers && form.employeeId) {
                    payload.employeeId = Number(form.employeeId);
                }
                await goalService.create(payload);
            } else {
                await goalService.update(editingGoal.id, payload);
            }

            setShowModal(false);
            await fetchData();
        } catch (err) {
            setFormError(err?.response?.data?.message || 'Failed to save goal.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (goal) => {
        if (!window.confirm('Are you sure you want to delete this goal?')) return;

        try {
            await goalService.remove(goal.id);
            await fetchData();
        } catch {
            setError('Failed to delete goal.');
        }
    };

    const handleComplete = async (goal) => {
        try {
            await goalService.markCompleted(goal.id);
            await fetchData();
        } catch {
            setError('Failed to mark goal as completed.');
        }
    };

    if (loading) {
        return (
            <div className="text-center mt-5">
                <Spinner animation="border" />
            </div>
        );
    }

    return (
        <div>
            <div className="d-flex justify-content-between align-items-center mb-3">
                <h3>Goals</h3>
                <Button variant="primary" size="sm" onClick={openCreate}>
                    + Add Goal
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>Description</th>
                        <th>Quarter</th>
                        <th>Year</th>
                        <th>Goal For</th>
                        <th>Status</th>
                        <th style={{ width: '200px' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {goals.length === 0 ? (
                        <tr>
                            <td colSpan="6" className="text-center text-muted">
                                No goals found.
                            </td>
                        </tr>
                    ) : (
                        goals.map((goal) => (
                            <tr key={goal.id}>
                                <td>{goal.description}</td>
                                <td>{goal.quarter}</td>
                                <td>{goal.year}</td>
                                <td>
                                    {goal.employeeName}
                                    <div className="text-muted small">{goal.employeeEmail}</div>
                                </td>
                                <td>
                                    {goal.isCompleted ? (
                                        <Badge bg="success">Completed</Badge>
                                    ) : (
                                        <Badge bg="secondary">Open</Badge>
                                    )}
                                </td>
                                <td>
                                    {goal.canEdit && (
                                        <Button
                                            variant="outline-primary"
                                            size="sm"
                                            className="me-1 mb-1"
                                            onClick={() => openEdit(goal)}
                                        >
                                            Edit
                                        </Button>
                                    )}
                                    {goal.canMarkCompleted && (
                                        <Button
                                            variant="outline-success"
                                            size="sm"
                                            className="me-1 mb-1"
                                            onClick={() => handleComplete(goal)}
                                        >
                                            Complete
                                        </Button>
                                    )}
                                    {goal.canDelete && (
                                        <Button
                                            variant="outline-danger"
                                            size="sm"
                                            className="mb-1"
                                            onClick={() => handleDelete(goal)}
                                        >
                                            Delete
                                        </Button>
                                    )}
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </Table>

            <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>{editingGoal ? 'Edit Goal' : 'Add Goal'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {formError && <Alert variant="danger" className="py-2">{formError}</Alert>}
                    <Form>
                        <Form.Group className="mb-2">
                            <Form.Label>Description *</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={3}
                                value={form.description}
                                onChange={(e) => onChange('description', e.target.value)}
                                placeholder="Describe the goal"
                            />
                        </Form.Group>

                        <div className="row">
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>Quarter *</Form.Label>
                                    <Form.Select
                                        value={form.quarter}
                                        onChange={(e) => onChange('quarter', e.target.value)}
                                    >
                                        {QUARTERS.map((q) => (
                                            <option key={q} value={q}>{q}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </div>
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>Year *</Form.Label>
                                    <Form.Control
                                        type="number"
                                        value={form.year}
                                        onChange={(e) => onChange('year', e.target.value)}
                                        min="2000"
                                        max="2100"
                                    />
                                </Form.Group>
                            </div>
                        </div>

                        {canAssignOthers && !editingGoal && (
                            <Form.Group>
                                <Form.Label>Assign To *</Form.Label>
                                <Form.Select
                                    value={form.employeeId}
                                    onChange={(e) => onChange('employeeId', e.target.value)}
                                >
                                    {employeeOptions.map((employee) => (
                                        <option key={employee.id} value={employee.id}>
                                            {employee.fullName} ({employee.empId})
                                        </option>
                                    ))}
                                </Form.Select>
                            </Form.Group>
                        )}
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>
                        Cancel
                    </Button>
                    <Button variant="primary" size="sm" onClick={handleSave} disabled={saving}>
                        {saving ? 'Saving...' : editingGoal ? 'Update' : 'Create'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
