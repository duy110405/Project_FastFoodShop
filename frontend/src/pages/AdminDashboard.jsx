import React, { useCallback, useMemo, useState } from 'react';
import {
  Button,
  Card,
  Col,
  DatePicker,
  Empty,
  Row,
  Space,
  Spin,
  Statistic,
  Typography,
  message
} from 'antd';
import {
  DollarOutlined,
  FileTextOutlined,
  LeftOutlined,
  RightOutlined,
  ShoppingOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { getAdminDashboard } from '../api/dashboardApi';
import '../css/AdminDashboard.css';

const { RangePicker } = DatePicker;
const { Title, Text } = Typography;
const POLL_INTERVAL_MS = 5000;

const formatCurrency = (value) => {
  const number = Number(value || 0);
  return `${number.toLocaleString('vi-VN')} đ`;
};

const getTopFoodCards = (foods) => {
  const pageSize = 3;
  const pages = [];

  for (let i = 0; i < foods.length; i += pageSize) {
    pages.push(foods.slice(i, i + pageSize));
  }

  return pages;
};

const AdminDashboard = () => {
  const [loading, setLoading] = useState(false);
  const [range, setRange] = useState([dayjs(), dayjs()]);
  const [foodPage, setFoodPage] = useState(0);
  const [dashboard, setDashboard] = useState({
    profit: 0,
    orderCount: 0,
    averageImportCost: 0,
    averageRevenue: 0,
    topOrderedFoods: []
  });

  const fetchDashboard = useCallback(async (nextRange, { silent = false, resetPage = true } = {}) => {
    const fromDate = nextRange?.[0]?.format('YYYY-MM-DD');
    const toDate = nextRange?.[1]?.format('YYYY-MM-DD');

    if (!silent) {
      setLoading(true);
    }
    try {
      const data = await getAdminDashboard({ fromDate, toDate, topN: 9 });
      setDashboard(data || {});
      if (resetPage) {
        setFoodPage(0);
      }
    } catch (error) {
      console.error(error);
      if (!silent) {
        message.error(error.response?.data?.message || 'Không thể tải dữ liệu dashboard');
      }
    } finally {
      if (!silent) {
        setLoading(false);
      }
    }
  }, []);

  React.useEffect(() => {
    fetchDashboard(range);

    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') {
        fetchDashboard(range, { silent: true, resetPage: false });
      }
    }, POLL_INTERVAL_MS);

    return () => clearInterval(intervalId);
  }, [fetchDashboard, range]);

  const foodPages = useMemo(() => getTopFoodCards(dashboard.topOrderedFoods || []), [dashboard.topOrderedFoods]);
  const currentFoods = foodPages[foodPage] || [];

  return (
    <div className="admin-dashboard-page">
      <div className="admin-dashboard-toolbar">
        <div>
          <Title level={4} style={{ margin: 0 }}>Dashboard Admin</Title>
          <Text type="secondary">Theo doi nhanh doanh thu va mon an duoc order nhieu nhat</Text>
        </div>

        <Space>
          <RangePicker
            value={range}
            onChange={(values) => setRange(values || [dayjs(), dayjs()])}
            allowClear={false}
            format="DD/MM/YYYY"
          />
          <Button type="primary" onClick={() => fetchDashboard(range, { resetPage: true })}>Truy van</Button>
        </Space>
      </div>

      <Spin spinning={loading} tip="Dang tai dashboard...">
        <Row gutter={[16, 16]}>
          <Col xs={24} md={12} lg={6}>
            <Card>
              <Statistic title="Loi nhuan" value={formatCurrency(dashboard.profit)} prefix={<DollarOutlined />} />
            </Card>
          </Col>

          <Col xs={24} md={12} lg={6}>
            <Card>
              <Statistic title="So don trong ky" value={dashboard.orderCount || 0} prefix={<FileTextOutlined />} />
            </Card>
          </Col>

          <Col xs={24} md={12} lg={6}>
            <Card>
              <Statistic title="Chi phi nhap TB" value={formatCurrency(dashboard.averageImportCost)} prefix={<DollarOutlined />} />
            </Card>
          </Col>

          <Col xs={24} md={12} lg={6}>
            <Card>
              <Statistic title="Doanh thu TB" value={formatCurrency(dashboard.averageRevenue)} prefix={<DollarOutlined />} />
            </Card>
          </Col>
        </Row>

        <Card className="top-food-card" title="Mon an duoc order nhieu nhat" extra={<ShoppingOutlined />}>
          {foodPages.length === 0 ? (
            <Empty description="Chua co du lieu mon an" />
          ) : (
            <>
              <div className="top-food-list">
                {currentFoods.map((food) => (
                  <div className="top-food-item" key={food.idFood}>
                    <img src={food.imageUrlFood} alt={food.foodName} className="top-food-image" />
                    <div className="top-food-info">
                      <div className="top-food-name">{food.foodName}</div>
                      <div className="top-food-meta">Da ban: {food.quantityOrdered} suat</div>
                      <div className="top-food-meta">Doanh thu: {formatCurrency(food.totalRevenue)}</div>
                    </div>
                  </div>
                ))}
              </div>

              <div className="top-food-pagination">
                <Button
                  icon={<LeftOutlined />}
                  disabled={foodPage <= 0}
                  onClick={() => setFoodPage((prev) => Math.max(prev - 1, 0))}
                />
                <Text type="secondary">{foodPage + 1}/{foodPages.length}</Text>
                <Button
                  icon={<RightOutlined />}
                  disabled={foodPage >= foodPages.length - 1}
                  onClick={() => setFoodPage((prev) => Math.min(prev + 1, foodPages.length - 1))}
                />
              </div>
            </>
          )}
        </Card>
      </Spin>
    </div>
  );
};

export default AdminDashboard;

