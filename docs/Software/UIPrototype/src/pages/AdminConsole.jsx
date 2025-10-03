import React, { useState, useEffect } from "react";
import { useAuth } from "../context/AuthContext";
import GenericModal from "../components/GenericModal";
import { Link } from "react-router-dom";

export default function AdminConsole() {
  const { user, handleLogout, showConfirm } = useAuth();
  const [activeTab, setActiveTab] = useState("overview");
  const [selectedReport, setSelectedReport] = useState(null);
  const [selectedPost, setSelectedPost] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [reportFilter, setReportFilter] = useState(""); // 举报过滤

  // 模拟数据
  const adminStats = [
    { label: "待处理举报", value: 12, icon: "🚨", color: "#ff6b6b" },
    { label: "待审核帖子", value: 8, icon: "📝", color: "#ffa726" },
    { label: "活跃用户", value: 156, icon: "👥", color: "#4caf50" },
    { label: "本月新增", value: 23, icon: "📈", color: "#2196f3" },
  ];

  const reports = [
    {
      id: 1,
      type: "不当内容",
      reporter: "用户A",
      target: "用户B",
      content: "发布的内容包含不当言论",
      time: "2小时前",
      status: "pending",
      priority: "high",
    },
    {
      id: 2,
      type: "垃圾信息",
      reporter: "用户C",
      target: "用户D",
      content: "重复发布广告信息",
      time: "4小时前",
      status: "pending",
      priority: "medium",
    },
    {
      id: 3,
      type: "骚扰行为",
      reporter: "用户E",
      target: "用户F",
      content: "恶意骚扰其他用户",
      time: "1天前",
      status: "resolved",
      priority: "high",
    },
    {
      id: 4,
      type: "不当内容",
      reporter: "用户G",
      target: "用户B",
      content: "发布的内容包含不当言论",
      time: "3小时前",
      status: "pending",
      priority: "low",
    },
  ];

  const pendingPosts = [
    {
      id: 1,
      author: "用户G",
      content: "今天心情不错，想和大家分享一些正能量！",
      time: "1小时前",
      status: "pending",
    },
    {
      id: 2,
      author: "用户H",
      content: "学习压力很大，需要一些鼓励...",
      time: "3小时前",
      status: "pending",
    },
  ];

  const users = [
    {
      id: 1,
      name: "用户A",
      email: "userA@example.com",
      role: "user",
      status: "active",
      joinDate: "2024-01-15",
      posts: 25,
      reports: 0,
      lastActive: "2小时前",
    },
    {
      id: 2,
      name: "用户B",
      email: "userB@example.com",
      role: "user",
      status: "suspended",
      joinDate: "2024-01-10",
      posts: 15,
      reports: 3,
      lastActive: "1天前",
    },
    {
      id: 3,
      name: "用户C",
      email: "userC@example.com",
      role: "user",
      status: "active",
      joinDate: "2024-01-20",
      posts: 8,
      reports: 1,
      lastActive: "30分钟前",
    },
    {
      id: 4,
      name: "用户D",
      email: "userD@example.com",
      role: "user",
      status: "active",
      joinDate: "2024-01-12",
      posts: 32,
      reports: 0,
      lastActive: "5小时前",
    },
    {
      id: 5,
      name: "用户E",
      email: "userE@example.com",
      role: "user",
      status: "active",
      joinDate: "2024-01-08",
      posts: 12,
      reports: 2,
      lastActive: "1小时前",
    },
  ];

  const weeklyReport = {
    newUsers: 45,
    totalPosts: 234,
    totalReports: 18,
    resolvedReports: 15,
    activeUsers: 156,
    avgMoodScore: 3.8,
  };

  const experts = [
    {
      id: 1,
      name: "张医生",
      specialty: "抑郁症治疗",
      contact: "zhang@psychology.com",
      status: "online",
    },
    {
      id: 2,
      name: "李咨询师",
      specialty: "焦虑症咨询",
      contact: "li@psychology.com",
      status: "offline",
    },
  ];

  // 过滤举报数据
  const filteredReports = reports.filter(
    (report) =>
      report.target.toLowerCase().includes(reportFilter.toLowerCase()) ||
      report.reporter.toLowerCase().includes(reportFilter.toLowerCase())
  );

  const handleLogoutClick = () => {
    showConfirm("确定要退出登录吗？", () => {
      handleLogout();
    });
  };

  const handleReportAction = (reportId, action) => {
    showConfirm(
      `确定要${action === "resolve" ? "处理" : "忽略"}这个举报吗？`,
      () => {
        // 这里添加处理逻辑
        console.log(`${action} report ${reportId}`);
      }
    );
  };

  const handlePostAction = (postId, action) => {
    showConfirm(
      `确定要${action === "approve" ? "通过" : "拒绝"}这个帖子吗？`,
      () => {
        // 这里添加处理逻辑
        console.log(`${action} post ${postId}`);
      }
    );
  };

  const handleUserAction = (userId, action) => {
    const actionText =
      action === "suspend" ? "禁用" : action === "activate" ? "启用" : "删除";
    showConfirm(`确定要${actionText}这个用户吗？`, () => {
      // 这里添加处理逻辑
      console.log(`${action} user ${userId}`);
    });
  };

  const getPriorityColor = (priority) => {
    const colors = { high: "#ff6b6b", medium: "#ffa726", low: "#4caf50" };
    return colors[priority] || "#e0e0e0";
  };

  const getStatusColor = (status) => {
    const colors = {
      pending: "#ffa726",
      resolved: "#4caf50",
      active: "#4caf50",
      suspended: "#ff6b6b",
      online: "#4caf50",
      offline: "#9e9e9e",
    };
    return colors[status] || "#e0e0e0";
  };

  return (
    <>
      <div className="profile-container">
        {/* 管理员信息卡片 */}
        <div className="profile-header">
          {/* 背景装饰 */}
          <div className="profile-header-bg"></div>

          <div className="profile-header-content">
            <div className="profile-header-main">
              <div className="profile-avatar-section">
                <img
                  src={user.avatar}
                  alt={user.name}
                  className="profile-avatar"
                />
                <div
                  className="profile-avatar-badge"
                  style={{ background: "#ff6b6b" }}
                >
                  👑
                </div>
              </div>

              <div className="profile-info">
                <h1 className="profile-name">{user.name} (管理员)</h1>
                <p className="profile-email">{user.email}</p>
                <p className="profile-join-date">管理员权限：完全控制</p>
              </div>

              <button onClick={handleLogoutClick} className="logout-button">
                退出登录
              </button>
            </div>

            {/* 统计数据 */}
            <div className="profile-stats">
              {adminStats.map((stat, index) => (
                <div key={index} className="stat-item">
                  <div className="stat-number" style={{ color: stat.color }}>
                    {stat.icon} {stat.value}
                  </div>
                  <div className="stat-label">{stat.label}</div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 标签页导航 */}
        <div className="profile-tabs">
          <div className="profile-tabs-nav">
            {[
              { id: "overview", label: "总览", icon: "📊" },
              { id: "reports", label: "举报处理", icon: "🚨" },
              { id: "posts", label: "发帖审核", icon: "📝" },
              { id: "users", label: "用户管理", icon: "👥" },
              { id: "weekly-reports", label: "月报周报", icon: "📈" },
              { id: "experts", label: "心理专家", icon: "👨‍⚕️" },
            ].map((tab) => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`profile-tab ${
                  activeTab === tab.id ? "active" : ""
                }`}
              >
                <span>{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </div>

          {/* 总览标签页 */}
          {activeTab === "overview" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">系统总览</h3>
              <div className="profile-tab-content-inner">
                <div className="admin-overview-grid">
                  <div className="admin-overview-card">
                    <h4>📊 本周数据</h4>
                    <div className="admin-overview-stats">
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.newUsers}
                        </span>
                        <span className="admin-overview-label">新增用户</span>
                      </div>
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.totalPosts}
                        </span>
                        <span className="admin-overview-label">总帖子数</span>
                      </div>
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.activeUsers}
                        </span>
                        <span className="admin-overview-label">活跃用户</span>
                      </div>
                    </div>
                  </div>

                  <div className="admin-overview-card">
                    <h4>🚨 举报处理</h4>
                    <div className="admin-overview-stats">
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.totalReports}
                        </span>
                        <span className="admin-overview-label">总举报数</span>
                      </div>
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.resolvedReports}
                        </span>
                        <span className="admin-overview-label">已处理</span>
                      </div>
                      <div className="admin-overview-stat">
                        <span className="admin-overview-number">
                          {weeklyReport.avgMoodScore}
                        </span>
                        <span className="admin-overview-label">平均心情</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* 举报处理标签页 */}
          {activeTab === "reports" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">举报处理</h3>
              <div className="profile-tab-content-inner">
                <div className="admin-report-filter">
                  <input
                    type="text"
                    placeholder="搜索被举报人或举报人..."
                    value={reportFilter}
                    onChange={(e) => setReportFilter(e.target.value)}
                    className="admin-filter-input"
                  />
                  <span className="admin-filter-count">
                    共 {filteredReports.length} 条举报
                  </span>
                </div>
                {filteredReports.map((report) => (
                  <div
                    key={report.id}
                    className="admin-report-item"
                    onClick={() => setSelectedReport(report)}
                  >
                    <div className="admin-report-header">
                      <div className="admin-report-type">
                        <span
                          className="admin-report-type-badge"
                          style={{
                            background: getPriorityColor(report.priority),
                          }}
                        >
                          {report.type}
                        </span>
                        <span
                          className="admin-report-priority"
                          style={{ color: getPriorityColor(report.priority) }}
                        >
                          {report.priority === "high"
                            ? "🔴 高优先级"
                            : report.priority === "medium"
                            ? "🟡 中优先级"
                            : "🟢 低优先级"}
                        </span>
                      </div>
                      <span className="admin-report-time">{report.time}</span>
                    </div>
                    <div className="admin-report-content">
                      <p>
                        <strong>举报人：</strong>
                        {report.reporter}
                      </p>
                      <p>
                        <strong>被举报：</strong>
                        {report.target}
                      </p>
                      <p>
                        <strong>内容：</strong>
                        {report.content}
                      </p>
                    </div>
                    <div className="admin-report-actions">
                      <button
                        className="admin-action-btn admin-action-btn-resolve"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleReportAction(report.id, "resolve");
                        }}
                      >
                        处理
                      </button>
                      <button
                        className="admin-action-btn admin-action-btn-ignore"
                        onClick={(e) => {
                          e.stopPropagation();
                          handleReportAction(report.id, "ignore");
                        }}
                      >
                        忽略
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 发帖审核标签页 */}
          {activeTab === "posts" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">发帖审核</h3>
              <div className="profile-tab-content-inner">
                {pendingPosts.map((post) => (
                  <div
                    key={post.id}
                    className="admin-post-item"
                    onClick={() => setSelectedPost(post)}
                  >
                    <div className="admin-post-header">
                      <span className="admin-post-author">{post.author}</span>
                      <span className="admin-post-time">{post.time}</span>
                    </div>
                    <div className="admin-post-content">
                      <p>{post.content}</p>
                    </div>
                    <div className="admin-post-actions">
                      <button
                        className="admin-action-btn admin-action-btn-approve"
                        onClick={(e) => {
                          e.stopPropagation();
                          handlePostAction(post.id, "approve");
                        }}
                      >
                        通过
                      </button>
                      <button
                        className="admin-action-btn admin-action-btn-reject"
                        onClick={(e) => {
                          e.stopPropagation();
                          handlePostAction(post.id, "reject");
                        }}
                      >
                        拒绝
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* 用户管理标签页 */}
          {activeTab === "users" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">用户权限管理</h3>
              <div className="profile-tab-content-inner">
                <div className="admin-users-table">
                  <div className="admin-users-header">
                    <div className="admin-users-header-cell">用户名</div>
                    <div className="admin-users-header-cell">邮箱</div>
                    <div className="admin-users-header-cell">状态</div>
                    <div className="admin-users-header-cell">发帖数</div>
                    <div className="admin-users-header-cell">被举报</div>
                    <div className="admin-users-header-cell">最后活跃</div>
                    <div className="admin-users-header-cell">操作</div>
                  </div>
                  {users.map((user) => (
                    <div key={user.id} className="admin-users-row">
                      <div className="admin-users-cell">
                        <span className="admin-users-name">{user.name}</span>
                      </div>
                      <div className="admin-users-cell">
                        <span className="admin-users-email">{user.email}</span>
                      </div>
                      <div className="admin-users-cell">
                        <span
                          className="admin-users-status"
                          style={{ color: getStatusColor(user.status) }}
                        >
                          {user.status === "active" ? "🟢 正常" : "🔴 已禁用"}
                        </span>
                      </div>
                      <div className="admin-users-cell">
                        <span className="admin-users-posts">{user.posts}</span>
                      </div>
                      <div className="admin-users-cell">
                        <span className="admin-users-reports">
                          {user.reports}
                        </span>
                      </div>
                      <div className="admin-users-cell">
                        <span className="admin-users-last-active">
                          {user.lastActive}
                        </span>
                      </div>
                      <div className="admin-users-cell">
                        <div className="admin-users-actions">
                          <button
                            className="admin-action-btn admin-action-btn-details"
                            onClick={() => setSelectedUser(user)}
                          >
                            详情
                          </button>
                          {user.status === "active" ? (
                            <button
                              className="admin-action-btn admin-action-btn-suspend"
                              onClick={() =>
                                handleUserAction(user.id, "suspend")
                              }
                            >
                              禁用
                            </button>
                          ) : (
                            <button
                              className="admin-action-btn admin-action-btn-activate"
                              onClick={() =>
                                handleUserAction(user.id, "activate")
                              }
                            >
                              启用
                            </button>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* 月报周报标签页 */}
          {activeTab === "weekly-reports" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">月报/周报</h3>
              <div className="profile-tab-content-inner">
                <div className="admin-report-section">
                  <h4>📊 本周数据报告</h4>
                  <div className="admin-report-chart">
                    <div className="admin-report-chart-item">
                      <div
                        className="admin-report-chart-bar"
                        style={{ height: "60px", background: "#4caf50" }}
                      ></div>
                      <span>新增用户</span>
                    </div>
                    <div className="admin-report-chart-item">
                      <div
                        className="admin-report-chart-bar"
                        style={{ height: "80px", background: "#2196f3" }}
                      ></div>
                      <span>总帖子</span>
                    </div>
                    <div className="admin-report-chart-item">
                      <div
                        className="admin-report-chart-bar"
                        style={{ height: "40px", background: "#ffa726" }}
                      ></div>
                      <span>举报数</span>
                    </div>
                    <div className="admin-report-chart-item">
                      <div
                        className="admin-report-chart-bar"
                        style={{ height: "70px", background: "#9c27b0" }}
                      ></div>
                      <span>活跃用户</span>
                    </div>
                  </div>
                </div>

                <div className="admin-report-summary">
                  <h4>📝 总结</h4>
                  <ul>
                    <li>本周新增用户45人，较上周增长12%</li>
                    <li>用户活跃度保持稳定，平均心情评分3.8</li>
                    <li>举报处理效率提升，平均处理时间缩短至2小时</li>
                    <li>社区氛围良好，不当内容比例控制在1%以下</li>
                  </ul>
                </div>
              </div>
            </div>
          )}

          {/* 心理专家标签页 */}
          {activeTab === "experts" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">联系心理专家</h3>
              <div className="profile-tab-content-inner">
                {experts.map((expert) => (
                  <div key={expert.id} className="admin-expert-item">
                    <div className="admin-expert-header">
                      <div className="admin-expert-info">
                        <span className="admin-expert-name">{expert.name}</span>
                        <span className="admin-expert-specialty">
                          {expert.specialty}
                        </span>
                      </div>
                      <span
                        className="admin-expert-status"
                        style={{ color: getStatusColor(expert.status) }}
                      >
                        {expert.status === "online" ? "🟢 在线" : "🔴 离线"}
                      </span>
                    </div>
                    <div className="admin-expert-contact">
                      <p>
                        <strong>联系方式：</strong>
                        {expert.contact}
                      </p>
                    </div>
                    <div className="admin-expert-actions">
                      <button className="admin-action-btn admin-action-btn-contact">
                        联系专家
                      </button>
                      <button className="admin-action-btn admin-action-btn-schedule">
                        预约咨询
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 举报详情模态框 */}
      <GenericModal
        isOpen={!!selectedReport}
        onClose={() => setSelectedReport(null)}
      >
        {selectedReport && (
          <div className="admin-modal-content">
            <div className="admin-modal-header">
              <h3>举报详情</h3>
              <span
                className="admin-modal-type"
                style={{
                  background: getPriorityColor(selectedReport.priority),
                }}
              >
                {selectedReport.type}
              </span>
            </div>
            <div className="admin-modal-body">
              <p>
                <strong>举报人：</strong>
                {selectedReport.reporter}
              </p>
              <p>
                <strong>被举报：</strong>
                {selectedReport.target}
              </p>
              <p>
                <strong>举报内容：</strong>
                {selectedReport.content}
              </p>
              <p>
                <strong>举报时间：</strong>
                {selectedReport.time}
              </p>
              <p>
                <strong>优先级：</strong>
                {selectedReport.priority}
              </p>
            </div>
            <div className="admin-modal-actions">
              <button
                className="admin-action-btn admin-action-btn-resolve"
                onClick={() => handleReportAction(selectedReport.id, "resolve")}
              >
                处理举报
              </button>
              <button
                className="admin-action-btn admin-action-btn-ignore"
                onClick={() => handleReportAction(selectedReport.id, "ignore")}
              >
                忽略举报
              </button>
            </div>
          </div>
        )}
      </GenericModal>

      {/* 帖子详情模态框 */}
      <GenericModal
        isOpen={!!selectedPost}
        onClose={() => setSelectedPost(null)}
      >
        {selectedPost && (
          <div className="admin-modal-content">
            <div className="admin-modal-header">
              <h3>帖子审核</h3>
              <span className="admin-modal-author">{selectedPost.author}</span>
            </div>
            <div className="admin-modal-body">
              <p>
                <strong>作者：</strong>
                {selectedPost.author}
              </p>
              <p>
                <strong>发布时间：</strong>
                {selectedPost.time}
              </p>
              <p>
                <strong>内容：</strong>
              </p>
              <div className="admin-modal-post-content">
                {selectedPost.content}
              </div>
            </div>
            <div className="admin-modal-actions">
              <button
                className="admin-action-btn admin-action-btn-approve"
                onClick={() => handlePostAction(selectedPost.id, "approve")}
              >
                通过审核
              </button>
              <button
                className="admin-action-btn admin-action-btn-reject"
                onClick={() => handlePostAction(selectedPost.id, "reject")}
              >
                拒绝发布
              </button>
            </div>
          </div>
        )}
      </GenericModal>

      {/* 用户详情模态框 */}
      <GenericModal
        isOpen={!!selectedUser}
        onClose={() => setSelectedUser(null)}
      >
        {selectedUser && (
          <div className="admin-modal-content">
            <div className="admin-modal-header">
              <h3>用户详情</h3>
              <span
                className="admin-modal-status"
                style={{ color: getStatusColor(selectedUser.status) }}
              >
                {selectedUser.status === "active" ? "🟢 正常" : "🔴 已禁用"}
              </span>
            </div>
            <div className="admin-modal-body">
              <div className="admin-modal-user-info">
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">用户名：</span>
                  <span className="admin-modal-value">{selectedUser.name}</span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">邮箱：</span>
                  <span className="admin-modal-value">
                    {selectedUser.email}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">加入时间：</span>
                  <span className="admin-modal-value">
                    {selectedUser.joinDate}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">发帖数：</span>
                  <span className="admin-modal-value">
                    {selectedUser.posts}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">被举报数：</span>
                  <span className="admin-modal-value">
                    {selectedUser.reports}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">最后活跃：</span>
                  <span className="admin-modal-value">
                    {selectedUser.lastActive}
                  </span>
                </div>
              </div>
            </div>
            <div className="admin-modal-actions">
              {selectedUser.status === "active" ? (
                <button
                  className="admin-action-btn admin-action-btn-suspend"
                  onClick={() => handleUserAction(selectedUser.id, "suspend")}
                >
                  禁用用户
                </button>
              ) : (
                <button
                  className="admin-action-btn admin-action-btn-activate"
                  onClick={() => handleUserAction(selectedUser.id, "activate")}
                >
                  启用用户
                </button>
              )}
              <button
                className="admin-action-btn admin-action-btn-delete"
                onClick={() => handleUserAction(selectedUser.id, "delete")}
              >
                删除用户
              </button>
            </div>
          </div>
        )}
      </GenericModal>
    </>
  );
}
