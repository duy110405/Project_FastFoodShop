import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  // Thêm đoạn cấu hình này để định nghĩa 'global' là 'window' trên trình duyệt
  define: {
    global: 'window',
  },
})