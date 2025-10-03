import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Header from "./Header";
import Sidebar from "./Sidebar";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import ForgotPasswordPage from "../pages/ForgotPasswordPage";

import CommunityPage from "../pages/CommunityPage";
import AIChatPage from "../pages/AIChatPage";
import ProfilePage from "../pages/ProfilePage";
import PostPage from "../pages/PostPage";
import PostDetailPage from "../pages/PostDetailPage";
import AdminConsole from "../pages/AdminConsole";
import ExpertConsole from "../pages/ExpertConsole";
import NotificationsPage from '../pages/NotificationsPage';
import ExpertAppointmentPage from "../pages/ExpertAppointmentPage";
import MyAppointmentsPage from "../pages/MyAppointmentsPage";
import FollowBlockPage from "../pages/FollowBlockPage";
import UserDetailPage from "../pages/UserDetailPage";
import ChatPage from "../pages/ChatPage";
import PrivateChatPage from "../pages/PrivateChatPage";

export default function AppRoutes() {
  const { user, isAdmin, isExpert, isLoading } = useAuth();

  // 在认证状态初始化期间显示加载界面
  if (isLoading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>正在加载...</p>
      </div>
    );
  }

  // 如果用户未登录，只显示登录、注册、忘记密码和认证错误页面
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />

        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    );
  }

  // 如果用户已登录，显示完整的应用布局
  return (
    <>
      <Header />
      <div className="app-layout">
        <Sidebar />
        <main className="main-container">
          <Routes>
            <Route path="/" element={<Navigate to="/community" replace />} />
            <Route path="/community" element={<CommunityPage />} />
            <Route path="/post/:postId" element={<PostDetailPage />} />
            <Route path="/ai-chat" element={<AIChatPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/user/:userId" element={<UserDetailPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/follow-block" element={<FollowBlockPage />} />
            <Route path="/post" element={<PostPage />} />
            <Route path="/chat" element={<ChatPage />} />
            <Route path="/private-chat" element={<PrivateChatPage />} />
            {/* 所有已登录用户都可以访问预约页面 */}
            <Route path="/expert-appointment" element={<ExpertAppointmentPage />} />
            <Route path="/my-appointments" element={<MyAppointmentsPage />} />
            {/* 只有管理员才能访问管理控制台 */}
            {isAdmin() && <Route path="/admin" element={<AdminConsole />} />}
            {/* 只有专家才能访问专家控制台 */}
            {isExpert() && <Route path="/expert-console" element={<ExpertConsole />} />}
            <Route path="*" element={<Navigate to="/community" replace />} />
          </Routes>
        </main>
      </div>
    </>
  );
}
