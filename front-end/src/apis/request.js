import axios from 'axios';
const instance = axios.create({
  // baseURL: "http://101.35.228.84:8882/luckyjourney",
  baseURL: "/api",
  timeout: 3000,
  headers: {},
});

// 添加请求拦截器
instance.interceptors.request.use(config => {
  return config;
}, error => {
  return Promise.reject(error)
})

// 添加响应拦截器
instance.interceptors.response.use((response) => {
  // 2xx 范围内的状态码都会触发该函数。
  // 对响应数据做点什么
  return response;
}, (error) => {
  // 超出 2xx 范围的状态码都会触发该函数。
  // 对响应错误做点什么
  return ;
});

export default instance;