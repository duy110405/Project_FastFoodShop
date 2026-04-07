import { useState } from 'react'
import './App.css'

function App() {
  const [orderCount, setOrderCount] = useState(0)

  return (
    <>
      <h1>🍔 Project FastFood 🍕</h1>
      <div className="card">
        <h2>Chào mừng bạn đến với hệ thống đặt đồ ăn!</h2>
        <p>Nhánh Develop đang hoạt động rất tốt.</p>
        
        <button onClick={() => setOrderCount((count) => count + 1)}>
          🛒 Thêm vào giỏ hàng: {orderCount} món
        </button>
      </div>
    </>
  )
}

export default App