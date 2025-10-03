package com.mentara.repository;

import com.mentara.entity.Notification;
import com.mentara.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import com.mentara.entity.Post;
import com.mentara.entity.PostLikeNotification;
import com.mentara.entity.CommentLikeNotification;
import com.mentara.entity.Comment;
import com.mentara.entity.CommentReplyNotification;
import com.mentara.entity.PostReplyNotification;



@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByReceiverAndReadFalseOrderByCreatedAtDesc(User receiver, Pageable pageable);

    // 使用自定义查询查找特定用户的特定帖子的点赞通知
    @Query("SELECT n FROM PostLikeNotification n WHERE n.receiver = :receiver AND n.post = :post AND n.read = false")
    Optional<PostLikeNotification> findUnreadPostLikeNotificationByReceiverAndPost(
        @Param("receiver") User receiver, 
        @Param("post") Post post
    );


    @Query("SELECT n FROM CommentLikeNotification n WHERE n.receiver = :receiver AND n.comment = :comment AND n.read = false")
    Optional<CommentLikeNotification> findUnreadCommentLikeNotificationByReceiverAndComment(
        @Param("receiver") User receiver, 
        @Param("comment") Comment comment
    );

    List<Notification> findByReceiver(User receiver);
    List<Notification> findByReceiverAndReadFalse(User receiver);
    Page<Notification> findByReceiver(User receiver, Pageable pageable);
    Page<Notification> findByReceiverOrderByCreatedAtDesc(User receiver, Pageable pageable);
    
    // 根据帖子ID删除帖子点赞通知
    @Modifying
    @Query("DELETE FROM PostLikeNotification n WHERE n.post.id = :postId")
    void deletePostLikeNotificationsByPostId(@Param("postId") Long postId);
    
    // 根据帖子ID删除帖子回复通知
    @Modifying
    @Query("DELETE FROM PostReplyNotification n WHERE n.post.id = :postId")
    void deletePostReplyNotificationsByPostId(@Param("postId") Long postId);
    
    // 根据评论ID删除评论点赞通知
    @Modifying
    @Query("DELETE FROM CommentLikeNotification n WHERE n.comment.id = :commentId")
    void deleteCommentLikeNotificationsByCommentId(@Param("commentId") Long commentId);
    
    // 根据评论ID删除评论回复通知
    @Modifying
    @Query("DELETE FROM CommentReplyNotification n WHERE n.comment.id = :commentId")
    void deleteCommentReplyNotificationsByCommentId(@Param("commentId") Long commentId);
    
    // 根据回复ID列表删除评论回复通知
    @Modifying
    @Query("DELETE FROM CommentReplyNotification n WHERE n.reply.id IN :replyIds")
    void deleteCommentReplyNotificationsByReplyIds(@Param("replyIds") List<Long> replyIds);

    // 根据用户ID删除所有通知
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.receiver.id = :receiverId")
    void deleteByReceiverId(@Param("receiverId") Long receiverId);
} 