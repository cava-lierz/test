import React from 'react';
import ReactDOM from 'react-dom/client';
import './styles/index.css';
import App from './App';
import { authAPI } from './services/api';
import { AUTH_CONFIG } from "./utils/constants";

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

let isAuthErrorHandled = false;
window.addEventListener('unhandledrejection', function(event) {
  if (
    event.reason &&
    (event.reason.message === '登录已过期，请重新登录' ||
     event.reason.message === 'Full authentication is required to access this resource')
  ) {
    if (!isAuthErrorHandled) {
      isAuthErrorHandled = true;
      // 调用全局登出逻辑
      const userInfo = localStorage.getItem(`${AUTH_CONFIG.USER_INFO_KEY}`);
      let userId = null;
      if (userInfo) {
        try {
          userId = JSON.parse(userInfo).id;
        } catch {}
      }
      authAPI.logout(userId);
      window.location.href = '/login';
    }
  }
});
