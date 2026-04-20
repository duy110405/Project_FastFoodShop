import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Button, Typography, message, Badge, Modal, List, Tabs, Space } from 'antd';
import { ShoppingCartOutlined, ArrowLeftOutlined, MinusOutlined, PlusOutlined, CloseOutlined } from '@ant-design/icons';
import '../css/FoodMenu.css';
import axios from 'axios';

const { Title, Text } = Typography;
const API_BASE_URL = 'http://localhost:8080/api';
const POLL_INTERVAL_MS = 10000;

const resolveTableNumberFromUser = () => {
  const fullName = localStorage.getItem('fullName') || '';
  const username = localStorage.getItem('username') || '';
  const source = `${fullName} ${username}`;

  const tableMatch = source.match(/([A-Z]\d{2})/i);
  if (tableMatch?.[1]) {
    const normalized = `Bàn ${tableMatch[1].toUpperCase()}`;
    localStorage.setItem('tableNumber', normalized);
    return normalized;
  }

  const savedTable = localStorage.getItem('tableNumber');
  if (savedTable) {
    return savedTable;
  }

  return 'Bàn A01';
};

const toTableCode = (value) => {
  const source = (value || '').toUpperCase().replace('BÀN', '').replace('BAN', '').trim();
  const match = source.match(/[A-Z]\d{2}/);
  return match ? match[0] : 'A01';
};

const FoodMenu = () => {
  const [tableNumber] = useState(resolveTableNumberFromUser);
  const [menuItems, setMenuItems] = useState([]); // Chứa TẤT CẢ món ăn
  const [filteredItems, setFilteredItems] = useState([]); // Chứa món ăn ĐÃ LỌC theo Tab
  const [categories, setCategories] = useState([]); // Chứa danh sách Tab (Danh mục)
  const [activeCategory, setActiveCategory] = useState('ALL');
  
  const [cart, setCart] = useState([]);
  const [isCartModalOpen, setIsCartModalOpen] = useState(false); 
  const [selectedFood, setSelectedFood] = useState(null); 
  const [foodQuantity, setFoodQuantity] = useState(1);

  const fetchCategories = async ({ silent = false } = {}) => {
    try {
      const res = await axios.get(`${API_BASE_URL}/foodCategory`);
      setCategories(res.data.data || []);
    } catch (err) {
      console.error(err);
      if (!silent) {
        message.error("Lỗi lấy danh mục!");
      }
    }
  };

  const fetchFoods = async ({ silent = false } = {}) => {
    try {
      const res = await axios.get(`${API_BASE_URL}/foods/menu`);
      const nextFoods = res.data.data || [];
      setMenuItems(nextFoods);
      setFilteredItems(activeCategory === 'ALL'
        ? nextFoods
        : nextFoods.filter(food => food.idCategory === activeCategory)
      );
    } catch (err) {
      console.error(err);
      if (!silent) {
        message.error("Lỗi kết nối Backend!");
      }
    }
  };

  useEffect(() => {
    fetchCategories();
    fetchFoods();

    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchCategories({ silent: true });
        fetchFoods({ silent: true });
      }
    }, POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [activeCategory]);   // [] có nghĩa là chỉ chạy 1 lần khi Load trang

  // ====================LOGIC LỌC KHI BẤM VÀO TAB ====================
const handleTabChange = (idCategory) => {
    setActiveCategory(idCategory);
    if (idCategory === "ALL") {
      setFilteredItems(menuItems); 
    } else {
      const filtered = menuItems.filter(food => food.idCategory === idCategory);
      setFilteredItems(filtered);
    }
  };

  // ==================== API ĐẶT MÓN  ====================
