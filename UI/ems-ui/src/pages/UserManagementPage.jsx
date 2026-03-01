import { useState, useEffect, useRef, useCallback } from 'react';
import { Modal, Button, Form, Table, Badge, Spinner, Alert } from 'react-bootstrap';
import * as userService from '../services/userService';
import * as departmentService from '../services/departmentService';

const ROLES = ['ADMIN', 'HR', 'MANAGER', 'EMPLOYEE'];

const emptyForm = {
    empId: '',
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    role: 'EMPLOYEE',
    departmentId: '',
    enabled: true,
};

// Debounce helper
function useDebounce(callback, delay) {
    const timerRef = useRef(null);
    const stableCallback = useCallback(callback, [callback]);

    return useCallback(
        (...args) => {
            if (timerRef.current) clearTimeout(timerRef.current);
            timerRef.current = setTimeout(() => stableCallback(...args), delay);
        },
        [stableCallback, delay]
    );
}

export default function UserManagementPage() {
    const [users, setUsers] = useState([]);
    const [departments, setDepartments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editing, setEditing] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [formError, setFormError] = useState('');
    const [saving, setSaving] = useState(false);

    // Uniqueness validation state
    const [empIdError, setEmpIdError] = useState('');
    const [emailError, setEmailError] = useState('');
    const [empIdChecking, setEmpIdChecking] = useState(false);
    const [emailChecking, setEmailChecking] = useState(false);
    const [generatingEmpId, setGeneratingEmpId] = useState(false);

    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            const [usersRes, deptRes] = await Promise.all([
                userService.getAll(),
                departmentService.getAll(),
            ]);
            setUsers(usersRes.data || []);
            setDepartments(deptRes.data || []);
        } catch {
            setError('Failed to load data.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const deptName = (deptId) => {
        const d = departments.find((dept) => dept.id === deptId);
        return d ? d.name : deptId ?? '—';
    };

    // --- Debounced uniqueness checks ---
    const checkEmpIdUnique = useDebounce(async (value) => {
        if (!value || value.trim().length < 3) {
            setEmpIdError('');
            return;
        }
        setEmpIdChecking(true);
        try {
            const res = await userService.checkEmpId(value.trim());
            if (res.data === true) {
                setEmpIdError('This Employee ID is already taken.');
            } else {
                setEmpIdError('');
            }
        } catch {
            // silently ignore network errors during debounced check
        } finally {
            setEmpIdChecking(false);
        }
    }, 500);

    const checkEmailUnique = useDebounce(async (value) => {
        if (!value || !value.includes('@')) {
            setEmailError('');
            return;
        }
        setEmailChecking(true);
        try {
            const res = await userService.checkEmail(value.trim());
            if (res.data === true) {
                setEmailError('This email is already taken.');
            } else {
                setEmailError('');
            }
        } catch {
            // silently ignore
        } finally {
            setEmailChecking(false);
        }
    }, 500);

    const handleGenerateEmpId = async () => {
        setGeneratingEmpId(true);
        try {
            const res = await userService.generateEmpId();
            setForm((prev) => ({ ...prev, empId: res.data }));
            setEmpIdError('');
        } catch {
            setFormError('Failed to generate Employee ID.');
        } finally {
            setGeneratingEmpId(false);
        }
    };

    const openCreate = () => {
        setEditing(null);
        setForm({ ...emptyForm });
        setFormError('');
        setEmpIdError('');
        setEmailError('');
        setShowModal(true);
    };

    const openEdit = (user) => {
        setEditing(user.id);
        setForm({
            empId: user.empId,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            password: '',
            role: user.role,
            departmentId: user.departmentId || '',
            enabled: user.enabled,
        });
        setFormError('');
        setEmpIdError('');
        setEmailError('');
        setShowModal(true);
    };

    const handleSave = async () => {
        setFormError('');
        if (!form.empId || !form.firstName || !form.lastName || !form.email || !form.role) {
            setFormError('Please fill in all required fields.');
            return;
        }
        if (empIdError || emailError) {
            setFormError('Please fix the validation errors before saving.');
            return;
        }
        if (!editing && !form.password) {
            setFormError('Password is required for new users.');
            return;
        }
        setSaving(true);
        try {
            const payload = { ...form, departmentId: form.departmentId ? Number(form.departmentId) : null };
            if (!payload.password) {
                payload.password = '________';
            }
            if (editing) {
                await userService.update(editing, payload);
            } else {
                await userService.create(payload);
            }
            setShowModal(false);
            fetchData();
        } catch (err) {
            setFormError(err?.response?.data?.message || 'Failed to save user.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this user?')) return;
        try {
            await userService.remove(id);
            fetchData();
        } catch {
            setError('Failed to delete user.');
        }
    };

    const onChange = (field, value) => {
        setForm((prev) => ({ ...prev, [field]: value }));

        // Trigger debounced uniqueness checks
        if (field === 'empId') {
            checkEmpIdUnique(value);
        }
        if (field === 'email') {
            checkEmailUnique(value);
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
                <h3>User Management</h3>
                <Button variant="primary" size="sm" onClick={openCreate}>
                    + Add User
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>Emp ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Department</th>
                        <th>Enabled</th>
                        <th style={{ width: '140px' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {users.length === 0 ? (
                        <tr>
                            <td colSpan="7" className="text-center text-muted">
                                No users found.
                            </td>
                        </tr>
                    ) : (
                        users.map((u) => (
                            <tr key={u.id}>
                                <td><code>{u.empId}</code></td>
                                <td>{u.firstName} {u.lastName}</td>
                                <td>{u.email}</td>
                                <td><Badge bg="secondary">{u.role}</Badge></td>
                                <td>{deptName(u.departmentId)}</td>
                                <td>{u.enabled ? '✓' : '✗'}</td>
                                <td>
                                    <Button variant="outline-primary" size="sm" className="me-1" onClick={() => openEdit(u)}>
                                        Edit
                                    </Button>
                                    <Button variant="outline-danger" size="sm" onClick={() => handleDelete(u.id)}>
                                        Delete
                                    </Button>
                                </td>
                            </tr>
                        ))
                    )}
                </tbody>
            </Table>

            {/* Add/Edit Modal */}
            <Modal show={showModal} onHide={() => setShowModal(false)} centered>
                <Modal.Header closeButton>
                    <Modal.Title>{editing ? 'Edit User' : 'Add New User'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {formError && <Alert variant="danger" className="py-2">{formError}</Alert>}
                    <Form>
                        {/* Employee ID with Generate button */}
                        <Form.Group className="mb-2">
                            <Form.Label>Employee ID *</Form.Label>
                            <div className="d-flex gap-2">
                                <div className="flex-grow-1">
                                    <Form.Control
                                        value={form.empId}
                                        onChange={(e) => onChange('empId', e.target.value)}
                                        placeholder="EMP2025001"
                                        isInvalid={!!empIdError}
                                    />
                                    {empIdChecking && (
                                        <Form.Text className="text-muted">
                                            <Spinner animation="border" size="sm" className="me-1" />
                                            Checking...
                                        </Form.Text>
                                    )}
                                    <Form.Control.Feedback type="invalid">
                                        {empIdError}
                                    </Form.Control.Feedback>
                                </div>
                                {!editing && (
                                    <Button
                                        variant="outline-secondary"
                                        size="sm"
                                        onClick={handleGenerateEmpId}
                                        disabled={generatingEmpId}
                                        style={{ whiteSpace: 'nowrap', height: 'fit-content' }}
                                    >
                                        {generatingEmpId ? (
                                            <Spinner animation="border" size="sm" />
                                        ) : (
                                            'Generate ID'
                                        )}
                                    </Button>
                                )}
                            </div>
                        </Form.Group>

                        <div className="row">
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>First Name *</Form.Label>
                                    <Form.Control value={form.firstName} onChange={(e) => onChange('firstName', e.target.value)} />
                                </Form.Group>
                            </div>
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>Last Name *</Form.Label>
                                    <Form.Control value={form.lastName} onChange={(e) => onChange('lastName', e.target.value)} />
                                </Form.Group>
                            </div>
                        </div>

                        {/* Email with debounced check */}
                        <Form.Group className="mb-2">
                            <Form.Label>Email *</Form.Label>
                            <Form.Control
                                type="email"
                                value={form.email}
                                onChange={(e) => onChange('email', e.target.value)}
                                isInvalid={!!emailError}
                            />
                            {emailChecking && (
                                <Form.Text className="text-muted">
                                    <Spinner animation="border" size="sm" className="me-1" />
                                    Checking...
                                </Form.Text>
                            )}
                            <Form.Control.Feedback type="invalid">
                                {emailError}
                            </Form.Control.Feedback>
                        </Form.Group>

                        <Form.Group className="mb-2">
                            <Form.Label>{editing ? 'Password (leave blank to keep)' : 'Password *'}</Form.Label>
                            <Form.Control type="password" value={form.password} onChange={(e) => onChange('password', e.target.value)} />
                        </Form.Group>
                        <div className="row">
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>Role *</Form.Label>
                                    <Form.Select value={form.role} onChange={(e) => onChange('role', e.target.value)}>
                                        {ROLES.map((r) => (
                                            <option key={r} value={r}>{r}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </div>
                            <div className="col-6">
                                <Form.Group className="mb-2">
                                    <Form.Label>Department</Form.Label>
                                    <Form.Select value={form.departmentId} onChange={(e) => onChange('departmentId', e.target.value)}>
                                        <option value="">— None —</option>
                                        {departments.map((d) => (
                                            <option key={d.id} value={d.id}>{d.name}</option>
                                        ))}
                                    </Form.Select>
                                </Form.Group>
                            </div>
                        </div>
                        <Form.Group>
                            <Form.Check
                                type="checkbox"
                                label="Enabled"
                                checked={form.enabled}
                                onChange={(e) => onChange('enabled', e.target.checked)}
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>Cancel</Button>
                    <Button
                        variant="primary"
                        size="sm"
                        onClick={handleSave}
                        disabled={saving || !!empIdError || !!emailError || empIdChecking || emailChecking}
                    >
                        {saving ? 'Saving...' : editing ? 'Update' : 'Create'}
                    </Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
