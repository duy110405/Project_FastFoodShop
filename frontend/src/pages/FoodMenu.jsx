import React, { useEffect, useMemo, useState, useCallback } from 'react';
import {
  Row,
  Col,
  Card,
  Button,
  Typography,
  message,
  Badge,
  Modal,
  List,
  Tabs,
  Space,
  Input,
  Select
} from 'antd';
import {
  ShoppingCartOutlined,
  ArrowLeftOutlined,
  MinusOutlined,
  PlusOutlined,
  CloseOutlined
} from '@ant-design/icons';
import '../css/FoodMenu.css';
import axios from 'axios';

const { Title, Text } = Typography;
const API_BASE_URL = 'http://localhost:8080/api';
const POLL_INTERVAL_MS = 10000;
const bannerImg = './images/banner.jpg';
const logoImg = './images/FAF_logo.jpg';

const normalizeRole = (role) => {
  const normalized = (role || '').trim().toLowerCase();
  if (normalized === 'thu ngân' || normalized === 'thu ngan') return 'Thu ngân';
  if (normalized === 'khách hàng' || normalized === 'khach hang') return 'Khách hàng';
  if (normalized === 'admin') return 'ADMIN';
  if (normalized === 'bếp' || normalized === 'bep') return 'Bếp';
  return role || '';
};

// Chuẩn hóa về dạng Ban01
const extractTableCode = (value) => {
  const source = String(value || '')
    .trim()
    .replace(/\s+/g, '')
    .toUpperCase();

  if (!source) return '';

  if (/^BAN\d{2}$/.test(source)) {
    return source.replace(/^BAN/, 'Ban');
  }

  const normalizedFromLabel = source.replace(/BÀN/g, 'BAN');
  if (/^BAN\d{2}$/.test(normalizedFromLabel)) {
    return normalizedFromLabel.replace(/^BAN/, 'Ban');
  }

  // nếu backend cũ trả N01
  if (/^N\d{2}$/.test(source)) {
    return source.replace(/^N/, 'Ban');
  }

  if (/^\d{2}$/.test(source)) {
    return `Ban${source}`;
  }

  return '';
};

const toBackendTableCode = (value) => {
  const code = extractTableCode(value);
  return code || '';
};

const resolveInitialTableNumber = () => {
  const storedTable = localStorage.getItem('tableNumber') || '';
  const fullName = localStorage.getItem('fullName') || '';
  const username = localStorage.getItem('username') || '';

  const fromStored = extractTableCode(storedTable);
  if (fromStored) {
    localStorage.setItem('tableNumber', fromStored);
    return fromStored;
  }

  const fromUsername = extractTableCode(username);
  if (fromUsername) {
    localStorage.setItem('tableNumber', fromUsername);
    return fromUsername;
  }

  const fromFullName = extractTableCode(fullName);
  if (fromFullName) {
    localStorage.setItem('tableNumber', fromFullName);
    return fromFullName;
  }

  return '';
};

