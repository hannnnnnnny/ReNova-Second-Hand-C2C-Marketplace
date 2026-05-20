import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const tunnelAllowedHosts = ['.loca.lt', '.trycloudflare.com']

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    allowedHosts: tunnelAllowedHosts,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  },
  preview: {
    allowedHosts: tunnelAllowedHosts
  }
})
