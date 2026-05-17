const themes = {
  mint: { name: '薄荷绿', primary: '#7CB9A8', primaryLight: '#B7DDD2', primaryBg: '#EAF5F1', bg: '#F4F7F6', white: '#FFFFFF', text: '#3D4A46', textSecondary: '#92A69E', textLight: '#B0C3BA', border: '#E8EEEA', shadow: '0 1px 3px rgba(60,80,70,0.06)' },
  ocean: { name: '深海蓝', primary: '#4D6BFE', primaryLight: '#B3C4FF', primaryBg: '#EDF0FF', bg: '#F5F7FA', white: '#FFFFFF', text: '#2C3E50', textSecondary: '#7F8C8D', textLight: '#A0AEC0', border: '#E8ECF1', shadow: '0 1px 3px rgba(40,60,120,0.06)' },
  warm: { name: '暖桃色', primary: '#E8957C', primaryLight: '#F5C6B8', primaryBg: '#FDF0EB', bg: '#FDF6F3', white: '#FFFFFF', text: '#4A3530', textSecondary: '#B8958A', textLight: '#D4BEB6', border: '#F0E3DD', shadow: '0 1px 3px rgba(80,40,30,0.06)' },
  dark: { name: '暗夜模式', primary: '#8B9DC3', primaryLight: '#B8C8E0', primaryBg: '#2A2D3E', bg: '#1E1E2E', white: '#2D2D3F', text: '#CDD6F4', textSecondary: '#9399B2', textLight: '#6C7086', border: '#3D3D55', shadow: '0 1px 3px rgba(0,0,0,0.2)' }
}

const THEME_KEY = 'ai_tavern_web_theme'

export function getCurrentTheme() {
  const name = localStorage.getItem(THEME_KEY) || 'mint'
  return themes[name] || themes.mint
}

export function getThemeName() {
  return localStorage.getItem(THEME_KEY) || 'mint'
}

export function setTheme(name) {
  if (!themes[name]) return
  localStorage.setItem(THEME_KEY, name)
  applyTheme(name)
}

export function getAllThemes() {
  return Object.entries(themes).map(([key, value]) => ({ key, name: value.name, primary: value.primary }))
}

export function applyTheme(name) {
  const theme = themes[name] || themes.mint
  const root = document.documentElement
  root.style.setProperty('--primary', theme.primary)
  root.style.setProperty('--primary-light', theme.primaryLight)
  root.style.setProperty('--primary-bg', theme.primaryBg)
  root.style.setProperty('--bg', theme.bg)
  root.style.setProperty('--white', theme.white)
  root.style.setProperty('--text', theme.text)
  root.style.setProperty('--text-secondary', theme.textSecondary)
  root.style.setProperty('--text-light', theme.textLight)
  root.style.setProperty('--border', theme.border)
  root.style.setProperty('--shadow', theme.shadow)
}
