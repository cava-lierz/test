import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Header from "./Header";
import Sidebar from "./Sidebar";
import LoginPage from "../pages/LoginPage";
import RegisterPage from "../pages/RegisterPage";
import CommunityPage from "../pages/CommunityPage";
import AIChatPage from "../pages/AIChatPage";
import ProfilePage from "../pages/ProfilePage";
import PostPage from "../pages/PostPage";
import PostDetailPage from "../pages/PostDetailPage";
import AdminConsole from "../pages/AdminConsole";

export default function AppRoutes() {
  const { user } = useAuth();

  // 如果用户未登录，只显示登录和注册页面
  if (!user) {
    return (
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
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
            <Route path="/post" element={<PostPage />} />
            <Route path="/admin" element={<AdminConsole />} />
            <Route path="*" element={<Navigate to="/community" replace />} />
          </Routes>
        </main>
      </div>
    </>
  );
}
