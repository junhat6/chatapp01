import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import 'uno.css'
import './style.css'

// Naive UI
import {
  create,
  NButton,
  NCard,
  NInput,
  NInputGroup,
  NSelect,
  NDatePicker,
  NTimePicker,
  NModal,
  NSpace,
  NGrid,
  NGridItem,
  NAlert,
  NIcon,
  NAvatar,
  NTag,
  NProgress,
  NDropdown,
  NMenu,
  NLayout,
  NLayoutHeader,
  NLayoutContent,
  NLayoutSider,
  NConfigProvider,
  NMessageProvider,
  NLoadingBarProvider,
  NDialogProvider,
  NNotificationProvider,
  NSpin,
  NForm,
  NFormItem,
  darkTheme
} from 'naive-ui'

const naive = create({
  components: [
    NButton,
    NCard,
    NInput,
    NInputGroup,
    NSelect,
    NDatePicker,
    NTimePicker,
    NModal,
    NSpace,
    NGrid,
    NGridItem,
    NAlert,
    NIcon,
    NAvatar,
    NTag,
    NProgress,
    NDropdown,
    NMenu,
    NLayout,
    NLayoutHeader,
    NLayoutContent,
    NLayoutSider,
    NConfigProvider,
    NMessageProvider,
    NLoadingBarProvider,
    NDialogProvider,
    NNotificationProvider,
    NSpin,
    NForm,
    NFormItem,
  ]
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(naive)

app.mount('#app') 