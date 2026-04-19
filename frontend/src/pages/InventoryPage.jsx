import React, { useEffect, useMemo, useState } from 'react';
import { message, Empty, Spin } from 'antd';
import axios from 'axios';
import '../css/InventoryPage.css';

const API_BASE_URL = 'http://localhost:8080/api';
const DEFAULT_RECEIPT_STATUS = 'CHO';

const IngredientImage = ({ src, alt }) => {
  const finalSrc = src || '/images/default-food.png';

  return (
    <div className="inventory-image-box">
      <img
        src={finalSrc}
        alt={alt || 'ingredient'}
        className="inventory-real-image"
        onError={(e) => {
          e.currentTarget.src = '/images/default-food.png';
        }}
      />
    </div>
  );
};

const formatMoney = (value) => {
  const number = Number(value || 0);
  return `${number.toLocaleString('vi-VN')} đ`;
};

const formatDateVN = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return dateStr;

  return `Ngày ${String(date.getDate()).padStart(2, '0')} Tháng ${String(
    date.getMonth() + 1
  ).padStart(2, '0')} Năm ${date.getFullYear()}`;
};

const normalizeStatus = (status) => {
  const value = (status || '').trim().toUpperCase();

  if (!value) return 'CHO';
  if (['CHO', 'PENDING'].includes(value)) return 'CHO';
  if (['DA_NHAP', 'RECEIVED', 'NHAN_HANG'].includes(value)) return 'DA_NHAP';
  if (['HOAN_TRA', 'RETURNED', 'CANCELLED'].includes(value)) return 'HOAN_TRA';

  return value;
};

const getStatusText = (status) => {
  const normalized = normalizeStatus(status);

  if (normalized === 'CHO') return 'Chờ';
  if (normalized === 'DA_NHAP') return 'Đã nhập';
  if (normalized === 'HOAN_TRA') return 'Hoàn trả';

  return normalized;
};

const isPendingStatus = (status) => normalizeStatus(status) === 'CHO';

function InventoryTrackingTab({ items, loading }) {
  if (loading) {
    return (
      <div className="inventory-loading">
        <Spin size="large" />
      </div>
    );
  }

  if (!items.length) {
    return <Empty description="Chưa có dữ liệu tồn kho" />;
  }

  return (
    <div className="inventory-panel inventory-scroll-area">
      {items.map((item) => (
        <div
          key={item.ingredientId}
          className={`inventory-card ${item.lowStock ? 'inventory-card--warning' : ''}`}
        >
          <IngredientImage
            src={item.imageUrlIngredient}
            alt={item.ingredientName}
          />

          <div className="inventory-card__center">
            <div className="inventory-card__title">{item.ingredientName}</div>
            <div className="inventory-card__sub">Mã nguyên liệu: {item.ingredientId}</div>
            <div className="inventory-card__sub">
              Số lượng tồn: {item.currentStock} {item.unit || ''}
            </div>
            <div className="inventory-card__sub">Mức tối thiểu: {item.minStock}</div>
          </div>

          <div className="inventory-card__right">
            {item.lowStock ? 'Nguyên liệu sắp hết' : 'Tồn kho ổn định'}
          </div>
        </div>
      ))}
    </div>
  );
}

