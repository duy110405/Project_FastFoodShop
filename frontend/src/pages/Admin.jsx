import React from 'react';
import { Routes, Route, Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Layout, Menu, Typography, Button } from 'antd';
import {
  DashboardOutlined,
  OrderedListOutlined,
  AppstoreOutlined,
  DatabaseOutlined,
  LogoutOutlined
} from '@ant-design/icons';


import MenuAdmin from './MenuAdmin';

const { Header, Content, Sider, Footer } = Layout;
const { Title } = Typography;

// Mock tạm các trang con chờ code
const DashboardPage = () => <div style={{ padding: 20 }}><h1>Màn hình Dashboard</h1></div>;
const OrdersPage = () => <div style={{ padding: 20 }}><h1>Màn hình Đơn hàng</h1></div>;
const InventoryPage = () => <div style={{ padding: 20 }}><h1>Màn hình Kho</h1></div>;

const Admin = () => {
  const location = useLocation();
  const navigate = useNavigate();

  // Lấy đường dẫn hiện tại để làm sáng Tab Menu bên trái
  // Ví dụ gõ /admin/orders -> lấy chữ 'orders'
  const selectedKey = location.pathname.split('/')[2] || 'dashboard';

  const handleLogout = () => {
    localStorage.removeItem('userRole');
    localStorage.removeItem('username');
    localStorage.removeItem('fullName');
    navigate('/login', { replace: true });
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      
      {/* ================= CỘT MENU BÊN TRÁI ================= */}
      <Sider theme="dark" breakpoint="lg" collapsedWidth="0">
        <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6, display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'white', fontWeight: 'bold' }}>
          ADMIN PANEL
        </div>

        <Menu theme="dark" mode="inline" selectedKeys={[selectedKey]}>
          <Menu.Item key="dashboard" icon={<DashboardOutlined />}>
            <Link to="/admin/dashboard">Dashboard</Link>
          </Menu.Item>
          
          <Menu.Item key="orders" icon={<OrderedListOutlined />}>
            <Link to="/admin/orders">Đơn hàng</Link>
          </Menu.Item>
          
          <Menu.Item key="menu" icon={<AppstoreOutlined />}>
            <Link to="/admin/menu">Quản lý Menu</Link>
          </Menu.Item>
          
          <Menu.Item key="inventory" icon={<DatabaseOutlined />}>
            <Link to="/admin/inventory">Quản lý Kho</Link>
          </Menu.Item>
        </Menu>
      </Sider>

      {/* ================= BÊN PHẢI (HEADER + NỘI DUNG) ================= */}
      <Layout>
        <Header style={{ background: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '0 20px' }}>
          <Title level={4} style={{ margin: 0 }}>Hệ thống Quản trị FastFood</Title>
          <Button type="primary" danger icon={<LogoutOutlined />} onClick={handleLogout}>
            Đăng xuất
          </Button>
        </Header>

        <Content style={{ margin: '24px 16px 0' }}>
          {/* CÁI KHUNG TRẮNG NÀY LÀ NƠI CHỨA TRANG CON */}
          <div style={{ padding: 24, minHeight: 360, background: '#fff', borderRadius: 8 }}>
            
            {/*  ĐẶT TRỰC TIẾP ROUTER Ở ĐÂY LUÔN */}
            <Routes>
              {/* Gõ /admin -> Tự nhảy sang /admin/dashboard */}
              <Route path="/" element={<Navigate to="dashboard" replace />} />
              
              {/* Các trang con bên trong Khung */}
              <Route path="dashboard" element={<DashboardPage />} />
              <Route path="orders" element={<OrdersPage />} />
              <Route path="menu" element={<MenuAdmin />} />
              <Route path="inventory" element={<InventoryPage />} />
            </Routes>

          </div>
        </Content>
      </Layout>

    </Layout>
  );
};

export default Admin;