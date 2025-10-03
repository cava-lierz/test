import React, { useState, useEffect } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import CheckinModal from "../components/CheckinModal";
import PostCard from "../components/PostCard";

export default function CommunityPage() {
  const navigate = useNavigate();
  const { user, showSuccess } = useAuth();
  const [showCheckinModal, setShowCheckinModal] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [activeFilter, setActiveFilter] = useState("全部");
  const [posts] = useState([
    {
      id: 1,
      username: "学生1145",
      avatar: "https://i.pravatar.cc/150?u=1",
      content:
        "今天心情特别好，和朋友们一起去了公园，感觉整个人都轻松了！希望每个人都能找到属于自己的快乐时光。",
      likes: 15,
      comments: 8,
      time: "2小时前",
      mood: "😊",
      tags: ["心理健康", "成长", "匿名"],
    },
    {
      id: 2,
      username: "xxx老师",
      avatar: "https://i.pravatar.cc/150?u=2",
      content:
        "最近学习压力有点大，但是我相信只要坚持下去，一定会有好结果的。大家一起加油！",
      likes: 23,
      comments: 12,
      time: "4小时前",
      mood: "💪",
      tags: ["心理健康", "成长", "匿名"],
    },
    {
      id: 3,
      username: "学生45",
      avatar: "https://i.pravatar.cc/150?u=3",
      content:
        "今天遇到了一些困难，但是通过和朋友的交流，我找到了解决问题的方法。感谢身边的朋友们！",
      likes: 18,
      comments: 6,
      time: "6小时前",
      mood: "🤗",
      tags: ["心理健康", "成长", "匿名"],
    },
  ]);

  // 检查是否需要显示打卡弹窗
  useEffect(() => {
    const today = new Date().toISOString().split("T")[0];
    const lastCheckin = localStorage.getItem(`lastCheckin_${user.name}`);

    // 如果今天还没有打卡，显示打卡弹窗
    if (lastCheckin !== today) {
      // 延迟1秒显示，让页面先加载完成
      const timer = setTimeout(() => {
        setShowCheckinModal(true);
      }, 1000);

      return () => clearTimeout(timer);
    }
  }, [user.name]);

  const handleCheckin = (moodData) => {
    // 记录今天已打卡
    const today = new Date().toISOString().split("T")[0];
    localStorage.setItem(`lastCheckin_${user.name}`, today);

    // 显示成功提示
    showSuccess(`打卡成功！今天的心情是 ${moodData.rating} 星 🌟`);
  };

  const handleCloseCheckinModal = () => {
    setShowCheckinModal(false);
  };

  // 搜索和筛选处理函数
  const handleSearch = (e) => {
    setSearchQuery(e.target.value);
  };

  const handleFilterChange = (filter) => {
    setActiveFilter(filter);
  };

  // 过滤动态列表
  const filteredPosts = posts.filter((post) => {
    const matchesSearch =
      searchQuery === "" ||
      post.content.toLowerCase().includes(searchQuery.toLowerCase()) ||
      post.author.toLowerCase().includes(searchQuery.toLowerCase());

    if (!matchesSearch) return false;

    switch (activeFilter) {
      case "最新":
        return post.time.includes("小时前") && parseInt(post.time) <= 6;
      case "最热":
        return post.likes >= 20;
      case "心情":
        return post.mood && post.mood.length > 0;
      default:
        return true;
    }
  });

  return (
    <>
      <div className="main-content">
        {/* 欢迎区域 */}
        <div className="welcome-card">
          <div className="search-filter-section">
            <div className="search-box">
              <input
                type="text"
                placeholder="搜索动态内容..."
                className="search-input"
                value={searchQuery}
                onChange={handleSearch}
              />
              <button className="search-button">🔍</button>
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
          </div>
        </div>

        {/* 动态列表 */}
        <div className="community-card">
          {/* 搜索和筛选区域 */}

          <div className="posts-grid">
            {filteredPosts.map((post) => (
              <PostCard key={post.id} post={post} />
            ))}
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
    </>
  );
}
