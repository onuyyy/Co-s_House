// 게시글 목록 페이지 JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 필터 기능
    const filterBtns = document.querySelectorAll('.filter-btn');
    const filterDropdowns = document.querySelectorAll('.filter-dropdown');

    filterBtns.forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            
            const filterType = this.getAttribute('data-filter');
            const dropdown = document.getElementById(filterType + 'Dropdown');
            const isActive = this.classList.contains('active');

            // 모든 필터 드롭다운 닫기
            filterBtns.forEach(b => b.classList.remove('active'));
            filterDropdowns.forEach(d => d.classList.remove('show'));

            if (!isActive) {
                this.classList.add('active');
                dropdown.classList.add('show');
            }
        });
    });

    // 필터 옵션 선택
    const radioInputs = document.querySelectorAll('.filter-dropdown input[type="radio"]');
    radioInputs.forEach(input => {
        input.addEventListener('change', function() {
            const form = document.getElementById('searchForm');
            form.submit();
        });
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', function() {
        filterBtns.forEach(btn => btn.classList.remove('active'));
        filterDropdowns.forEach(dropdown => dropdown.classList.remove('show'));
    });

    // 드롭다운 내부 클릭 시 이벤트 전파 중단
    filterDropdowns.forEach(dropdown => {
        dropdown.addEventListener('click', function(e) {
            e.stopPropagation();
        });
    });
});

// 북마크 토글 함수
function toggleBookmark(button) {
    const postId = button.getAttribute('data-post-id');
    
    // 로그인 확인
    const currentUserId = document.querySelector('body').getAttribute('data-user-id');
    if (!currentUserId) {
        if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
            window.location.href = '/login';
        }
        return;
    }

    // AJAX 요청
    fetch(`/posts/${postId}/scrap`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('스크랩 처리 중 오류가 발생했습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 버튼 상태 업데이트
        if (data.isScraped) {
            button.classList.add('active');
        } else {
            button.classList.remove('active');
        }

        // 스크랩 수 업데이트
        const postCard = button.closest('.post-card');
        const scrapCountElement = postCard.querySelector('.post-stats span:first-child span');
        if (scrapCountElement) {
            scrapCountElement.textContent = data.scrapCount;
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('스크랩 처리 중 오류가 발생했습니다.');
    });
}
