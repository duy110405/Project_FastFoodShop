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
import MenuAdmin from './pages/MenuAdmin';
import OrderPage from './pages/OrderPage'; 
import './App.css';

const { Header, Content, Footer, Sider } = Layout;
const { Title } = Typography;

// ========================================================
//  CÁC THÀNH PHẦN HỖ TRỢ (HELPER)
// ========================================================

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
//  CẤU HÌNH MENU SIDEBAR
// ========================================================
const menuItems = [
  { key: 'dashboard', path: '/admin/dashboard', label: 'Dashboard', icon: <DashboardOutlined />, roles: ['ADMIN'] },
  { key: 'orders', path: '/admin/orders', label: 'Đơn hàng', icon: <OrderedListOutlined />, roles: ['ADMIN'] },
  { key: 'admin-menu', path: '/admin/menu', label: 'Quản lý Menu', icon: <AppstoreOutlined />, roles: ['ADMIN'] },
  { key: 'inventory', path: '/admin/inventory', label: 'Kho', icon: <DatabaseOutlined />, roles: ['ADMIN'] },
  { key: 'customer-menu', path: '/menu', label: 'Menu Đặt Món', icon: <AppstoreOutlined />, roles: ['Khách hàng', 'Thu ngân'] },
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
      {/* Sửa Sider: Đổi màu nền sang đỏ đô #a30000 */}
      <Sider 
        theme="dark" 
        breakpoint="lg" 
        collapsedWidth="0"
        style={{ backgroundColor: '#a30000' }}
      >
        <div style={{ 
          height: 60, 
          margin: 16, 
          background: 'rgba(255, 255, 255, 0.1)', 
          borderRadius: 8, 
          display: 'flex', 
          flexDirection: 'column',
          justifyContent: 'center', 
          alignItems: 'center', 
          color: '#ffd700', // Màu vàng Logo
          fontWeight: 'bold',
          border: '1px solid rgba(255, 215, 0, 0.3)'
        }}>
          <span style={{ fontSize: '18px', letterSpacing: '2px' }}>FAF POS</span>
          <span style={{ fontSize: '8px', color: '#fff' }}>HỆ THỐNG QUẢN LÝ</span>
        </div>

        {/* Sửa Menu: Đồng bộ CSS ngay trong file để ghi đè theme dark mặc định */}
        <Menu 
          theme="dark" 
          mode="inline" 
          selectedKeys={[selectedKey]}
          style={{ backgroundColor: '#a30000', borderRight: 0 }}
          className="faf-admin-menu"
        >
          {visibleMenuItems.map((item) => (
            <Menu.Item 
              key={item.key} 
              icon={React.cloneElement(item.icon, { style: { color: selectedKey === item.key ? '#a30000' : '#fff' } })}
              style={{
                backgroundColor: selectedKey === item.key ? '#ffd700' : 'transparent',
                borderRadius: '8px',
                margin: '4px 8px',
                color: selectedKey === item.key ? '#a30000' : '#fff',
                fontWeight: selectedKey === item.key ? 'bold' : 'normal',
              }}
            >
              <Link to={item.path} style={{ color: 'inherit' }}>{item.label}</Link>
            </Menu.Item>
          ))}
        </Menu>
      </Sider>

      <Layout style={{ background: '#fffaf0' }}> {/* Nền kem nhẹ cho trang nhã */}
        <Header style={{ 
            background: '#fff', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'space-between', 
            padding: '0 20px',
            boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            borderBottom: '2px solid #a30000' // Vạch kẻ đỏ thương hiệu
        }}>
          <Title level={4} style={{ margin: 0, color: '#a30000' }}>Hệ Thống FastFood FAF</Title>
          <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
             <span style={{ fontWeight: '600', color: '#555' }}>{localStorage.getItem('fullName')}</span>
             <Button 
                danger 
                type="primary" 
                icon={<LogoutOutlined />} 
                onClick={handleLogout}
                style={{ borderRadius: '6px', backgroundColor: '#a30000' }}
             >
                Đăng xuất
             </Button>
          </div>
        </Header>
        
        <Content style={{ margin: '24px 16px 0' }}>
          <div style={{ 
            minHeight: 360, 
            background: '#fff', 
            borderRadius: 12, 
            padding: '20px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.05)'
          }}>
            <Outlet />
          </div>
        </Content>

        <Footer style={{ textAlign: 'center', color: '#999' }}>
          FAF ©2026 - Đặt hàng tích tắc, Vị ngon xuất sắc
        </Footer>
      </Layout>
    </Layout>
  );
};

// ========================================================
//  COMPONENT CHÍNH (Giữ nguyên logic của bạn)
// ========================================================
function App() {
  const role = getStoredRole();
  const roleHomePath = getRoleHomePath(role);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={role && isRoleNavigable(role) ? <Navigate to={roleHomePath} replace /> : <Login />} />

        <Route element={<ProtectedRoute><MainLayout /></ProtectedRoute>}>
          <Route path="/" element={<HomeRedirect />} />
          
          <Route path="/admin">
            <Route 
                path="dashboard" 
                element={<ProtectedRoute allowedRoles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} 
            />
            <Route 
                path="orders" 
                element={<ProtectedRoute allowedRoles={['ADMIN']}><OrderPage /></ProtectedRoute>} 
            />
            <Route 
                path="menu" 
                element={<ProtectedRoute allowedRoles={['ADMIN']}><MenuAdmin /></ProtectedRoute>} 
            />
            <Route 
                path="inventory" 
                element={<ProtectedRoute allowedRoles={['ADMIN']}><InventoryPage /></ProtectedRoute>} 
            />
          </Route>

          <Route path="/menu" element={<ProtectedRoute allowedRoles={['Khách hàng', 'Thu ngân']}><FoodMenu /></ProtectedRoute>} />
          <Route path="/kitchen" element={<ProtectedRoute allowedRoles={['Bếp']}><KitchenPage /></ProtectedRoute>} />
          <Route path="/payment" element={<ProtectedRoute allowedRoles={['Thu ngân']}><PaymentPage /></ProtectedRoute>} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;