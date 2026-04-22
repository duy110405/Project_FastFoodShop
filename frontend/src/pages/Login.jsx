import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient.js';
import './Login.css';

const normalizeRole = (role) => {
    const normalized = (role || '').trim().toLowerCase();
    if (normalized === 'admin') return 'ADMIN';
    if (normalized === 'thu ngân' || normalized === 'thu ngan') return 'Thu ngân';
    if (normalized === 'bếp' || normalized === 'bep') return 'Bếp';
    if (normalized === 'khách hàng' || normalized === 'khach hang') return 'Khách hàng';
    return role || '';
};

const getRoleHomePath = (role) => {
    const normalizedRole = normalizeRole(role);
    if (normalizedRole === 'ADMIN') return '/dashboard';
    if (normalizedRole === 'Thu ngân') return '/payment';
    if (normalizedRole === 'Bếp') return '/kitchen';
    if (normalizedRole === 'Khách hàng') return '/menu';
    return '/login';
};

const isRoleNavigable = (role) => getRoleHomePath(role) !== '/login';

const Login = () => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    // Định nghĩa đường dẫn ảnh từ thư mục public
    const bannerPath = '/images/loginBanner.png';
    const bgPath = '/images/loginBackground.jpg';

    useEffect(() => {
        const savedRole = localStorage.getItem('userRole');
        if (savedRole && isRoleNavigable(savedRole)) {
            navigate(getRoleHomePath(savedRole), { replace: true });
        }
    }, [navigate]);

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const response = await apiClient.post('/auth/login', { username, password });
            const userRole = normalizeRole(response.data.role);
            localStorage.setItem('userRole', userRole);
            localStorage.setItem('username', response.data.username || '');
            localStorage.setItem('fullName', response.data.fullName || '');

            const source = `${response.data.fullName || ''} ${response.data.username || ''}`;
            const tableMatch = source.match(/([A-Z]\d{2})/i);
            if (tableMatch?.[1]) {
                localStorage.setItem('tableNumber', `Bàn ${tableMatch[1].toUpperCase()}`);
            } else {
                localStorage.removeItem('tableNumber');
            }
            navigate(getRoleHomePath(userRole), { replace: true });
        } catch (err) {
            setError(err?.response?.data || 'Sai tài khoản hoặc mật khẩu!');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            {/* Sidebar bên trái */}
            <div className="login-sidebar">
                <img src={bannerPath} alt="Banner FAF" className="sidebar-img" />
            </div>

            {/* Vùng đăng nhập bên phải có ảnh nền burger */}
            <div className="login-content" style={{ backgroundImage: `url(${bgPath})` }}>
                <div className="login-overlay"></div>
                
                <div className="login-card">
                    <div className="login-header">
                        <h2>Hệ Thống FAF</h2>
                        <p>Đăng nhập để bắt đầu phục vụ</p>
                    </div>

                    <form onSubmit={handleLogin} className="login-form">
                        <div className="form-group">
                            <label>Tài khoản</label>
                            <input
                                type="text"
                                placeholder="Nhập tên tài khoản"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)}
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>Mật khẩu</label>
                            <input
                                type="password"
                                placeholder="Nhập mật khẩu"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                required
                            />
                        </div>

                        {error && <div className="error-alert">{error}</div>}

                        <button type="submit" className="btn-login" disabled={loading}>
                            {loading ? 'Đang xác thực...' : 'Đăng nhập'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;