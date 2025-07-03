import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import UnoCSS from 'unocss/vite'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig({
    plugins: [
        vue(),
        UnoCSS()
    ],
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    },
    server: {
        port: 3000,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                secure: false
            }
        }
    },
    define: {
        'import.meta.env.VITE_WEBSOCKET_URL': JSON.stringify('http://localhost:8080/ws'),
        'import.meta.env.VITE_API_BASE_URL': JSON.stringify('http://localhost:8080/api'),
        global: 'globalThis'
    }
}) 