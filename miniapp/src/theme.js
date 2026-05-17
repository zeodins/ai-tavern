const themes = {
  mint: {
    name: '薄荷绿',
    primary: '#7CB9A8',
    primaryLight: '#B7DDD2',
    primaryBg: '#EAF5F1',
    bg: '#F4F7F6',
    white: '#FFFFFF',
    text: '#3D4A46',
    textSecondary: '#92A69E',
    textLight: '#B0C3BA',
    border: '#E8EEEA',
    shadow: '0 1px 3px rgba(60,80,70,0.06)'
  },
  ocean: {
    name: '深海蓝',
    primary: '#4D6BFE',
    primaryLight: '#B3C4FF',
    primaryBg: '#EDF0FF',
    bg: '#F5F7FA',
    white: '#FFFFFF',
    text: '#2C3E50',
    textSecondary: '#7F8C8D',
    textLight: '#A0AEC0',
    border: '#E8ECF1',
    shadow: '0 1px 3px rgba(40,60,120,0.06)'
  },
  warm: {
    name: '暖桃色',
    primary: '#E8957C',
    primaryLight: '#F5C6B8',
    primaryBg: '#FDF0EB',
    bg: '#FDF6F3',
    white: '#FFFFFF',
    text: '#4A3530',
    textSecondary: '#B8958A',
    textLight: '#D4BEB6',
    border: '#F0E3DD',
    shadow: '0 1px 3px rgba(80,40,30,0.06)'
  },
  dark: {
    name: '暗夜模式',
    primary: '#8B9DC3',
    primaryLight: '#B8C8E0',
    primaryBg: '#2A2D3E',
    bg: '#1E1E2E',
    white: '#2D2D3F',
    text: '#CDD6F4',
    textSecondary: '#9399B2',
    textLight: '#6C7086',
    border: '#3D3D55',
    shadow: '0 1px 3px rgba(0,0,0,0.2)'
  }
}

const THEME_KEY = 'ai_tavern_theme'

export function getCurrentTheme() {
  const name = uni.getStorageSync(THEME_KEY) || 'mint'
  return themes[name] || themes.mint
}

export function getThemeName() {
  return uni.getStorageSync(THEME_KEY) || 'mint'
}

export function setTheme(name) {
  if (!themes[name]) return
  uni.setStorageSync(THEME_KEY, name)
  // Store in globalData for pages to access
  const app = getApp()
  if (app) {
    app.globalData = app.globalData || {}
    app.globalData.theme = themes[name]
    app.globalData.themeName = name
  }
}

export function getAllThemes() {
  return Object.entries(themes).map(([key, value]) => ({
    key,
    name: value.name,
    primary: value.primary
  }))
}
