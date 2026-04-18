import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { Layout, Menu, Typography } from 'antd';
import { ShopOutlined, DatabaseOutlined, FireOutlined, UserOutlined } from '@ant-design/icons';

const { Header, Content, Footer, Sider } = Layout;
const { Title } = Typography;

// Mấy component này là chỗ để 4 anh em code nè
const POSPage = () => <div style={{ padding: 20 }}><h1>Màn hình Thu Ngân (Chờ code...)</h1></div>;
const KitchenPage = () => <div style={{ padding: 20 }}><h1>Màn hình Bếp (Chờ code...)</h1></div>;
const InventoryPage = () => <div style={{ padding: 20 }}><h1>Màn hình Kho (Chờ code...)</h1></div>;

function App() {
  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        {/* Cột Menu bên trái */}
        <Sider theme="dark" breakpoint="lg" collapsedWidth="0">
          <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6 }} />
          <Menu theme="dark" mode="inline" defaultSelectedKeys={['1']}>
            <Menu.Item key="1" icon={<ShopOutlined />}>
              <Link to="/pos">Bán hàng (POS)</Link>
            </Menu.Item>
            <Menu.Item key="2" icon={<FireOutlined />}>
              <Link to="/kitchen">Nhà Bếp</Link>
            </Menu.Item>
            <Menu.Item key="3" icon={<DatabaseOutlined />}>
              <Link to="/inventory">Quản lý Kho</Link>
            </Menu.Item>
            <Menu.Item key="4" icon={<UserOutlined />}>
              <Link to="/login">Đăng xuất</Link>
            </Menu.Item>
          </Menu>
        </Sider>

        {/* Nội dung chính bên phải */}
        <Layout>
          <Header style={{ padding: 0, background: '#fff', display: 'flex', alignItems: 'center', paddingLeft: 20 }}>
            <Title level={4} style={{ margin: 0 }}>Hệ Thống Quản Lý FastFood</Title>
          </Header>
          
          <Content style={{ margin: '24px 16px 0' }}>
            <div style={{ padding: 24, minHeight: 360, background: '#fff', borderRadius: 8 }}>
              {/* Vùng này sẽ tự động thay đổi tùy theo đường dẫn link */}
              <Routes>
                <Route path="/" element={<POSPage />} />
                <Route path="/pos" element={<POSPage />} />
                <Route path="/kitchen" element={<KitchenPage />} />
                <Route path="/inventory" element={<InventoryPage />} />
                {/* Trang Login thì sau này làm full màn hình sau, giờ cứ nhét tạm đây */}
                <Route path="/login" element={<div style={{ padding: 20 }}><h1>Trang Đăng Nhập</h1></div>} />
              </Routes>
            </div>
          </Content>
          
          <Footer style={{ textAlign: 'center' }}>
            FastFood ©{new Date().getFullYear()} 
          </Footer>
        </Layout>
      </Layout>
    </BrowserRouter>
  );
}

export default App;