import { createApp } from "vue"
import App from "./App.vue"
import {createBootstrap} from "bootstrap-vue-next/plugins/createBootstrap"

import "bootstrap/dist/css/bootstrap.css"
import "bootstrap-vue-next/dist/bootstrap-vue-next.css"
import "./style.css"

createApp(App).use(createBootstrap()).mount("#app")
