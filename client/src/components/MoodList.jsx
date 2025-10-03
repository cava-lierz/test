import React from 'react';

const MoodList = ({ weekData, onMoodClick }) => {
  const getRatingEmoji = (rating) => {
    const emojis = ['😢', '😔', '😐', '😊', '😄'];
    return emojis[rating - 1] || '😐';
  };

  const getRatingText = (rating) => {
    const texts = ['很差', '不好', '一般', '不错', '很好'];
    return texts[rating - 1] || '未知';
  };

  const getWeekdayText = (weekday) => {
    const weekdays = {
      'MONDAY': '周一',
      'TUESDAY': '周二', 
      'WEDNESDAY': '周三',
      'THURSDAY': '周四',
      'FRIDAY': '周五',
      'SATURDAY': '周六',
      'SUNDAY': '周日'
    };
    return weekdays[weekday] || '';
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  // 过滤出有数据的记录并按日期降序排序
  const validMoods = weekData
    .filter(mood => mood.rating !== null)
    .sort((a, b) => new Date(b.checkinDate) - new Date(a.checkinDate));

  return (
    <div className="mood-list-section">
      <h4 className="mood-list-title">
        📝 心情记录
      </h4>
      <div className="mood-list">
        {validMoods.length > 0 ? (
          validMoods.map((mood, index) => (
            <div key={index} className="mood-list-item" onClick={() => onMoodClick(mood)}>
              <div className="mood-list-emoji">
                {getRatingEmoji(mood.rating)}
              </div>
              <div className="mood-list-content">
                <div className="mood-list-header">
                  <span className="mood-list-title-text">
                    {getRatingText(mood.rating)}
                  </span>
                  <span className="mood-list-rating">
                    {mood.rating} 星
                  </span>
                </div>
                <p className="mood-list-description">
                  {mood.note || '今天心情保密'}
                </p>
                <div className="mood-list-date">
                  <span className="mood-list-date-text">
                    {formatDate(mood.checkinDate)}
                  </span>
                  <span className="mood-list-weekday">
                    {getWeekdayText(mood.weekday)}
                  </span>
                </div>
              </div>
            </div>
          ))
        ) : (
          <div className="mood-list-empty">
            本周暂无心情记录
          </div>
        )}
      </div>
    </div>
  );
};

export default MoodList; 