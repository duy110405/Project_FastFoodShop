import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient.js'; // Đảm bảo đường dẫn này đúng với project của bạn
import './Login.css'; // Import file giao diện vừa tạo ở trên

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
    // 1. Tạo các state để lưu dữ liệu người dùng gõ vào
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    useEffect(() => {
        const savedRole = localStorage.getItem('userRole');
        if (savedRole && isRoleNavigable(savedRole)) {
            navigate(getRoleHomePath(savedRole), { replace: true });
        }
    }, [navigate]);

    // 2. Hàm xử lý khi bấm nút Đăng nhập
    const handleLogin = async (e) => {
        e.preventDefault(); // Chặn việc tự động reload trang của thẻ form
        setError('');
        setLoading(true); // Hiển thị trạng thái đang load

        try {
            // Gửi API lên Spring Boot
            const response = await apiClient.post('/auth/login', { username, password });

            // Lấy vai trò (role) từ Backend trả về
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
            // Nếu Spring Boot trả về lỗi 401 (Sai tài khoản/Mật khẩu)
            setError(err?.response?.data || 'Sai tài khoản hoặc mật khẩu!');
        } finally {
            setLoading(false); // Tắt trạng thái load
        }
    };

    // 3. Phần giao diện (JSX)
    return (
        <div className="page-container">
            {/* Thanh bên (Sidebar) màu xám bên trái */}
            <div className="sidebar">
                <div className="logo">LOGO</div>
                <div className="slogan-box">
                    <p className="large-gap">Slogan và 1 số ảnh liên quan</p>
                    <p>ăn nhanh chết sớm</p>
                </div>
            </div>

            {/* Nội dung chính màu trắng bên phải */}
            <div className="main-content">
                <p className="instruction-text">Ảnh nền chủ đề đồ ăn</p>
                <div className="login-card">
                    <h2>Đăng nhập vào hệ thống</h2>

                    {/* Gắn hàm handleLogin vào sự kiện submit của form */}
                    <form onSubmit={handleLogin}>
                        <div className="input-group">
                            <label htmlFor="username">Tài khoản</label>
                            <input
                                type="text"
                                id="username"
                                placeholder="Tên tài khoản"
                                value={username}
                                onChange={(e) => setUsername(e.target.value)} // Lưu chữ khách gõ vào state
                                required
                            />
                        </div>
                        <div className="input-group">
                            <label htmlFor="password">Mật khẩu</label>
                            <input
                                type="password"
                                id="password"
                                placeholder="Nhập mật khẩu"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)} // Lưu pass khách gõ vào state
                                required
                            />
                        </div>

                        {/* Hiện thông báo chữ màu đỏ nếu nhập sai mật khẩu */}
                        {error && <p style={{ color: 'red', textAlign: 'center', marginBottom: '15px' }}>{error}</p>}

                        <button type="submit" className="login-button" disabled={loading}>
                            {loading ? 'Đang xử lý...' : 'Đăng nhập'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default Login;