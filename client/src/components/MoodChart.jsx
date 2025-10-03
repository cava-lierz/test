import React from 'react';

const MoodChart = ({ weekData }) => {
  const getRatingColor = (rating) => {
    const colors = ['#ff6b6b', '#ffa726', '#ffd54f', '#81c784', '#4caf50'];
    return colors[rating - 1] || '#e0e0e0';
  };

  const getWeekdayLabel = (weekday) => {
    const labels = {
      'MONDAY': '周一',
      'TUESDAY': '周二', 
      'WEDNESDAY': '周三',
      'THURSDAY': '周四',
      'FRIDAY': '周五',
      'SATURDAY': '周六',
      'SUNDAY': '周日'
    };
    return labels[weekday] || '';
  };

  // 格式化日期为月/日格式
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getMonth() + 1}/${date.getDate()}`;
  };

  // 生成固定7天的数据，按weekday排序
  const generateChartData = () => {
    const weekdayOrder = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'];
    
    return weekdayOrder.map(weekday => {
      const dayData = weekData.find(item => item.weekday === weekday);
      return {
        weekday,
        hasData: dayData && dayData.rating !== null,
        rating: dayData ? dayData.rating : null,
        date: dayData ? dayData.checkinDate : null,
        formattedDate: dayData ? formatDate(dayData.checkinDate) : ''
      };
    });
  };

  const chartData = generateChartData();

  return (
    <div className="mood-chart-section">
      <h4 className="mood-chart-title">
        📈 心情趋势图
      </h4>
      <div className="mood-chart-container">
        {chartData.map((day, index) => (
          <div key={index} className="mood-chart-item">
            <div
              className="mood-chart-bar"
              style={{
                height: day.hasData ? `${(day.rating / 5) * 100}px` : '0px',
                background: day.hasData ? 
                  `linear-gradient(to top, ${getRatingColor(day.rating)}, ${getRatingColor(day.rating)}80)` : 
                  'transparent',
                border: day.hasData ? 'none' : '1px dashed #ddd'
              }}
            ></div>
            <div className="mood-chart-label">
              <span className="mood-chart-weekday">
                {getWeekdayLabel(day.weekday)}
              </span>
              {day.formattedDate && (
                <span className="mood-chart-date">
                  {day.formattedDate}
                </span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MoodChart; 