const handlePlaceOrder = async () => {
    if (cart.length === 0) return message.warning("Giỏ hàng đang trống!");

    const orderPayload = {
      tableNumber: toTableCode(tableNumber),
      customerName: "Khách Lẻ",
      createdBy: "U_001", 
      items: cart.map(item => ({
        foodId: item.idFood,
        quantity: item.quantity
      }))
    };

    try {
      // API chuẩn: http://localhost:8080/api/v1/sales/orders
      await axios.post(`${API_BASE_URL}/v1/sales/orders`, orderPayload);
      message.success("Đặt món thành công! Bếp đang chuẩn bị.");
      setCart([]); 
      setIsCartModalOpen(false); 
    } catch (err) {
      const errorMsg = err.response?.data?.error || "Lỗi đặt món!";
      if (errorMsg.includes("HẾT_HÀNG")) {
         message.error(`Món ${errorMsg.split('|')[1]} đã hết nguyên liệu trong kho!`);
      } else {
         message.error(errorMsg);
      }
    }
  };

  const openFoodDetail = (food) => { setSelectedFood(food); setFoodQuantity(1); };
  const closeFoodDetail = () => { setSelectedFood(null); };

  const handleAddToCart = () => {
    const existingItem = cart.find(item => item.idFood === selectedFood.idFood);
    if (existingItem) {
      setCart(cart.map(item => item.idFood === selectedFood.idFood ? { ...item, quantity: item.quantity + foodQuantity } : item));
    } else {
      setCart([...cart, { ...selectedFood, quantity: foodQuantity }]);
    }
    message.success(`Đã thêm vào giỏ!`);
    closeFoodDetail(); 
  };

  const updateCartQuantity = (foodId, delta) => {
    setCart(cart.map(item => {
      if (item.idFood === foodId) {
        const newQty = item.quantity + delta;
        return { ...item, quantity: newQty > 0 ? newQty : 1 }; 
      }
      return item;
    }));
  };

  const removeFromCart = (foodId) => setCart(cart.filter(item => item.idFood !== foodId));
  const calculateTotal = () => cart.reduce((total, item) => total + (item.unitPrice * item.quantity), 0);
  const getTotalItems = () => cart.reduce((total, item) => total + item.quantity, 0);

  // Ép cái mảng categories (Lấy từ API) thành định dạng Tabs của Ant Design
  const tabItems = [
    { key: 'ALL', label: 'TẤT CẢ' },
    ...categories.map(cat => ({
      key: cat.idCategory, // ID của danh mục làm Khóa
      label: cat.categoryName.toUpperCase() 
    }))
  ];

  return (
    <div>
      {/* 1. HEADER */}
      <div className="header">
        <Badge count={getTotalItems()} offset={[5, 0]}>
          <Button type="text" icon={<ShoppingCartOutlined style={{ fontSize: 28, color: 'white' }} />} onClick={() => setIsCartModalOpen(true)} />
        </Badge>
        <Title level={3} style={{ margin: 0, color: 'white' }}>LOGO</Title>
        <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
          <Text strong style={{ fontSize: 16, color: 'white' }}>{tableNumber}</Text>
          <Button type="text" icon={<ArrowLeftOutlined style={{ fontSize: 24, color: 'white' }} />} />
        </div>
      </div>

      <div style={{ padding: '0 20px' }}>
        {/* 2. TABS DANH MỤC */}
        <Tabs 
          defaultActiveKey="ALL" 
          items={tabItems} 
          centered 
          size="large" 
          onChange={handleTabChange} 
        />
        
        {/* COMBO CON (Vẫn để mock tĩnh tạm thời) */}
        <div style={{ display: 'flex', gap: 10, overflowX: 'auto', paddingBottom: 10, scrollbarWidth: 'none' }}>
          {['combo A', 'combo B', 'combo C'].map(combo => (
             <Button key={combo} shape="round" className="combo-btn" style={{ minWidth: 90 }}>{combo}</Button>
          ))}
        </div>
        
        <div style={{ background: '#d9d9d9', height: 180, borderRadius: 12, margin: '20px 0', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <Text type="secondary" strong>BANNER</Text>
        </div>

        {/* 3. GRID MÓN ĂN */}
        <Row gutter={[16, 16]}>
          {filteredItems.length === 0 ? (
             <div style={{ width: '100%', textAlign: 'center', padding: 50 }}><Text type="secondary">Không có món nào trong danh mục này</Text></div>
          ) : (
            filteredItems.map((food) => (
              <Col xs={12} sm={8} md={8} key={food.idFood}>
                <Card
                  hoverable
                  className="food-card"
                  styles={{ body: { padding: 12, textAlign: 'center', background: '#fff' } }}
                  cover={<div style={{ padding: 10, background: '#f5f5f5' }}>
                     <img alt={food.foodName} src={food.imageUrlFood} style={{ width: '100%', height: 120, objectFit: 'cover', borderRadius: 8 }} />
                  </div>}
                >
                  <Title level={5} style={{ margin: '5px 0' }}>{food.foodName}</Title>
                  <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>{food.unitPrice.toLocaleString('vi-VN')} đ</Text>
                  
                  <Button 
                    type="primary" shape="round" className="btn-primary-custom" style={{ width: '80%' }} 
                    onClick={() => openFoodDetail(food)}
                    disabled={!food.available}
                  >
                    {food.available ? 'Thêm' : 'Hết món'}
                  </Button>
                </Card>
              </Col>
            ))
          )}
        </Row>
      </div>

      {/* ================= MODAL 1: CHI TIẾT MÓN ĂN ================= */}
      <Modal open={!!selectedFood} onCancel={closeFoodDetail} footer={null} closeIcon={<CloseOutlined style={{ fontSize: 20 }} />} bodyStyle={{ padding: 0 }} width={400}>
        {selectedFood && (
          <div>
            <img src={selectedFood.imageUrlFood} alt={selectedFood.foodName} style={{ width: '100%', height: 250, objectFit: 'cover', borderRadius: '8px 8px 0 0' }} />
            <div style={{ padding: 20 }}>
              <Title level={4} style={{ margin: 0 }}>{selectedFood.foodName}</Title>
              <Text type="secondary">{selectedFood.description}</Text>
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 30 }}>
                <Space size="middle">
                  <Button shape="circle" icon={<MinusOutlined />} onClick={() => setFoodQuantity(Math.max(1, foodQuantity - 1))} />
                  <Text strong style={{ fontSize: 18 }}>{foodQuantity}</Text>
                  <Button shape="circle" icon={<PlusOutlined />} onClick={() => setFoodQuantity(foodQuantity + 1)} />
                </Space>
                
                <Button type="primary" shape="round" size="large" className="btn-primary-custom" onClick={handleAddToCart}>
                  Thêm vào giỏ ({(selectedFood.unitPrice * foodQuantity).toLocaleString('vi-VN')} đ)
                </Button>
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* ================= MODAL 2: GIỎ HÀNG ================= */}
      <Modal title={<Title level={4} style={{ textAlign: 'center', margin: 0 }}>GIỎ HÀNG</Title>} open={isCartModalOpen} onCancel={() => setIsCartModalOpen(false)} footer={null} closeIcon={<CloseOutlined style={{ fontSize: 20 }} />} width={450} bodyStyle={{ background: '#f5f5f5', padding: 10, maxHeight: '60vh', overflowY: 'auto' }}>
        <List
          dataSource={cart}
          renderItem={item => (
            <Card size="small" style={{ marginBottom: 10, borderRadius: 8 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <img src={item.imageUrlFood} alt={item.foodName} style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4, marginRight: 10 }} />
                <div style={{ flex: 1 }}>
                  <Text strong>{item.foodName}</Text><br/>
                  <Text type="secondary">{item.unitPrice.toLocaleString('vi-VN')} đ</Text>
                </div>
                <Space>
                  <Button size="small" icon={<MinusOutlined />} onClick={() => updateCartQuantity(item.idFood, -1)} />
                  <Text>{item.quantity}</Text>
                  <Button size="small" icon={<PlusOutlined />} onClick={() => updateCartQuantity(item.idFood, 1)} />
                  <Button size="small" type="text" danger onClick={() => removeFromCart(item.idFood)}>Xóa</Button>
                </Space>
              </div>
            </Card>
          )}
        />
        
        <div style={{ background: '#fff', padding: 15, borderRadius: 8, marginTop: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} type="danger" style={{ margin: 0 }}>Tổng: {calculateTotal().toLocaleString('vi-VN')} đ</Title>
          <Button type="primary" size="large" shape="round" className="btn-primary-custom" onClick={handlePlaceOrder}>
            Đặt món
          </Button>
        </div>
      </Modal>

    </div>
  );
};

export default FoodMenu;