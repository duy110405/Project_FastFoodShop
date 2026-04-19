import React, { useState, useEffect } from 'react';
import { Tabs, Button, Modal, Form, Input, InputNumber, Select, Space, Popconfirm, message, Empty, Spin } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, MinusCircleOutlined } from '@ant-design/icons';
import axios from 'axios';
import '../css/MenuAdmin.css';

const { Option } = Select;
const { TextArea } = Input;

// Đặt Base URL chuẩn của Backend
const API_BASE_URL = 'http://localhost:8081/api';

const MenuAdmin = () => {
  const [foods, setFoods] = useState([]);
  const [categories, setCategories] = useState([]);
  const [ingredientsList, setIngredientsList] = useState([]);
  const [foodCostsById, setFoodCostsById] = useState({});
  
  const [loading, setLoading] = useState(false); // Trạng thái chờ load data
  const [submitLoading, setSubmitLoading] = useState(false); // Trạng thái chờ nút Lưu

  const [activeTab, setActiveTab] = useState('LH001');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFood, setEditingFood] = useState(null);
  const [form] = Form.useForm();

  // GỌI API LẤY DỮ LIỆU KHI VỪA VÀO TRANG (READ)
 
  const fetchData = async () => {
    setLoading(true);
    try {
      // Chạy song song 3 API chính: Danh mục, Nguyên liệu và Món ăn
      const [catRes, ingRes, foodRes] = await Promise.all([
        axios.get(`${API_BASE_URL}/foodCategory`),
        axios.get(`${API_BASE_URL}/ingredient`), // API lấy danh sách kho
        axios.get(`${API_BASE_URL}/foods/menu`)   // API lấy list món ăn
      ]);

      // API chi phí món là dữ liệu phụ, lỗi thì vẫn cho màn hình menu chạy bình thường.
      let costs = [];
      try {
        const costRes = await axios.get(`${API_BASE_URL}/foods/costs`);
        costs = costRes.data?.data || [];
      } catch (costError) {
        console.warn('Không tải được chi phí món ăn:', costError);
      }

      setCategories(catRes.data.data || []);
      setIngredientsList(ingRes.data.data || []);
      setFoods(foodRes.data.data || []);
      setFoodCostsById(Object.fromEntries(costs.map(item => [item.idFood, item])));
      
      // Mặc định chọn Tab đầu tiên nếu có danh mục
      if (catRes.data.data && catRes.data.data.length > 0 && activeTab === 'LH001') {
        setActiveTab(catRes.data.data[0].idCategory);
      }
    } catch (error) {
      console.error("Lỗi lấy dữ liệu:", error);
      message.error("Lỗi kết nối máy chủ! Vui lòng bật Backend.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const filteredFoods = foods.filter(food => food.idCategory === activeTab);
  const formatCurrency = (value) => Number(value || 0).toLocaleString('vi-VN');

  // Mở Modal (Sửa hoặc Thêm mới)
  const openModal = (food = null) => {
    setEditingFood(food);
    if (food) {
      form.setFieldsValue(food);
    } else {
      form.resetFields();
      form.setFieldsValue({ idCategory: activeTab });
    }
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingFood(null);
    form.resetFields();
  };

  // GỌI API THÊM HOẶC SỬA MÓN (CREATE / UPDATE)

  const handleFinish = async (values) => {
    setSubmitLoading(true);
    try {
      if (editingFood) {
        // GỌI API SỬA MÓN (PUT)
        await axios.put(`${API_BASE_URL}/foods/${editingFood.idFood}`, values);
        message.success(`Đã cập nhật món: ${values.foodName}`);
      } else {
        // GỌI API THÊM MỚI (POST)
        await axios.post(`${API_BASE_URL}/foods`, values);
        message.success(`Đã thêm món mới: ${values.foodName}`);
      }
      
      closeModal();
      fetchData(); // Tải lại danh sách sau khi Lưu thành công
      
    } catch (error) {
      console.error("Lỗi lưu món ăn:", error);
      message.error(error.response?.data?.message || "Lỗi khi lưu món ăn!");
    } finally {
      setSubmitLoading(false);
    }
  };

  // GỌI API XÓA MÓN (DELETE)
  const handleDelete = async (idFood) => {
    try {
      await axios.delete(`${API_BASE_URL}/foods/${idFood}`);
      message.success(`Đã xóa món ăn thành công!`);
      fetchData(); // Tải lại danh sách
    } catch (error) {
      console.error("Lỗi xóa món ăn:", error);
      message.error("Không thể xóa món này. Có thể nó đang bị dính trong đơn hàng cũ!");
    }
  };

  // Biến mảng categories thành cấu trúc Tabs của Ant Design
  const tabItems = categories.map(cat => ({
    key: cat.idCategory,
    label: cat.categoryName.toUpperCase()
  }));

  return (
    <div className="menu-admin-container">
      
      {/* HEADER: TABS VÀ NÚT THÊM */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <Tabs 
          className="menu-admin-tabs" 
          activeKey={activeTab} 
          onChange={setActiveTab}
          style={{ flex: 1 }}
          items={tabItems}
        />
        <Button type="primary" icon={<PlusOutlined />} size="large" className="btn-primary-custom" onClick={() => openModal()}>
          Thêm Món
        </Button>
      </div>

      {/* DANH SÁCH MÓN ĂN (CÓ VÒNG QUAY LOADING) */}
      <Spin spinning={loading} tip="Đang tải dữ liệu...">
        <div className="food-list-wrapper">
          {filteredFoods.length === 0 ? (
            <Empty description="Không có món ăn nào trong danh mục này" style={{ marginTop: 50 }} />
          ) : (
            filteredFoods.map(food => (
              <div className="food-list-item" key={food.idFood}>
                <img src={food.imageUrlFood} alt={food.foodName} className="food-image" />
                
                <div className="food-info">
                  {(() => {
                    const costData = foodCostsById[food.idFood];
                    return (
                      <>
                  <h3 className="food-title">{food.foodName}</h3>
                  <div className="food-price">Giá: {food.unitPrice.toLocaleString('vi-VN')} đ</div>
                  <div className="food-desc">{food.description}</div>
                        {costData && (
                          <div className="food-cost-meta">
                            <div>Chi phí: {formatCurrency(costData.productionCost)} đ</div>
                            <div>Lãi gộp: {formatCurrency(costData.grossProfit)} đ</div>
                            <div>Biên lợi nhuận: {Number(costData.marginPercent || 0).toFixed(1)}%</div>
                          </div>
                        )}
                      </>
                    );
                  })()}
                </div>

                <div className="food-actions">
                  <Button onClick={() => openModal(food)} icon={<EditOutlined />}>Sửa</Button>
                  <Popconfirm title="Bạn có chắc chắn muốn xóa món này?" onConfirm={() => handleDelete(food.idFood)} okText="Xóa" cancelText="Hủy">
                    <Button danger icon={<DeleteOutlined />}>Xóa</Button>
                  </Popconfirm>
                </div>
              </div>
            ))
          )}
        </div>
      </Spin>

      {/* MODAL THÊM/SỬA MÓN ĂN */}
      <Modal
        title={editingFood ? "Sửa thông tin món ăn" : "Thêm món ăn mới"}
        open={isModalOpen}
        onCancel={closeModal}
        width={700}
        footer={[
          <Button key="cancel" onClick={closeModal}>Hủy</Button>,
          <Button key="submit" type="primary" className="btn-primary-custom" loading={submitLoading} onClick={() => form.submit()}>Lưu lại</Button>
        ]}
      >
        <Form form={form} layout="vertical" onFinish={handleFinish}>
          <div style={{ display: 'flex', gap: '20px' }}>
            <div style={{ flex: 1 }}>
              <Form.Item name="foodName" label="Tên món ăn" rules={[{ required: true, message: 'Vui lòng nhập tên món' }]}>
                <Input placeholder="VD: Hamburger Bò" />
              </Form.Item>
              
              <Form.Item name="idCategory" label="Danh mục" rules={[{ required: true }]}>
                <Select>
                  {categories.map(cat => (
                    <Option key={cat.idCategory} value={cat.idCategory}>{cat.categoryName}</Option>
                  ))}
                </Select>
              </Form.Item>

              <Form.Item name="unitPrice" label="Đơn giá (VNĐ)" rules={[{ required: true, message: 'Vui lòng nhập giá' }]}>
                <InputNumber style={{ width: '100%' }} formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
              </Form.Item>
            </div>

            <div style={{ flex: 1 }}>
              <Form.Item name="imageUrlFood" label="Đường dẫn ảnh (URL)" rules={[{ required: true, message: 'Vui lòng nhập link ảnh' }]}>
                <Input placeholder="/images/burger.jpg" />
              </Form.Item>

              <Form.Item name="description" label="Mô tả món ăn">
                <TextArea rows={4} placeholder="Nhập mô tả..." />
              </Form.Item>
            </div>
          </div>

          {/* LIST NGUYÊN LIỆU ĐỘNG */}
          <div style={{ marginTop: 10, padding: 15, background: '#f9f9f9', borderRadius: 8, border: '1px solid #e8e8e8' }}>
            <h4 style={{ margin: '0 0 15px 0', color: '#e05252' }}>Định mức nguyên liệu (Recipe)</h4>
            
            <Form.List name="ingredients">
              {(fields, { add, remove }) => (
                <>
                  {fields.map(({ key, name, ...restField }) => (
                    <Space key={key} style={{ display: 'flex', marginBottom: 8 }} align="baseline">
                      
                      <Form.Item {...restField} name={[name, 'idIngredient']} rules={[{ required: true, message: 'Chọn nguyên liệu' }]}>
                        <Select showSearch placeholder="Chọn nguyên liệu" style={{ width: 300 }}>
                          {ingredientsList.map(ing => (
                            <Option key={ing.idIngredient} value={ing.idIngredient}>{ing.ingredientName} ({ing.unit})</Option>
                          ))}
                        </Select>
                      </Form.Item>

                      <Form.Item {...restField} name={[name, 'quantityUsed']} rules={[{ required: true, message: 'Nhập SL' }]}>
                        <InputNumber placeholder="Số lượng" min={0.01} step={0.01} style={{ width: 120 }} />
                      </Form.Item>

                      <MinusCircleOutlined onClick={() => remove(name)} style={{ color: 'red', fontSize: 18, marginLeft: 10 }} />
                    </Space>
                  ))}
                  <Form.Item>
                    <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                      Thêm nguyên liệu cho món này
                    </Button>
                  </Form.Item>
                </>
              )}
            </Form.List>
          </div>
          
        </Form>
      </Modal>
    </div>
  );
};

export default MenuAdmin;