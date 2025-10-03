import React from "react";

const SidebarToggleButton = ({ open, onClick }) => (
  <button
    className="sidebar-toggle-btn"
    onClick={onClick}
    title={open ? "收起侧栏" : "展开侧栏"}
    aria-label={open ? "收起侧栏" : "展开侧栏"}
  >
    {open ? (
      <span style={{ fontSize: 20 }}>&lt;</span>
    ) : (
      <span style={{ fontSize: 20 }}>&gt;</span>
    )}
  </button>
);

export default SidebarToggleButton; 