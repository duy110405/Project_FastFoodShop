import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Typography, Tabs, List, Button, message, Space, Avatar } from 'antd';
import { CheckSquareOutlined, ClockCircleOutlined } from '@ant-design/icons';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import apiClient from '../api/apiClient';

const { Title, Text } = Typography;

const OrderPage = () => {
  const [pendingTables, setPendingTables] = useState([]);
  const [completedItems, setCompletedItems] = useState([]);

  // 1. Lấy đơn hàng đang chờ (theo từng bàn)
  const fetchPendingOrders = async () => {
    try {
      const response = await apiClient.get('/kitchen/orders');
      const rawData = response.data.data || response.data;
      if (rawData && Array.isArray(rawData)) {
        setPendingTables(rawData);
      }
    } catch (error) {
      console.error("Lỗi khi tải đơn hàng:", error);
    }
  };

  // 2. Lấy đơn hàng đã xong (làm phẳng ra để hiện danh sách lịch sử)
  const fetchCompletedOrders = async () => {
    try {
      const response = await apiClient.get('/kitchen/orders/completed');
      const rawData = response.data.data || response.data;
      if (Array.isArray(rawData)) {
        let flatList = [];
        rawData.forEach(t => {
          const items = t.items || t.orderDetails || [];
          items.forEach(i => flatList.push({ ...i, tableNumber: t.tableNumber }));
        });
        setCompletedItems(flatList);
      }
    } catch (error) {
      console.error("Lỗi lịch sử:", error);
    }
  };

  useEffect(() => {
    fetchPendingOrders();
    fetchCompletedOrders();

    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = () => {};
    stompClient.connect({}, () => {
      stompClient.subscribe('/topic/kitchen', (msg) => {
        if (msg.body === 'NEW_ORDER') {
          fetchPendingOrders();
        }
      });
    });
    return () => { if (stompClient.connected) stompClient.disconnect(); };
  }, []);

  const handleMarkItemServed = async (item) => {
    const itemId = item.id || item.orderDetailId;
    try {
      await apiClient.post(`/kitchen/orders/items/${itemId}/served`);
      message.success("Đã hoàn thành món!");
      fetchPendingOrders();
      fetchCompletedOrders();
    } catch (error) {
      message.error("Lỗi cập nhật món ăn.");
    }
  };

  return (
    <div style={{ padding: '24px', background: '#f5f5f5', minHeight: '100vh' }}>
      {/* Khối thống kê */}
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card style={{ textAlign: 'center', borderRadius: 12 }}>
            <Title level={2} style={{ margin: 0, color: '#d9363e' }}>{pendingTables.length}</Title>
            <Text strong>Bàn đang chờ</Text>
          </Card>
        </Col>
        <Col span={12}>
          <Card style={{ textAlign: 'center', borderRadius: 12 }}>
            <Title level={2} style={{ margin: 0, color: '#52c41a' }}>{completedItems.length}</Title>
            <Text strong>Món đã phục vụ</Text>
          </Card>
        </Col>
      </Row>

      <Tabs defaultActiveKey="1" items={[
        {
          key: '1',
          label: 'Đang chế biến',
          children: (
            <Space direction="vertical" size="large" style={{ width: '100%' }}>
              {pendingTables.map((table) => (
                <div key={table.tableNumber} style={{ background: '#e0e0e0', borderRadius: '12px', padding: '12px' }}>
                  {/* Thanh tiêu đề bàn giống Figma */}
                  <div style={{ background: '#bcbcbc', padding: '10px 20px', borderRadius: '8px 8px 0 0', display: 'flex', justifyContent: 'space-between' }}>
                    <Text strong style={{ fontSize: '18px', color: '#fff' }}>Bàn: {table.tableNumber}</Text>
                    <Text style={{ color: '#fff' }}><ClockCircleOutlined /> {table.elapsedMinutes || 0}m</Text>
                  </div>
                  {/* Danh sách món trong bàn */}
                  <div style={{ background: '#fff', borderRadius: '0 0 8px 8px' }}>
                    <List
                      dataSource={table.items || table.orderDetails || []}
                      renderItem={(item) => (
                        <List.Item
                          style={{ padding: '15px 20px' }}
                          actions={[
                            <Button type="primary" danger icon={<CheckSquareOutlined />} onClick={() => handleMarkItemServed(item)}>
                              Xong
                            </Button>
                          ]}
                        >
                          <List.Item.Meta
                            avatar={<Avatar shape="square" size={64} src={item.imageUrlFood} />}
                            title={<Text strong style={{ fontSize: '16px' }}>{item.foodName}</Text>}
                            description={<Text type="danger">Số lượng: {item.quantity}</Text>}
                          />
                        </List.Item>
                      )}
                    />
                  </div>
                </div>
              ))}
              {pendingTables.length === 0 && <Text type="secondary">Hiện tại không có đơn hàng nào.</Text>}
            </Space>
          )
        },
        {
          key: '2',
          label: 'Lịch sử phục vụ',
          children: (
            <List
              dataSource={completedItems}
              renderItem={(item) => (
                <List.Item style={{ background: '#fff', marginBottom: '8px', padding: '12px 20px', borderRadius: '8px' }}>
                  <List.Item.Meta
                    title={<Text strong>{item.foodName}</Text>}
                    description={`Bàn: ${item.tableNumber} | Số lượng: ${item.quantity}`}
                  />
                  <Text strong style={{ color: '#52c41a' }}>Đã phục vụ ✓</Text>
                </List.Item>
              )}
            />
          )
        }
      ]} />
    </div>
  );
};

export default OrderPage;