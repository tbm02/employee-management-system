import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { savePersonalDetails } from '../services/userService';

export default function PersonalDetailsPage() {
    const [phone, setPhone] = useState('');
    const [address, setAddress] = useState('');
    const [dob, setDob] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { user, markOnboardingComplete } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            await savePersonalDetails(user.userId, {
                phoneNumber: phone,
                address,
                dateOfBirth: dob || null,
            });
            markOnboardingComplete();
            navigate('/dashboard');
        } catch (err) {
            setError(err?.response?.data?.message || 'Failed to save details.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="d-flex justify-content-center align-items-center vh-100 bg-light">
            <div className="card shadow" style={{ width: '100%', maxWidth: '500px' }}>
                <div className="card-body p-4">
                    <h4 className="card-title text-center mb-2">Personal Details</h4>
                    <p className="text-muted text-center mb-4">
                        Please fill in your personal information to complete onboarding.
                    </p>

                    {error && (
                        <div className="alert alert-danger py-2">{error}</div>
                    )}

                    <form onSubmit={handleSubmit}>
                        <div className="mb-3">
                            <label htmlFor="phone" className="form-label">Phone Number</label>
                            <input
                                type="tel"
                                className="form-control"
                                id="phone"
                                placeholder="e.g. +91 9876543210"
                                value={phone}
                                onChange={(e) => setPhone(e.target.value)}
                            />
                        </div>

                        <div className="mb-3">
                            <label htmlFor="address" className="form-label">Address</label>
                            <textarea
                                className="form-control"
                                id="address"
                                rows="2"
                                placeholder="Your address"
                                value={address}
                                onChange={(e) => setAddress(e.target.value)}
                            ></textarea>
                        </div>

                        <div className="mb-3">
                            <label htmlFor="dob" className="form-label">Date of Birth</label>
                            <input
                                type="date"
                                className="form-control"
                                id="dob"
                                value={dob}
                                onChange={(e) => setDob(e.target.value)}
                            />
                        </div>

                        <button type="submit" className="btn btn-primary w-100" disabled={loading}>
                            {loading ? (
                                <>
                                    <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                                    Saving...
                                </>
                            ) : (
                                'Save & Continue'
                            )}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
}
