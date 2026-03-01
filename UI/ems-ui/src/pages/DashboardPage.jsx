import { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { getDashboard } from '../services/dashboardService';

export default function DashboardPage() {
    const { user } = useAuth();
    const [dashboard, setDashboard] = useState(null);

    useEffect(() => {
        const loadDashboard = async () => {
            try {
                const res = await getDashboard();
                setDashboard(res.data);
            } catch (error) {
                setDashboard(null);
            }
        };

        loadDashboard();
    }, []);

    return (
        <div>
            <h3 className="mb-4">Dashboard</h3>
            <div className="alert alert-info">
                <strong>Welcome, {user?.email}!</strong>
                <br />
                <small>Employee ID: {user?.empId} · Role: {user?.role}</small>
            </div>

            <div className="row g-3">
                <div className="col-md-4">
                    <div className="card text-bg-primary">
                        <div className="card-body">
                            <h5 className="card-title">Employees</h5>
                            <p className="card-text display-6">{dashboard?.totalEmployees ?? '—'}</p>
                            <small>Total active employees</small>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card text-bg-success">
                        <div className="card-body">
                            <h5 className="card-title">Departments</h5>
                            <p className="card-text display-6">{dashboard?.totalDepartments ?? '—'}</p>
                            <small>Total departments</small>
                        </div>
                    </div>
                </div>
                <div className="col-md-4">
                    <div className="card text-bg-warning">
                        <div className="card-body">
                            <h5 className="card-title">Pending</h5>
                            <p className="card-text display-6">{dashboard?.pendingRequests ?? '—'}</p>
                            <small>Pending requests</small>
                        </div>
                    </div>
                </div>
            </div>

            <div className="mt-4 p-3 bg-light rounded">
                <h6 className="text-muted">Quick Info</h6>
                <p className="mb-0 text-muted">
                    {dashboard?.note || 'This dashboard will show real-time stats once the modules are connected.'}
                </p>
            </div>
        </div>
    );
}
