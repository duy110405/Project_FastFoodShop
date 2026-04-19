import React, { useEffect, useMemo, useState } from 'react';
import { Input, Button, Empty, Spin, message } from 'antd';
import { SearchOutlined, DownOutlined, UpOutlined, MinusSquareOutlined } from '@ant-design/icons';
import apiClient from '../api/apiClient';
import '../css/KitchenPage.css';

const DEFAULT_FOOD_IMAGE = 'https://via.placeholder.com/120x120?text=FOOD';

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

  const fetchKitchenData = async () => {
    try {
      setLoading(true);
      const [ordersRes, foodsRes] = await Promise.all([
        apiClient.get('/v1/kitchen/orders'),
        apiClient.get('/v1/kitchen/foods/remaining')
      ]);

      const orders = ordersRes?.data?.data ?? [];
      const foods = foodsRes?.data?.data ?? [];

      setTableOrders(orders);
      setRemainingFoods(foods);
      setExpandedMap((prev) => {
        const next = { ...prev };
        orders.forEach((table) => {
          if (next[table.tableNumber] === undefined) {
            next[table.tableNumber] = true;
          }
        });
        return next;
      });
    } catch (error) {
      console.error(error);
      message.error('Khong tai duoc du lieu nha bep');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchKitchenData();
  }, []);

  const onServeItem = async (orderDetailId) => {
    try {
      setServingIds((prev) => [...prev, orderDetailId]);
      await apiClient.post(`/v1/kitchen/orders/items/${orderDetailId}/served`);
      message.success('Da cap nhat mon da hoan thanh');
      await fetchKitchenData();
    } catch (error) {
      console.error(error);
      const errorMessage = error?.response?.data?.message || 'Cap nhat mon that bai';
      message.error(errorMessage);
    } finally {
      setServingIds((prev) => prev.filter((id) => id !== orderDetailId));
    }
  };

  const filteredTableOrders = useMemo(() => {
    const normalized = searchText.trim().toLowerCase();
    if (!normalized) {
      return tableOrders;
    }

    return tableOrders
      .map((table) => {
        const matchedItems = table.items.filter((item) =>
          item.foodName?.toLowerCase().includes(normalized)
        );
        const matchedTable = table.tableNumber?.toLowerCase().includes(normalized);

        if (matchedTable) {
          return table;
        }

        return { ...table, items: matchedItems };
      })
      .filter((table) => table.items.length > 0 || table.tableNumber?.toLowerCase().includes(normalized));
  }, [searchText, tableOrders]);

  const filteredRemainingFoods = useMemo(() => {
    const normalized = searchText.trim().toLowerCase();
    if (!normalized) {
      return remainingFoods;
    }

    return remainingFoods.filter((food) =>
      food.foodName?.toLowerCase().includes(normalized)
    );
  }, [searchText, remainingFoods]);

  const headerTitle = activeTab === 'orders' ? 'Don hang' : 'Mon an';

  return (
    <div className="kitchen-page">
      <aside className="kitchen-sidebar">
        <div className="kitchen-logo">LOGO</div>

        <Button
          className={`kitchen-tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
          onClick={() => setActiveTab('orders')}
        >
          Don hang
        </Button>
        <Button
          className={`kitchen-tab-btn ${activeTab === 'foods' ? 'active' : ''}`}
          onClick={() => setActiveTab('foods')}
        >
          Danh sach mon
        </Button>
      </aside>

      <section className="kitchen-main">
        <div className="kitchen-topbar">
          <h3>{headerTitle}</h3>
          <Input
            prefix={<SearchOutlined />}
            value={searchText}
            onChange={(event) => setSearchText(event.target.value)}
            placeholder="search"
            className="kitchen-search"
          />
        </div>

        <div className="kitchen-content">
          {loading ? (
            <div className="kitchen-loading">
              <Spin size="large" />
            </div>
          ) : activeTab === 'orders' ? (
            filteredTableOrders.length === 0 ? (
              <Empty description="Khong co don hang dang cho" />
            ) : (
              filteredTableOrders.map((table) => {
                const expanded = expandedMap[table.tableNumber] !== false;

                return (
                  <div className="table-card" key={table.tableNumber}>
                    <div className="table-card-header">
                      <span>
                        Ban: {table.tableNumber} - {formatElapsed(table.elapsedMinutes)}
                      </span>
                      <button
                        type="button"
                        className="toggle-btn"
                        onClick={() =>
                          setExpandedMap((prev) => ({
                            ...prev,
                            [table.tableNumber]: !expanded
                          }))
                        }
                      >
                        {expanded ? (
                          <>
                            An bot <UpOutlined />
                          </>
                        ) : (
                          <>
                            Hien them <DownOutlined />
                          </>
                        )}
                      </button>
                    </div>

                    {expanded && (
                      <div className="table-card-body">
                        {table.items.map((item) => (
                          <div className="kitchen-item" key={item.orderDetailId}>
                            <img
                              src={item.imageUrlFood || DEFAULT_FOOD_IMAGE}
                              alt={item.foodName}
                              className="item-image"
                              onError={(event) => {
                                event.currentTarget.src = DEFAULT_FOOD_IMAGE;
                              }}
                            />
                            <div className="item-info">
                              <p className="item-title">{item.foodName}</p>
                              <p className="item-qty">So luong: {item.quantity}</p>
                            </div>
                            <Button
                              type="text"
                              icon={<MinusSquareOutlined />}
                              className="done-btn"
                              onClick={() => onServeItem(item.orderDetailId)}
                              loading={servingIds.includes(item.orderDetailId)}
                            />
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                );
              })
            )
          ) : filteredRemainingFoods.length === 0 ? (
            <Empty description="Khong co mon con lai" />
          ) : (
            <div className="food-list-card">
              {filteredRemainingFoods.map((food) => (
                <div className="kitchen-item" key={food.foodId}>
                  <img
                    src={food.imageUrlFood || DEFAULT_FOOD_IMAGE}
                    alt={food.foodName}
                    className="item-image"
                    onError={(event) => {
                      event.currentTarget.src = DEFAULT_FOOD_IMAGE;
                    }}
                  />
                  <div className="item-info">
                    <p className="item-title">{food.foodName}</p>
                    <p className="item-qty">So luong con lai: {food.remainingQuantity}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
};

export default KitchenPage;


