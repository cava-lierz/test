import React, { useState, useEffect, useCallback } from "react";
import { useAuth } from "../context/AuthContext";
import GenericModal from "../components/GenericModal";
import { adminAPI, statisticsAPI, expertAPI } from "../services/api";
import ExpertManagement from "../components/ExpertManagement";
import { getUserAvatarUrl } from "../utils/avatarUtils";

export default function AdminConsole() {
  const { user, isAdmin, handleLogout, showConfirm, isLoading } = useAuth();
  const [activeTab, setActiveTab] = useState("overview");
  const [selectedReport, setSelectedReport] = useState(null);
  const [selectedPost, setSelectedPost] = useState(null);
  const [selectedUser, setSelectedUser] = useState(null);
  const [reportFilter, setReportFilter] = useState(""); // 举报过滤
  const [adminStats, setAdminStats] = useState([]);
  const [users, setUsers] = useState([]);
  const [reportedPosts, setReportedPosts] = useState([]); // 被举报的帖子
  const [reportedComments, setReportedComments] = useState([]); // 被举报的评论

  // 举报审核相关状态
  const [showResolvedReports, setShowResolvedReports] = useState(false); // 是否显示已处理举报页面
  const [resolvedPosts, setResolvedPosts] = useState([]); // 已处理的举报帖子
  const [resolvedPostsLoading, setResolvedPostsLoading] = useState(false);
  const [resolvedPostsPage, setResolvedPostsPage] = useState(0);
  const [resolvedPostsTotalPages, setResolvedPostsTotalPages] = useState(0);
  const [resolvedPostsTotalElements, setResolvedPostsTotalElements] =
    useState(0);
  const [resolvedPostFilter, setResolvedPostFilter] = useState(""); // 已处理举报过滤
  
  // 评论举报相关状态
  const [showResolvedCommentReports, setShowResolvedCommentReports] = useState(false); // 是否显示已处理评论举报页面
  const [resolvedComments, setResolvedComments] = useState([]); // 已处理的评论举报
  const [resolvedCommentsLoading, setResolvedCommentsLoading] = useState(false);
  const [resolvedCommentsPage, setResolvedCommentsPage] = useState(0);
  const [resolvedCommentsTotalPages, setResolvedCommentsTotalPages] = useState(0);
  const [resolvedCommentsTotalElements, setResolvedCommentsTotalElements] = useState(0);
  const [resolvedCommentFilter, setResolvedCommentFilter] = useState(""); // 已处理评论举报过滤
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalUsers, setTotalUsers] = useState(0);
  const [overviewData, setOverviewData] = useState(null);
  const [weeklyStats, setWeeklyStats] = useState(null);
  const [monthlyStats, setMonthlyStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(false);
  const [experts, setExperts] = useState([]);
  const [expertsLoading, setExpertsLoading] = useState(false);
  const [chatRooms, setChatRooms] = useState([]);
  const [chatRoomsLoading, setChatRoomsLoading] = useState(false);

  // 帖子审核相关状态
  const [pendingPosts, setPendingPosts] = useState([]);
  const [pendingPostsLoading, setPendingPostsLoading] = useState(false);
  const [pendingPostsPage, setPendingPostsPage] = useState(0);
  const [pendingPostsTotalPages, setPendingPostsTotalPages] = useState(0);
  const [pendingPostsTotalElements, setPendingPostsTotalElements] = useState(0);
  const [postFilter, setPostFilter] = useState(""); // 帖子过滤

  // 已拒绝帖子相关状态
  const [rejectedPosts, setRejectedPosts] = useState([]);
  const [rejectedPostsLoading, setRejectedPostsLoading] = useState(false);
  const [rejectedPostsPage, setRejectedPostsPage] = useState(0);
  const [rejectedPostsTotalPages, setRejectedPostsTotalPages] = useState(0);
  const [rejectedPostsTotalElements, setRejectedPostsTotalElements] =
    useState(0);
  const [rejectedPostFilter, setRejectedPostFilter] = useState(""); // 已拒绝帖子过滤
  const [showRejectedPosts, setShowRejectedPosts] = useState(false); // 是否显示已拒绝帖子页面

  // 搜索相关状态
  const [searchKeyword, setSearchKeyword] = useState("");
  const [isSearching, setIsSearching] = useState(false);
  const [searchResults, setSearchResults] = useState([]);
  const [searchTotalPages, setSearchTotalPages] = useState(0);
  const [searchTotalUsers, setSearchTotalUsers] = useState(0);
  const [searchTrigger, setSearchTrigger] = useState(0); // 用于触发搜索的计数器

  // 获取管理员统计数据
  useEffect(() => {
    if (!user || isLoading) return;
    const fetchAdminStats = async () => {
      try {
        setLoading(true);
        const stats = await adminAPI.getAdminStats();

        // 设置顶部统计卡片
        setAdminStats([
          {
            label: "待处理举报",
            value: stats.pendingReports || 0,
            icon: "🚨",
            color: "#ff6b6b",
          },
          {
            label: "待审核帖子",
            value: stats.pendingPosts || 0,
            icon: "📝",
            color: "#ffa726",
          },
          {
            label: "活跃用户",
            value: stats.activeUsers || 0,
            icon: "👥",
            color: "#4caf50",
          },
          {
            label: "本月新增",
            value: stats.newUsersThisMonth || 0,
            icon: "📈",
            color: "#2196f3",
          },
        ]);

        // 设置总览页面数据
        setOverviewData({
          totalUsers: stats.totalUsers || 0,
          totalPosts: stats.totalPosts || 0,
          activeUsers: stats.activeUsers || 0,
          newUsersThisMonth: stats.newUsersThisMonth || 0,
          pendingReports: stats.pendingReports || 0,
          pendingPosts: stats.pendingPosts || 0,
          avgMoodScore: stats.averageMoodScore || 3.8,
          totalComments: stats.totalComments || 0,
          totalLikes: stats.totalLikes || 0,
        });
      } catch (error) {
        console.error("获取管理员统计数据失败:", error);
        // 设置默认值
        setAdminStats([
          { label: "待处理举报", value: 0, icon: "🚨", color: "#ff6b6b" },
          { label: "待审核帖子", value: 0, icon: "📝", color: "#ffa726" },
          { label: "活跃用户", value: 0, icon: "👥", color: "#4caf50" },
          { label: "本月新增", value: 0, icon: "📈", color: "#2196f3" },
        ]);
        setOverviewData({
          totalUsers: 0,
          totalPosts: 0,
          activeUsers: 0,
          newUsersThisMonth: 0,
          pendingReports: 0,
          pendingPosts: 0,
          avgMoodScore: 0.0,
        });
      } finally {
        setLoading(false);
      }
    };

    fetchAdminStats();
  }, [user, isLoading]);

  // 获取用户列表
  useEffect(() => {
    if (!user || isLoading) return;
    const fetchUsers = async () => {
      try {
        setLoading(true);
        let response;

        if (searchKeyword.trim()) {
          // 如果有搜索关键词，使用搜索API
          response = await adminAPI.searchUsers(searchKeyword, currentPage, 10);
          setSearchResults(response.content || []);
          setSearchTotalPages(response.totalPages || 0);
          setSearchTotalUsers(response.totalElements || 0);
          setIsSearching(true);
        } else {
          // 否则获取所有用户
          response = await adminAPI.getAllUsers(currentPage, 10);
          setUsers(response.content || []);
          setTotalPages(response.totalPages || 0);
          setTotalUsers(response.totalElements || 0);
          setIsSearching(false);
        }
      } catch (error) {
        console.error("获取用户列表失败:", error);
        setUsers([]);
        setSearchResults([]);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, [currentPage, searchTrigger, user, isLoading, searchKeyword]);

  // 获取被举报的帖子列表（默认只获取WAITING状态）
  useEffect(() => {
    const fetchReportedPosts = async () => {
      if (!user || isLoading) return;
      try {
        const response = await adminAPI.getReportedPosts(0, 50); // 获取前50个被举报的帖子
        // 过滤出WAITING状态的帖子
        console.log(response.content);
        const waitingPosts = (response.content || []).filter(
          (post) => post.state === "WAITING"
        );
        setReportedPosts(waitingPosts);
      } catch (error) {
        console.error("获取被举报帖子失败:", error);
        setReportedPosts([]);
      }
    };

    // 获取已处理的举报帖子列表
    const fetchResolvedPosts = async () => {
      if (!user || isLoading) return;
      try {
        const response = await adminAPI.getReportedPosts(0, 50);
        // 过滤出VALID和INVALID状态的帖子
        const resolvedPosts = (response.content || []).filter(
          (post) => post.state === "VALID" || post.state === "INVALID"
        );
        setResolvedPosts(resolvedPosts);
      } catch (error) {
        console.error("获取已处理举报帖子失败:", error);
        setResolvedPosts([]);
      }
    };

    // 获取被举报的评论列表（默认只获取WAITING状态）
    const fetchReportedComments = async () => {
      if (!user || isLoading) return;
      try {
        const response = await adminAPI.getReportedComments(0, 50, "waiting"); // 获取等待处理的评论举报
        setReportedComments(response.content || []);
      } catch (error) {
        console.error("获取被举报评论失败:", error);
        setReportedComments([]);
      }
    };

    // 获取已处理的评论举报列表
    const fetchResolvedComments = async () => {
      if (!user || isLoading) return;
      try {
        const validResponse = await adminAPI.getReportedComments(0, 25, "valid"); // 获取举报有效的评论
        const invalidResponse = await adminAPI.getReportedComments(0, 25, "invalid"); // 获取举报无效的评论
        const resolvedComments = [
          ...(validResponse.content || []),
          ...(invalidResponse.content || [])
        ];
        setResolvedComments(resolvedComments);
      } catch (error) {
        console.error("获取已处理评论举报失败:", error);
        setResolvedComments([]);
      }
    };

    if (activeTab === "reports") {
      fetchReportedPosts();
      fetchResolvedPosts();
      fetchReportedComments();
      fetchResolvedComments();
    }
  }, [activeTab, user, isLoading]);

  // 获取周报月报数据
  useEffect(() => {
    const fetchStats = async () => {
      if (activeTab === "weekly-reports" && user && !isLoading) {
        setStatsLoading(true);
        try {
          // 获取当前年份和周数
          const now = new Date();
          const year = now.getFullYear();
          const week = Math.ceil(
            ((now - new Date(now.getFullYear(), 0, 1)) / 86400000 + 1) / 7
          );
          // 获取当前月份
          const month = now.getMonth() + 1;
          // 获取周报和月报数据
          const [weekData, monthData] = await Promise.all([
            statisticsAPI.getWeeklyStats(year, week),
            statisticsAPI.getMonthlyStats(year, month),
          ]);
          // 设置默认值
          const defaultStats = {
            newUsers: 0,
            totalPosts: 0,
            reportCount: 0,
            activeUsers: 0,
          };
          setWeeklyStats(weekData || defaultStats);
          setMonthlyStats(monthData || defaultStats);
        } catch (error) {
          console.error("获取统计数据失败:", error);
          // 如果是认证错误，重定向到登录页面
          if (error.message && error.message.includes("authentication")) {
            handleLogout();
          }
          // 设置默认值
          const defaultStats = {
            newUsers: 0,
            totalPosts: 0,
            reportCount: 0,
            activeUsers: 0,
          };
          setWeeklyStats(defaultStats);
          setMonthlyStats(defaultStats);
        } finally {
          setStatsLoading(false);
        }
      }
    };
    fetchStats();
  }, [activeTab, handleLogout, user, isLoading]);

  // 获取专家列表，当切换到专家标签页时触发
  const fetchExperts = useCallback(async () => {
    if (!user) return;
    try {
      setExpertsLoading(true);
      const data = await expertAPI.getExpertUsers();
      setExperts(data || []);
    } catch (error) {
      console.error("获取专家列表失败:", error);
      setExperts([]);
    } finally {
      setExpertsLoading(false);
    }
  }, [user]);

  // 获取聊天室列表，当切换到聊天室管理标签页时触发
  const fetchChatRooms = useCallback(async () => {
    if (!user) return;
    try {
      setChatRoomsLoading(true);
      const data = await adminAPI.getAllChatRooms();
      setChatRooms(data || []);
    } catch (error) {
      console.error("获取聊天室列表失败:", error);
      setChatRooms([]);
    } finally {
      setChatRoomsLoading(false);
    }
  }, [user]);

  useEffect(() => {
    if (activeTab === "experts") {
      if (!user || isLoading) return;
      fetchExperts();
    }
    if (activeTab === "chat-rooms") {
      if (!user || isLoading) return;
      fetchChatRooms();
    }
    if (activeTab === "posts") {
      if (!user || isLoading) return;
      fetchPendingPosts();
    }
  }, [activeTab, user, isLoading, fetchExperts, fetchChatRooms]);

  // 获取待审核的帖子列表（默认只获取WAITING状态）
  const fetchPendingPosts = async () => {
    if (!user) return;
    try {
      setPendingPostsLoading(true);
      const response = await adminAPI.getPendingPosts(
        pendingPostsPage,
        10,
        "WAITING" // 默认只获取等待审核的帖子
      );
      setPendingPosts(response.content || []);
      setPendingPostsTotalPages(response.totalPages || 0);
      setPendingPostsTotalElements(response.totalElements || 0);
    } catch (error) {
      console.error("获取待审核帖子失败:", error);
      setPendingPosts([]);
    } finally {
      setPendingPostsLoading(false);
    }
  };

  // 获取已拒绝的帖子列表
  const fetchRejectedPosts = async () => {
    if (!user) return;
    try {
      setRejectedPostsLoading(true);
      const response = await adminAPI.getPendingPosts(
        rejectedPostsPage,
        10,
        "INVALID" // 获取已拒绝的帖子
      );
      setRejectedPosts(response.content || []);
      setRejectedPostsTotalPages(response.totalPages || 0);
      setRejectedPostsTotalElements(response.totalElements || 0);
    } catch (error) {
      console.error("获取已拒绝帖子失败:", error);
      setRejectedPosts([]);
    } finally {
      setRejectedPostsLoading(false);
    }
  };

  // 当页码改变时重新获取数据
  useEffect(() => {
    if (activeTab === "posts" && user && !isLoading) {
      if (showRejectedPosts) {
        fetchRejectedPosts();
      } else {
        fetchPendingPosts();
      }
    }
  }, [
    pendingPostsPage,
    rejectedPostsPage,
    activeTab,
    user,
    isLoading,
    showRejectedPosts,
  ]);

  const handleLogoutClick = () => {
    showConfirm("确定要退出登录吗？", () => {
      handleLogout();
    });
  };

  // 处理搜索
  const handleSearch = (keyword) => {
    setSearchKeyword(keyword);
    setCurrentPage(0); // 重置到第一页
    setSearchTrigger((prev) => prev + 1); // 触发搜索
  };

  // 清除搜索
  const handleClearSearch = () => {
    setSearchKeyword("");
    setCurrentPage(0);
    setIsSearching(false);
    setSearchResults([]);
    setSearchTrigger((prev) => prev + 1); // 触发重新加载
  };

  const handleReportAction = async (postId, action) => {
    let confirmMessage = "";
    let actionText = "";

    switch (action) {
      case "delete":
        actionText = "删除";
        confirmMessage = "确定要删除这个被举报的帖子吗？";
        break;
      case "approve":
        actionText = "通过";
        confirmMessage = "确定要通过这个举报的审核吗？";
        break;
      case "changeToInvalid":
        actionText = "驳回";
        confirmMessage = "确定要驳回这个举报吗？";
        break;
      default:
        confirmMessage = "确定要执行这个操作吗？";
    }

    showConfirm(confirmMessage, async () => {
      try {
        if (action === "delete") {
          await adminAPI.deleteReportedPost(postId);
          // 从当前列表中移除已删除的帖子
          if (showResolvedReports) {
            setResolvedPosts((prev) =>
              prev.filter((post) => post.id !== postId)
            );
          } else {
            setReportedPosts((prev) =>
              prev.filter((post) => post.id !== postId)
            );
          }
          showConfirm(`✅ 帖子已删除！`, () => {});
        } else if (action === "approve") {
          await adminAPI.approveReportedPost(postId);
          // 从待处理列表中移除已通过的帖子
          setReportedPosts((prev) => prev.filter((post) => post.id !== postId));
          showConfirm(`✅ 举报已通过！`, () => {});
        } else if (action === "changeToInvalid") {
          await adminAPI.changePostStatus(postId, "INVALID");
          // 从已处理列表中移除已驳回的帖子
          setResolvedPosts((prev) => prev.filter((post) => post.id !== postId));
          showConfirm(`✅ 举报已驳回！`, () => {});
        }

        // 刷新统计数据
        const stats = await adminAPI.getAdminStats();
        setAdminStats([
          {
            label: "待处理举报",
            value: stats.pendingReports || 0,
            icon: "🚨",
            color: "#ff6b6b",
          },
          {
            label: "待审核帖子",
            value: stats.pendingPosts || 0,
            icon: "📝",
            color: "#ffa726",
          },
          {
            label: "活跃用户",
            value: stats.activeUsers || 0,
            icon: "👥",
            color: "#4caf50",
          },
          {
            label: "本月新增",
            value: stats.newUsersThisMonth || 0,
            icon: "📈",
            color: "#2196f3",
          },
        ]);
      } catch (error) {
        console.error("处理举报失败:", error);
      }
    });
  };

  const handleCommentReportAction = async (commentId, action) => {
    let confirmMessage = "";
    let actionText = "";

    switch (action) {
      case "delete":
        actionText = "删除";
        confirmMessage = "确定要删除这个被举报的评论吗？";
        break;
      case "approve":
        actionText = "通过";
        confirmMessage = "确定要通过这个评论举报的审核吗？";
        break;
      case "ignore":
        actionText = "忽略";
        confirmMessage = "确定要忽略这个评论举报吗？";
        break;
      case "restore":
        actionText = "恢复";
        confirmMessage = "确定要恢复这个评论吗？";
        break;
      default:
        confirmMessage = "确定要执行这个操作吗？";
    }

    showConfirm(confirmMessage, async () => {
      try {
        if (action === "delete") {
          await adminAPI.deleteReportedComment(commentId);
          // 从列表中移除已删除的评论
          setReportedComments((prev) =>
            prev.filter((comment) => comment.id !== commentId)
          );

          // 🔥 新增：触发全局评论删除事件，通知其他页面刷新
          const commentDeletedEvent = new CustomEvent("commentDeleted", {
            detail: { commentId, source: "admin" },
          });
          window.dispatchEvent(commentDeletedEvent);

          showConfirm(`✅ 评论删除成功！`, () => {});
        } else if (action === "approve") {
          await adminAPI.approveCommentReport(commentId);
          // 从待处理列表中移除已通过的评论
          setReportedComments((prev) =>
            prev.filter((comment) => comment.id !== commentId)
          );
          showConfirm(`✅ 举报已通过！`, () => {});
        } else if (action === "ignore") {
          await adminAPI.ignoreCommentReports(commentId);
          // 从列表中移除已忽略的评论
          setReportedComments((prev) =>
            prev.filter((comment) => comment.id !== commentId)
          );
          showConfirm(`✅ 举报已忽略！`, () => {});
        } else if (action === "restore") {
          await adminAPI.restoreComment(commentId);
          // 从已处理列表中移除已恢复的评论
          setResolvedComments((prev) =>
            prev.filter((comment) => comment.id !== commentId)
          );
          showConfirm(`✅ 评论已恢复！`, () => {});
        }

        // 刷新统计数据
        const stats = await adminAPI.getAdminStats();
        setAdminStats([
          {
            label: "待处理举报",
            value: stats.pendingReports || 0,
            icon: "🚨",
            color: "#ff6b6b",
          },
          {
            label: "待审核帖子",
            value: stats.pendingPosts || 0,
            icon: "📝",
            color: "#ffa726",
          },
          {
            label: "活跃用户",
            value: stats.activeUsers || 0,
            icon: "👥",
            color: "#4caf50",
          },
          {
            label: "本月新增",
            value: stats.newUsersThisMonth || 0,
            icon: "📈",
            color: "#2196f3",
          },
        ]);
      } catch (error) {
        console.error("处理评论举报失败:", error);
        showConfirm(`❌ 操作失败：${error.message || "未知错误"}`, () => {});
      }
    });
  };

  const handlePostAction = async (postId, action) => {
    let actionText = "";
    let confirmMessage = "";

    switch (action) {
      case "approve":
        actionText = "通过审核";
        confirmMessage = "确定要通过这个帖子的审核吗？";
        break;
      case "reject":
        actionText = "拒绝审核";
        confirmMessage = "确定要拒绝这个帖子的审核吗？";
        break;
      case "restore":
        actionText = "恢复帖子";
        confirmMessage = "确定要恢复这个帖子吗？";
        break;
      case "delete":
        actionText = "删除帖子";
        confirmMessage = "确定要删除这个帖子吗？";
        break;
      default:
        actionText = "操作";
        confirmMessage = "确定要执行这个操作吗？";
    }

    showConfirm(confirmMessage, async () => {
      try {
        if (action === "approve") {
          await adminAPI.approvePost(postId);
          // 从列表中移除已审核的帖子
          setPendingPosts((prev) => prev.filter((post) => post.id !== postId));
          showConfirm(`✅ 帖子审核通过！`, () => {});
        } else if (action === "reject") {
          await adminAPI.deletePost(postId);
          // 从列表中移除已删除的帖子
          setPendingPosts((prev) => prev.filter((post) => post.id !== postId));
          showConfirm(`✅ 帖子已删除！`, () => {});
        } else if (action === "restore") {
          await adminAPI.restorePost(postId);
          // 从已拒绝列表中移除已恢复的帖子
          setRejectedPosts((prev) => prev.filter((post) => post.id !== postId));
          showConfirm(`✅ 帖子已恢复！`, () => {});
        } else if (action === "delete") {
          await adminAPI.deletePost(postId);
          // 从当前列表中移除已删除的帖子
          if (showRejectedPosts) {
            setRejectedPosts((prev) =>
              prev.filter((post) => post.id !== postId)
            );
          } else {
            setPendingPosts((prev) =>
              prev.filter((post) => post.id !== postId)
            );
          }
          showConfirm(`✅ 帖子已删除！`, () => {});
        }

        // 刷新统计数据
        const stats = await adminAPI.getAdminStats();
        setAdminStats([
          {
            label: "待处理举报",
            value: stats.pendingReports || 0,
            icon: "🚨",
            color: "#ff6b6b",
          },
          {
            label: "待审核帖子",
            value: stats.pendingPosts || 0,
            icon: "📝",
            color: "#ffa726",
          },
          {
            label: "活跃用户",
            value: stats.activeUsers || 0,
            icon: "👥",
            color: "#4caf50",
          },
          {
            label: "本月新增",
            value: stats.newUsersThisMonth || 0,
            icon: "📈",
            color: "#2196f3",
          },
        ]);
      } catch (error) {
        console.error("处理帖子审核失败:", error);
        showConfirm(`❌ 操作失败：${error.message || "未知错误"}`, () => {});
      }
    });
  };

  const handleUserAction = async (userId, action) => {
    const actionText =
      action === "suspend" ? "禁用" : action === "activate" ? "启用" : "删除";
    showConfirm(`确定要${actionText}这个用户吗？`, async () => {
      try {
        if (action === "suspend") {
          await adminAPI.suspendUser(userId);
          // 更新用户列表中的状态
          setUsers((prev) =>
            prev.map((user) =>
              user.id === userId ? { ...user, isDisabled: true } : user
            )
          );
        } else if (action === "activate") {
          await adminAPI.activateUser(userId);
          // 更新用户列表中的状态
          setUsers((prev) =>
            prev.map((user) =>
              user.id === userId ? { ...user, isDisabled: false } : user
            )
          );
        } else if (action === "delete") {
          await adminAPI.deleteUser(userId);
          // 从用户列表中移除已删除的用户
          setUsers((prev) => prev.filter((user) => user.id !== userId));
          setTotalUsers((prev) => prev - 1);
        }

        console.log(`${actionText}用户成功`);
      } catch (error) {
        console.error(`${actionText}用户失败:`, error);
        showConfirm(`${actionText}用户失败，请稍后重试`, () => {});
      }
    });
  };

  const handleChatRoomAction = async (roomId, action) => {
    const actionText = action === "delete" ? "删除" : "操作";
    showConfirm(
      `确定要${actionText}这个聊天室吗？此操作将隐藏聊天室但保留数据。`,
      async () => {
        try {
          if (action === "delete") {
            await adminAPI.deleteChatRoom(roomId);
            // 从列表中移除已删除的聊天室
            setChatRooms((prev) => prev.filter((room) => room.id !== roomId));
            showConfirm(
              `✅ 聊天室已软删除！数据已隐藏但未真正删除。`,
              () => {}
            );
          }
        } catch (error) {
          console.error(`${actionText}聊天室失败:`, error);
          showConfirm(`❌ 操作失败：${error.message || "未知错误"}`, () => {});
        }
      }
    );
  };

  const getPriorityColor = (priority) => {
    const colors = { high: "#ff6b6b", medium: "#ffa726", low: "#4caf50" };
    return colors[priority] || "#e0e0e0";
  };

  const getStatusColor = (status) => {
    const colors = {
      pending: "#ffa726",
      waiting: "#ff6b6b",
      valid: "#4caf50",
      invalid: "#9e9e9e",
      resolved: "#4caf50",
      active: "#4caf50",
      suspended: "#ff6b6b",
      online: "#4caf50",
      offline: "#9e9e9e",
    };
    return colors[status] || "#e0e0e0";
  };

  // 权限检查
  if (!isAdmin()) {
    return (
      <div className="profile-container">
        <div className="profile-header">
          <div className="profile-header-bg"></div>
          <div className="profile-header-content">
            <div className="profile-header-main">
              <div className="profile-info">
                <h1 className="profile-name">权限不足</h1>
                <p className="profile-email">您没有访问管理员控制台的权限</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

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
                  src={getUserAvatarUrl(user)}
                  alt={user.nickname || user.username}
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
                <h1 className="profile-name">
                  {user.nickname || user.username} (管理员)
                </h1>
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
              { id: "chat-rooms", label: "聊天室管理", icon: "💬" },
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
                {loading ? (
                  <div className="admin-overview-loading">
                    <p>加载中...</p>
                  </div>
                ) : overviewData ? (
                  <div className="admin-overview-grid">
                    <div className="admin-overview-card">
                      <h4>👥 用户统计</h4>
                      <div className="admin-overview-stats">
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.totalUsers}
                          </span>
                          <span className="admin-overview-label">总用户数</span>
                        </div>
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.activeUsers}
                          </span>
                          <span className="admin-overview-label">活跃用户</span>
                        </div>
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.newUsersThisMonth}
                          </span>
                          <span className="admin-overview-label">本月新增</span>
                        </div>
                      </div>
                    </div>

                    <div className="admin-overview-card">
                      <h4>📝 内容统计</h4>
                      <div className="admin-overview-stats">
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.totalPosts}
                          </span>
                          <span className="admin-overview-label">总帖子数</span>
                        </div>
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.pendingPosts}
                          </span>
                          <span className="admin-overview-label">待审核</span>
                        </div>
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.avgMoodScore}
                          </span>
                          <span className="admin-overview-label">平均心情</span>
                        </div>
                      </div>
                    </div>

                    <div className="admin-overview-card">
                      <h4>🚨 举报处理</h4>
                      <div className="admin-overview-stats">
                        <div className="admin-overview-stat">
                          <span className="admin-overview-number">
                            {overviewData.pendingReports}
                          </span>
                          <span className="admin-overview-label">待处理</span>
                        </div>
                      </div>
                    </div>

                    <div className="admin-overview-card">
                      <h4>📈 趋势分析</h4>
                      <div className="admin-overview-stats">
                        <div className="admin-overview-stat">
                          <span
                            className="admin-overview-number"
                            style={{
                              color:
                                overviewData.newUsersThisMonth > 10
                                  ? "#4caf50"
                                  : "#ffa726",
                            }}
                          >
                            {overviewData.newUsersThisMonth > 10 ? "📈" : "📊"}
                          </span>
                          <span className="admin-overview-label">
                            {overviewData.newUsersThisMonth > 10
                              ? "增长良好"
                              : "增长平稳"}
                          </span>
                        </div>
                        <div className="admin-overview-stat">
                          <span
                            className="admin-overview-number"
                            style={{
                              color:
                                overviewData.pendingReports === 0
                                  ? "#4caf50"
                                  : "#ff6b6b",
                            }}
                          >
                            {overviewData.pendingReports === 0 ? "✅" : "⚠️"}
                          </span>
                          <span className="admin-overview-label">
                            {overviewData.pendingReports === 0
                              ? "无待处理"
                              : "需要关注"}
                          </span>
                        </div>
                        <div className="admin-overview-stat">
                          <span
                            className="admin-overview-number"
                            style={{
                              color:
                                overviewData.avgMoodScore >= 3.5
                                  ? "#4caf50"
                                  : "#ffa726",
                            }}
                          >
                            {overviewData.avgMoodScore >= 3.5 ? "😊" : "😐"}
                          </span>
                          <span className="admin-overview-label">
                            {overviewData.avgMoodScore >= 3.5
                              ? "心情良好"
                              : "需要关注"}
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                ) : (
                  <div className="admin-overview-error">
                    <p>暂无数据</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* 举报处理标签页 */}
          {activeTab === "reports" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">
                {showResolvedReports ? "已处理举报" : "举报处理"}
              </h3>
              <div className="profile-tab-content-inner">
                {/* 页面切换按钮 */}
                <div className="admin-posts-header">
                  <div className="admin-posts-tabs">
                    <button
                      className={`admin-posts-tab ${
                        !showResolvedReports ? "active" : ""
                      }`}
                      onClick={() => setShowResolvedReports(false)}
                    >
                      🚨 待处理举报
                    </button>
                    <button
                      className={`admin-posts-tab ${
                        showResolvedReports ? "active" : ""
                      }`}
                      onClick={() => setShowResolvedReports(true)}
                    >
                      ✅ 已处理举报
                    </button>
                  </div>
                </div>

                {/* 帖子举报部分 */}
                <div className="admin-report-section">
                  <h4>
                    📝{" "}
                    {showResolvedReports ? "已处理的举报帖子" : "被举报的帖子"}
                  </h4>
                  <div className="admin-report-filter">
                    <input
                      type="text"
                      placeholder={
                        showResolvedReports
                          ? "搜索已处理举报帖子内容或作者..."
                          : "搜索被举报的帖子内容或作者..."
                      }
                      value={
                        showResolvedReports ? resolvedPostFilter : reportFilter
                      }
                      onChange={(e) =>
                        showResolvedReports
                          ? setResolvedPostFilter(e.target.value)
                          : setReportFilter(e.target.value)
                      }
                      className="admin-filter-input"
                    />
                    <span className="admin-filter-count">
                      共{" "}
                      {showResolvedReports
                        ? resolvedPosts.length
                        : reportedPosts.length}{" "}
                      个{showResolvedReports ? "已处理" : "被举报"}的帖子
                    </span>
                  </div>
                  {showResolvedReports ? (
                    // 已处理举报帖子列表
                    resolvedPosts.length === 0 ? (
                      <div className="admin-no-data">
                        <p>暂无已处理的举报帖子</p>
                      </div>
                    ) : (
                      resolvedPosts
                        .filter(
                          (post) =>
                            post.content
                              ?.toLowerCase()
                              .includes(resolvedPostFilter.toLowerCase()) ||
                            post.authorName
                              ?.toLowerCase()
                              .includes(resolvedPostFilter.toLowerCase())
                        )
                        .map((post) => (
                          <div key={post.id} className="admin-report-item">
                            <div className="admin-report-header">
                              <div className="admin-report-type">
                                <span
                                  className="admin-report-type-badge"
                                  style={{
                                    background:
                                      post.reportCount >= 3
                                        ? "#ff6b6b"
                                        : post.reportCount >= 2
                                        ? "#ffa726"
                                        : "#4caf50",
                                  }}
                                >
                                  被举报 {post.reportCount} 次
                                </span>
                                <span
                                  className="admin-report-priority"
                                  style={{
                                    color:
                                      post.state === "VALID"
                                        ? "#4caf50"
                                        : "#9e9e9e",
                                  }}
                                >
                                  {post.state === "VALID"
                                    ? "🟢 已通过"
                                    : "⚪ 已驳回"}
                                </span>
                              </div>
                              <span className="admin-report-time">
                                {new Date(post.createdAt).toLocaleString()}
                              </span>
                            </div>
                            <div className="admin-report-content">
                              <p>
                                <strong>作者：</strong>
                                {post.authorName}
                              </p>
                              <p>
                                <strong>标题：</strong>
                                {post.title || "无标题"}
                              </p>
                              <p>
                                <strong>心情：</strong>
                                {post.mood}
                              </p>
                              <p>
                                <strong>内容：</strong>
                                {post.content}
                              </p>
                              <div className="admin-report-reason-row">
                                <strong>举报原因：</strong>
                                {post.reportReasons &&
                                post.reportReasons.length > 0 ? (
                                  <span className="admin-report-reasons">
                                    {post.reportReasons.map((reason, index) => (
                                      <span
                                        key={index}
                                        className="admin-report-reason-tag"
                                      >
                                        {reason}
                                      </span>
                                    ))}
                                  </span>
                                ) : (
                                  "未提供具体原因"
                                )}
                              </div>
                              <p>
                                <strong>处理结果：</strong>
                                <span
                                  className="admin-report-state"
                                  style={{
                                    color:
                                      post.state === "VALID"
                                        ? "#4caf50"
                                        : "#9e9e9e",
                                  }}
                                >
                                  {post.state === "VALID"
                                    ? "🟢 已通过"
                                    : "⚪ 已驳回"}
                                </span>
                              </p>
                              <p>
                                <strong>最近举报时间：</strong>
                                {post.lastReportTime
                                  ? new Date(
                                      post.lastReportTime
                                    ).toLocaleString()
                                  : "未知"}
                              </p>
                            </div>
                            <div className="admin-report-actions">
                              {/* 已处理举报只保留驳回选项 */}
                              <button
                                className="admin-action-btn admin-action-btn-ignore"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleReportAction(
                                    post.id,
                                    "changeToInvalid"
                                  );
                                }}
                              >
                                ❌ 驳回
                              </button>
                            </div>
                          </div>
                        ))
                    )
                  ) : // 待处理举报帖子列表
                  reportedPosts.length === 0 ? (
                    <div className="admin-no-data">
                      <p>暂无被举报的帖子</p>
                    </div>
                  ) : (
                    reportedPosts
                      .filter(
                        (post) =>
                          post.content
                            ?.toLowerCase()
                            .includes(reportFilter.toLowerCase()) ||
                          post.authorName
                            ?.toLowerCase()
                            .includes(reportFilter.toLowerCase())
                      )
                      .map((post) => (
                        <div key={post.id} className="admin-report-item">
                          <div className="admin-report-header">
                            <div className="admin-report-type">
                              <span
                                className="admin-report-type-badge"
                                style={{
                                  background:
                                    post.reportCount >= 3
                                      ? "#ff6b6b"
                                      : post.reportCount >= 2
                                      ? "#ffa726"
                                      : "#4caf50",
                                }}
                              >
                                被举报 {post.reportCount} 次
                              </span>
                              <span
                                className="admin-report-priority"
                                style={{
                                  color:
                                    post.reportCount >= 3
                                      ? "#ff6b6b"
                                      : post.reportCount >= 2
                                      ? "#ffa726"
                                      : "#4caf50",
                                }}
                              >
                                {post.reportCount >= 3
                                  ? "🔴 高优先级"
                                  : post.reportCount >= 2
                                  ? "🟡 中优先级"
                                  : "🟢 低优先级"}
                              </span>
                            </div>
                            <span className="admin-report-time">
                              {new Date(post.createdAt).toLocaleString()}
                            </span>
                          </div>
                          <div className="admin-report-content">
                            <p>
                              <strong>作者：</strong>
                              {post.authorName}
                            </p>
                            <p>
                              <strong>标题：</strong>
                              {post.title || "无标题"}
                            </p>
                            <p>
                              <strong>心情：</strong>
                              {post.mood}
                            </p>
                            <p>
                              <strong>内容：</strong>
                              {post.content}
                            </p>
                            <div className="admin-report-reason-row">
                              <strong>举报原因：</strong>
                              {post.reportReasons &&
                              post.reportReasons.length > 0 ? (
                                <span className="admin-report-reasons">
                                  {post.reportReasons.map((reason, index) => (
                                    <span
                                      key={index}
                                      className="admin-report-reason-tag"
                                    >
                                      {reason}
                                    </span>
                                  ))}
                                </span>
                              ) : (
                                "未提供具体原因"
                              )}
                            </div>
                            <p>
                              <strong>举报状态：</strong>
                              <span
                                className="admin-report-state"
                                style={{
                                  color: "#ff6b6b",
                                }}
                              >
                                🔴 等待处理
                              </span>
                            </p>
                            <p>
                              <strong>最近举报时间：</strong>
                              {post.lastReportTime
                                ? new Date(post.lastReportTime).toLocaleString()
                                : "未知"}
                            </p>
                          </div>
                          <div className="admin-report-actions">
                            {/* WAITING状态 - 审核操作 */}
                            <button
                              className="admin-action-btn admin-action-btn-resolve"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleReportAction(post.id, "approve");
                              }}
                            >
                              ✅ 通过
                            </button>
                            <button
                              className="admin-action-btn admin-action-btn-delete"
                              onClick={(e) => {
                                e.stopPropagation();
                                handleReportAction(post.id, "delete");
                              }}
                            >
                              🗑️ 删除帖子
                            </button>
                          </div>
                        </div>
                      ))
                  )}
                </div>

                {/* 评论举报部分 */}
                <div className="admin-report-section">
                  <h4>
                    💬{" "}
                    {showResolvedCommentReports ? "已处理的评论举报" : "被举报的评论"}
                  </h4>
                  <div className="admin-report-filter">
                    <input
                      type="text"
                      placeholder={
                        showResolvedCommentReports
                          ? "搜索已处理评论举报内容或作者..."
                          : "搜索被举报的评论内容或作者..."
                      }
                      value={
                        showResolvedCommentReports ? resolvedCommentFilter : reportFilter
                      }
                      onChange={(e) =>
                        showResolvedCommentReports
                          ? setResolvedCommentFilter(e.target.value)
                          : setReportFilter(e.target.value)
                      }
                      className="admin-filter-input"
                    />
                    <span className="admin-filter-count">
                      共{" "}
                      {showResolvedCommentReports
                        ? resolvedComments.length
                        : reportedComments.length}{" "}
                      个{showResolvedCommentReports ? "已处理" : "被举报"}的评论
                    </span>
                  </div>
                  
                  {/* 页面切换按钮 */}
                  <div className="admin-comments-header">
                    <div className="admin-comments-tabs">
                      <button
                        className={`admin-comments-tab ${
                          !showResolvedCommentReports ? "active" : ""
                        }`}
                        onClick={() => {
                          setShowResolvedCommentReports(false);
                        }}
                      >
                        🔴 待处理评论
                      </button>
                      <button
                        className={`admin-comments-tab ${
                          showResolvedCommentReports ? "active" : ""
                        }`}
                        onClick={() => {
                          setShowResolvedCommentReports(true);
                        }}
                      >
                        ✅ 已处理评论
                      </button>
                    </div>
                  </div>

                  {showResolvedCommentReports ? (
                    // 已处理评论举报列表
                    resolvedCommentsLoading ? (
                      <div className="admin-comments-loading">
                        <p>加载中...</p>
                      </div>
                    ) : resolvedComments.length === 0 ? (
                      <div className="admin-no-data">
                        <p>暂无已处理的评论举报</p>
                      </div>
                    ) : (
                      resolvedComments
                        .filter(
                          (comment) =>
                            comment.content
                              ?.toLowerCase()
                              .includes(resolvedCommentFilter.toLowerCase()) ||
                            comment.authorName
                              ?.toLowerCase()
                              .includes(resolvedCommentFilter.toLowerCase())
                        )
                        .map((comment) => (
                          <div key={comment.id} className="admin-report-item">
                            <div className="admin-report-header">
                              <div className="admin-report-type">
                                <span
                                  className="admin-report-type-badge"
                                  style={{
                                    background: comment.isDeleted ? "#ff6b6b" : "#4caf50",
                                  }}
                                >
                                  {comment.isDeleted ? "🔴 已删除" : "🟢 已驳回"}
                                </span>
                                <span
                                  className="admin-report-priority"
                                  style={{
                                    color: comment.isDeleted ? "#ff6b6b" : "#4caf50",
                                  }}
                                >
                                  {comment.isDeleted ? "举报有效" : "举报无效"}
                                </span>
                              </div>
                              <span className="admin-report-time">
                                {new Date(comment.createdAt).toLocaleString()}
                              </span>
                            </div>
                            <div className="admin-report-content">
                              <p>
                                <strong>评论者：</strong>
                                {comment.authorName}
                              </p>
                              <p>
                                <strong>所属帖子：</strong>
                                {comment.postTitle || "未知帖子"}
                              </p>
                              <p>
                                <strong>评论内容：</strong>
                                {comment.content}
                              </p>
                              <p>
                                <strong>处理结果：</strong>
                                <span
                                  className="admin-report-state"
                                  style={{
                                    color: comment.isDeleted ? "#ff6b6b" : "#4caf50",
                                  }}
                                >
                                  {comment.isDeleted ? "🔴 已删除" : "🟢 已驳回"}
                                </span>
                              </p>
                            </div>
                            <div className="admin-report-actions">
                              {/* 已处理评论举报的操作按钮 */}
                              {comment.isDeleted ? (
                                // 如果评论已被删除，提供恢复选项
                                <button
                                  className="admin-action-btn admin-action-btn-resolve"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleCommentReportAction(comment.id, "restore");
                                  }}
                                >
                                  🔄 恢复评论
                                </button>
                              ) : (
                                // 如果评论未被删除（已驳回），提供删除选项
                                <button
                                  className="admin-action-btn admin-action-btn-delete"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    handleCommentReportAction(comment.id, "delete");
                                  }}
                                >
                                  🗑️ 删除评论
                                </button>
                              )}
                            </div>
                          </div>
                        ))
                    )
                  ) : (
                    // 待处理评论举报列表
                    reportedComments.length === 0 ? (
                      <div className="admin-no-data">
                        <p>暂无被举报的评论</p>
                      </div>
                    ) : (
                      reportedComments
                        .filter(
                          (comment) =>
                            comment.content
                              ?.toLowerCase()
                              .includes(reportFilter.toLowerCase()) ||
                            comment.authorName
                              ?.toLowerCase()
                              .includes(reportFilter.toLowerCase())
                        )
                        .map((comment) => (
                          <div key={comment.id} className="admin-report-item">
                            <div className="admin-report-header">
                              <div className="admin-report-type">
                                <span
                                  className="admin-report-type-badge"
                                  style={{
                                    background:
                                      comment.reportCount >= 3
                                        ? "#ff6b6b"
                                        : comment.reportCount >= 2
                                        ? "#ffa726"
                                        : "#4caf50",
                                  }}
                                >
                                  被举报 {comment.reportCount} 次
                                </span>
                                <span
                                  className="admin-report-priority"
                                  style={{
                                    color:
                                      comment.reportCount >= 3
                                        ? "#ff6b6b"
                                        : comment.reportCount >= 2
                                        ? "#ffa726"
                                        : "#4caf50",
                                  }}
                                >
                                  {comment.reportCount >= 3
                                    ? "🔴 高优先级"
                                    : comment.reportCount >= 2
                                    ? "🟡 中优先级"
                                    : "🟢 低优先级"}
                                </span>
                              </div>
                              <span className="admin-report-time">
                                {new Date(comment.createdAt).toLocaleString()}
                              </span>
                            </div>
                            <div className="admin-report-content">
                              <p>
                                <strong>评论者：</strong>
                                {comment.authorName}
                              </p>
                              <p>
                                <strong>所属帖子：</strong>
                                {comment.postTitle || "未知帖子"}
                              </p>
                              <p>
                                <strong>评论内容：</strong>
                                {comment.content}
                              </p>
                              <p>
                                <strong>举报状态：</strong>
                                <span
                                  className="admin-report-state"
                                  style={{
                                    color: "#ff6b6b",
                                  }}
                                >
                                  🔴 等待处理
                                </span>
                              </p>
                            </div>
                            <div className="admin-report-actions">
                              <button
                                className="admin-action-btn admin-action-btn-resolve"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleCommentReportAction(comment.id, "approve");
                                }}
                              >
                                ✅ 通过
                              </button>
                              <button
                                className="admin-action-btn admin-action-btn-delete"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  handleCommentReportAction(comment.id, "delete");
                                }}
                              >
                                🗑️ 删除评论
                              </button>
                            </div>
                          </div>
                        ))
                    )
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 发帖审核标签页 */}
          {activeTab === "posts" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">
                {showRejectedPosts ? "已拒绝帖子" : "发帖审核"}
              </h3>
              <div className="profile-tab-content-inner">
                {/* 页面切换按钮 */}
                <div className="admin-posts-header">
                  <div className="admin-posts-tabs">
                    <button
                      className={`admin-posts-tab ${
                        !showRejectedPosts ? "active" : ""
                      }`}
                      onClick={() => {
                        setShowRejectedPosts(false);
                        setPendingPostsPage(0);
                      }}
                    >
                      📝 待审核帖子
                    </button>
                    <button
                      className={`admin-posts-tab ${
                        showRejectedPosts ? "active" : ""
                      }`}
                      onClick={() => {
                        setShowRejectedPosts(true);
                        setRejectedPostsPage(0);
                      }}
                    >
                      ❌ 已禁用帖子
                    </button>
                  </div>
                </div>

                {/* 过滤和搜索 */}
                <div className="admin-posts-filter">
                  <div className="admin-filter-row">
                    <input
                      type="text"
                      placeholder={
                        showRejectedPosts
                          ? "搜索已禁用帖子内容或作者..."
                          : "搜索帖子内容或作者..."
                      }
                      value={
                        showRejectedPosts ? rejectedPostFilter : postFilter
                      }
                      onChange={(e) =>
                        showRejectedPosts
                          ? setRejectedPostFilter(e.target.value)
                          : setPostFilter(e.target.value)
                      }
                      className="admin-filter-input"
                    />
                    <span className="admin-filter-count">
                      共{" "}
                      {showRejectedPosts
                        ? rejectedPostsTotalElements
                        : pendingPostsTotalElements}{" "}
                      个{showRejectedPosts ? "已禁用" : "待审核"}帖子
                    </span>
                  </div>
                </div>

                {/* 帖子列表 */}
                {showRejectedPosts ? (
                  // 已拒绝帖子列表
                  rejectedPostsLoading ? (
                    <div className="admin-posts-loading">
                      <p>加载中...</p>
                    </div>
                  ) : rejectedPosts.length === 0 ? (
                    <div className="admin-no-data">
                      <p>暂无已拒绝的帖子</p>
                    </div>
                  ) : (
                    rejectedPosts
                      .filter(
                        (post) =>
                          post.content
                            ?.toLowerCase()
                            .includes(rejectedPostFilter.toLowerCase()) ||
                          post.authorName
                            ?.toLowerCase()
                            .includes(rejectedPostFilter.toLowerCase()) ||
                          post.title
                            ?.toLowerCase()
                            .includes(rejectedPostFilter.toLowerCase())
                      )
                      .map((post) => (
                        <div key={post.id} className="admin-post-item">
                          <div className="admin-post-header">
                            <div className="admin-post-type">
                              <span
                                className="admin-post-type-badge"
                                style={{
                                  background: "#ff6b6b",
                                }}
                              >
                                🔴 已禁用
                              </span>
                              <span
                                className="admin-post-priority"
                                style={{
                                  color: "#ff6b6b",
                                }}
                              >
                                已被禁用
                              </span>
                            </div>
                            <span className="admin-post-time">
                              {new Date(post.createdAt).toLocaleString()}
                            </span>
                          </div>
                          <div className="admin-post-content">
                            <p>
                              <strong>作者：</strong>
                              {post.authorName}
                            </p>
                            <p>
                              <strong>标题：</strong>
                              {post.title || "无标题"}
                            </p>
                            <p>
                              <strong>心情：</strong>
                              {post.mood}
                            </p>
                            <p>
                              <strong>内容：</strong>
                              {post.content}
                            </p>
                            {post.images && post.images.length > 0 && (
                              <p>
                                <strong>图片：</strong>
                                <span className="admin-post-images">
                                  {post.images.length} 张图片
                                </span>
                              </p>
                            )}
                            <p>
                              <strong>点赞数：</strong>
                              {post.likeCount || 0}
                            </p>
                            <p>
                              <strong>评论数：</strong>
                              {post.commentCount || 0}
                            </p>
                          </div>
                          <div className="admin-post-actions">
                            {/* 已拒绝帖子只保留恢复选项 */}
                            <button
                              className="admin-action-btn admin-action-btn-resolve"
                              onClick={(e) => {
                                e.stopPropagation();
                                handlePostAction(post.id, "restore");
                              }}
                            >
                              ✅ 恢复帖子
                            </button>
                          </div>
                        </div>
                      ))
                  )
                ) : // 待审核帖子列表
                pendingPostsLoading ? (
                  <div className="admin-posts-loading">
                    <p>加载中...</p>
                  </div>
                ) : pendingPosts.length === 0 ? (
                  <div className="admin-no-data">
                    <p>暂无待审核的帖子</p>
                  </div>
                ) : (
                  pendingPosts
                    .filter(
                      (post) =>
                        post.content
                          ?.toLowerCase()
                          .includes(postFilter.toLowerCase()) ||
                        post.authorName
                          ?.toLowerCase()
                          .includes(postFilter.toLowerCase()) ||
                        post.title
                          ?.toLowerCase()
                          .includes(postFilter.toLowerCase())
                    )
                    .map((post) => (
                      <div key={post.id} className="admin-post-item">
                        <div className="admin-post-header">
                          <div className="admin-post-type">
                            <span
                              className="admin-post-type-badge"
                              style={{
                                background: "#ffa726",
                              }}
                            >
                              🟡 等待审核
                            </span>
                            <span
                              className="admin-post-priority"
                              style={{
                                color: "#ffa726",
                              }}
                            >
                              需要人工审核
                            </span>
                          </div>
                          <span className="admin-post-time">
                            {new Date(post.createdAt).toLocaleString()}
                          </span>
                        </div>
                        <div className="admin-post-content">
                          <p>
                            <strong>作者：</strong>
                            {post.authorName}
                          </p>
                          <p>
                            <strong>标题：</strong>
                            {post.title || "无标题"}
                          </p>
                          <p>
                            <strong>心情：</strong>
                            {post.mood}
                          </p>
                          <p>
                            <strong>内容：</strong>
                            {post.content}
                          </p>
                          {post.images && post.images.length > 0 && (
                            <p>
                              <strong>图片：</strong>
                              <span className="admin-post-images">
                                {post.images.length} 张图片
                              </span>
                            </p>
                          )}
                          <p>
                            <strong>点赞数：</strong>
                            {post.likeCount || 0}
                          </p>
                          <p>
                            <strong>评论数：</strong>
                            {post.commentCount || 0}
                          </p>
                        </div>
                        <div className="admin-post-actions">
                          {/* WAITING状态 - 审核操作 */}
                          <button
                            className="admin-action-btn admin-action-btn-resolve"
                            onClick={(e) => {
                              e.stopPropagation();
                              handlePostAction(post.id, "approve");
                            }}
                          >
                            ✅ 通过审核
                          </button>
                          <button
                            className="admin-action-btn admin-action-btn-ignore"
                            onClick={(e) => {
                              e.stopPropagation();
                              handlePostAction(post.id, "reject");
                            }}
                          >
                            ❌ 删除帖子
                          </button>
                        </div>
                      </div>
                    ))
                )}

                {/* 分页控件 */}
                {!pendingPostsLoading &&
                  !rejectedPostsLoading &&
                  (showRejectedPosts ? rejectedPosts : pendingPosts).length >
                    0 && (
                    <div className="admin-posts-pagination">
                      <button
                        className="admin-pagination-btn"
                        disabled={
                          showRejectedPosts
                            ? rejectedPostsPage === 0
                            : pendingPostsPage === 0
                        }
                        onClick={() => {
                          if (showRejectedPosts) {
                            setRejectedPostsPage(rejectedPostsPage - 1);
                          } else {
                            setPendingPostsPage(pendingPostsPage - 1);
                          }
                        }}
                      >
                        上一页
                      </button>

                      {/* 快速跳转按钮 */}
                      <div className="admin-pagination-quick">
                        <button
                          className="admin-quick-btn"
                          disabled={
                            showRejectedPosts
                              ? rejectedPostsPage === 0
                              : pendingPostsPage === 0
                          }
                          onClick={() => {
                            if (showRejectedPosts) {
                              setRejectedPostsPage(0);
                            } else {
                              setPendingPostsPage(0);
                            }
                          }}
                          title="跳转到第一页"
                        >
                          ⏮️
                        </button>
                        <button
                          className="admin-quick-btn"
                          disabled={
                            (showRejectedPosts
                              ? rejectedPostsPage
                              : pendingPostsPage) >=
                            (showRejectedPosts
                              ? rejectedPostsTotalPages
                              : pendingPostsTotalPages) -
                              1
                          }
                          onClick={() => {
                            if (showRejectedPosts) {
                              setRejectedPostsPage(rejectedPostsTotalPages - 1);
                            } else {
                              setPendingPostsPage(pendingPostsTotalPages - 1);
                            }
                          }}
                          title="跳转到最后一页"
                        >
                          ⏭️
                        </button>
                      </div>

                      {/* 页码显示 */}
                      <div className="admin-pagination-pages">
                        {(() => {
                          const totalPagesNum = showRejectedPosts
                            ? rejectedPostsTotalPages
                            : pendingPostsTotalPages;
                          const currentPageNum =
                            (showRejectedPosts
                              ? rejectedPostsPage
                              : pendingPostsPage) + 1;
                          const pages = [];

                          if (totalPagesNum <= 0) return pages;

                          // 显示第一页
                          pages.push(
                            <button
                              key="first"
                              className={`admin-page-btn ${
                                (showRejectedPosts
                                  ? rejectedPostsPage
                                  : pendingPostsPage) === 0
                                  ? "active"
                                  : ""
                              }`}
                              onClick={() => {
                                if (showRejectedPosts) {
                                  setRejectedPostsPage(0);
                                } else {
                                  setPendingPostsPage(0);
                                }
                              }}
                            >
                              1
                            </button>
                          );

                          // 显示省略号和中间页码
                          if (totalPagesNum > 7) {
                            if (currentPageNum > 4) {
                              pages.push(
                                <span
                                  key="ellipsis1"
                                  className="admin-page-ellipsis"
                                >
                                  ...
                                </span>
                              );
                            }

                            const start = Math.max(2, currentPageNum - 1);
                            const end = Math.min(
                              totalPagesNum - 1,
                              currentPageNum + 1
                            );

                            for (let i = start; i <= end; i++) {
                              if (i > 1 && i < totalPagesNum) {
                                pages.push(
                                  <button
                                    key={i}
                                    className={`admin-page-btn ${
                                      (showRejectedPosts
                                        ? rejectedPostsPage
                                        : pendingPostsPage) ===
                                      i - 1
                                        ? "active"
                                        : ""
                                    }`}
                                    onClick={() => {
                                      if (showRejectedPosts) {
                                        setRejectedPostsPage(i - 1);
                                      } else {
                                        setPendingPostsPage(i - 1);
                                      }
                                    }}
                                  >
                                    {i}
                                  </button>
                                );
                              }
                            }

                            if (currentPageNum < totalPagesNum - 3) {
                              pages.push(
                                <span
                                  key="ellipsis2"
                                  className="admin-page-ellipsis"
                                >
                                  ...
                                </span>
                              );
                            }
                          } else {
                            // 如果总页数较少，显示所有页码
                            for (let i = 2; i < totalPagesNum; i++) {
                              pages.push(
                                <button
                                  key={i}
                                  className={`admin-page-btn ${
                                    (showRejectedPosts
                                      ? rejectedPostsPage
                                      : pendingPostsPage) ===
                                    i - 1
                                      ? "active"
                                      : ""
                                  }`}
                                  onClick={() => {
                                    if (showRejectedPosts) {
                                      setRejectedPostsPage(i - 1);
                                    } else {
                                      setPendingPostsPage(i - 1);
                                    }
                                  }}
                                >
                                  {i}
                                </button>
                              );
                            }
                          }

                          // 显示最后一页
                          if (totalPagesNum > 1) {
                            pages.push(
                              <button
                                key="last"
                                className={`admin-page-btn ${
                                  (showRejectedPosts
                                    ? rejectedPostsPage
                                    : pendingPostsPage) ===
                                  totalPagesNum - 1
                                    ? "active"
                                    : ""
                                }`}
                                onClick={() => {
                                  if (showRejectedPosts) {
                                    setRejectedPostsPage(totalPagesNum - 1);
                                  } else {
                                    setPendingPostsPage(totalPagesNum - 1);
                                  }
                                }}
                              >
                                {totalPagesNum}
                              </button>
                            );
                          }

                          return pages;
                        })()}
                      </div>

                      {/* 页码输入和跳转 */}
                      <div className="admin-pagination-jump">
                        <span>跳转到第</span>
                        <input
                          type="number"
                          min="1"
                          max={
                            showRejectedPosts
                              ? rejectedPostsTotalPages
                              : pendingPostsTotalPages
                          }
                          className="admin-pagination-input"
                          onKeyPress={(e) => {
                            if (e.key === "Enter") {
                              const page = parseInt(e.target.value) - 1;
                              const maxPage = showRejectedPosts
                                ? rejectedPostsTotalPages
                                : pendingPostsTotalPages;
                              if (page >= 0 && page < maxPage) {
                                if (showRejectedPosts) {
                                  setRejectedPostsPage(page);
                                } else {
                                  setPendingPostsPage(page);
                                }
                              }
                            }
                          }}
                          placeholder={`1-${
                            showRejectedPosts
                              ? rejectedPostsTotalPages
                              : pendingPostsTotalPages
                          }`}
                        />
                        <span>页</span>
                        <button
                          className="admin-pagination-jump-btn"
                          onClick={(e) => {
                            const input =
                              e.target.previousElementSibling
                                .previousElementSibling;
                            const page = parseInt(input.value) - 1;
                            const maxPage = showRejectedPosts
                              ? rejectedPostsTotalPages
                              : pendingPostsTotalPages;
                            if (page >= 0 && page < maxPage) {
                              if (showRejectedPosts) {
                                setRejectedPostsPage(page);
                              } else {
                                setPendingPostsPage(page);
                              }
                            }
                          }}
                        >
                          跳转
                        </button>
                      </div>

                      <span className="admin-pagination-info">
                        第{" "}
                        {(showRejectedPosts
                          ? rejectedPostsPage
                          : pendingPostsPage) + 1}{" "}
                        页，共{" "}
                        {showRejectedPosts
                          ? rejectedPostsTotalPages
                          : pendingPostsTotalPages}{" "}
                        页 （共{" "}
                        {showRejectedPosts
                          ? rejectedPostsTotalElements
                          : pendingPostsTotalElements}{" "}
                        个{showRejectedPosts ? "已拒绝" : "待审核"}帖子）
                      </span>
                      <button
                        className="admin-pagination-btn"
                        disabled={
                          (showRejectedPosts
                            ? rejectedPostsPage
                            : pendingPostsPage) >=
                          (showRejectedPosts
                            ? rejectedPostsTotalPages
                            : pendingPostsTotalPages) -
                            1
                        }
                        onClick={() => {
                          if (showRejectedPosts) {
                            setRejectedPostsPage(rejectedPostsPage + 1);
                          } else {
                            setPendingPostsPage(pendingPostsPage + 1);
                          }
                        }}
                      >
                        下一页
                      </button>
                    </div>
                  )}
              </div>
            </div>
          )}

          {/* 用户管理标签页 */}
          {activeTab === "users" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">用户权限管理</h3>
              <div className="profile-tab-content-inner">
                {/* 搜索栏 */}
                <div className="admin-users-search">
                  <div className="admin-search-container">
                    <input
                      type="text"
                      placeholder="搜索用户名、昵称或邮箱..."
                      value={searchKeyword}
                      onChange={(e) => setSearchKeyword(e.target.value)}
                      onKeyPress={(e) => {
                        if (e.key === "Enter") {
                          handleSearch(e.target.value);
                        }
                      }}
                      className="admin-search-input"
                    />
                    <button
                      onClick={() => handleSearch(searchKeyword)}
                      className="admin-search-btn"
                    >
                      🔍 搜索
                    </button>
                    {searchKeyword && (
                      <button
                        onClick={handleClearSearch}
                        className="admin-clear-search-btn"
                      >
                        ✕ 清除
                      </button>
                    )}
                  </div>
                  {isSearching && (
                    <div className="admin-search-info">
                      搜索关键词: "{searchKeyword}" - 找到 {searchTotalUsers}{" "}
                      个用户
                    </div>
                  )}
                </div>

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
                  {loading ? (
                    <div className="admin-users-loading">
                      <p>加载中...</p>
                    </div>
                  ) : (isSearching ? searchResults : users).length === 0 ? (
                    <div className="admin-users-empty">
                      <p>
                        {isSearching
                          ? `未找到包含"${searchKeyword}"的用户`
                          : "暂无用户数据"}
                      </p>
                    </div>
                  ) : (
                    (isSearching ? searchResults : users).map((user) => (
                      <div key={user.id} className="admin-users-row">
                        <div className="admin-users-cell">
                          <span className="admin-users-name">
                            {user.nickname || user.username}
                          </span>
                        </div>
                        <div className="admin-users-cell">
                          <span className="admin-users-email">
                            {user.email || "未设置"}
                          </span>
                        </div>
                        <div className="admin-users-cell">
                          <span
                            className="admin-users-status"
                            style={{
                              color: getStatusColor(
                                user.isDisabled ? "suspended" : "active"
                              ),
                            }}
                          >
                            {user.isDisabled ? "🔴 已禁用" : "🟢 正常"}
                          </span>
                        </div>
                        <div className="admin-users-cell">
                          <span className="admin-users-posts">
                            {user.postsCount || 0}
                          </span>
                        </div>
                        <div className="admin-users-cell">
                          <span
                            className="admin-users-reports"
                            style={{
                              color:
                                user.reportedPostsCount > 0
                                  ? "#ff6b6b"
                                  : "#666",
                              fontWeight:
                                user.reportedPostsCount > 0 ? "bold" : "normal",
                            }}
                          >
                            {user.reportedPostsCount || 0}
                          </span>
                        </div>
                        <div className="admin-users-cell">
                          <span className="admin-users-last-active">
                            {user.lastLoginAt
                              ? new Date(user.lastLoginAt).toLocaleString()
                              : "从未登录"}
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
                            {user.isDisabled ? (
                              <button
                                className="admin-action-btn admin-action-btn-activate"
                                onClick={() =>
                                  handleUserAction(user.id, "activate")
                                }
                              >
                                启用
                              </button>
                            ) : (
                              <button
                                className="admin-action-btn admin-action-btn-suspend"
                                onClick={() =>
                                  handleUserAction(user.id, "suspend")
                                }
                              >
                                禁用
                              </button>
                            )}
                          </div>
                        </div>
                      </div>
                    ))
                  )}

                  {/* 分页控件 */}
                  {!loading &&
                    (isSearching ? searchResults : users).length > 0 && (
                      <div className="admin-users-pagination">
                        <button
                          className="admin-pagination-btn"
                          disabled={currentPage === 0}
                          onClick={() => setCurrentPage(currentPage - 1)}
                        >
                          上一页
                        </button>

                        {/* 快速跳转按钮 */}
                        <div className="admin-pagination-quick">
                          <button
                            className="admin-quick-btn"
                            disabled={currentPage === 0}
                            onClick={() => setCurrentPage(0)}
                            title="跳转到第一页"
                          >
                            ⏮️
                          </button>
                          <button
                            className="admin-quick-btn"
                            disabled={
                              currentPage >=
                              (isSearching ? searchTotalPages : totalPages) - 1
                            }
                            onClick={() =>
                              setCurrentPage(
                                (isSearching ? searchTotalPages : totalPages) -
                                  1
                              )
                            }
                            title="跳转到最后一页"
                          >
                            ⏭️
                          </button>
                        </div>

                        {/* 页码显示 */}
                        <div className="admin-pagination-pages">
                          {(() => {
                            const totalPagesNum = isSearching
                              ? searchTotalPages
                              : totalPages;
                            const currentPageNum = currentPage + 1;
                            const pages = [];

                            if (totalPagesNum <= 0) return pages;

                            // 显示第一页
                            pages.push(
                              <button
                                key="first"
                                className={`admin-page-btn ${
                                  currentPage === 0 ? "active" : ""
                                }`}
                                onClick={() => setCurrentPage(0)}
                              >
                                1
                              </button>
                            );

                            // 显示省略号和中间页码
                            if (totalPagesNum > 7) {
                              if (currentPageNum > 4) {
                                pages.push(
                                  <span
                                    key="ellipsis1"
                                    className="admin-page-ellipsis"
                                  >
                                    ...
                                  </span>
                                );
                              }

                              const start = Math.max(2, currentPageNum - 1);
                              const end = Math.min(
                                totalPagesNum - 1,
                                currentPageNum + 1
                              );

                              for (let i = start; i <= end; i++) {
                                if (i > 1 && i < totalPagesNum) {
                                  pages.push(
                                    <button
                                      key={i}
                                      className={`admin-page-btn ${
                                        currentPage === i - 1 ? "active" : ""
                                      }`}
                                      onClick={() => setCurrentPage(i - 1)}
                                    >
                                      {i}
                                    </button>
                                  );
                                }
                              }

                              if (currentPageNum < totalPagesNum - 3) {
                                pages.push(
                                  <span
                                    key="ellipsis2"
                                    className="admin-page-ellipsis"
                                  >
                                    ...
                                  </span>
                                );
                              }
                            } else {
                              // 如果总页数较少，显示所有页码
                              for (let i = 2; i < totalPagesNum; i++) {
                                pages.push(
                                  <button
                                    key={i}
                                    className={`admin-page-btn ${
                                      currentPage === i - 1 ? "active" : ""
                                    }`}
                                    onClick={() => setCurrentPage(i - 1)}
                                  >
                                    {i}
                                  </button>
                                );
                              }
                            }

                            // 显示最后一页
                            if (totalPagesNum > 1) {
                              pages.push(
                                <button
                                  key="last"
                                  className={`admin-page-btn ${
                                    currentPage === totalPagesNum - 1
                                      ? "active"
                                      : ""
                                  }`}
                                  onClick={() =>
                                    setCurrentPage(totalPagesNum - 1)
                                  }
                                >
                                  {totalPagesNum}
                                </button>
                              );
                            }

                            return pages;
                          })()}
                        </div>

                        {/* 页码输入和跳转 */}
                        <div className="admin-pagination-jump">
                          <span>跳转到第</span>
                          <input
                            type="number"
                            min="1"
                            max={isSearching ? searchTotalPages : totalPages}
                            className="admin-pagination-input"
                            onKeyPress={(e) => {
                              if (e.key === "Enter") {
                                const page = parseInt(e.target.value) - 1;
                                if (
                                  page >= 0 &&
                                  page <
                                    (isSearching
                                      ? searchTotalPages
                                      : totalPages)
                                ) {
                                  setCurrentPage(page);
                                }
                              }
                            }}
                            placeholder={`1-${
                              isSearching ? searchTotalPages : totalPages
                            }`}
                          />
                          <span>页</span>
                          <button
                            className="admin-pagination-jump-btn"
                            onClick={(e) => {
                              const input =
                                e.target.previousElementSibling
                                  .previousElementSibling;
                              const page = parseInt(input.value) - 1;
                              if (
                                page >= 0 &&
                                page <
                                  (isSearching ? searchTotalPages : totalPages)
                              ) {
                                setCurrentPage(page);
                              }
                            }}
                          >
                            跳转
                          </button>
                        </div>

                        <span className="admin-pagination-info">
                          第 {currentPage + 1} 页，共{" "}
                          {isSearching ? searchTotalPages : totalPages} 页 （共{" "}
                          {isSearching ? searchTotalUsers : totalUsers} 个用户）
                        </span>
                        <button
                          className="admin-pagination-btn"
                          disabled={
                            currentPage >=
                            (isSearching ? searchTotalPages : totalPages) - 1
                          }
                          onClick={() => setCurrentPage(currentPage + 1)}
                        >
                          下一页
                        </button>
                      </div>
                    )}
                </div>
              </div>
            </div>
          )}

          {/* 月报周报标签页 */}
          {activeTab === "weekly-reports" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">月报/周报</h3>
              <div className="profile-tab-content-inner">
                {statsLoading ? (
                  <div className="admin-stats-loading">
                    <p>加载中...</p>
                  </div>
                ) : (
                  <>
                    <div className="admin-report-section">
                      <h4>📊 本周数据报告</h4>
                      <div className="admin-report-chart">
                        {(() => {
                          // 计算最大值
                          const maxValue = Math.max(
                            weeklyStats?.newUsers || 0,
                            weeklyStats?.totalPosts || 0,
                            weeklyStats?.reportCount || 0,
                            weeklyStats?.activeUsers || 0
                          );

                          // 计算高度比例（最大高度为160px）
                          const getHeight = (value) => {
                            if (maxValue === 0) return 0;
                            return Math.max((value / maxValue) * 120, 2); // 最小高度2px
                          };

                          return (
                            <>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      weeklyStats?.newUsers || 0
                                    )}px`,
                                    background: "#4caf50",
                                  }}
                                ></div>
                                <span>新增用户</span>
                                <div className="admin-report-value">
                                  {weeklyStats?.newUsers || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      weeklyStats?.totalPosts || 0
                                    )}px`,
                                    background: "#2196f3",
                                  }}
                                ></div>
                                <span>总帖子</span>
                                <div className="admin-report-value">
                                  {weeklyStats?.totalPosts || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      weeklyStats?.reportCount || 0
                                    )}px`,
                                    background: "#ffa726",
                                  }}
                                ></div>
                                <span>举报数</span>
                                <div className="admin-report-value">
                                  {weeklyStats?.reportCount || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      weeklyStats?.activeUsers || 0
                                    )}px`,
                                    background: "#9c27b0",
                                  }}
                                ></div>
                                <span>活跃用户</span>
                                <div className="admin-report-value">
                                  {weeklyStats?.activeUsers || 0}
                                </div>
                              </div>
                            </>
                          );
                        })()}
                      </div>
                    </div>

                    <div className="admin-report-section">
                      <h4>📈 本月数据报告</h4>
                      <div className="admin-report-chart">
                        {(() => {
                          // 计算最大值
                          const maxValue = Math.max(
                            monthlyStats?.newUsers || 0,
                            monthlyStats?.totalPosts || 0,
                            monthlyStats?.reportCount || 0,
                            monthlyStats?.activeUsers || 0
                          );

                          // 计算高度比例（最大高度为160px）
                          const getHeight = (value) => {
                            if (maxValue === 0) return 0;
                            return Math.max((value / maxValue) * 120, 2); // 最小高度2px
                          };

                          return (
                            <>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      monthlyStats?.newUsers || 0
                                    )}px`,
                                    background: "#4caf50",
                                  }}
                                ></div>
                                <span>新增用户</span>
                                <div className="admin-report-value">
                                  {monthlyStats?.newUsers || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      monthlyStats?.totalPosts || 0
                                    )}px`,
                                    background: "#2196f3",
                                  }}
                                ></div>
                                <span>总帖子</span>
                                <div className="admin-report-value">
                                  {monthlyStats?.totalPosts || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      monthlyStats?.reportCount || 0
                                    )}px`,
                                    background: "#ffa726",
                                  }}
                                ></div>
                                <span>举报数</span>
                                <div className="admin-report-value">
                                  {monthlyStats?.reportCount || 0}
                                </div>
                              </div>
                              <div className="admin-report-chart-item">
                                <div
                                  className="admin-report-chart-bar"
                                  style={{
                                    height: `${getHeight(
                                      monthlyStats?.activeUsers || 0
                                    )}px`,
                                    background: "#9c27b0",
                                  }}
                                ></div>
                                <span>活跃用户</span>
                                <div className="admin-report-value">
                                  {monthlyStats?.activeUsers || 0}
                                </div>
                              </div>
                            </>
                          );
                        })()}
                      </div>
                    </div>

                    <div className="admin-report-summary">
                      <h4>📝 数据分析</h4>
                      <ul>
                        <li>
                          本周新增用户{weeklyStats?.newUsers || 0}人
                          {monthlyStats?.newUsers > 0 &&
                            `，本月累计${monthlyStats?.newUsers}人`}
                        </li>
                        <li>
                          本周发布帖子{weeklyStats?.totalPosts || 0}篇
                          {monthlyStats?.totalPosts > 0 &&
                            `，本月累计${monthlyStats?.totalPosts}篇`}
                        </li>
                        <li>
                          本周举报{weeklyStats?.reportCount || 0}次
                          {monthlyStats?.reportCount > 0 &&
                            `，本月累计${monthlyStats?.reportCount}次`}
                        </li>
                        <li>当前活跃用户{weeklyStats?.activeUsers || 0}人</li>
                      </ul>
                    </div>
                  </>
                )}
              </div>
            </div>
          )}

          {/* 心理专家管理标签页 */}
          {activeTab === "experts" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">心理专家管理</h3>
              <div className="profile-tab-content-inner">
                {expertsLoading ? (
                  <div className="admin-loading">
                    <p>加载专家列表中...</p>
                  </div>
                ) : (
                  <ExpertManagement
                    experts={experts}
                    onExpertsChange={fetchExperts}
                  />
                )}
              </div>
            </div>
          )}

          {/* 聊天室管理标签页 */}
          {activeTab === "chat-rooms" && (
            <div className="profile-tab-content">
              <h3 className="profile-tab-title">聊天室管理</h3>
              <div className="profile-tab-content-inner">
                {chatRoomsLoading ? (
                  <div className="admin-loading">
                    <p>加载聊天室列表中...</p>
                  </div>
                ) : chatRooms.length === 0 ? (
                  <div className="admin-no-data">
                    <p>暂无聊天室数据</p>
                  </div>
                ) : (
                  <div className="admin-chat-rooms-table">
                    <div className="admin-chat-rooms-header">
                      <div className="admin-chat-rooms-header-cell">
                        聊天室名称
                      </div>
                      <div className="admin-chat-rooms-header-cell">描述</div>
                      <div className="admin-chat-rooms-header-cell">类型</div>
                      <div className="admin-chat-rooms-header-cell">
                        创建时间
                      </div>
                      <div className="admin-chat-rooms-header-cell">操作</div>
                    </div>
                    {chatRooms.map((room) => (
                      <div key={room.id} className="admin-chat-rooms-row">
                        <div className="admin-chat-rooms-cell">
                          <span className="admin-chat-rooms-name">
                            {room.name || `聊天室${room.id}`}
                          </span>
                        </div>
                        <div className="admin-chat-rooms-cell">
                          <span className="admin-chat-rooms-description">
                            {room.description || "无描述"}
                          </span>
                        </div>
                        <div className="admin-chat-rooms-cell">
                          <span className="admin-chat-rooms-type">
                            {room.type === "REALNAME"
                              ? "实名聊天室"
                              : "匿名聊天室"}
                          </span>
                        </div>
                        <div className="admin-chat-rooms-cell">
                          <span className="admin-chat-rooms-created">
                            {room.createdAt
                              ? new Date(room.createdAt).toLocaleString()
                              : "未知"}
                          </span>
                        </div>
                        <div className="admin-chat-rooms-cell">
                          <div className="admin-chat-rooms-actions">
                            <button
                              onClick={() =>
                                handleChatRoomAction(room.id, "delete")
                              }
                              className="admin-action-btn admin-action-btn-danger"
                              title="软删除聊天室（隐藏但保留数据）"
                            >
                              🗑️ 软删除
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
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
                通过
              </button>
              <button
                className="admin-action-btn admin-action-btn-reject"
                onClick={() => handlePostAction(selectedPost.id, "reject")}
              >
                删除帖子
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
                style={{
                  color: getStatusColor(
                    selectedUser.isDisabled ? "suspended" : "active"
                  ),
                }}
              >
                {selectedUser.isDisabled ? "🔴 已禁用" : "🟢 正常"}
              </span>
            </div>
            <div className="admin-modal-body">
              <div className="admin-modal-user-info">
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">用户名：</span>
                  <span className="admin-modal-value">
                    {selectedUser.nickname || selectedUser.username}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">邮箱：</span>
                  <span className="admin-modal-value">
                    {selectedUser.email || "未设置"}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">加入时间：</span>
                  <span className="admin-modal-value">
                    {selectedUser.createdAt
                      ? new Date(selectedUser.createdAt).toLocaleDateString()
                      : "未知"}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">发帖数：</span>
                  <span className="admin-modal-value">
                    {selectedUser.postsCount || 0}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">获得点赞：</span>
                  <span className="admin-modal-value">
                    {selectedUser.totalLikes || 0}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">评论数：</span>
                  <span className="admin-modal-value">
                    {selectedUser.commentsCount || 0}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">被举报次数：</span>
                  <span
                    className="admin-modal-value"
                    style={{
                      color:
                        selectedUser.reportedPostsCount > 0
                          ? "#ff6b6b"
                          : "#666",
                      fontWeight:
                        selectedUser.reportedPostsCount > 0 ? "bold" : "normal",
                    }}
                  >
                    {selectedUser.reportedPostsCount || 0}
                  </span>
                </div>
                <div className="admin-modal-user-row">
                  <span className="admin-modal-label">最后登录：</span>
                  <span className="admin-modal-value">
                    {selectedUser.lastLoginAt
                      ? new Date(selectedUser.lastLoginAt).toLocaleString()
                      : "从未登录"}
                  </span>
                </div>
              </div>
            </div>
            <div className="admin-modal-actions">
              {!selectedUser.isDisabled ? (
                <button
                  className="admin-action-btn admin-action-btn-suspend"
                  onClick={() => {
                    handleUserAction(selectedUser.id, "suspend");
                    setSelectedUser(null);
                  }}
                >
                  禁用用户
                </button>
              ) : (
                <button
                  className="admin-action-btn admin-action-btn-activate"
                  onClick={() => {
                    handleUserAction(selectedUser.id, "activate");
                    setSelectedUser(null);
                  }}
                >
                  启用用户
                </button>
              )}
              <button
                className="admin-action-btn admin-action-btn-delete"
                onClick={() => {
                  handleUserAction(selectedUser.id, "delete");
                  setSelectedUser(null);
                }}
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
