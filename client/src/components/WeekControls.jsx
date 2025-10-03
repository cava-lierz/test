import React from 'react';

const WeekControls = ({ currentWeek, onPreviousWeek, onNextWeek, isLoading = false }) => {
  const getWeekTitle = () => {
    if (currentWeek === 0) return '本周';
    return `${currentWeek}周前`;
  };

  return (
    <div className="mood-week-controls">
      <button 
        className="week-nav-button"
        onClick={onPreviousWeek}
      >
        {(
          '← 前一周'
        )}
      </button>
      <div className="week-info">
        <span className="week-title">
          {getWeekTitle()}
        </span>

      </div>
      {currentWeek > 0 && (
        <button 
          className="week-nav-button"
          onClick={onNextWeek}
        >
          {(
            '后一周 →'
          )}
        </button>
      )}
      {currentWeek === 0 && (
        <div className="week-nav-placeholder">
          {/* 占位元素，保持布局平衡 */}
        </div>
      )}
    </div>
  );
};

export default WeekControls; 