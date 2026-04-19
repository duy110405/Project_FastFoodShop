import React from 'react';
import { BrowserRouter, Routes, Route, Link, Outlet, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Button, Layout, Menu, Typography } from 'antd';
import {
  AppstoreOutlined,
  CreditCardOutlined,
  DashboardOutlined,
  DatabaseOutlined,
  FireOutlined,
  LogoutOutlined,
  OrderedListOutlined
} from '@ant-design/icons';

// Các trang giao diện
import FoodMenu from './pages/FoodMenu';
import KitchenPage from './pages/KitchenPage';
import PaymentPage from './pages/PaymentPage';
import Login from './pages/Login';
import AdminDashboard from './pages/AdminDashboard';
import InventoryPage from './pages/InventoryPage';

// Import trang MenuAdmin 
import MenuAdmin from './pages/MenuAdmin'; 

const OrdersPage = () => <div style={{ padding: 20 }}><h1>Màn hình Đơn hàng</h1></div>;

const { Header, Content, Footer, Sider } = Layout;
const { Title } = Typography;

const normalizeRole = (role) => {
  const normalized = (role || '').trim().toLowerCase();
  if (normalized === 'admin') return 'ADMIN';
  if (normalized === 'thu ngân' || normalized === 'thu ngan') return 'Thu ngân';
  if (normalized === 'bếp' || normalized === 'bep') return 'Bếp';
  if (normalized === 'khách hàng' || normalized === 'khach hang') return 'Khách hàng';
  return role || '';
};

const getStoredRole = () => normalizeRole(localStorage.getItem('userRole'));

const getRoleHomePath = (role) => {
  const normalizedRole = normalizeRole(role);
  if (normalizedRole === 'ADMIN') return '/admin/dashboard'; 
  if (normalizedRole === 'Thu ngân') return '/payment';
  if (normalizedRole === 'Bếp') return '/kitchen';
  if (normalizedRole === 'Khách hàng') return '/menu';
  return '/login';
};

const isRoleNavigable = (role) => getRoleHomePath(role) !== '/login';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const role = getStoredRole();
  if (!role) return <Navigate to="/login" replace />;
  if (allowedRoles && !allowedRoles.map(normalizeRole).includes(role)) {
    return <Navigate to={getRoleHomePath(role)} replace />;
  }
  return children;
};

const HomeRedirect = () => {
  return <Navigate to={getRoleHomePath(getStoredRole())} replace />;
};

// ========================================================
//  TÁCH RIÊNG MENU CỦA ADMIN VÀ KHÁCH
// ========================================================
const menuItems = [
  // 1. Nhóm nút chỉ hiện cho ADMIN
  { key: 'dashboard', path: '/admin/dashboard', label: 'Dashboard', icon: <DashboardOutlined />, roles: ['ADMIN'] },
  { key: 'orders', path: '/admin/orders', label: 'Đơn hàng', icon: <OrderedListOutlined />, roles: ['ADMIN'] },
  { key: 'admin-menu', path: '/admin/menu', label: 'Quản lý Menu', icon: <AppstoreOutlined />, roles: ['ADMIN'] }, // Sẽ mở MenuAdmin
  { key: 'inventory', path: '/admin/inventory', label: 'Kho', icon: <DatabaseOutlined />, roles: ['ADMIN'] },
  
  // 2. Nhóm nút cho KHÁCH & THU NGÂN
  { key: 'customer-menu', path: '/menu', label: 'Menu Đặt Món', icon: <AppstoreOutlined />, roles: ['Khách hàng', 'Thu ngân'] }, // Sẽ mở FoodMenu màu cam
  
  // 3. Nhóm nút cho BẾP
  { key: 'kitchen', path: '/kitchen', label: 'Bếp', icon: <FireOutlined />, roles: ['Bếp'] },
  { key: 'payment', path: '/payment', label: 'Thanh Toán', icon: <CreditCardOutlined />, roles: ['Thu ngân'] }
];

const MainLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const role = getStoredRole();

  const visibleMenuItems = menuItems.filter((item) => item.roles.map(normalizeRole).includes(role));
  const selectedKey = menuItems.find((item) => item.path === location.pathname)?.key || '';

  const handleLogout = () => {
    localStorage.removeItem('userRole');
    localStorage.removeItem('username');
    localStorage.removeItem('fullName');
    localStorage.removeItem('tableNumber');
    navigate('/login', { replace: true });
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider theme="dark" breakpoint="lg" collapsedWidth="0">
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6, display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'white', fontWeight: 'bold' }}>
          HỆ THỐNG POS
        </div>

        <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]}>
          {visibleMenuItems.map((item) => (
            <Menu.Item key={item.key} icon={item.icon}>
              <Link to={item.path}>{item.label}</Link>
            </Menu.Item>
          ))}
        </Menu>
      </Sider>

      <Layout>
        <Header style={{ background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 20px' }}>
          <Title level={4} style={{ margin: 0 }}>FastFood Dashboard</Title>
          <Button icon={<LogoutOutlined />} onClick={handleLogout}>Đăng xuất</Button>
        </Header>

        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ minHeight: 360, background: '#fff', borderRadius: 8 }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
};

function App() {
  const role = getStoredRole();
  const roleHomePath = getRoleHomePath(role);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={role && isRoleNavigable(role) ? <Navigate to={roleHomePath} replace /> : <Login />} />

        <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route path="/" element={<HomeRedirect />} />
          
          {/* ========================================================
              CẤP ROUTE CHUẨN CHO ADMIN
              ======================================================== */}
          <Route path="/admin">
            <Route path="dashboard" element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />
            <Route path="orders" element={<ProtectedRoute allowedRoles={['ADMIN']}><OrdersPage /></ProtectedRoute>} />
            
            {/* Nhét MenuAdmin vào đường dẫn /admin/menu */}
            <Route path="menu" element={<ProtectedRoute allowedRoles={['ADMIN']}><MenuAdmin /></ProtectedRoute>} />
            
            <Route path="inventory" element={<ProtectedRoute allowedRoles={['ADMIN']}><InventoryPage /></ProtectedRoute>} />
          </Route>

          {/* Các trang của Role khác */}
          <Route path="/menu" element={<ProtectedRoute allowedRoles={['Khách hàng', 'Thu ngân']}><FoodMenu /></ProtectedRoute>} />
          <Route path="/pos" element={<Navigate to="/menu" replace />} />
          <Route path="/kitchen" element={<ProtectedRoute allowedRoles={['Bếp']}><KitchenPage /></ProtectedRoute>} />
          <Route path="/payment" element={<ProtectedRoute allowedRoles={['Thu ngân']}><PaymentPage /></ProtectedRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;