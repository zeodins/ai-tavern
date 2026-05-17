<script>
import { getCurrentTheme } from '@/theme'

export default {
  onLaunch() {
    console.log('AI Tavern App Launch')
    // Store current theme in globalData for pages to access
    this.globalData = this.globalData || {}
    const theme = getCurrentTheme()
    this.globalData.theme = theme
  }
}
</script>

<style>
page {
  background-color: #F4F7F6;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
</style>
