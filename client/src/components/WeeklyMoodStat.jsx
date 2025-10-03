import React from 'react';

export default function WeeklyMoodStat({ currentWeekData }) {
  // 计算本周心情均值
  const calculateWeeklyMoodAverage = () => {
    if (!currentWeekData || currentWeekData.length === 0) return '--';
    
    const validMoods = currentWeekData.filter(mood => mood && mood.rating && mood.rating > 0);
    if (validMoods.length === 0) return '--';
    
    const totalRating = validMoods.reduce((sum, mood) => sum + mood.rating, 0);
    const average = totalRating / validMoods.length;
    return average.toFixed(1);
  };

  // 根据心情均值获取对应的表情
  const getMoodAverageEmoji = () => {
    const average = calculateWeeklyMoodAverage();
    
    if (average === '--') return '😐';
    
    const numAverage = parseFloat(average);
    if (numAverage >= 4.5) return '😄';
    if (numAverage >= 3.5) return '😊';
    if (numAverage >= 2.5) return '😐';
    if (numAverage >= 1.5) return '😔';
    return '😢';
  };

  return (
    <div className="stat-item">
      <div className="stat-number">
        {getMoodAverageEmoji()} {calculateWeeklyMoodAverage()}
      </div>
      <div className="stat-label">
        本周心情
      </div>
    </div>
  );
} 