import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Button, Typography, message, Badge, Modal, List, Tabs, Space } from 'antd';
import { ShoppingCartOutlined, ArrowLeftOutlined, MinusOutlined, PlusOutlined, CloseOutlined } from '@ant-design/icons';

const { Title, Text } = Typography;

const FoodMenu = () => {
  // --- STATE QUẢN LÝ DỮ LIỆU ---
  const [menuItems, setMenuItems] = useState([]);
  const [cart, setCart] = useState([]);
  
  // --- STATE QUẢN LÝ 2 CÁI POPUP (MODAL) ---
  const [isCartModalOpen, setIsCartModalOpen] = useState(false); // Modal Giỏ hàng (Ảnh 3)
  
  const [selectedFood, setSelectedFood] = useState(null); // Lưu món đang được bấm chọn
  const [foodQuantity, setFoodQuantity] = useState(1); // Số lượng trong Modal chi tiết món (Ảnh 2)

  // Gọi API lấy Menu
  useEffect(() => {
    const fetchMenu = async () => {
      try {
        const mockData = [
          { available: true, foodName: "Hamburger Bò Phô Mai", idFood: "H001", description: "Bò nướng lửa hồng, phô mai Cheddar", imageUrlFood: "https://cdn.pixabay.com/photo/2016/03/05/19/02/hamburger-1238246_1280.jpg", unitPrice: 45000 },
          { available: true, foodName: "Gà Rán Phần", idFood: "G001", description: "Gà giòn cay 2 miếng", imageUrlFood: "https://cdn.pixabay.com/photo/2014/10/19/20/59/hamburger-494706_1280.jpg", unitPrice: 35000 },
          { available: true, foodName: "Khoai Tây Chiên", idFood: "K001", description: "Khoai tây chiên giòn cỡ vừa", imageUrlFood: "https://cdn.pixabay.com/photo/2016/11/20/09/06/bowl-1842294_1280.jpg", unitPrice: 20000 },
        ];
        setMenuItems(mockData);
      } catch (error) {
        console.error(error);
        message.error("Lỗi tải thực đơn!");
      }
    };
    fetchMenu();
  }, []);

  // --- LOGIC CHO MODAL CHI TIẾT MÓN (ẢNH 2) ---
  const openFoodDetail = (food) => {
    setSelectedFood(food);
    setFoodQuantity(1); // Reset số lượng về 1 mỗi lần mở
  };

  const closeFoodDetail = () => {
    setSelectedFood(null);
  };

  const handleAddToCart = () => {
    const existingItem = cart.find(item => item.idFood === selectedFood.idFood);
    if (existingItem) {
      setCart(cart.map(item => item.idFood === selectedFood.idFood 
        ? { ...item, quantity: item.quantity + foodQuantity } 
        : item
      ));
    } else {
      setCart([...cart, { ...selectedFood, quantity: foodQuantity }]);
    }
    message.success(`Đã thêm ${foodQuantity} ${selectedFood.foodName} vào giỏ!`);
    closeFoodDetail(); // Thêm xong thì đóng popup
  };

  // --- LOGIC CHO GIỎ HÀNG (ẢNH 3) ---
  const updateCartQuantity = (foodId, delta) => {
    setCart(cart.map(item => {
      if (item.idFood === foodId) {
        const newQty = item.quantity + delta;
        return { ...item, quantity: newQty > 0 ? newQty : 1 }; // Không cho giảm dưới 1
      }
      return item;
    }));
  };

  const removeFromCart = (foodId) => setCart(cart.filter(item => item.idFood !== foodId));
  const calculateTotal = () => cart.reduce((total, item) => total + (item.unitPrice * item.quantity), 0);
  const getTotalItems = () => cart.reduce((total, item) => total + item.quantity, 0);

  return (
    <div style={{ background: '#f5f5f5', minHeight: '100vh', paddingBottom: 50 }}>
      {/* 1. HEADER */}
      <div style={{ background: '#e0e0e0', padding: '15px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderRadius: '0 0 16px 16px', margin: '0 10px' }}>
        <Badge count={getTotalItems()} offset={[5, 0]}>
          <Button type="text" icon={<ShoppingCartOutlined style={{ fontSize: 28 }} />} onClick={() => setIsCartModalOpen(true)} />
        </Badge>
        <Title level={3} style={{ margin: 0 }}>LOGO</Title>
        <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
          <Text strong style={{ fontSize: 16 }}>Bàn A01</Text>
          <Button type="text" icon={<ArrowLeftOutlined style={{ fontSize: 24 }} />} />
        </div>
      </div>

      <div style={{ padding: '0 20px' }}>
        {/* 2. TABS & BANNER */}
        <Tabs defaultActiveKey="2" items={[{ key: '1', label: 'COMBO' }, { key: '2', label: 'FOOD' }, { key: '3', label: 'DRINK' }]} centered size="large" />
        <div style={{ display: 'flex', gap: 10, overflowX: 'auto', paddingBottom: 10, scrollbarWidth: 'none' }}>
          {['combo A', 'combo B', 'combo C', 'combo D', 'combo E'].map(combo => (
             <Button key={combo} shape="round" style={{ minWidth: 90, background: '#d9d9d9', border: 'none' }}>{combo}</Button>
          ))}
        </div>
        <div style={{ background: '#d9d9d9', height: 180, borderRadius: 12, margin: '20px 0', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
          <Text type="secondary" strong>BANNER</Text>
        </div>

        {/* 3. GRID MÓN ĂN (ẢNH 1) */}
        <Row gutter={[16, 16]}>
          {menuItems.map((food) => (
            <Col xs={12} sm={8} md={8} key={food.idFood}>
              <Card
                hoverable
                styles={{ body: { padding: 12, textAlign: 'center', background: '#e0e0e0', borderRadius: '0 0 12px 12px' } }}
                cover={<div style={{ padding: 10, background: '#e0e0e0', borderRadius: '12px 12px 0 0' }}>
                   <img alt={food.foodName} src={food.imageUrlFood} style={{ width: '100%', height: 120, objectFit: 'cover', borderRadius: 8 }} />
                </div>}
              >
                <Title level={5} style={{ margin: '5px 0' }}>{food.foodName}</Title>
                <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>{food.unitPrice.toLocaleString('vi-VN')} đ</Text>
                <Button type="primary" shape="round" style={{ background: '#666', width: '80%' }} onClick={() => openFoodDetail(food)}>
                  Thêm
                </Button>
              </Card>
            </Col>
          ))}
        </Row>
      </div>

      {/* ================= MODAL 1: CHI TIẾT MÓN ĂN (ẢNH 2) ================= */}
      <Modal
        open={!!selectedFood} // Nếu selectedFood có dữ liệu thì mở Modal
        onCancel={closeFoodDetail}
        footer={null} // Tắt nút OK/Cancel mặc định để tự code Footer
        closeIcon={<CloseOutlined style={{ fontSize: 20 }} />}
        bodyStyle={{ padding: 0 }}
        width={400}
      >
        {selectedFood && (
          <div>
            <img src={selectedFood.imageUrlFood} alt={selectedFood.foodName} style={{ width: '100%', height: 250, objectFit: 'cover', borderRadius: '8px 8px 0 0' }} />
            <div style={{ padding: 20 }}>
              <Title level={4} style={{ margin: 0 }}>{selectedFood.foodName}</Title>
              <Text type="secondary">{selectedFood.description || "Thông tin món ăn"}</Text>
              
              {/* Footer chọn số lượng và Add */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 30 }}>
                <Space size="middle">
                  <Button shape="circle" icon={<MinusOutlined />} onClick={() => setFoodQuantity(Math.max(1, foodQuantity - 1))} />
                  <Text strong style={{ fontSize: 18 }}>{foodQuantity}</Text>
                  <Button shape="circle" icon={<PlusOutlined />} onClick={() => setFoodQuantity(foodQuantity + 1)} />
                </Space>
                <Button type="primary" shape="round" size="large" style={{ background: '#666' }} onClick={handleAddToCart}>
                  Thêm vào giỏ ({(selectedFood.unitPrice * foodQuantity).toLocaleString('vi-VN')} đ)
                </Button>
              </div>
            </div>
          </div>
        )}
      </Modal>

      {/* ================= MODAL 2: GIỎ HÀNG (ẢNH 3) ================= */}
      <Modal
        title={<Title level={4} style={{ textAlign: 'center', margin: 0 }}>GIỎ HÀNG</Title>}
        open={isCartModalOpen}
        onCancel={() => setIsCartModalOpen(false)}
        footer={null}
        closeIcon={<CloseOutlined style={{ fontSize: 20 }} />}
        width={450}
        bodyStyle={{ background: '#f5f5f5', padding: 10, maxHeight: '60vh', overflowY: 'auto' }}
      >
        <List
          dataSource={cart}
          renderItem={item => (
            <Card size="small" style={{ marginBottom: 10, borderRadius: 8 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <img src={item.imageUrlFood} alt={item.foodName} style={{ width: 60, height: 60, objectFit: 'cover', borderRadius: 4, marginRight: 10 }} />
                <div style={{ flex: 1 }}>
                  <Text strong>{item.foodName}</Text>
                  <br/>
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
        
        {/* Tổng tiền và nút Đặt món */}
        <div style={{ background: '#fff', padding: 15, borderRadius: 8, marginTop: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={4} type="danger" style={{ margin: 0 }}>
            Tổng: {calculateTotal().toLocaleString('vi-VN')} đ
          </Title>
          <Button type="primary" size="large" shape="round" style={{ background: '#d9d9d9', color: '#000', fontWeight: 'bold' }}>
            Đặt món
          </Button>
        </div>
      </Modal>

    </div>
  );
};

export default FoodMenu;