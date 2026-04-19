import React, { useState, useEffect } from 'react';
import { Card, Row, Col, Typography, Tabs, List, Button, message } from 'antd';
import { CheckSquareOutlined } from '@ant-design/icons';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import apiClient from '../api/apiClient';

const { Title, Text } = Typography;

const OrderPage = () => {
  const [pendingItems, setPendingItems] = useState([]);
  const [completedItems, setCompletedItems] = useState([]);

  const fetchPendingOrders = async () => {
    try {
      const response = await apiClient.get('/kitchen/orders');
      if (response.data && response.data.data) {
        let flatPendingList = [];
        response.data.data.forEach(table => {
          const items = table.items || table.orderDetails || [];
          items.forEach(item => {
            flatPendingList.push({
              ...item,
              tableNumber: table.tableNumber || table.tableId
            });
          });
        });
        setPendingItems(flatPendingList);
      }
    } catch (error) {
      console.error("Lỗi khi tải đơn hàng từ bếp:", error);
      message.error("Không thể kết nối đến máy chủ lấy đơn hàng!");
    }
  };

  useEffect(() => {
    fetchPendingOrders(); 
    
    const socket = new SockJS('http://localhost:8080/ws');
    const stompClient = Stomp.over(socket);
    stompClient.debug = () => {}; 

    stompClient.connect({}, () => {
      console.log('Đã kết nối đường truyền Real-time tới Bếp!');
      stompClient.subscribe('/topic/kitchen', (socketMessage) => {
        if (socketMessage.body === 'NEW_ORDER') {
          message.success({ content: 'Có đơn hàng mới!', duration: 3, style: { marginTop: '10vh' }});
          fetchPendingOrders();
        }
      });
    }, (error) => {
      console.error("Lỗi WebSocket:", error);
    });

    return () => {
      if (stompClient && stompClient.connected) stompClient.disconnect();
    };
  }, []);

  const handleMarkItemServed = async (itemToMark) => {
    const itemId = itemToMark.id || itemToMark.orderDetailId;
    try {
      await apiClient.post(`/kitchen/orders/items/${itemId}/served`);
      message.success("Đã hoàn thành món!");
      setCompletedItems(prev => [itemToMark, ...prev]);
      fetchPendingOrders(); 
    } catch (error) {
      console.error("Lỗi cập nhật món:", error);
      message.error("Không thể cập nhật trạng thái món ăn.");
    }
  };

  const tabItems = [{ key: 'order', label: 'Order' }, { key: 'history', label: 'Lịch sử' }];

  return (
    <div style={{ padding: '24px', background: '#fff', borderRadius: '8px' }}>
      <Tabs defaultActiveKey="order" items={tabItems} style={{ marginBottom: 16 }} />

      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card style={{ background: '#e0e0e0', textAlign: 'center', borderRadius: '12px' }}>
            <Title level={2} style={{ margin: 0 }}>{pendingItems.length}</Title>
            <Text strong style={{ color: '#d9363e' }}>Món đang chờ</Text>
          </Card>
        </Col>
        <Col span={12}>
          <Card style={{ background: '#e0e0e0', textAlign: 'center', borderRadius: '12px' }}>
            <Title level={2} style={{ margin: 0 }}>{completedItems.length}</Title>
            <Text>Món đã xong (phiên này)</Text>
          </Card>
        </Col>
      </Row>

      <div style={{ height: 'calc(100vh - 350px)', overflowY: 'auto', paddingRight: '10px' }}>
        
        {/* DANH SÁCH CHƯA XONG (ĐỎ) */}
        <Title level={5} style={{ color: '#d9363e', borderBottom: '2px solid #d9363e', paddingBottom: '8px' }}>
          CHƯA XONG ({pendingItems.length})
        </Title>
        <List
          dataSource={pendingItems}
          renderItem={item => (
            <List.Item style={{ background: '#ffcccc', marginBottom: '12px', padding: '16px', borderRadius: '8px', borderLeft: '6px solid #ff4d4f' }}>
              <List.Item.Meta
                title={<Text strong style={{ fontSize: '16px' }}>{item.foodName || item.name}</Text>}
                description={<Text strong type="danger">Bàn: {item.tableNumber} | Số lượng: {item.quantity}</Text>}
              />
              <Button 
                type="primary" 
                danger 
                icon={<CheckSquareOutlined />} 
                onClick={() => handleMarkItemServed(item)}
              >
                Hoàn thành
              </Button>
            </List.Item>
          )}
        />

        {/* DANH SÁCH ĐÃ XONG (XANH) */}
        <Title level={5} style={{ color: '#52c41a', borderBottom: '2px solid #52c41a', paddingBottom: '8px', marginTop: '30px' }}>
          ĐÃ XONG ({completedItems.length})
        </Title>
        <List
          dataSource={completedItems}
          renderItem={item => (
            <List.Item style={{ background: '#d9f7be', marginBottom: '12px', padding: '16px', borderRadius: '8px', borderLeft: '6px solid #52c41a' }}>
              <List.Item.Meta
                title={<Text strong style={{ fontSize: '16px', textDecoration: 'line-through' }}>{item.foodName || item.name}</Text>}
                description={<Text type="secondary">Bàn: {item.tableNumber} | Số lượng: {item.quantity}</Text>}
              />
              <Text strong style={{ color: '#52c41a' }}>Đã phục vụ</Text>
            </List.Item>
          )}
        />

      </div>
    </div>
  );
};

export default OrderPage;