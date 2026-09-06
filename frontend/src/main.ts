import { createApp } from 'vue'
import { createPinia } from 'pinia'
import 'element-plus/theme-chalk/el-message.css'
import './styles.css'
import App from './App.vue'
createApp(App).use(createPinia()).mount('#app')
