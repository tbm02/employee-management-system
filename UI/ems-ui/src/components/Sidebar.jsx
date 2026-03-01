import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Sidebar() {
    const { user } = useAuth();
    const isAdminOrHR = user?.role === 'ADMIN' || user?.role === 'HR';

    const linkClass = ({ isActive }) =>
        `list-group-item list-group-item-action ${isActive ? 'active' : ''}`;

    return (
        <div className="d-flex flex-column p-3 bg-light vh-100" style={{ width: '220px', minWidth: '220px' }}>
            <h6 className="text-muted text-uppercase mb-3">Menu</h6>
            <div className="list-group list-group-flush">
                <NavLink to="/dashboard" className={linkClass}>
                    <i className="bi bi-house-door me-2"></i>Dashboard
                </NavLink>
                <NavLink to="/calendar" className={linkClass}>
                    <i className="bi bi-calendar3 me-2"></i>Calendar
                </NavLink>
                <NavLink to="/goals" className={linkClass}>
                    <i className="bi bi-bullseye me-2"></i>Goals
                </NavLink>
                <NavLink to="/appraisals/my-reviews" className={linkClass}>
                    <i className="bi bi-star me-2"></i>My Reviews
                </NavLink>
                {isAdminOrHR && (
                    <>
                        <NavLink to="/users" className={linkClass}>
                            <i className="bi bi-people me-2"></i>Users
                        </NavLink>
                        <NavLink to="/departments" className={linkClass}>
                            <i className="bi bi-diagram-3 me-2"></i>Departments
                        </NavLink>
                        <NavLink to="/appraisals" className={linkClass}>
                            <i className="bi bi-clipboard-check me-2"></i>Appraisals
                        </NavLink>
                    </>
                )}
            </div>
        </div>
    );
}