const FoodMenu = () => {
  const [userRole] = useState(() => normalizeRole(localStorage.getItem('userRole')));
  const isCashier = userRole === 'Thu ngân';

  const [tableNumber, setTableNumber] = useState(resolveInitialTableNumber);
  const [tableInput, setTableInput] = useState(resolveInitialTableNumber);
  const [tables, setTables] = useState([]);

  const [menuItems, setMenuItems] = useState([]);
  const [categories, setCategories] = useState([]);
  const [activeCategory, setActiveCategory] = useState('ALL');

  const [cart, setCart] = useState([]);
  const [isCartModalOpen, setIsCartModalOpen] = useState(false);
  const [selectedFood, setSelectedFood] = useState(null);
  const [foodQuantity, setFoodQuantity] = useState(1);

  const availableTableOptions = useMemo(() => {
    return tables
      .map((table) => {
        const rawValue =
          table?.username ||
          table?.tableNumber ||
          table?.fullName ||
          '';

        const code = extractTableCode(rawValue);
        if (!code) return null;

        return {
          value: code,
          label: code
        };
      })
      .filter(Boolean)
      .filter((item, index, arr) => arr.findIndex((x) => x.value === item.value) === index);
  }, [tables]);

  const effectiveTableCode = useMemo(() => extractTableCode(tableNumber), [tableNumber]);
  const canPlaceOrder = Boolean(effectiveTableCode);

  const filteredItems = useMemo(() => {
    return activeCategory === 'ALL'
      ? menuItems
      : menuItems.filter((food) => food.idCategory === activeCategory);
  }, [menuItems, activeCategory]);

  const fetchCategories = useCallback(async ({ silent = false } = {}) => {
    try {
      const res = await axios.get(`${API_BASE_URL}/foodCategory`);
      setCategories(res.data.data || []);
    } catch (err) {
      console.error(err);
      if (!silent) message.error('Lỗi lấy danh mục!');
    }
  }, []);

  const fetchFoods = useCallback(async ({ silent = false } = {}) => {
    try {
      const res = await axios.get(`${API_BASE_URL}/foods/menu`);
      setMenuItems(res.data.data || []);
    } catch (err) {
      console.error(err);
      if (!silent) message.error('Lỗi kết nối Backend!');
    }
  }, []);

  const fetchTables = useCallback(async ({ silent = false } = {}) => {
    try {
      const res = await axios.get(`${API_BASE_URL}/sales/tables`);
      setTables(res.data?.data || []);
    } catch (err) {
      console.error(err);
      if (!silent && isCashier) {
        message.error(err.response?.data?.message || 'Không tải được danh sách bàn');
      }
    }
  }, [isCashier]);

  useEffect(() => {
    fetchCategories();
    fetchFoods();
    if (isCashier) {
      fetchTables();
    }

    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchCategories({ silent: true });
        fetchFoods({ silent: true });
        if (isCashier) {
          fetchTables({ silent: true });
        }
      }
    }, POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [fetchCategories, fetchFoods, fetchTables, isCashier]);

  const handleTabChange = (idCategory) => {
    setActiveCategory(idCategory);
  };

  const commitCashierTable = (rawValue) => {
    const normalizedCode = extractTableCode(rawValue);

    if (!normalizedCode) {
      setTableNumber('');
      localStorage.removeItem('tableNumber');
      return false;
    }

    const existsInSystem =
      tables.length === 0 ||
      tables.some((table) => {
        const candidate =
          table?.username ||
          table?.tableNumber ||
          table?.fullName ||
          '';
        return extractTableCode(candidate) === normalizedCode;
      });

    if (!existsInSystem) {
      message.warning('Bàn này không tồn tại trong hệ thống. Vui lòng nhập đúng mã bàn như Ban01, Ban02...');
      return false;
    }

    setTableNumber(normalizedCode);
    setTableInput(normalizedCode);
    localStorage.setItem('tableNumber', normalizedCode);
    message.success(`Đang đặt món cho ${normalizedCode}`);
    return true;
  };

  const handleSelectTable = (value) => {
    if (!value) {
      setTableInput('');
      setTableNumber('');
      localStorage.removeItem('tableNumber');
      return;
    }

    setTableInput(value);
    commitCashierTable(value);
  };

  const handleTableInputChange = (e) => {
    const raw = e.target.value.replace(/\s+/g, '').slice(0, 5);
    setTableInput(raw);
  };

  const handleConfirmTableInput = () => {
    if (!commitCashierTable(tableInput)) {
      message.warning('Thu ngân cần nhập đúng mã bàn như Ban01 rồi mới đặt món được.');
    }
  };

  const handlePlaceOrder = async () => {
    if (cart.length === 0) {
      return message.warning('Giỏ hàng đang trống!');
    }

    if (isCashier && !effectiveTableCode) {
      return message.warning('Vui lòng nhập hoặc chọn bàn trước khi đặt món!');
    }

    const orderPayload = {
      tableNumber: toBackendTableCode(tableNumber),
      customerName: 'Khách lẻ',
      createdBy: localStorage.getItem('username') || '',
      items: cart.map((item) => ({
        foodId: item.idFood,
        quantity: item.quantity
      }))
    };

    try {
      await axios.post(`${API_BASE_URL}/sales/orders`, orderPayload);
      message.success(`Đặt món thành công cho ${effectiveTableCode}!`);
      setCart([]);
      setIsCartModalOpen(false);
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Lỗi đặt món!';
      if (errorMsg.includes('HẾT_HÀNG|')) {
        message.error(`Món ${errorMsg.split('|')[1]} đã hết nguyên liệu trong kho!`);
      } else {
        message.error(errorMsg);
      }
    }
  };

  const openFoodDetail = (food) => {
    setSelectedFood(food);
    setFoodQuantity(1);
  };

  const closeFoodDetail = () => {
    setSelectedFood(null);
  };

  const handleAddToCart = () => {
    if (isCashier && !effectiveTableCode) {
      return message.warning('Thu ngân cần nhập hoặc chọn bàn trước khi thêm món vào giỏ.');
    }

    const existingItem = cart.find((item) => item.idFood === selectedFood.idFood);

    if (existingItem) {
      setCart(
        cart.map((item) =>
          item.idFood === selectedFood.idFood
            ? { ...item, quantity: item.quantity + foodQuantity }
            : item
        )
      );
    } else {
      setCart([...cart, { ...selectedFood, quantity: foodQuantity }]);
    }

    message.success('Đã thêm vào giỏ!');
    closeFoodDetail();
  };

  const updateCartQuantity = (foodId, delta) => {
    setCart(
      cart.map((item) => {
        if (item.idFood === foodId) {
          const newQty = item.quantity + delta;
          return { ...item, quantity: newQty > 0 ? newQty : 1 };
        }
        return item;
      })
    );
  };

  const removeFromCart = (foodId) => {
    setCart(cart.filter((item) => item.idFood !== foodId));
  };

  const calculateTotal = () => {
    return cart.reduce((total, item) => total + item.unitPrice * item.quantity, 0);
  };

  const getTotalItems = () => {
    return cart.reduce((total, item) => total + item.quantity, 0);
  };

  const tabItems = [
    { key: 'ALL', label: 'TẤT CẢ' },
    ...categories.map((cat) => ({
      key: cat.idCategory,
      label: cat.categoryName.toUpperCase()
    }))
  ];

  return (
      <div>
      {/* HEADER: Chèn ảnh logo thực tế */}
      <div className="header" style={{
        // Tối ưu header: padding-top/bottom nhỏ hơn để tiết kiệm không gian
        padding: '10px 20px', 
        display: 'flex', 
        alignItems: 'center', 
        justifyContent: 'space-between', 
        backgroundColor: '#a30000', // Giả sử màu đỏ thương hiệu
        boxShadow: '0 3px 10px rgba(0,0,0,0.1)'
      }}>
        <Badge count={getTotalItems()} offset={[5, 0]}>
          <Button
            type="text"
            icon={<ShoppingCartOutlined style={{ fontSize: 28, color: 'white' }} />}
            onClick={() => setIsCartModalOpen(true)}
          />
        </Badge>

        {/* 2. THAY THẾ CHỮ "LOGO" CŨ BẰNG ẢNH THỰC TẾ */}
        <div style={{
          display: 'flex', 
          alignItems: 'center', 
          gap: 10, // Khoảng cách giữa ảnh và text nếu cần
          justifyContent: 'center', 
          flex: 1 // Đẩy logo ra giữa
        }}>
          <img 
            src={logoImg} 
            alt="FAF Logo" 
            style={{ 
              height: 48, // Chiều cao vừa phải để cân đối với nút
              width: 48, 
              borderRadius: '50%', // Bo tròn ảnh (nếu logo hình tròn)
              objectFit: 'cover', 
              border: '2px solid white', // Viền trắng nổi bật trên nền đỏ
              boxShadow: '0 2px 6px rgba(0,0,0,0.15)'
            }} 
          />
          {/* Nếu muốn thêm tên thương hiệu nhỏ bên cạnh */}
          {/* <Title level={4} style={{ margin: 0, color: 'white' }}>FAF Fast Food</Title> */}
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 15 }}>
          <Text strong style={{ fontSize: 16, color: 'white' }}>
            {tableNumber || (isCashier ? 'Chưa chọn bàn' : 'Chưa xác định')}
          </Text>
          <Button
            type="text"
            icon={<ArrowLeftOutlined style={{ fontSize: 24, color: 'white' }} />}
          />
        </div>
      </div>

      <div style={{ padding: '0 20px' }}>
        {isCashier && (
          <Card style={{ margin: '16px 0', borderRadius: 12 }}>
            <Space wrap style={{ width: '100%', justifyContent: 'space-between' }}>
              <div>
                <Title level={5} style={{ margin: 0 }}>
                  Chọn bàn để đặt món
                </Title>
                <Text type="secondary">
                  Thu ngân cần nhập hoặc chọn bàn đúng theo database, ví dụ Ban01, Ban02.
                </Text>
              </div>

              <Space wrap>
                <Input
                  placeholder="Nhập mã bàn, ví dụ Ban01"
                  value={tableInput}
                  onChange={handleTableInputChange}
                  onPressEnter={handleConfirmTableInput}
                  style={{ width: 220 }}
                  maxLength={5}
                />

                <Button type="primary" onClick={handleConfirmTableInput}>
                  Xác nhận bàn
                </Button>

                <Select
                  allowClear
                  showSearch
                  placeholder="Hoặc chọn bàn có sẵn"
                  value={tableNumber || undefined}
                  options={availableTableOptions}
                  onChange={handleSelectTable}
                  style={{ width: 220 }}
                  optionFilterProp="label"
                />
              </Space>
            </Space>
          </Card>
        )}

        <Tabs
          activeKey={activeCategory}
          items={tabItems}
          centered
          size="large"
          onChange={handleTabChange}
        />


        <div style={{ margin: '20px 0' }}>
          <img 
            src={bannerImg} 
            alt="Promotion Banner" 
            style={{ 
              width: '100%', 
              height: 200, // Bạn có thể chỉnh lại chiều cao tùy ý
              objectFit: 'cover', 
              borderRadius: 12,
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)'
            }} 
          />
        </div>

        <Row gutter={[16, 16]}>
          {filteredItems.length === 0 ? (
            <div style={{ width: '100%', textAlign: 'center', padding: 50 }}>
              <Text type="secondary">Không có món nào trong danh mục này</Text>
            </div>
          ) : (
            filteredItems.map((food) => (
              <Col xs={12} sm={8} md={8} key={food.idFood}>
                <Card
                  hoverable
                  className="food-card"
                  styles={{ body: { padding: 12, textAlign: 'center', background: '#fff' } }}
                  cover={
                    <div style={{ padding: 10, background: '#f5f5f5' }}>
                      <img
                        alt={food.foodName}
                        src={food.imageUrlFood}
                        style={{
                          width: '100%',
                          height: 120,
                          objectFit: 'cover',
                          borderRadius: 8
                        }}
                      />
                    </div>
                  }
                >
                  <Title level={5} style={{ margin: '5px 0' }}>
                    {food.foodName}
                  </Title>

                  <Text type="secondary" style={{ display: 'block', marginBottom: 10 }}>
                    {food.unitPrice.toLocaleString('vi-VN')} đ
                  </Text>

                  <Button
                    type="primary"
                    shape="round"
                    className="btn-primary-custom"
                    style={{ width: '80%' }}
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

      <Modal
        open={!!selectedFood}
        onCancel={closeFoodDetail}
        footer={null}
        closeIcon={<CloseOutlined style={{ fontSize: 20 }} />}
        bodyStyle={{ padding: 0 }}
        width={400}
      >
        {selectedFood && (
          <div>
            <img
              src={selectedFood.imageUrlFood}
              alt={selectedFood.foodName}
              style={{
                width: '100%',
                height: 250,
                objectFit: 'cover',
                borderRadius: '8px 8px 0 0'
              }}
            />
            <div style={{ padding: 20 }}>
              <Title level={4} style={{ margin: 0 }}>
                {selectedFood.foodName}
              </Title>
              <Text type="secondary">{selectedFood.description}</Text>

              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  marginTop: 30
                }}
              >
                <Space size="middle">
                  <Button
                    shape="circle"
                    icon={<MinusOutlined />}
                    onClick={() => setFoodQuantity(Math.max(1, foodQuantity - 1))}
                  />
                  <Text strong style={{ fontSize: 18 }}>
                    {foodQuantity}
                  </Text>
                  <Button
                    shape="circle"
                    icon={<PlusOutlined />}
                    onClick={() => setFoodQuantity(foodQuantity + 1)}
                  />
                </Space>

                <Button
                  type="primary"
                  shape="round"
                  size="large"
                  className="btn-primary-custom"
                  onClick={handleAddToCart}
                >
                  Thêm vào giỏ ({(selectedFood.unitPrice * foodQuantity).toLocaleString('vi-VN')} đ)
                </Button>
              </div>
            </div>
          </div>
        )}
      </Modal>

      <Modal
        title={
          <Title level={4} style={{ textAlign: 'center', margin: 0 }}>
            GIỎ HÀNG
          </Title>
        }
        open={isCartModalOpen}
        onCancel={() => setIsCartModalOpen(false)}
        footer={null}
        closeIcon={<CloseOutlined style={{ fontSize: 20 }} />}
        width={450}
        bodyStyle={{
          background: '#f5f5f5',
          padding: 10,
          maxHeight: '60vh',
          overflowY: 'auto'
        }}
      >
        <List
          dataSource={cart}
          renderItem={(item) => (
            <Card size="small" style={{ marginBottom: 10, borderRadius: 8 }}>
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between'
                }}
              >
                <img
                  src={item.imageUrlFood}
                  alt={item.foodName}
                  style={{
                    width: 60,
                    height: 60,
                    objectFit: 'cover',
                    borderRadius: 4,
                    marginRight: 10
                  }}
                />
                <div style={{ flex: 1 }}>
                  <Text strong>{item.foodName}</Text>
                  <br />
                  <Text type="secondary">{item.unitPrice.toLocaleString('vi-VN')} đ</Text>
                </div>
                <Space>
                  <Button
                    size="small"
                    icon={<MinusOutlined />}
                    onClick={() => updateCartQuantity(item.idFood, -1)}
                  />
                  <Text>{item.quantity}</Text>
                  <Button
                    size="small"
                    icon={<PlusOutlined />}
                    onClick={() => updateCartQuantity(item.idFood, 1)}
                  />
                  <Button
                    size="small"
                    type="text"
                    danger
                    onClick={() => removeFromCart(item.idFood)}
                  >
                    Xóa
                  </Button>
                </Space>
              </div>
            </Card>
          )}
        />

        <div
          style={{
            background: '#fff',
            padding: 15,
            borderRadius: 8,
            marginTop: 10,
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            gap: 12
          }}
        >
          <div>
            <Title level={4} type="danger" style={{ margin: 0 }}>
              Tổng: {calculateTotal().toLocaleString('vi-VN')} đ
            </Title>

            {isCashier && (
              <Text type={canPlaceOrder ? 'secondary' : 'danger'}>
                {canPlaceOrder ? `Đang đặt cho ${effectiveTableCode}` : 'Chưa chọn bàn'}
              </Text>
            )}
          </div>

          <Button
            type="primary"
            size="large"
            shape="round"
            className="btn-primary-custom"
            onClick={handlePlaceOrder}
            disabled={!canPlaceOrder}
          >
            Đặt món
          </Button>
        </div>
      </Modal>
    </div>
  );
};

export default FoodMenu;