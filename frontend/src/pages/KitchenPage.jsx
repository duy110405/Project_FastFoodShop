import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Input, Button, Empty, Spin, message } from 'antd';
import { SearchOutlined, DownOutlined, CheckSquareOutlined, UpOutlined } from '@ant-design/icons';
import apiClient from '../api/apiClient';
import '../css/KitchenPage.css';

const logoImg = './images/FAF_logo.jpg'; 

const DEFAULT_FOOD_IMAGE = 'https://via.placeholder.com/120x120?text=FOOD';
const POLL_INTERVAL_MS = 3000;

const formatElapsed = (minutes) => {
  const safeMinutes = Number.isFinite(minutes) ? Math.max(minutes, 0) : 0;
  const hour = String(Math.floor(safeMinutes / 60)).padStart(2, '0');
  const minute = String(safeMinutes % 60).padStart(2, '0');
  return `${hour}h:${minute}m`;
};

const KitchenPage = () => {
  const [activeTab, setActiveTab] = useState('orders');
  const [searchText, setSearchText] = useState('');
  const [tableOrders, setTableOrders] = useState([]);
  const [remainingFoods, setRemainingFoods] = useState([]);
  const [expandedMap, setExpandedMap] = useState({});
  const [loading, setLoading] = useState(true);
  const [servingIds, setServingIds] = useState([]);
  const isFetchingRef = useRef(false);

  const fetchKitchenData = useCallback(async ({ silent = false } = {}) => {
    if (isFetchingRef.current) return;
    try {
      isFetchingRef.current = true;
      if (!silent) setLoading(true);
      const [ordersRes, foodsRes] = await Promise.all([
        apiClient.get('/kitchen/orders'),
        apiClient.get('/kitchen/foods/remaining')
      ]);
      const orders = ordersRes?.data?.data ?? [];
      const foods = foodsRes?.data?.data ?? [];
      setTableOrders(orders);
      setRemainingFoods(foods);
      setExpandedMap((prev) => {
        const next = { ...prev };
        orders.forEach((table) => {
          if (next[table.tableNumber] === undefined) next[table.tableNumber] = true;
        });
        return next;
      });
    } catch (error) {
      console.error(error);
      message.error('Không thể tải dữ liệu nhà bếp');
    } finally {
      isFetchingRef.current = false;
      if (!silent) setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchKitchenData();
    const intervalId = setInterval(() => {
      if (document.visibilityState === 'visible') fetchKitchenData({ silent: true });
    }, POLL_INTERVAL_MS);
    return () => clearInterval(intervalId);
  }, [fetchKitchenData]);

  const onServeItem = async (orderDetailId) => {
    try {
      setServingIds((prev) => [...prev, orderDetailId]);
      await apiClient.post(`/kitchen/orders/items/${orderDetailId}/served`);
      message.success('Đã hoàn thành món ăn!');
      await fetchKitchenData({ silent: true });
    } catch (error) {
      message.error('Cập nhật thất bại');
    } finally {
      setServingIds((prev) => prev.filter((id) => id !== orderDetailId));
    }
  };

  const filteredTableOrders = useMemo(() => {
    const normalized = searchText.trim().toLowerCase();
    if (!normalized) return tableOrders;
    return tableOrders
      .map((table) => {
        const matchedItems = table.items.filter((item) =>
          item.foodName?.toLowerCase().includes(normalized)
        );
        const matchedTable = table.tableNumber?.toLowerCase().includes(normalized);
        if (matchedTable) return table;
        return { ...table, items: matchedItems };
      })
      .filter((table) => table.items.length > 0 || table.tableNumber?.toLowerCase().includes(normalized));
  }, [searchText, tableOrders]);

  const filteredRemainingFoods = useMemo(() => {
    const normalized = searchText.trim().toLowerCase();
    return remainingFoods.filter(f => f.foodName?.toLowerCase().includes(normalized));
  }, [searchText, remainingFoods]);

  return (
    <div className="kitchen-page">
      <aside className="kitchen-sidebar">
        <div className="kitchen-logo-container">
          <img src={logoImg} alt="FAF Logo" className="logo-circle" />
          <h2>FAF KITCHEN</h2>
        </div>
        <div className="sidebar-menu">
          <Button
            className={`kitchen-tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
            onClick={() => setActiveTab('orders')}
          >Đơn hàng</Button>
          <Button
            className={`kitchen-tab-btn ${activeTab === 'foods' ? 'active' : ''}`}
            onClick={() => setActiveTab('foods')}
          >Danh sách món</Button>
        </div>
      </aside>

      <section className="kitchen-main">
        <div className="kitchen-topbar">
          <h3>{activeTab === 'orders' ? 'ĐƠN HÀNG' : 'KHO MÓN'}</h3>
          <Input 
            className="kitchen-search" 
            prefix={<SearchOutlined />} 
            placeholder="Tìm kiếm..." 
            value={searchText} 
            onChange={e => setSearchText(e.target.value)} 
          />
        </div>

        <div className="kitchen-content-wrapper">
          {loading ? (
            <div className="kitchen-loading"><Spin size="large" /></div>
          ) : activeTab === 'orders' ? (
             /* HIỂN THỊ TAB ĐƠN HÀNG */
             filteredTableOrders.length === 0 ? (
                <div className="empty-white-card">
                   <Empty description="Không có đơn hàng đang chờ" />
                </div>
             ) : (
                <div className="orders-grid">
                   {filteredTableOrders.map((table) => {
                      const expanded = expandedMap[table.tableNumber] !== false;
                      return (
                        <div key={table.tableNumber} className="table-card">
                           <div className="table-card-header">
                              <span>Bàn: {table.tableNumber} - {formatElapsed(table.elapsedMinutes)}</span>
                              <Button 
                                type="text" 
                                className="toggle-btn"
                                icon={expanded ? <UpOutlined /> : <DownOutlined />} 
                                onClick={() => setExpandedMap(p => ({...p, [table.tableNumber]: !expanded}))}
                              >
                                {expanded ? 'Ẩn bớt' : 'Hiện thêm'}
                              </Button>
                           </div>
                           {expanded && (
                             <div className="table-card-body">
                                {table.items.map((item) => (
                                  <div key={item.orderDetailId} className="kitchen-item">
                                     <img src={item.imageUrlFood || DEFAULT_FOOD_IMAGE} className="item-image" />
                                     <div className="item-info">
                                        <p className="item-title">{item.foodName}</p>
                                        <p className="item-qty">Số lượng: {item.quantity}</p>
                                     </div>
                                     <Button 
                                        type="primary" 
                                        className="done-btn-new"
                                        onClick={() => onServeItem(item.orderDetailId)} 
                                        loading={servingIds.includes(item.orderDetailId)}
                                     >Xong</Button>
                                  </div>
                                ))}
                             </div>
                           )}
                        </div>
                      );
                   })}
                </div>
             )
          ) : (
            /* HIỂN THỊ TAB DANH SÁCH MÓN */
            <div className="food-list-white-card">
               {filteredRemainingFoods.length === 0 ? (
                 <Empty description="Không có món nào trong kho" />
               ) : (
                 <div className="scrollable-list">
                    {filteredRemainingFoods.map((food) => (
                      <div key={food.foodId} className="food-item-row">
                         <img src={food.imageUrlFood || DEFAULT_FOOD_IMAGE} className="food-img-small" />
                         <div className="food-info-left">
                            <p className="food-name">{food.foodName}</p>
                            <p className="food-qty-text">Còn lại: {food.remainingQuantity}</p>
                         </div>
                      </div>
                    ))}
                 </div>
               )}
            </div>
          )}
        </div>
      </section>
    </div>
  );
};

export default KitchenPage;