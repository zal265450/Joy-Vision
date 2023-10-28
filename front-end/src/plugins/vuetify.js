import '@mdi/font/css/materialdesignicons.css';
import { createVuetify } from 'vuetify';
import * as components from 'vuetify/components';
import * as directives from 'vuetify/directives';
import 'vuetify/styles';
const myCustomLightTheme = {
  dark: false,
  colors: {
    background: "#252632",
    surface: '#36393f',
    primary: '#5865f2',
    'primary-darken-1': '#3700B3',
    secondary: '#03DAC6',
    'secondary-darken-1': '#018786',
    
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