function ImportHistoryTab({
  receipts,
  loading,
  onCreateNew,
  supplierKeyword,
  setSupplierKeyword,
  fromDate,
  setFromDate,
  toDate,
  setToDate,
  ingredientId,
  setIngredientId,
  inventoryItems,
  handleSearch,
  handleReset,
  handleUpdateStatus,
  updatingReceiptId
}) {
  if (loading) {
    return (
      <div className="inventory-loading">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="inventory-scroll-page">
      <div className="inventory-create-title">Lịch sử nhập nguyên liệu</div>

      <div className="inventory-search-box">
        <div className="inventory-search-grid">
          <div className="form-group">
            <label>Nhà cung cấp:</label>
            <input
              value={supplierKeyword}
              onChange={(e) => setSupplierKeyword(e.target.value)}
              placeholder="Nhập tên NCC"
            />
          </div>

          <div className="form-group">
            <label>Từ ngày:</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Đến ngày:</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Nguyên liệu:</label>
            <select
              value={ingredientId}
              onChange={(e) => setIngredientId(e.target.value)}
            >
              <option value="">-- Tất cả --</option>
              {inventoryItems.map((item) => (
                <option key={item.ingredientId} value={item.ingredientId}>
                  {item.ingredientId} - {item.ingredientName}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="inventory-search-actions">
  <button type="button" className="btn btn-primary" onClick={handleSearch}>
    Tìm kiếm
  </button>
  <button type="button" className="btn btn-secondary" onClick={handleReset}>
    Làm mới
  </button>
  <button type="button" className="btn btn-primary" onClick={onCreateNew}>
    Tạo phiếu mới
  </button>
</div>
      </div>

      {!receipts.length ? (
        <Empty description="Chưa có phiếu nhập" />
      ) : (
        receipts.map((receipt) => {
          const totalPrice = (receipt.details || []).reduce((sum, item) => {
            return sum + Number(item.quantityImport || 0) * Number(item.importPrice || 0);
          }, 0);

          const normalizedStatus = normalizeStatus(receipt.status);
          const locked = !isPendingStatus(normalizedStatus);
          const isUpdating = updatingReceiptId === receipt.idReceipt;

          return (
            <div className="receipt-block" key={receipt.idReceipt}>
              <div className="receipt-block__header">
                <span>{receipt.idReceipt} - NCC: {receipt.supplierName || '---'}</span>
                <span>{formatDateVN(receipt.receiptDate)}</span>
              </div>

              <div className="receipt-block__body">
                <div className="receipt-block__items inventory-scroll-area small">
                  {(receipt.details || []).map((item, index) => (
                    <div className="receipt-item" key={`${receipt.idReceipt}-${index}`}>
                      <IngredientImage
                        src={item.imageUrlIngredient}
                        alt={item.ingredientName}
                      />

                      <div className="receipt-item__main">
                        <div className="receipt-item__code">{item.ingredientId}</div>
                        <div className="receipt-item__name">{item.ingredientName}</div>
                      </div>

                      <div className="receipt-item__meta">
                        <div>Số lượng nhập: <span>{item.quantityImport}</span></div>
                        <div>Đơn giá: <span>{formatMoney(item.importPrice)}</span></div>
                        <div>
                          Tổng tiền:{' '}
                          <span>
                            {formatMoney(
                              Number(item.quantityImport || 0) * Number(item.importPrice || 0)
                            )}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="receipt-actions">
                  {!locked ? (
                    <>
                      <button
                        type="button"
                        className="btn btn-danger"
                        onClick={() => handleUpdateStatus(receipt, 'HOAN_TRA')}
                        disabled={isUpdating}
                      >
                        {isUpdating ? 'Đang cập nhật...' : 'Hoàn trả'}
                      </button>

                      <button
                        type="button"
                        className="btn btn-success"
                        onClick={() => handleUpdateStatus(receipt, 'DA_NHAP')}
                        disabled={isUpdating}
                      >
                        {isUpdating ? 'Đang cập nhật...' : 'Nhận hàng'}
                      </button>
                    </>
                  ) : (
                    <div className={`status-badge status-badge--${normalizedStatus.toLowerCase()}`}>
                      Trạng thái: {getStatusText(normalizedStatus)}
                    </div>
                  )}

                  <div>Người tạo: {receipt.createdBy || '---'}</div>
                  <div className="receipt-total">Tổng giá: {formatMoney(totalPrice)}</div>
                </div>
              </div>
            </div>
          );
        })
      )}

    </div>
  );
}

function CreateImportTab({ inventoryItems, onBack, onCreated }) {
  const [supplier, setSupplier] = useState('');
  const [date, setDate] = useState('');
  const [createdBy, setCreatedBy] = useState(localStorage.getItem('username') || 'admin');
  const [rows, setRows] = useState([
    { ingredientId: '', quantityImport: 1, importPrice: 0 }
  ]);
  const [submitting, setSubmitting] = useState(false);

  const grandTotal = useMemo(() => {
    return rows.reduce((sum, row) => {
      return sum + Number(row.quantityImport || 0) * Number(row.importPrice || 0);
    }, 0);
  }, [rows]);

  const updateRow = (index, field, value) => {
    const next = [...rows];
    next[index][field] = value;
    setRows(next);
  };

  const addRow = () => {
    setRows([...rows, { ingredientId: '', quantityImport: 1, importPrice: 0 }]);
  };

  const removeRow = () => {
    if (rows.length <= 1) return;
    setRows(rows.slice(0, rows.length - 1));
  };

  const getIngredientInfo = (ingredientId) => {
    return inventoryItems.find((item) => item.ingredientId === ingredientId);
  };

  const handleSubmit = async () => {
    if (!supplier.trim()) {
      message.warning('Vui lòng nhập nhà cung cấp');
      return;
    }

    if (!date) {
      message.warning('Vui lòng chọn ngày lập phiếu');
      return;
    }

    const validRows = rows.filter((row) => row.ingredientId);

    if (!validRows.length) {
      message.warning('Vui lòng chọn ít nhất 1 nguyên liệu');
      return;
    }

    const payload = {
      supplierName: supplier,
      receiptDate: date,
      status: DEFAULT_RECEIPT_STATUS,
      createdBy,
      details: validRows.map((row) => ({
        ingredientId: row.ingredientId,
        quantityImport: Number(row.quantityImport || 0),
        importPrice: Number(row.importPrice || 0)
      }))
    };

    try {
      setSubmitting(true);
      await axios.post(`${API_BASE_URL}/inventory/receipts`, payload);
      message.success('Tạo phiếu nhập thành công');
      onCreated();
    } catch (err) {
      console.error(err);
      message.error(err.response?.data?.message || 'Tạo phiếu nhập thất bại');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="create-receipt-page">
      <div className="create-receipt-header">
        <button type="button" className="back-button" onClick={onBack}>↩</button>
        <div className="inventory-create-title">Tạo phiếu nhập nguyên liệu</div>
      </div>

      <div className="create-receipt-panel">
        <div className="create-top-grid">
          <div className="form-group">
            <label>Nhà cung cấp:</label>
            <input value={supplier} onChange={(e) => setSupplier(e.target.value)} />
          </div>

          <div className="form-group">
            <label>Ngày lập phiếu:</label>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
          </div>

          <div className="form-group">
            <label>Trạng thái:</label>
            <input value="Chờ" readOnly />
          </div>

          <div className="form-group">
            <label>Người tạo:</label>
            <input value={createdBy} onChange={(e) => setCreatedBy(e.target.value)} />
          </div>
        </div>

        <div className="create-list-header">
          <h3>Danh sách nguyên liệu nhập</h3>

          <div className="mini-actions">
            <button type="button" onClick={addRow}>Thêm</button>
            <button type="button" onClick={removeRow}>Xóa</button>
          </div>
        </div>

        <div className="inventory-scroll-area create-list">
          {rows.map((row, index) => {
            const ingredient = getIngredientInfo(row.ingredientId);
            const total = Number(row.quantityImport || 0) * Number(row.importPrice || 0);

            return (
              <div className="create-item" key={index}>
                <IngredientImage
                  src={ingredient?.imageUrlIngredient}
                  alt={ingredient?.ingredientName}
                />

                <div className="create-item__main">
                  <div className="meta-row">
                    <label>Nguyên liệu:</label>
                    <select
                      value={row.ingredientId}
                      onChange={(e) => updateRow(index, 'ingredientId', e.target.value)}
                    >
                      <option value="">-- Chọn nguyên liệu --</option>
                      {inventoryItems.map((item) => (
                        <option key={item.ingredientId} value={item.ingredientId}>
                          {item.ingredientId} - {item.ingredientName}
                        </option>
                      ))}
                    </select>
                  </div>

                  <div className="create-item__name">
                    {ingredient?.ingredientName || 'Tên nguyên liệu'}
                  </div>

                  <div className="create-item__stock">
                    Số lượng tồn: {ingredient?.currentStock ?? 0} {ingredient?.unit || ''}
                  </div>
                </div>

                <div className="create-item__meta">
                  <div className="meta-row">
                    <label>Số lượng nhập:</label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={row.quantityImport}
                      onChange={(e) => updateRow(index, 'quantityImport', e.target.value)}
                    />
                  </div>

                  <div className="meta-row">
                    <label>Đơn giá:</label>
                    <input
                      type="number"
                      min="0"
                      step="0.01"
                      value={row.importPrice}
                      onChange={(e) => updateRow(index, 'importPrice', e.target.value)}
                    />
                  </div>

                  <div className="meta-row">
                    <label>Tổng tiền:</label>
                    <input value={formatMoney(total)} readOnly />
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="create-footer">
          <div className="grand-total">
            <label>Thành tiền:</label>
            <input value={formatMoney(grandTotal)} readOnly />
          </div>

          <button
            type="button"
            className="submit-receipt-btn"
            onClick={handleSubmit}
            disabled={submitting}
          >
            {submitting ? 'Đang gửi...' : 'Gửi phiếu'}
          </button>
        </div>
      </div>
    </div>
  );
}

function ConsumptionHistoryTab({ groups, loading }) {
  if (loading) {
    return (
      <div className="inventory-loading">
        <Spin size="large" />
      </div>
    );
  }

  if (!groups.length) {
    return (
      <div className="inventory-scroll-page">
        <div className="inventory-create-title">Lịch sử tiêu thụ nguyên liệu</div>
        <Empty description="Chưa có dữ liệu tiêu thụ nguyên liệu" />
      </div>
    );
  }

  return (
    <div className="inventory-scroll-page">
      <div className="inventory-create-title">Lịch sử tiêu thụ nguyên liệu</div>

      {groups.map((group, groupIndex) => (
        <div className="receipt-block" key={`${group.date}-${groupIndex}`}>
          <div className="receipt-block__header">
            <span>{formatDateVN(group.date)}</span>
          </div>

          <div className="receipt-block__body">
            <div className="receipt-block__items inventory-scroll-area small">
              {(group.items || []).map((item, index) => (
                <div className="receipt-item" key={`${group.date}-${item.ingredientId}-${index}`}>
                  <IngredientImage
                    src={item.imageUrlIngredient}
                    alt={item.ingredientName}
                  />

                  <div className="receipt-item__main">
                    <div className="receipt-item__code">{item.ingredientId}</div>
                    <div className="receipt-item__name">{item.ingredientName}</div>
                    <div className="inventory-card__sub">
                      Tồn kho hiện tại: {item.currentStock} {item.unit || ''}
                    </div>
                  </div>

                  <div className="receipt-item__meta">
                    <div>
                      Số lượng đã tiêu thụ: <span>{item.consumedQuantity}</span>
                    </div>
                    <div>
                      Đơn vị: <span>{item.unit || '---'}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function InventoryPage() {
  const [mainTab, setMainTab] = useState('tracking');
  const [importMode, setImportMode] = useState('history');

  const [inventoryItems, setInventoryItems] = useState([]);
  const [receipts, setReceipts] = useState([]);
  const [consumptionHistory, setConsumptionHistory] = useState([]);

  const [loadingInventory, setLoadingInventory] = useState(false);
  const [loadingReceipts, setLoadingReceipts] = useState(false);
  const [loadingConsumption, setLoadingConsumption] = useState(false);
  const [updatingReceiptId, setUpdatingReceiptId] = useState('');

  const [supplierKeyword, setSupplierKeyword] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [ingredientId, setIngredientId] = useState('');

  const fetchInventoryReport = async () => {
    try {
      setLoadingInventory(true);
      const res = await axios.get(`${API_BASE_URL}/inventory/report`);
      setInventoryItems(res.data.data || []);
    } catch (err) {
      console.error(err);
      message.error('Lỗi lấy dữ liệu tồn kho!');
    } finally {
      setLoadingInventory(false);
    }
  };

  const fetchReceipts = async () => {
    try {
      setLoadingReceipts(true);
      const res = await axios.get(`${API_BASE_URL}/inventory/receipts`);
      setReceipts(res.data.data || []);
    } catch (err) {
      console.error(err);
      message.error('Lỗi lấy phiếu nhập!');
    } finally {
      setLoadingReceipts(false);
    }
  };

  const fetchConsumptionHistory = async (fromDateParam, toDateParam) => {
    try {
      setLoadingConsumption(true);
      const params = {};
      if (fromDateParam) params.fromDate = fromDateParam;
      if (toDateParam) params.toDate = toDateParam;

      const res = await axios.get(`${API_BASE_URL}/inventory/consumption-history`, { params });
      setConsumptionHistory(res.data.data || []);
    } catch (err) {
      console.error(err);
      message.error('Lỗi lấy lịch sử tiêu thụ nguyên liệu!');
    } finally {
      setLoadingConsumption(false);
    }
  };

  const searchReceipts = async () => {
    try {
      setLoadingReceipts(true);

      const params = {};
      if (supplierKeyword.trim()) params.supplierName = supplierKeyword.trim();
      if (fromDate) params.fromDate = fromDate;
      if (toDate) params.toDate = toDate;
      if (ingredientId) params.ingredientId = ingredientId;

      const res = await axios.get(`${API_BASE_URL}/inventory/receipts/search`, { params });
      setReceipts(res.data.data || []);
    } catch (err) {
      console.error(err);
      message.error('Lỗi tìm phiếu nhập!');
    } finally {
      setLoadingReceipts(false);
    }
  };

  const resetSearch = async () => {
    setSupplierKeyword('');
    setFromDate('');
    setToDate('');
    setIngredientId('');
    await fetchReceipts();
  };

  useEffect(() => {
    fetchInventoryReport();
    fetchReceipts();
    fetchConsumptionHistory();
  }, []);

const handleCreated = async () => {
  setImportMode('history');
  await fetchInventoryReport();
  await fetchReceipts();
  await fetchConsumptionHistory();
};

  const handleUpdateStatus = async (receipt, nextStatus) => {
    if (!isPendingStatus(receipt.status)) {
      message.warning('Phiếu này đã cập nhật trạng thái, không thể đổi lại.');
      return;
    }

    const payload = {
      supplierName: receipt.supplierName,
      receiptDate: receipt.receiptDate,
      status: nextStatus,
      createdBy: receipt.createdBy,
      details: (receipt.details || []).map((item) => ({
        ingredientId: item.ingredientId,
        quantityImport: Number(item.quantityImport || 0),
        importPrice: Number(item.importPrice || 0)
      }))
    };

    try {
  setUpdatingReceiptId(receipt.idReceipt);
  await axios.put(`${API_BASE_URL}/inventory/receipts/${receipt.idReceipt}`, payload);
  message.success('Cập nhật trạng thái phiếu thành công');

  await fetchReceipts();
  await fetchInventoryReport();
  await fetchConsumptionHistory();
} catch (err) {
      console.error(err);
      message.error(err.response?.data?.message || 'Cập nhật trạng thái thất bại');
    } finally {
      setUpdatingReceiptId('');
    }
  };

  return (
    <div className="inventory-page">
      <div className="inventory-top-tabs">
        <button
          type="button"
          className={mainTab === 'tracking' ? 'active' : ''}
          onClick={() => setMainTab('tracking')}
        >
          Sổ theo dõi
          <br />
          nguyên liệu
        </button>

        <button
          type="button"
          className={mainTab === 'import' ? 'active' : ''}
          onClick={() => setMainTab('import')}
        >
          Lịch sử nhập
          <br />
          nguyên liệu
        </button>

        <button
          type="button"
          className={mainTab === 'consume' ? 'active' : ''}
          onClick={() => setMainTab('consume')}
        >
          Lịch sử tiêu thụ
          <br />
          nguyên liệu
        </button>
      </div>

      {mainTab === 'tracking' && (
        <InventoryTrackingTab items={inventoryItems} loading={loadingInventory} />
      )}

      {mainTab === 'import' && (
        <>
          {importMode === 'history' ? (
            <ImportHistoryTab
              receipts={receipts}
              loading={loadingReceipts}
              onCreateNew={() => setImportMode('create')}
              supplierKeyword={supplierKeyword}
              setSupplierKeyword={setSupplierKeyword}
              fromDate={fromDate}
              setFromDate={setFromDate}
              toDate={toDate}
              setToDate={setToDate}
              ingredientId={ingredientId}
              setIngredientId={setIngredientId}
              inventoryItems={inventoryItems}
              handleSearch={searchReceipts}
              handleReset={resetSearch}
              handleUpdateStatus={handleUpdateStatus}
              updatingReceiptId={updatingReceiptId}
            />
          ) : (
            <CreateImportTab
              inventoryItems={inventoryItems}
              onBack={() => setImportMode('history')}
              onCreated={handleCreated}
            />
          )}
        </>
      )}

      {mainTab === 'consume' && (
        <ConsumptionHistoryTab
          groups={consumptionHistory}
          loading={loadingConsumption}
        />
      )}
    </div>
  );
}