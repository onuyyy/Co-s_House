// 댓글 기능 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    const commentsSection = document.querySelector('.comments-section');
    if (!commentsSection) return;

    const postId = commentsSection.getAttribute('data-post-id');

    // 페이지 로드 시 댓글 불러오기
    loadComments(postId);

    // 댓글 입력 글자 수 카운팅
    const commentInput = document.getElementById('commentInput');
    const charCount = document.getElementById('charCount');

    if (commentInput && charCount) {
        commentInput.addEventListener('input', function() {
            charCount.textContent = this.value.length;
        });
    }
});

// 댓글 목록 불러오기
function loadComments(postId) {
    fetch(`/api/comments/${postId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('댓글을 불러오는 데 실패했습니다.');
            }
            return response.json();
        })
        .then(comments => {
            displayComments(comments);
            updateCommentCount(countTotalComments(comments));
        })
        .catch(error => {
            console.error('Error loading comments:', error);
        });
}

// 댓글 표시
function displayComments(comments) {
    const commentsList = document.getElementById('commentsList');

    if (!comments || comments.length === 0) {
        commentsList.innerHTML = '<div class="comments-empty">첫 댓글을 작성해보세요!</div>';
        return;
    }

    commentsList.innerHTML = comments.map(comment => createCommentHTML(comment)).join('');
}

// 댓글 HTML 생성
function createCommentHTML(comment, isReply = false) {
    const date = formatDate(comment.createdAt);
    const initial = comment.username ? comment.username.charAt(0).toUpperCase() : 'U';

    const commentClass = isReply ? 'comment-reply-item' : 'comment-item';

    let html = `
        <div class="${commentClass}" data-comment-id="${comment.commentId}">
            <div class="comment-header">
                <div class="comment-author">
                    <div class="comment-avatar">${initial}</div>
                    <span class="comment-username">${comment.username}</span>
                </div>
                <span class="comment-date">${date}</span>
            </div>
            <div class="comment-content">${escapeHtml(comment.content)}</div>
            <div class="comment-actions">
                <button class="btn-reply" onclick="showReplyForm(${comment.commentId})">답글</button>
            </div>
    `;

    // 대댓글이 있으면 표시
    if (comment.childComments && comment.childComments.length > 0) {
        html += `
            <div class="comment-replies">
                ${comment.childComments.map(reply => createCommentHTML(reply, true)).join('')}
            </div>
        `;
    }

    // 대댓글 입력 폼 (처음엔 숨김)
    if (!isReply) {
        html += `
            <div class="reply-form-wrapper" id="replyForm-${comment.commentId}">
                <form class="reply-form" onsubmit="return false;">
                    <textarea class="reply-input"
                              id="replyInput-${comment.commentId}"
                              placeholder="답글을 입력하세요..."
                              rows="2"
                              maxlength="500"
                              required></textarea>
                    <div class="reply-form-actions">
                        <button type="button" class="btn-cancel-reply" onclick="hideReplyForm(${comment.commentId})">취소</button>
                        <button type="button" class="btn-submit-reply" onclick="submitReply(${comment.commentId})">답글 작성</button>
                    </div>
                </form>
            </div>
        `;
    }

    html += '</div>';
    return html;
}

// 댓글 작성
function submitComment() {
    const commentInput = document.getElementById('commentInput');
    const content = commentInput.value.trim();

    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    const commentsSection = document.querySelector('.comments-section');
    const postId = commentsSection.getAttribute('data-post-id');

    const requestData = {
        postId: parseInt(postId),
        content: content,
        parentCommentId: null
    };

    fetch('/api/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error('로그인이 필요합니다.');
            }
            throw new Error('댓글 작성에 실패했습니다.');
        }
        return response;
    })
    .then(() => {
        // 입력 초기화
        commentInput.value = '';
        document.getElementById('charCount').textContent = '0';

        // 댓글 목록 새로고침
        loadComments(postId);
    })
    .catch(error => {
        console.error('Error:', error);
        alert(error.message);
    });
}

// 답글 폼 표시
function showReplyForm(commentId) {
    // 다른 모든 답글 폼 숨기기
    document.querySelectorAll('.reply-form-wrapper').forEach(form => {
        form.classList.remove('active');
    });

    // 해당 답글 폼 표시
    const replyForm = document.getElementById(`replyForm-${commentId}`);
    if (replyForm) {
        replyForm.classList.add('active');
        const input = document.getElementById(`replyInput-${commentId}`);
        if (input) {
            input.focus();
        }
    }
}

// 답글 폼 숨기기
function hideReplyForm(commentId) {
    const replyForm = document.getElementById(`replyForm-${commentId}`);
    if (replyForm) {
        replyForm.classList.remove('active');
        const input = document.getElementById(`replyInput-${commentId}`);
        if (input) {
            input.value = '';
        }
    }
}

// 답글 작성
function submitReply(parentCommentId) {
    const replyInput = document.getElementById(`replyInput-${parentCommentId}`);
    const content = replyInput.value.trim();

    if (!content) {
        alert('답글 내용을 입력해주세요.');
        return;
    }

    const commentsSection = document.querySelector('.comments-section');
    const postId = commentsSection.getAttribute('data-post-id');

    const requestData = {
        postId: parseInt(postId),
        content: content,
        parentCommentId: parentCommentId
    };

    fetch('/api/comments', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestData)
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error('로그인이 필요합니다.');
            }
            throw new Error('답글 작성에 실패했습니다.');
        }
        return response;
    })
    .then(() => {
        // 답글 폼 숨기기 및 초기화
        hideReplyForm(parentCommentId);

        // 댓글 목록 새로고침
        loadComments(postId);
    })
    .catch(error => {
        console.error('Error:', error);
        alert(error.message);
    });
}

// 댓글 수 업데이트
function updateCommentCount(count) {
    const commentCountElement = document.getElementById('commentCount');
    if (commentCountElement) {
        commentCountElement.textContent = count;
    }
}

// 전체 댓글 수 계산 (대댓글 포함)
function countTotalComments(comments) {
    let count = 0;
    comments.forEach(comment => {
        count++; // 댓글 자체
        if (comment.childComments && comment.childComments.length > 0) {
            count += countTotalComments(comment.childComments); // 대댓글 재귀 카운트
        }
    });
    return count;
}

// 날짜 포맷팅
function formatDate(dateString) {
    const date = new Date(dateString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}.${month}.${day}`;
}

// HTML 이스케이프 (XSS 방지)
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}