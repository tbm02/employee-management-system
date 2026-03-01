import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const userData = await login(identifier, password);
            if (userData.firstLogin || !userData.passwordUpdated) {
                navigate('/change-password');
            } else if (!userData.personalDetailsUpdated) {
                navigate('/personal-details');
            } else {
                navigate('/dashboard');
            }
        } catch (err) {
            const msg =
                err?.response?.data?.message || err.message || 'Login failed. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
            <div className="card shadow" style={{ width: '100%', maxWidth: '420px' }}>
                <div className="card-body p-4">
                    <h4 className="card-title text-center mb-4">
                        <i className="bi bi-building me-2"></i>EMS Login
                    </h4>

                    {error && (
                        <div className="alert alert-danger py-2" role="alert">
                            {error}
                        </div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="identifier" className="form-label">
                                Email or Employee ID
                            </label>
                            <input
                                type="text"
                                className="form-control"
                                id="identifier"
                                placeholder="e.g. john@company.com or EMP001"
                                value={identifier}
                                onChange={(e) => setIdentifier(e.target.value)}
                                required
                            />
                        </div>

                        <div className="mb-3">
                            <label htmlFor="password" className="form-label">
                                Password
                            </label>
                            <input
                                type="password"
                                className="form-control"
                                id="password"
                                placeholder="Enter your password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary w-100"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                                    Signing in...
                                </>
                            ) : (
                                'Sign In'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}
