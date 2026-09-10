import { copyFileSync, existsSync } from 'node:fs'
import { resolve } from 'node:path'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig, type Plugin } from 'vite'

function normalizeBase(raw: string | undefined): string {
  if (!raw || raw === '/') {
    return '/'
  }
  return raw.endsWith('/') ? raw : `${raw}/`
}

/** GitHub Pages has no SPA rewrite; unknown paths serve 404.html. */
function githubPagesSpaFallback(): Plugin {
  return {
    name: 'github-pages-spa-fallback',
    writeBundle() {
      const indexHtml = resolve('dist/index.html')
      if (existsSync(indexHtml)) {
        copyFileSync(indexHtml, resolve('dist/404.html'))
      }
    }
  }
}

export default defineConfig({
  base: normalizeBase(process.env.VITE_BASE_PATH),
  plugins: [react(), tailwindcss(), githubPagesSpaFallback()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
})
