import vue from '@vitejs/plugin-vue'
import { defineConfig } from 'vite'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': 'src'
    }
  },
  server: {
    port: 5378,
    proxy: {
      '/api': {
        target: 'http://192.168.0.107:8883/luckyjourney',
        rewrite: (path) => path.replace(/^\/api/, ""),
        changeOrigin: true
      }
    }
  },
  rules: [
    {
      test: /\.scss$/,
      use: [
        'style-loader',
        {
          loader: 'css-loader',
          options: { sourceMap: true },
        },
        {
          loader: 'sass-loader',
          options: { sourceMap: true },
        },
      ],
    },
  ],
})
