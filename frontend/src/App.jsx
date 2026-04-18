import React from 'react';
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import { Layout, Menu, Typography } from 'antd';
import { ShopOutlined, DatabaseOutlined, FireOutlined, UserOutlined } from '@ant-design/icons';

// LÔI COMPONENT FoodMenu VÀO ĐÂY
import FoodMenu from './pages/FoodMenu'; 

// Mấy component này là chỗ để 3 anh em kia code
const KitchenPage = () => <div style={{ padding: 20 }}><h1>Màn hình Bếp (Chờ code...)</h1></div>;
const InventoryPage = () => <div style={{ padding: 20 }}><h1>Màn hình Kho (Chờ code...)</h1></div>;

const { Header, Content, Footer, Sider } = Layout;
const { Title } = Typography;

function App() {
  return (
    <BrowserRouter>
      <Layout style={{ minHeight: '100vh' }}>
        {/* CỘT MENU BÊN TRÁI (Dành cho Thu ngân / Quản lý) */}
        <Sider theme="dark" breakpoint="lg" collapsedWidth="0">
          <div style={{ height: 32, margin: 16, background: 'rgba(255, 255, 255, 0.2)', borderRadius: 6, display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'white', fontWeight: 'bold' }}>
            HỆ THỐNG POS
          </div>
          <Menu theme="dark" mode="inline" defaultSelectedKeys={['1']}>
            <Menu.Item key="1" icon={<ShopOutlined />}>
              <Link to="/pos">Máy POS (Kiosk)</Link>
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

        {/* KHUNG HIỂN THỊ NỘI DUNG CHÍNH (BÊN PHẢI) */}
        <Layout>
          {/* Cẩn thận chỗ này: Màn hình Kiosk của bạn ĐÃ CÓ Header riêng rồi. 
              Nếu bạn muốn giữ lại Header tổng của hệ thống thì để lại đoạn dưới đây. 
              Nếu muốn nó full màn hình (ẩn Header của App.jsx đi), bạn cứ xóa thẻ <Header> này đi nhé! */}
          <Header style={{ padding: 0, background: '#fff', display: 'flex', alignItems: 'center', paddingLeft: 20 }}>
            <Title level={4} style={{ margin: 0 }}>FastFood Team 4 Dashboard</Title>
          </Header>
          
          <Content style={{ margin: '24px 16px 0' }}>
            <div style={{ minHeight: 360, background: '#fff', borderRadius: 8 }}>
              
              {/* CHUYỂN HƯỚNG ROUTER TẠI ĐÂY */}
              <Routes>
                {/* Vừa vào Web là nhảy thẳng vào Kiosk */}
                <Route path="/" element={<FoodMenu />} />
                
                {/* Bấm vào link /pos cũng vào Kiosk */}
                <Route path="/pos" element={<FoodMenu />} />
                
                {/* Các trang chờ anh em code */}
                <Route path="/kitchen" element={<KitchenPage />} />
                <Route path="/inventory" element={<InventoryPage />} />
                <Route path="/login" element={<div style={{ padding: 20 }}><h1>Trang Đăng Nhập</h1></div>} />
              </Routes>

            </div>
          </Content>
          
          <Footer style={{ textAlign: 'center' }}>
            FastFood Team 4 ©{new Date().getFullYear()} - UI designed with Ant Design
          </Footer>
        </Layout>
      </Layout>
    </BrowserRouter>
  );
}

export default App;