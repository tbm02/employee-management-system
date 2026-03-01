import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function AppNavbar() {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/login');
    };

    return (
        <nav className="navbar navbar-expand-lg navbar-dark bg-dark">
            <div className="container-fluid">
                <Link className="navbar-brand" to="/dashboard">
                    <i className="bi bi-building me-2"></i>EMS
                </Link>
                <button
                    className="navbar-toggler"
                    type="button"
                    data-bs-toggle="collapse"
                    data-bs-target="#navbarNav"
                >
                    <span className="navbar-toggler-icon"></span>
                </button>
                <div className="collapse navbar-collapse" id="navbarNav">
                    <ul className="navbar-nav me-auto">
                        <li className="nav-item">
                            <Link className="nav-link" to="/dashboard">Dashboard</Link>
                        </li>
                        <li className="nav-item">
                            <Link className="nav-link" to="/calendar">Calendar</Link>
                        </li>
                        <li className="nav-item">
                            <Link className="nav-link" to="/appraisals/my-reviews">My Reviews</Link>
                        </li>
                        {(user?.role === 'ADMIN' || user?.role === 'HR') && (
                            <>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/users">Users</Link>
                                </li>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/departments">Departments</Link>
                                </li>
                                <li className="nav-item">
                                    <Link className="nav-link" to="/appraisals">Appraisals</Link>
                                </li>
                            </>
                        )}
                    </ul>
                    <div className="d-flex align-items-center">
                        <span className="text-light me-3">
                            <small>{user?.email} ({user?.role})</small>
                        </span>
                        <button className="btn btn-outline-light btn-sm" onClick={handleLogout}>
                            Logout
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
}
