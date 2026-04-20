import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Button, Card, Col, Empty, Input, List, message, Modal, Row, Select, Space, Spin, Tag, Typography } from 'antd';
import apiClient from '../api/apiClient';

const { Title, Text } = Typography;

const PAYMENT_METHOD_OPTIONS = [
  { value: 'CASH', label: 'Tiền mặt' },
  { value: 'TRANSFER', label: 'Chuyển khoản' }
];

const POLL_INTERVAL_MS = 3000;

const formatCurrency = (value) => `${Number(value || 0).toLocaleString('vi-VN')} đ`;

const PaymentPage = () => {
  const [tables, setTables] = useState([]);
  const [selectedTable, setSelectedTable] = useState('');
  const [orderDetail, setOrderDetail] = useState(null);
  const [loadingTables, setLoadingTables] = useState(false);
  const [loadingOrder, setLoadingOrder] = useState(false);
  const [paying, setPaying] = useState(false);

  const [paymentMethod, setPaymentMethod] = useState('CASH');
  const [customerPhone, setCustomerPhone] = useState('');
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const isPollingRef = useRef(false);

  // ==========================================
  // 1. API LẤY DANH SÁCH BÀN & MÀU SẮC
  // ==========================================
  const fetchTables = useCallback(async ({ silent = false } = {}) => {
    if (isPollingRef.current && silent) return;

    try {
      isPollingRef.current = true;
      if (!silent) setLoadingTables(true);

      const res = await apiClient.get('/sales/tables');
      const nextTables = res.data?.data || [];
      setTables(nextTables);

      // Nếu đang chọn bàn mà có data mới, tự động fetch lại chi tiết bàn đó
      if (selectedTable) {
        const selected = nextTables.find((t) => t.tableNumber === selectedTable);
        if (!selected || selected.status === 'EMPTY') {
          setOrderDetail(null);
        } else {
          await fetchPendingOrder(selectedTable, { silent: true, showNotFound: false });
        }
      }
    } catch (error) {
      console.error(error);
      if (!silent) message.error(error.response?.data?.message || 'Không tải được trạng thái bàn');
    } finally {
      isPollingRef.current = false;
      if (!silent) setLoadingTables(false);
    }
  }, [selectedTable]);

  // ==========================================
  // 2. API LẤY CHI TIẾT ĐƠN HÀNG CỦA BÀN
  // ==========================================
  const fetchPendingOrder = useCallback(async (tableNumber, { silent = false, showNotFound = true } = {}) => {
    try {
      if (!silent) setLoadingOrder(true);
      const res = await apiClient.get(`/sales/tables/${tableNumber}/order`);
      setOrderDetail(res.data?.data || null);
    } catch (error) {
      console.error(error);
      setOrderDetail(null);
      if (showNotFound) message.warning(error.response?.data?.message || 'Bàn này đang trống');
    } finally {
      if (!silent) setLoadingOrder(false);
    }
  }, []);

  // Tự động quét cập nhật
  useEffect(() => {
    fetchTables();
    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchTables({ silent: true });
      }
    }, POLL_INTERVAL_MS);
    return () => clearInterval(intervalId);
  }, [fetchTables]);

  // ==========================================
  // 3. HÀM CHỌN BÀN
  // ==========================================
  const handleSelectTable = (table) => {
    setSelectedTable(table.tableNumber);
    // Nếu bàn trống thì không gọi API chi tiết
    if (table.status === 'EMPTY' || !table.status) {
      setOrderDetail(null);
      message.info(`Bàn ${table.tableNumber} hiện đang trống.`);
      return;
    }
    fetchPendingOrder(table.tableNumber);
  };

  const totalAmount = useMemo(() => Number(orderDetail?.totalAmount || 0), [orderDetail]);

  // ==========================================
  // 4. HÀM THANH TOÁN (Từ Cam -> Vàng)
  // ==========================================
  const submitPayment = async () => {
    if (!orderDetail?.orderId) return message.warning('Vui lòng chọn bàn có đơn');
    try {
      setPaying(true);
      const res = await apiClient.post('/sales/payments', {
        orderId: orderDetail.orderId,
        customerPhone: customerPhone || null,
        paymentMethod
      });
      message.success(`Thanh toán thành công. Đã báo bếp!`);
      setIsConfirmOpen(false);
      setOrderDetail(null);
      setCustomerPhone('');
      await fetchTables();
    } catch (error) {
      console.error(error);
      message.error(error.response?.data?.message || 'Thanh toán thất bại');
    } finally {
      setPaying(false);
    }
  };

  // ==========================================
  // 5. 🎯 HÀM DỌN BÀN (Từ Đỏ -> Xanh)
  // ==========================================
  const handleReleaseTable = async () => {
    try {
      setPaying(true);
      // Gọi API dọn bàn chốt đơn (BE cần tạo API này)
      await apiClient.post(`/sales/orders/${orderDetail.orderId}/complete`);
      
      message.success(`Đã dọn xong bàn ${orderDetail.tableNumber}`);
      setOrderDetail(null);
      await fetchTables(); 
    } catch (error) {
      console.error(error);
      message.error(error.response?.data?.message || 'Lỗi khi dọn bàn');
    } finally {
      setPaying(false);
    }
  };

  return (
    <div style={{ padding: 20 }}>
      <Title level={3} style={{ marginBottom: 16 }}>Thanh Toán Thu Ngân</Title>

      {/* ----------------- KHU VỰC 1: LƯỚI BÀN ----------------- */}
      <Card title="Trạng thái bàn" style={{ marginBottom: 16 }}>
        <Spin spinning={loadingTables}>
          {tables.length === 0 ? (
            <Empty description="Hệ thống chưa cấu hình bàn" />
          ) : (
            <Row gutter={[12, 12]}>
              {tables.map((table) => {
                // LOGIC MÀU SẮC CHUẨN XÁC
                let bgColor = '#52c41a'; // Xanh lá (EMPTY)
                let textColor = '#fff';
                
                if (table.status === 'PENDING') {
                  bgColor = '#faad14'; // Cam: Chưa thanh toán
                } else if (table.status === 'PAID') {
                  bgColor = '#fadb14'; // Vàng: Đã thanh toán, Bếp đang nấu
                  textColor = '#000';
                } else if (table.status === 'SERVED') {
                  bgColor = '#f5222d'; // Đỏ: Bếp trả xong, Khách đang ăn
                }

                return (
                  <Col xs={12} sm={8} md={6} lg={4} key={table.tableNumber}>
                    <Button
                      block
                      style={{
                        height: 52,
                        color: textColor,
                        border: 'none',
                        background: bgColor,
                        boxShadow: selectedTable === table.tableNumber ? '0 0 0 2px rgba(24, 144, 255, 0.5)' : 'none',
                        fontWeight: 'bold'
                      }}
                      onClick={() => handleSelectTable(table)}
                    >
                      {table.tableNumber}
                    </Button>
                  </Col>
                );
              })}
            </Row>
          )}
        </Spin>
      </Card>

      {/* ----------------- KHU VỰC 2: CHI TIẾT ĐƠN & THANH TOÁN ----------------- */}
      <Card title={selectedTable ? `Chi tiết bàn ${selectedTable}` : 'Chọn bàn để thao tác'}>
        <Spin spinning={loadingOrder}>
          {!orderDetail ? (
            <Empty description="Bàn này đang trống" />
          ) : (
            <>
              <Space style={{ marginBottom: 12 }}>
                <Tag color="red">Order: {orderDetail.orderId}</Tag>
                <Tag color="blue">Khách: {orderDetail.customerName || 'Khách lẻ'}</Tag>
              </Space>

              <List
                dataSource={orderDetail.items || []}
                renderItem={(item) => (
                  <List.Item>
                    <List.Item.Meta
                      avatar={<img src={item.imageUrlFood} alt={item.foodName} style={{ width: 64, height: 64, objectFit: 'cover', borderRadius: 6 }} />}
                      title={item.foodName}
                      description={`Số lượng: ${item.quantity} | Đơn giá: ${formatCurrency(item.unitPrice)}`}
                    />
                    <Text strong>{formatCurrency(item.lineTotal)}</Text>
                  </List.Item>
                )}
              />

              {/* 🎯 LOGIC ĐỔI NÚT THÔNG MINH Ở ĐÂY */}
              <div style={{ marginTop: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap', background: '#fafafa', padding: 15, borderRadius: 8 }}>
                <Title level={4} type="danger" style={{ margin: 0 }}>
                  Tổng tiền: {formatCurrency(totalAmount)}
                </Title>

                <Space wrap>
                  {/* BÀN ĐỎ: Hiện nút Dọn bàn */}
                  {orderDetail?.status === 'SERVED' ? (
                    <Button 
                      type="primary" 
                      style={{ background: '#52c41a', borderColor: '#52c41a', fontWeight: 'bold' }} 
                      disabled={paying} 
                      onClick={handleReleaseTable}
                    >
                      Dọn Bàn (Giải phóng)
                    </Button>
                  ) : 
                  
                  /* BÀN VÀNG: Đã trả tiền, đợi bếp */
                  orderDetail?.status === 'PAID' ? (
                    <Button type="default" disabled style={{ color: '#faad14', fontWeight: 'bold', borderColor: '#faad14' }}>
                      Bếp đang chuẩn bị món...
                    </Button>
                  ) : 
                  
                  /* BÀN CAM: Chưa trả tiền -> Hiện ô thanh toán */
                  (
                    <>
                      <Input
                        placeholder="SĐT khách (không bắt buộc)"
                        value={customerPhone}
                        onChange={(e) => setCustomerPhone(e.target.value)}
                        style={{ width: 180 }}
                      />
                      <Select
                        value={paymentMethod}
                        options={PAYMENT_METHOD_OPTIONS}
                        onChange={setPaymentMethod}
                        style={{ width: 140 }}
                      />
                      <Button type="primary" danger disabled={paying || !orderDetail?.orderId} onClick={() => setIsConfirmOpen(true)}>
                        Thanh toán ngay
                      </Button>
                    </>
                  )}
                </Space>
              </div>
            </>
          )}
        </Spin>
      </Card>

      {/* Modal xác nhận */}
      <Modal
        title="Xác nhận thanh toán"
        open={isConfirmOpen}
        onCancel={() => setIsConfirmOpen(false)}
        onOk={submitPayment}
        okText={paying ? 'Đang xử lý...' : 'Xác nhận'}
        confirmLoading={paying}
      >
        <p>Bàn: {orderDetail?.tableNumber}</p>
        <p>Mã đơn: {orderDetail?.orderId}</p>
        <p>Hình thức: {PAYMENT_METHOD_OPTIONS.find((m) => m.value === paymentMethod)?.label}</p>
        <p><strong>Tổng tiền: {formatCurrency(totalAmount)}</strong></p>
      </Modal>
    </div>
  );
};

export default PaymentPage;