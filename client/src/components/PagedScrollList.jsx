import React, { useRef, useState, useEffect, useCallback, forwardRef, useImperativeHandle } from 'react';
import { postService } from '../services/postService';

const PagedScrollList = forwardRef(function PagedScrollList({
                                                              fetchPage,
                                                              pageSize = 10,
                                                              renderItem,
                                                              initialLoad = true,
                                                              className = '',
                                                              style = {},
                                                              sortOrder = 'asc',
                                                              initialPage = 0,
                                                              items: externalItems,
                                                            }, ref) {
  const [items, setItems] = useState([]);
  const [loadedPages, setLoadedPages] = useState(new Set());
  const [totalPages, setTotalPages] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  // 滑动窗口状态
  const [pageWindow, setPageWindow] = useState({ start: 0, end: 0 });
  const [lastScrollTop, setLastScrollTop] = useState(0);

  const listRef = useRef(null);
  const itemRefs = useRef({});
  const scrollTimeout = useRef();

  // 加载指定页 - 支持滑动窗口
  const loadPage = useCallback(async (page, afterLoaded, force = false, keepOnlyPages = null) => {
    if (isLoading || (loadedPages.has(page) && !force)) return;
    setIsLoading(true);
    try {
      const data = await fetchPage(page);
      setTotalPages(data.totalPages);

      setItems(prev => {
        let filteredItems;
        if (keepOnlyPages) {
          // 滑动窗口模式：只保留指定页面，移除其他页面
          filteredItems = prev.filter(i => keepOnlyPages.includes(i._page) && i._page !== page);
        } else {
          // 普通模式：只移除当前页面的旧数据
          filteredItems = prev.filter(i => i._page !== page);
        }
        const newItems = [...filteredItems, ...data.content.map(i => ({ ...i, _page: page }))];
        return newItems.sort((a, b) => {
          if (sortOrder === 'asc') {
            return new Date(a.createdAt) - new Date(b.createdAt);
          } else {
            return new Date(b.createdAt) - new Date(a.createdAt);
          }
        });
      });

      if (keepOnlyPages) {
        // 更新滑动窗口状态
        setPageWindow({ start: Math.min(...keepOnlyPages), end: Math.max(...keepOnlyPages) });
        setLoadedPages(new Set(keepOnlyPages));
        setHasMore(Math.max(...keepOnlyPages) < data.totalPages - 1);
      } else {
        setLoadedPages(prev => new Set([...prev, page]));
        setHasMore(loadedPages.size + 1 < data.totalPages);
      }

      if (afterLoaded) afterLoaded();
    } finally {
      setIsLoading(false);
    }
  }, [fetchPage, isLoading, loadedPages, sortOrder]);

  // 滑动窗口管理
  const loadWithWindow = useCallback(async (newPage, direction) => {
    const { start, end } = pageWindow;
    let keepPages;

    if (direction === 'down') {
      keepPages = start === end ? [start, newPage] : [end, newPage];
    } else { // direction === 'up'
      keepPages = start === end ? [newPage, start] : [newPage, start];
    }

    await loadPage(newPage, null, false, keepPages);
  }, [pageWindow, loadPage]);

  // 滚动处理
  const handleScroll = (e) => {
    if (scrollTimeout.current) clearTimeout(scrollTimeout.current);
    scrollTimeout.current = setTimeout(() => {
      const { scrollTop, scrollHeight, clientHeight } = e.target;
      const scrollDirection = scrollTop > lastScrollTop ? 'down' : 'up';
      setLastScrollTop(scrollTop);

      if (scrollDirection === 'down') {
        // 向下滚动：加载下一页
        if (scrollHeight - scrollTop - clientHeight < 10 && hasMore && !isLoading) {
          const nextPage = pageWindow.end + 1;
          if (nextPage < totalPages) {
            const visibleItem = items.find(i => {
              const ref = itemRefs.current[i.id];
              return ref && ref.offsetTop >= scrollTop;
            });
            const visibleId = visibleItem?.id;
            const oldOffset = visibleId ? itemRefs.current[visibleId].offsetTop : 0;

            loadWithWindow(nextPage, 'down').then(() => {
              if (visibleId && itemRefs.current[visibleId]) {
                const newOffset = itemRefs.current[visibleId].offsetTop;
                listRef.current.scrollTop += newOffset - oldOffset;
              }
            });
          }
        }
      } else {
        // 向上滚动：加载上一页
        if (scrollTop < 10 && pageWindow.start > 0 && !isLoading) {
          const prevPage = pageWindow.start - 1;
          const anchorItem = items.find(i => {
            const ref = itemRefs.current[i.id];
            return ref && ref.offsetTop >= scrollTop;
          });
          const anchorId = anchorItem?.id;
          const anchorRelativePos = anchorId ? itemRefs.current[anchorId].offsetTop - scrollTop : 0;

          loadWithWindow(prevPage, 'up').then(() => {
            setTimeout(() => {
              if (anchorId && itemRefs.current[anchorId]) {
                const newPos = itemRefs.current[anchorId].offsetTop;
                listRef.current.scrollTop = newPos - anchorRelativePos;
              }
            }, 0);
          });
        }
      }
    }, 100);
  };

  // 外部items变化时，直接分页渲染
  useEffect(() => {
    if (externalItems) {
      setItems(externalItems.map((item, idx) => ({ ...item, _page: Math.floor(idx / pageSize) })));
      setTotalPages(Math.ceil(externalItems.length / pageSize));
      setLoadedPages(new Set());
      setPageWindow({ start: 0, end: Math.max(0, Math.ceil(externalItems.length / pageSize) - 1) });
      setHasMore(false);
    }
  }, [externalItems, pageSize]);

  // 首次加载
  useEffect(() => {
    if (initialLoad) {
      loadPage(initialPage).then(() => {
        setPageWindow({ start: initialPage, end: initialPage });
      });
    }
    // eslint-disable-next-line
  }, [initialLoad, initialPage]);

  // 暴露方法：拉取最后一页并滚动到底部
  useImperativeHandle(ref, () => ({
    scrollToBottomAndLoadLastPage: async () => {
      // 获取最新总页数
      const lastPageData = await fetchPage(totalPages? totalPages - 1 : totalPages);
      const actualTotal = lastPageData.totalPages;
      const lastPage = actualTotal - 1;
      const secondLastPage = Math.max(0, lastPage - 1);

      setTotalPages(actualTotal);

      // 加载最后两页
      const keepPages = secondLastPage === lastPage ? [lastPage] : [secondLastPage, lastPage];
      await loadPage(lastPage, null, true, keepPages);

      // 如果需要，加载倒数第二页
      if (secondLastPage !== lastPage) {
        await loadPage(secondLastPage, null, true, keepPages);
      }

      setTimeout(() => {
        listRef.current?.scrollTo({ top: listRef.current.scrollHeight, behavior: 'smooth' });
      }, 0);
    },
    reloadPage: async (page) => {
      await loadPage(page, undefined, true);
    },
    scrollToItemById: async (topCommentId, parentId) => {
      // 使用后端API获取目标评论所在页对象
      let pageObj;
      try {
        pageObj = await postService.getPageByCommentId(topCommentId, parentId, pageSize);
      } catch (e) {
        // fallback: 兼容老接口或失败时回退原逻辑
        let foundItem = items.find(i => i.id === parentId);
        let pageToLoad = 0;
        while (!foundItem && pageToLoad < totalPages) {
          await loadPage(pageToLoad, undefined, true);
          foundItem = items.find(i => i.id === parentId);
          if (foundItem) {
            break;
          }
          pageToLoad++;
        }
        if (foundItem && typeof foundItem._page === 'number') {
          setItems(prev => prev.filter(i => i._page === foundItem._page));
          setLoadedPages(new Set([foundItem._page]));
          setPageWindow({ start: foundItem._page, end: foundItem._page });
          setTimeout(() => {
            if (itemRefs.current[parentId]) {
              itemRefs.current[parentId].scrollIntoView({ behavior: 'smooth', block: 'center' });
              itemRefs.current[parentId].classList.add('highlighted');
              setTimeout(() => {
                itemRefs.current[parentId]?.classList.remove('highlighted');
              }, 800);
            }
          }, 0);
        }
        return;
      }
      // pageObj为目标评论所在页对象
      if (pageObj && Array.isArray(pageObj.content)) {
        // 标记每个item的_page
        const pageNum = typeof pageObj.number === 'number' ? pageObj.number : 0;
        const newItems = pageObj.content.map(i => ({ ...i, _page: pageNum }));
        setItems(newItems);
        setLoadedPages(new Set([pageNum]));
        setPageWindow({ start: pageNum, end: pageNum });
        setTotalPages(pageObj.totalPages || 1);
        setHasMore(pageNum < (pageObj.totalPages || 1) - 1);
        setTimeout(() => {
          if (itemRefs.current[parentId]) {
            itemRefs.current[parentId].scrollIntoView({ behavior: 'smooth', block: 'center' });
            itemRefs.current[parentId].classList.add('highlighted');
            setTimeout(() => {
              itemRefs.current[parentId]?.classList.remove('highlighted');
            }, 800);
          }
        }, 0);
      }
    },
    // 新增：按page跳转并滚动到itemId（如有），否则滚动到页首
    scrollToPageAndScrollToItem: async (page, itemId = null) => {
      await loadPage(page, undefined, true, [page]);
      setTimeout(() => {
        if (itemId && itemRefs.current[itemId]) {
          itemRefs.current[itemId].scrollIntoView({ behavior: 'smooth', block: 'center' });
          itemRefs.current[itemId].classList.add('highlighted');
          setTimeout(() => {
            itemRefs.current[itemId]?.classList.remove('highlighted');
          }, 800);
        } else if (listRef.current) {
          listRef.current.scrollTo({ top: 0, behavior: 'smooth' });
        }
      }, 0);
    }
  }));

  return (
      <div
          className={`paged-scroll-list ${className}`}
          style={{ maxHeight: 400, overflowY: 'auto', ...style }}
          onScroll={handleScroll}
          ref={listRef}
      >
        {(externalItems ?
          externalItems.slice(pageWindow.start * pageSize, (pageWindow.end + 1) * pageSize)
          : items
        ).map(item => (
            <div key={item.id} ref={el => itemRefs.current[item.id] = el} style={{ marginBottom: 16 }}>
              {renderItem(item, externalItems || items)}
            </div>
        ))}
        {isLoading && items.length > 0 && !externalItems && (
            <div style={{ color: '#888', textAlign: 'center', marginTop: 12 }}>加载中...</div>
        )}
        {!hasMore && items.length > 0 && pageWindow.end >= totalPages - 1 && !externalItems && (
            <div style={{ color: '#aaa', textAlign: 'center', marginTop: 12 }}>没有更多了</div>
        )}
      </div>
  );
});

export default PagedScrollList;

// 在文件末尾添加内联样式，确保高亮效果明显
if (typeof window !== 'undefined') {
  const style = document.createElement('style');
  style.innerHTML = `.highlighted { background: #e0c3fc !important; transition: background 0.5s; }`;
  document.head.appendChild(style);
} 