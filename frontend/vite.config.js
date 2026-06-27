import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const tunnelAllowedHosts = ['.loca.lt', '.trycloudflare.com']
const githubPagesBase = '/ReNova-Second-Hand-C2C-Marketplace/'
const base = process.env.GITHUB_PAGES === 'true' ? githubPagesBase : '/'

export default defineConfig({
  base,
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
