import '@mdi/font/css/materialdesignicons.css';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import 'vuetify/styles';
const myCustomLightTheme = {
  dark: false,
  colors: {
    background: "rgb(74 76 81)",
    surface: '#36393f',
    primary: '#5865f2',
    
  },
}
const vuetify = createVuetify({
  ssr: true,
  components,
    directives,
    defaults: {
      VBtn: { color: '#5865f2' }
    },
    theme: {
      defaultTheme: "myCustomLightTheme",
      themes: {
        myCustomLightTheme
      }
    }
})
export default vuetify;