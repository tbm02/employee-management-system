import { useState, useEffect } from 'react';
import { Modal, Button, Form, Table, Badge, Spinner, Alert } from 'react-bootstrap';
import * as departmentService from '../services/departmentService';
import * as userService from '../services/userService';

const emptyForm = { name: '', description: '' };

export default function DepartmentManagementPage() {
    const [departments, setDepartments] = useState([]);
    const [allUsers, setAllUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [showModal, setShowModal] = useState(false);
    const [editing, setEditing] = useState(null);
    const [form, setForm] = useState({ ...emptyForm });
    const [formError, setFormError] = useState('');
    const [saving, setSaving] = useState(false);

    // View-users modal state
    const [showUsersModal, setShowUsersModal] = useState(false);
    const [selectedDept, setSelectedDept] = useState(null);
    const [deptUsers, setDeptUsers] = useState([]);

    const fetchData = async () => {
        setLoading(true);
        setError('');
        try {
            const [deptRes, usersRes] = await Promise.all([
                departmentService.getAll(),
                userService.getAll(),
            ]);
            setDepartments(deptRes.data || []);
            setAllUsers(usersRes.data || []);
        } catch {
            setError('Failed to load data.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const getUserCount = (deptId) => allUsers.filter((u) => u.departmentId === deptId).length;

    const openViewUsers = (dept) => {
        setSelectedDept(dept);
        setDeptUsers(allUsers.filter((u) => u.departmentId === dept.id));
        setShowUsersModal(true);
    };

    const openCreate = () => {
        setEditing(null);
        setForm({ ...emptyForm });
        setFormError('');
        setShowModal(true);
    };

    const openEdit = (dept) => {
        setEditing(dept.id);
        setForm({ name: dept.name, description: dept.description || '' });
        setFormError('');
        setShowModal(true);
    };

    const handleSave = async () => {
        setFormError('');
        if (!form.name.trim()) {
            setFormError('Department name is required.');
            return;
        }
        setSaving(true);
        try {
            if (editing) {
                await departmentService.update(editing, form);
            } else {
                await departmentService.create(form);
            }
            setShowModal(false);
            fetchData();
        } catch (err) {
            setFormError(err?.response?.data?.message || 'Failed to save department.');
        } finally {
            setSaving(false);
        }
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this department?')) return;
        try {
            await departmentService.remove(id);
            fetchData();
        } catch {
            setError('Failed to delete department.');
        }
    };

    const onChange = (field, value) => setForm((prev) => ({ ...prev, [field]: value }));

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
                <h3>Department Management</h3>
                <Button variant="primary" size="sm" onClick={openCreate}>
                    + Add Department
                </Button>
            </div>

            {error && <Alert variant="danger">{error}</Alert>}

            <Table striped bordered hover responsive size="sm">
                <thead className="table-dark">
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Description</th>
                        <th>Employees</th>
                        <th>Created At</th>
                        <th style={{ width: '200px' }}>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {departments.length === 0 ? (
                        <tr>
                            <td colSpan="6" className="text-center text-muted">
                                No departments found.
                            </td>
                        </tr>
                    ) : (
                        departments.map((d) => (
                            <tr key={d.id}>
                                <td>{d.id}</td>
                                <td>{d.name}</td>
                                <td>{d.description || '—'}</td>
                                <td>
                                    <Badge bg="info">{getUserCount(d.id)}</Badge>
                                </td>
                                <td>{d.createdAt ? new Date(d.createdAt).toLocaleDateString() : '—'}</td>
                                <td>
                                    <Button variant="outline-info" size="sm" className="me-1" onClick={() => openViewUsers(d)}>
                                        Users
                                    </Button>
                                    <Button variant="outline-primary" size="sm" className="me-1" onClick={() => openEdit(d)}>
                                        Edit
                                    </Button>
                                    <Button variant="outline-danger" size="sm" onClick={() => handleDelete(d.id)}>
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
                    <Modal.Title>{editing ? 'Edit Department' : 'Add Department'}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {formError && <Alert variant="danger" className="py-2">{formError}</Alert>}
                    <Form>
                        <Form.Group className="mb-3">
                            <Form.Label>Name *</Form.Label>
                            <Form.Control
                                value={form.name}
                                onChange={(e) => onChange('name', e.target.value)}
                                placeholder="e.g. Engineering"
                            />
                        </Form.Group>
                        <Form.Group>
                            <Form.Label>Description</Form.Label>
                            <Form.Control
                                as="textarea"
                                rows={2}
                                value={form.description}
                                onChange={(e) => onChange('description', e.target.value)}
                                placeholder="Optional description"
                            />
                        </Form.Group>
                    </Form>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowModal(false)}>Cancel</Button>
                    <Button variant="primary" size="sm" onClick={handleSave} disabled={saving}>
                        {saving ? 'Saving...' : editing ? 'Update' : 'Create'}
                    </Button>
                </Modal.Footer>
            </Modal>

            {/* View Users Modal */}
            <Modal show={showUsersModal} onHide={() => setShowUsersModal(false)} centered size="lg">
                <Modal.Header closeButton>
                    <Modal.Title>
                        Users in <strong>{selectedDept?.name}</strong>
                    </Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {deptUsers.length === 0 ? (
                        <p className="text-muted text-center mb-0">No users in this department.</p>
                    ) : (
                        <Table striped bordered hover size="sm" className="mb-0">
                            <thead className="table-light">
                                <tr>
                                    <th>Emp ID</th>
                                    <th>Name</th>
                                    <th>Email</th>
                                    <th>Role</th>
                                    <th>Enabled</th>
                                </tr>
                            </thead>
                            <tbody>
                                {deptUsers.map((u) => (
                                    <tr key={u.id}>
                                        <td>{u.empId}</td>
                                        <td>{u.firstName} {u.lastName}</td>
                                        <td>{u.email}</td>
                                        <td><Badge bg="secondary">{u.role}</Badge></td>
                                        <td>{u.enabled ? '✓' : '✗'}</td>
                                    </tr>
                                ))}
                            </tbody>
                        </Table>
                    )}
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" size="sm" onClick={() => setShowUsersModal(false)}>Close</Button>
                </Modal.Footer>
            </Modal>
        </div>
    );
}
