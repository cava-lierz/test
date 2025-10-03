import React, { useState, useEffect, useCallback } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useBlock } from "../context/BlockContext";
import CheckinModal from "../components/CheckinModal";
import EnhancedPostCard from "../components/EnhancedPostCard";
import { postService, moods } from "../services/postService";
import { getUserAvatarUrl } from "../utils/avatarUtils";
import { postAPI, moodAPI } from "../services/api";
import UserProfileModal from "../components/UserProfileModal";

export default function CommunityPage() {
  const navigate = useNavigate();
  const { user, showSuccess } = useAuth();
  const { blockedUserIds } = useBlock();
  const location = useLocation();
  const urlParams = new URLSearchParams(location.search);
  const tagFilter = urlParams.get("tag");
  const tagsParam = urlParams.get("tags");
  const [showCheckinModal, setShowCheckinModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("全部");

  const [activeTags, setActiveTags] = useState(
    tagsParam ? tagsParam.split(",") : []
  );
  const [posts, setPosts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [allTags, setAllTags] = useState([]);
  const [showUserProfile, setShowUserProfile] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);
  const [isSearchMode, setIsSearchMode] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState("");

  // 分页相关状态
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [totalElements, setTotalElements] = useState(0);

  // 检查是否需要显示打卡弹窗
  useEffect(() => {
    const checkTodayCheckin = async () => {
      try {
        const todayCheckin = await moodAPI.getTodayMood();

        // 如果rating为null，说明今天还没有打卡
        if (todayCheckin.rating === null) {
          // 延迟1秒显示，让页面先加载完成
          const timer = setTimeout(() => {
            setShowCheckinModal(true);
          }, 1000);

          return () => clearTimeout(timer);
        }
      } catch (error) {
        console.error("检查今日打卡状态失败:", error);
        // 如果检查失败，也显示打卡弹窗
        const timer = setTimeout(() => {
          setShowCheckinModal(true);
        }, 1000);

        return () => clearTimeout(timer);
      }
    };

    checkTodayCheckin();
  }, [user.nickname, user.username]);

  // 获取帖子数据
  const fetchPosts = useCallback(async (filter = "全部", tags = null, page = 0, append = false) => {
    if (page === 0) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }
    setError(null);
    
    try {
      let res;
      if (tags && tags.length > 0) {
        // 如果有标签筛选，使用标签筛选API
        res = await postService.getCommunityPosts(page, 20, null, tags.join(","));
      } else if (filter !== "全部") {
        // 如果有筛选条件，使用筛选API
        res = await postService.getPostsByFilter(filter, page, 20);
      } else {
        // 默认获取全部帖子
        res = await postService.getCommunityPosts(page, 20);
      }

      // 适配后端数据结构到PostCard所需字段
      const data = res.content || res.data || res.posts || res || [];
      const mapped = (Array.isArray(data) ? data : []).map((item) => {
        // mood类型转emoji
        let moodEmoji = "";
        if (item.mood) {
          const found = moods.find((m) => m.value === item.mood);
          moodEmoji = found ? found.emoji : "";
        }
        return {
          id: item.id,
          title: item.title || "",
          username: item.authorNickname || item.authorName || "匿名",
          avatar: getUserAvatarUrl(
            { avatar: item.authorAvatar },
            "https://i.pravatar.cc/150?u=anon"
          ),
          content: item.content,
          likes: item.likesCount || 0,
          comments: item.commentsCount || 0,
          time: item.createdAt
            ? item.createdAt.replace("T", " ").slice(0, 16)
            : "",
          mood: moodEmoji,
          tags: item.tags && item.tags.length > 0 ? item.tags : [],
          liked: item.liked || false,
          // 添加EnhancedPostCard需要的字段
          authorId: item.authorId,
          authorRole: item.authorRole,
          isAnnouncement: item.isAnnouncement || false,
        };
      });

      if (append) {
        setPosts(prev => [...prev, ...mapped]);
      } else {
        setPosts(mapped);
      }

      // 更新分页状态
      setCurrentPage(page);
      setTotalElements(res.totalElements || 0);
      setHasMore(!res.last && mapped.length > 0);
      
    } catch (err) {
      setError("获取动态失败");
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  // 加载更多帖子
  const loadMorePosts = useCallback(() => {
    if (!loadingMore && hasMore) {
      const nextPage = currentPage + 1;
      fetchPosts(activeFilter, activeTags, nextPage, true);
    }
  }, [loadingMore, hasMore, currentPage, activeFilter, activeTags, fetchPosts]);

  // 搜索处理函数
  const handleSearchSubmit = () => {
    if (searchQuery.trim()) {
      // 点击搜索时，关闭所有筛选条件
      setActiveFilter("全部");
      setActiveTags([]);
      // 设置搜索模式和保存搜索关键词
      setIsSearchMode(true);
      setSearchKeyword(searchQuery);
      // 执行后端搜索
      performSearch(searchQuery);
    } else {
      setIsSearchMode(false);
      setSearchKeyword("");
      setCurrentPage(0);
      setHasMore(true);
      fetchPosts(activeFilter, activeTags, 0, false);
    }
  };

  // 执行搜索
  const performSearch = async (keyword) => {
    setLoading(true);
    setError(null);
    try {
      const res = await postAPI.searchPosts(keyword, 0, 20);

      // 适配后端数据结构到PostCard所需字段
      const data = res.content || res.data || res.posts || res || [];
      const mapped = (Array.isArray(data) ? data : []).map((item) => {
        // mood类型转emoji
        let moodEmoji = "";
        if (item.mood) {
          const found = moods.find((m) => m.value === item.mood);
          moodEmoji = found ? found.emoji : "";
        }
        return {
          id: item.id,
          title: item.title || "",
          username: item.authorNickname || item.authorName || "匿名",
          avatar: getUserAvatarUrl(
            { avatar: item.authorAvatar },
            "https://i.pravatar.cc/150?u=anon"
          ),
          content: item.content,
          likes: item.likesCount || 0,
          comments: item.commentsCount || 0,
          time: item.createdAt
            ? item.createdAt.replace("T", " ").slice(0, 16)
            : "",
          mood: moodEmoji,
          tags: item.tags && item.tags.length > 0 ? item.tags : [],
          liked: item.liked || false,
          // 添加EnhancedPostCard需要的字段
          authorId: item.authorId,
          authorRole: item.authorRole,
          isAnnouncement: item.isAnnouncement || false,
        };
      });
      setPosts(mapped);
      // 保持搜索模式
      setIsSearchMode(true);
      setTotalElements(res.totalElements || mapped.length);
      setHasMore(!res.last && mapped.length > 0);
    } catch (err) {
      setError("搜索失败");
      setIsSearchMode(false);
      setSearchKeyword("");
      setSearchQuery("");
    } finally {
      setLoading(false);
    }
  };

  // 初始加载和筛选条件变化时重新获取数据
  useEffect(() => {
    // 在搜索模式下，不响应筛选条件变化
    if (!isSearchMode) {
      setCurrentPage(0);
      setHasMore(true);
      fetchPosts(activeFilter, activeTags, 0, false);
    }
  }, [activeFilter, activeTags, isSearchMode, fetchPosts]);

  useEffect(() => {
    setActiveTags(tagsParam ? tagsParam.split(",") : []);
    // 当URL参数变化时，退出搜索模式
    if (isSearchMode) {
      setIsSearchMode(false);
      setSearchKeyword("");
      setSearchQuery("");
    }
  }, [tagFilter, location.search, tagsParam, isSearchMode]);

  useEffect(() => {
    postAPI
      .getTags()
      .then(setAllTags)
      .catch(() => setAllTags([]));
  }, []);

  const handleCheckin = (moodData) => {
    // 显示成功提示
    showSuccess(`打卡成功！今天的心情是 ${moodData.rating} 星 🌟`);
  };

  const handleCloseCheckinModal = () => {
    setShowCheckinModal(false);
  };

  // 搜索输入处理函数
  const handleSearchInput = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleFilterChange = (filter) => {
    setActiveFilter(filter);
    // 当用户点击筛选按钮时，退出搜索模式
    if (isSearchMode) {
      setIsSearchMode(false);
      setSearchKeyword("");
      setSearchQuery("");
    }
  };

  const handleClearFilters = () => {
    setActiveFilter("全部");
    setActiveTags([]);
    setSearchQuery("");
    setIsSearchMode(false);
    setSearchKeyword("");
    setCurrentPage(0);
    setHasMore(true);
    // 重新获取所有帖子
    fetchPosts("全部", [], 0, false);
    navigate("/community");
  };

  // 只进行搜索过滤，筛选逻辑已经移到后端，公告帖子已在后端置顶
  const filteredPosts = posts.filter((post) => {
    // 过滤被拉黑用户的帖子
    if (blockedUserIds.includes(post.authorId)) {
      return false;
    }

    // 在搜索模式下，不进行前端的响应式搜索过滤
    if (isSearchMode) {
      return true;
    }

    const matchesSearch =
      searchQuery === "" ||
      post.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (post.username &&
        post.username.toLowerCase().includes(searchQuery.toLowerCase()));
    return matchesSearch;
  });

  // 需要定义getTagNameById辅助函数，假设有全局tag列表
  const getTagNameById = (id) => {
    const found = allTags.find((tag) => String(tag.id) === String(id));
    return found ? found.tag : "";
  };

  const getTagColorById = (id) => {
    const found = allTags.find((tag) => String(tag.id) === String(id));
    return found ? found.color : "#5e60ce";
  };

  // 处理帖子删除后的刷新
  const handlePostDeleted = (deletedPostId) => {
    // 从当前帖子列表中移除被删除的帖子
    setPosts((prevPosts) =>
      prevPosts.filter((post) => post.id !== deletedPostId)
    );
  };

  // 滚动加载更多
  const handleScroll = useCallback((e) => {
    const { scrollTop, scrollHeight, clientHeight } = e.target;
    // 当滚动到距离底部100px时加载更多
    if (scrollHeight - scrollTop - clientHeight < 100 && !loadingMore && hasMore && !isSearchMode) {
      loadMorePosts();
    }
  }, [loadingMore, hasMore, isSearchMode, loadMorePosts]);

  // 添加滚动监听
  useEffect(() => {
    const container = document.querySelector('.community-card');
    if (container) {
      container.addEventListener('scroll', handleScroll);
      return () => container.removeEventListener('scroll', handleScroll);
    }
  }, [handleScroll]);

  return (
    <>
      <div className="main-content">
        {/* 欢迎区域 */}
        <div className="welcome-card">
          <div className="search-filter-section">
            <div className="search-box">
              {isSearchMode ? (
                <div className="search-mode-container">
                  <input
                    type="text"
                    placeholder="搜索结果..."
                    className="search-input search-mode-input"
                    value={searchKeyword}
                    readOnly
                    onKeyDown={(e) => {
                      if (e.key === "Escape") {
                        setIsSearchMode(false);
                        setSearchKeyword("");
                        setSearchQuery("");
                      }
                    }}
                  />
                  <button
                    className="search-exit-button"
                    onClick={() => {
                      setIsSearchMode(false);
                      setSearchKeyword("");
                      setSearchQuery("");
                    }}
                    title="退出搜索模式"
                  >
                    ✕
                  </button>
                </div>
              ) : (
                <input
                  type="text"
                  placeholder="搜索动态内容（点击🔍模糊搜索）"
                  className="search-input"
                  value={searchQuery}
                  onChange={handleSearchInput}
                  onKeyPress={(e) => {
                    if (e.key === "Enter") {
                      handleSearchSubmit();
                    }
                  }}
                />
              )}
              <button className="search-button" onClick={handleSearchSubmit}>
                🔍
              </button>
            </div>

            <div className="filter-bar">
              <button
                className={`filter-button ${
                  activeFilter === "全部" ? "filter-active" : ""
                }`}
                onClick={() => handleFilterChange("全部")}
              >
                全部
              </button>
              <button
                className={`filter-button ${
                  activeFilter === "最新" ? "filter-active" : ""
                }`}
                onClick={() => handleFilterChange("最新")}
              >
                最新
              </button>
              <button
                className={`filter-button ${
                  activeFilter === "最热" ? "filter-active" : ""
                }`}
                onClick={() => handleFilterChange("最热")}
              >
                最热
              </button>
              <button
                className={`filter-button ${
                  activeFilter === "心情" ? "filter-active" : ""
                }`}
                onClick={() => handleFilterChange("心情")}
              >
                心情
              </button>
            </div>

            {/* 标签过滤提示区 */}
            {activeTags.length > 0 && (
              <div
                style={{
                  margin: "8px 0",
                  color: "#5e60ce",
                  fontWeight: "bold",
                  display: "flex",
                  alignItems: "center",
                  gap: 8,
                }}
              >
                当前标签筛选：
                {activeTags.map((tagId) => (
                  <span
                    key={tagId}
                    className="post-tag-btn"
                    style={{
                      cursor: "pointer",
                      padding: "2px 10px",
                      fontSize: "13px",
                      color: getTagColorById(tagId),
                      border: `1.5px solid ${getTagColorById(tagId)}`,
                    }}
                    onClick={() => {
                      // 移除单个tag
                      const newTags = activeTags.filter((t) => t !== tagId);
                      setActiveTags(newTags);
                      // 当用户点击标签时，退出搜索模式
                      if (isSearchMode) {
                        setIsSearchMode(false);
                        setSearchKeyword("");
                        setSearchQuery("");
                      }
                      const param =
                        newTags.length > 0 ? `?tags=${newTags.join(",")}` : "";
                      navigate(`/community${param}`);
                    }}
                  >
                    #{getTagNameById(tagId)} ×
                  </span>
                ))}
              </div>
            )}

            {/* 筛选结果提示 */}
            {!loading && !error && (
              <div
                style={{
                  margin: "8px 0",
                  padding: "8px 12px",
                  backgroundColor: "rgba(102, 126, 234, 0.1)",
                  borderRadius: "8px",
                  fontSize: "14px",
                  color: "#5e60ce",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between",
                }}
              >
                <div
                  style={{ display: "flex", alignItems: "center", gap: "8px" }}
                >
                  <span>📊</span>
                  <span>
                    {isSearchMode &&
                      searchKeyword.trim() &&
                      `搜索结果：${searchKeyword}`}
                    {!isSearchMode &&
                      searchQuery.trim() &&
                      `搜索结果：${searchQuery}`}
                    {!isSearchMode &&
                      !searchQuery.trim() &&
                      activeFilter !== "全部" &&
                      `筛选条件：${activeFilter}`}
                    {!isSearchMode &&
                      !searchQuery.trim() &&
                      activeTags.length > 0 &&
                      ` | 标签：${activeTags
                        .map((id) => getTagNameById(id))
                        .join(", ")}`}
                    {!isSearchMode &&
                      !searchQuery.trim() &&
                      activeFilter === "全部" &&
                      activeTags.length === 0 &&
                      "显示全部动态"}
                    {` (共 ${filteredPosts.length} 条)`}
                  </span>
                </div>
                {(activeFilter !== "全部" ||
                  activeTags.length > 0 ||
                  searchQuery ||
                  isSearchMode) && (
                  <button
                    onClick={handleClearFilters}
                    style={{
                      background: "rgba(102, 126, 234, 0.2)",
                      border: "1px solid rgba(102, 126, 234, 0.3)",
                      borderRadius: "6px",
                      padding: "4px 8px",
                      fontSize: "12px",
                      color: "#5e60ce",
                      cursor: "pointer",
                      transition: "all 0.2s ease",
                    }}
                    onMouseEnter={(e) => {
                      e.target.style.background = "rgba(102, 126, 234, 0.3)";
                    }}
                    onMouseLeave={(e) => {
                      e.target.style.background = "rgba(102, 126, 234, 0.2)";
                    }}
                  >
                    清除筛选
                  </button>
                )}
              </div>
            )}
          </div>
        </div>

        {/* 动态列表 */}
        <div className="community-card">
          {/* 搜索和筛选区域 */}

          <div className="posts-grid" onScroll={handleScroll}>
            {loading ? (
              <div style={{ textAlign: "center", padding: 32 }}>加载中...</div>
            ) : error ? (
              <div style={{ color: "red", textAlign: "center", padding: 32 }}>
                {error}
              </div>
            ) : filteredPosts.length === 0 ? (
              <div style={{ textAlign: "center", padding: 32 }}>暂无动态</div>
            ) : (
              <>
                {filteredPosts.map((post) => (
                  <EnhancedPostCard
                    key={post.id}
                    post={post}
                    onPostDeleted={handlePostDeleted}
                    onAvatarClick={(userId) => {
                      setSelectedUserId(userId);
                      setShowUserProfile(true);
                    }}
                    onBlockSuccess={() => {
                      // 拉黑成功后，BlockContext会自动更新状态
                      console.log("拉黑成功，状态已自动更新");
                    }}
                  />
                ))}
                {loadingMore && (
                  <div className="load-more-container">
                    <div className="loading-spinner"></div>
                    <span>加载更多中...</span>
                  </div>
                )}
                {!hasMore && filteredPosts.length > 0 && !isSearchMode && (
                  <div className="all-loaded-container">
                    已显示全部动态 ({totalElements} 条)
                  </div>
                )}
              </>
            )}
          </div>
        </div>

        {/* 分享想法按钮 - 移到底部 */}
        <div className="fab-container">
          <Link to="/post">
            <button className="fab-button">✏️</button>
          </Link>
        </div>
      </div>

      {/* 打卡弹窗 */}
      <CheckinModal
        isOpen={showCheckinModal}
        onClose={handleCloseCheckinModal}
        onCheckin={handleCheckin}
      />

      {/* 用户信息弹窗 */}
      <UserProfileModal
        isOpen={showUserProfile}
        onClose={() => setShowUserProfile(false)}
        userId={selectedUserId}
      />
    </>
  );
}
