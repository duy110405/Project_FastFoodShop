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

  const fetchPendingOrder = useCallback(async (tableNumber, { silent = false, showNotFound = true } = {}) => {
    try {
      if (!silent) {
        setLoadingOrder(true);
      }
      const res = await apiClient.get(`/v1/sales/tables/${tableNumber}/order`);
      setOrderDetail(res.data?.data || null);
    } catch (error) {
      console.error(error);
      setOrderDetail(null);
      if (showNotFound) {
        message.warning(error.response?.data?.message || 'Bàn này chưa có đơn cần thanh toán');
      }
    } finally {
      if (!silent) {
        setLoadingOrder(false);
      }
    }
  }, []);

  const fetchTables = useCallback(async ({ silent = false } = {}) => {
    if (isPollingRef.current && silent) {
      return;
    }

    try {
      isPollingRef.current = true;
      if (!silent) {
        setLoadingTables(true);
      }

      const res = await apiClient.get('/v1/sales/tables');
      const nextTables = res.data?.data || [];
      setTables(nextTables);

      if (selectedTable) {
        const selected = nextTables.find((table) => table.tableNumber === selectedTable);
        if (!selected || !selected.unpaid) {
          setOrderDetail(null);
        } else {
          await fetchPendingOrder(selectedTable, { silent: true, showNotFound: false });
        }
      }
    } catch (error) {
      console.error(error);
      if (!silent) {
        message.error(error.response?.data?.message || 'Không tải được trạng thái bàn');
      }
    } finally {
      isPollingRef.current = false;
      if (!silent) {
        setLoadingTables(false);
      }
    }
  }, [fetchPendingOrder, selectedTable]);

  useEffect(() => {
    fetchTables();

    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchTables({ silent: true });
      }
    }, POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [fetchTables]);

  const handleSelectTable = (table) => {
    setSelectedTable(table.tableNumber);
    if (!table.unpaid) {
      setOrderDetail(null);
      message.info(`Bàn ${table.tableNumber} đã thanh toán hoặc chưa có khách.`);
      return;
    }
    fetchPendingOrder(table.tableNumber);
  };

  const totalAmount = useMemo(() => Number(orderDetail?.totalAmount || 0), [orderDetail]);

  const submitPayment = async () => {
    if (!orderDetail?.orderId) {
      message.warning('Vui lòng chọn bàn có đơn chưa thanh toán');
      return;
    }

    try {
      setPaying(true);
      const res = await apiClient.post('/v1/sales/payments', {
        orderId: orderDetail.orderId,
        customerPhone: customerPhone || null,
        paymentMethod
      });

      const invoice = res.data?.data;
      message.success(`Thanh toán thành công. Mã hóa đơn: ${invoice?.invoiceId || ''}`.trim());
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

  return (
    <div style={{ padding: 20 }}>
      <Title level={3} style={{ marginBottom: 16 }}>Thanh Toán Thu Ngân</Title>

      <Card title="Trạng thái bàn (Đỏ: chưa thanh toán, Xanh: đã thanh toán)" style={{ marginBottom: 16 }}>
        <Spin spinning={loadingTables}>
          {tables.length === 0 ? (
            <Empty description="Chưa có bàn nào đặt món" />
          ) : (
            <Row gutter={[12, 12]}>
              {tables.map((table) => (
                <Col xs={12} sm={8} md={6} lg={4} key={table.tableNumber}>
                  <Button
                    block
                    style={{
                      height: 52,
                      color: '#fff',
                      border: 'none',
                      background: table.unpaid ? '#ff4d4f' : '#52c41a',
                      boxShadow: selectedTable === table.tableNumber ? '0 0 0 2px rgba(24, 144, 255, 0.35)' : 'none'
                    }}
                    onClick={() => handleSelectTable(table)}
                  >
                    {table.tableNumber}
                  </Button>
                </Col>
              ))}
            </Row>
          )}
        </Spin>
      </Card>

      <Card title={selectedTable ? `Chi tiết bàn ${selectedTable}` : 'Chọn bàn để xem đơn'}>
        <Spin spinning={loadingOrder}>
          {!orderDetail ? (
            <Empty description="Không có đơn chưa thanh toán" />
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

              <div style={{ marginTop: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                <Title level={4} type="danger" style={{ margin: 0 }}>
                  Tổng tiền: {formatCurrency(totalAmount)}
                </Title>

                <Space wrap>
                  <Input
                    placeholder="SĐT khách (không bắt buộc)"
                    value={customerPhone}
                    onChange={(e) => setCustomerPhone(e.target.value)}
                    style={{ width: 220 }}
                  />
                  <Select
                    value={paymentMethod}
                    options={PAYMENT_METHOD_OPTIONS}
                    onChange={setPaymentMethod}
                    style={{ width: 180 }}
                  />
                  <Button type="primary" danger disabled={paying || !orderDetail?.orderId} onClick={() => setIsConfirmOpen(true)}>
                    Thanh toán
                  </Button>
                </Space>
              </div>
            </>
          )}
        </Spin>
      </Card>

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

